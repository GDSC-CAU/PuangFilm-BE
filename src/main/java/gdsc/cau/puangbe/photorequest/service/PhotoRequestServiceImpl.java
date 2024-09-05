package gdsc.cau.puangbe.photorequest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gdsc.cau.puangbe.common.annotation.ExeTimer;
import gdsc.cau.puangbe.common.annotation.Retry;
import gdsc.cau.puangbe.common.enums.Gender;
import gdsc.cau.puangbe.common.enums.RequestStatus;
import gdsc.cau.puangbe.common.exception.BaseException;
import gdsc.cau.puangbe.common.exception.PhotoRequestException;
import gdsc.cau.puangbe.common.util.ConstantUtil;
import gdsc.cau.puangbe.common.util.ResponseCode;
import gdsc.cau.puangbe.photo.entity.PhotoRequest;
import gdsc.cau.puangbe.photo.entity.PhotoResult;
import gdsc.cau.puangbe.photo.repository.PhotoRequestRepository;
import gdsc.cau.puangbe.photo.repository.PhotoResultRepository;
import gdsc.cau.puangbe.photorequest.dto.CreateImageDto;
import gdsc.cau.puangbe.photorequest.dto.ImageInfo;
import gdsc.cau.puangbe.user.entity.User;
import gdsc.cau.puangbe.user.repository.UserRepository;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoRequestServiceImpl implements PhotoRequestService {

    private final PhotoResultRepository photoResultRepository;
    private final PhotoRequestRepository photoRequestRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Long> redisTemplate;
    private final ObjectMapper mapper;
    private final RabbitMqService rabbitMqService;

    //이미지 처리 요청 생성 (RabbitMQ호출)
    @Override
    @Transactional
    public Long createImage(CreateImageDto dto, Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(ResponseCode.USER_NOT_FOUND));

        // 가장 최신의 photo_request 조회
        PhotoRequest latestRequest = photoRequestRepository
                .findTopByUserIdOrderByCreateDateDesc(userId)
                .orElseThrow(() -> new RuntimeException("No photo requests found for userId: " + userId));

        // 상태 체크
        if (latestRequest.getStatus() == RequestStatus.WAITING) {
            return latestRequest.getId(); // 상태가 'waiting'이면 requestId 반환
        }

        // PhotoRequest 생성
        PhotoRequest request = PhotoRequest.builder()
                .user(user)
                .gender(Gender.fromInt(dto.getGender()))
                .urls(dto.getPhotoOriginUrls())
                .email(dto.getEmail())
                .build();
        photoRequestRepository.save(request);

        // PhotoRequest에 일대일 대응되는 PhotoResult 생성
        PhotoResult photoResult = PhotoResult.builder()
                .user(request.getUser())
                .photoRequest(request)
                .createDate(LocalDateTime.now())
                .build();
        photoResultRepository.save(photoResult);
        log.info("사용자의 이미지 요청 생성 완료, RabbitMQ에 전송 준비: {}", userId);

        try {
            ImageInfo imageInfo = ImageInfo.builder()
                    .photoOriginUrls(dto.getPhotoOriginUrls())
                    .gender(Gender.fromInt(dto.getGender()))
                    .requestId(request.getId())
                    .build();
            String message = mapper.writeValueAsString(imageInfo);

            rabbitMqService.sendMessage(message); // 1. RabbitMQ를 호출해서 message를 큐에 함께 넣어서 파이썬에서 접근할 수 있도록 한다.
            // 2. Redis에 <String keyName, Long requestId> 형식으로 진행되고 있는 request 정보를 저장한다.
            // 3. 추후 사진이 완성된다면 requestId를 통해 request를 찾아서 상태를 바꾸고 1:1 관계인 result에 접근해서 imageUrl를 수정한다.
            // 4. 즉, 파이썬에서 스프링으로 향하는 POST API는 {requestId, imageUrl}이 필수적으로 존재해야 한다.
            log.info("RabbitMQ 전송 완료: {}", message);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패");
            throw new PhotoRequestException(ResponseCode.JSON_PARSE_ERROR);
        }

        return request.getId();
    }

    // 유저의 전체 사진 리스트 조회
    @Override
    @Transactional(readOnly = true)
    public List<String> getRequestImages(Long userId){
        validateUser(userId);

        // 현재 처리가 완료되지 않은 이미지(imageUrl이 null)는 보내지 않음
        log.info("사용자의 이미지 리스트 조회 시도: {}", userId);
        return photoResultRepository.findAllByUserId(userId)
                .stream()
                .map(PhotoResult::getImageUrl)
                .filter(Objects::nonNull)
                .toList();
    }

    //최근 생성 요청한 이미지의 상태 조회 (추후 boolean 등으로 변환될 수도 있음)
    @Override
    @Transactional(readOnly = true)
    public String getRequestStatus(Long userId){
        validateUser(userId);

        RequestStatus status = photoRequestRepository.findTopByUserIdOrderByCreateDateDesc(userId)
                .orElseThrow(() -> new BaseException(ResponseCode.PHOTO_REQUEST_NOT_FOUND))
                .getStatus();
        log.info("사용자의 요청 상태 조회, 현재 상태: {} {}", status.name(), userId);
        return status.name();
    }


    @Override
    @Transactional
    public Long updateEmail(Long userId, String email) {

        // 가장 최근의 PhotoRequest 조회
        PhotoRequest photoRequest = photoRequestRepository.findTopByUserIdOrderByCreateDateDesc(userId)
                .orElseThrow(() -> new BaseException(ResponseCode.PHOTO_REQUEST_NOT_FOUND));

        photoRequest.modifyEmail(email);
        photoRequestRepository.save(photoRequest);

        log.info("update email");

        return photoRequest.getId();
    }

    // 유저id 유효성 검사
    private void validateUser(Long userId){
        if(!userRepository.existsById(userId)){
            throw new BaseException(ResponseCode.USER_NOT_FOUND);
        }
    }
}
