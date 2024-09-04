package gdsc.cau.puangbe.photorequest.service;

import gdsc.cau.puangbe.photorequest.dto.CreateImageDto;
import java.util.List;

public interface PhotoRequestService {
    //이미지 처리 요청 생성 (RabbitMQ호출)
    Long createImage(CreateImageDto dto, Long userId);

    //유저의 전체 사진 리스트 조회
    List<String> getRequestImages(Long userId);

    //최근 생성 요청한 이미지의 상태 조회
    String getRequestStatus(Long userId);

    // 이미지 처리 요청이 끝나지 않았을 경우 이메일 업데이트
    Long updateEmail(Long userId, String email);
}
