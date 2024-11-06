package gdsc.cau.puangbe.photo.service;
import gdsc.cau.puangbe.common.enums.Gender;
import gdsc.cau.puangbe.common.exception.BaseException;
import gdsc.cau.puangbe.common.util.ConstantUtil;
import gdsc.cau.puangbe.common.util.ResponseCode;
import gdsc.cau.puangbe.photo.entity.PhotoRequest;
import gdsc.cau.puangbe.photo.entity.PhotoResult;
import gdsc.cau.puangbe.photo.repository.PhotoRequestRepository;
import gdsc.cau.puangbe.photo.repository.PhotoResultRepository;
import gdsc.cau.puangbe.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class PhotoServiceImplTest {

    @InjectMocks
    private PhotoServiceFacadeImpl photoService;

    @Mock
    private PhotoServiceImpl photoServiceImpl;

    @Mock
    private PhotoRequestRepository photoRequestRepository;

    @Mock
    private PhotoResultRepository photoResultRepository;

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private SetOperations<String, Long> setOperations;

    @Mock
    private User user;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private JavaMailSender mailSender;

    private Long photoRequestId = 1L;
    private String imageUrl = "https://example.com/image.jpg";
    private PhotoRequest photoRequest;
    private PhotoResult photoResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        given(user.getUserName()).willReturn("test");
        given(user.getId()).willReturn(1L);
        photoRequest = PhotoRequest.builder()
                .user(user)
                .gender(Gender.MALE)
                .urls(List.of("https://example.com/image.jpg"))
                .build();
        photoResult = PhotoResult.builder()
                .user(user)
                .photoRequest(photoRequest)
                .createDate(LocalDateTime.now())
                .build();
    }

    @DisplayName("uploadPhoto: 사진 URL을 업로드하고 상태를 업데이트한다.")
    // @Test FIXME: TemplateEngine 관련 Mocking이 올바르지 이루어지지 않고 있음
    void uploadPhotoTest() {
        // given
        given(photoResultRepository.findByPhotoRequestId(photoRequestId)).willReturn(Optional.of(photoResult));
        given(photoRequestRepository.findById(any())).willReturn(Optional.of(photoRequest));
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.remove(ConstantUtil.USER_ID_KEY, user.getId())).willReturn(1L);
        given(redisTemplate.delete(user.getId().toString())).willReturn(true);
        given(templateEngine.process(anyString(), any(IContext.class))).willReturn("test");

        // when
        photoService.uploadPhoto(photoRequestId, imageUrl);

        // then
        assertThat(photoResult.getImageUrl()).isEqualTo(imageUrl);
        verify(photoResultRepository, times(1)).save(photoResult);
        verify(photoRequestRepository, times(1)).save(photoRequest);
        verify(setOperations, times(1)).remove(ConstantUtil.USER_ID_KEY, user.getId().toString());
        verify(redisTemplate, times(1)).delete(anyString());
    }

    @DisplayName("uploadPhoto: 사진 결과를 찾을 수 없는 경우 예외가 발생한다.")
    // @Test
    void uploadPhotoNotFoundTest() {
        // given
        given(photoResultRepository.findByPhotoRequestId(photoRequestId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> photoService.uploadPhoto(photoRequestId, imageUrl))
                .isInstanceOf(BaseException.class)
                .hasMessage(ResponseCode.PHOTO_REQUEST_NOT_FOUND.getMessage());
    }

    @DisplayName("uploadPhoto: 이미 URL이 업로드된 경우 예외가 발생한다.")
    // @Test
    void uploadPhotoAlreadyUploadedTest() {
        // given
        photoRequest.finishStatus();
        given(photoResultRepository.findByPhotoRequestId(photoRequestId)).willReturn(Optional.of(photoResult));
        given(photoRequestRepository.findById(any())).willReturn(Optional.of(photoRequest));

        // when & then
        assertThatThrownBy(() -> photoService.uploadPhoto(photoRequestId, imageUrl))
                .isInstanceOf(BaseException.class)
                .hasMessage(ResponseCode.URL_ALREADY_UPLOADED.getMessage());
    }

    @DisplayName("getPhotoUrl: 요청된 사진의 URL을 반환한다.")
    // @Test
    void getPhotoUrlTest() {
        // given
        given(photoResultRepository.findByPhotoRequestId(photoRequestId)).willReturn(Optional.of(photoResult));
        photoResult.update(imageUrl);

        // when
        String resultUrl = photoService.getPhotoUrl(photoRequestId);

        // then
        assertThat(resultUrl).isEqualTo(imageUrl);
        verify(photoResultRepository, times(1)).findByPhotoRequestId(photoRequestId);
    }

    @DisplayName("getPhotoUrl: 사진 결과를 찾을 수 없는 경우 예외가 발생한다.")
    //@Test
    void getPhotoUrlNotFoundTest() {
        // given
        given(photoResultRepository.findByPhotoRequestId(photoRequestId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> photoService.getPhotoUrl(photoRequestId))
                .isInstanceOf(BaseException.class)
                .hasMessage(ResponseCode.PHOTO_RESULT_NOT_FOUND.getMessage());
    }
}
