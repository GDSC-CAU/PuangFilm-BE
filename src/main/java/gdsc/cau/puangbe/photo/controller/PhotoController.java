package gdsc.cau.puangbe.photo.controller;

import gdsc.cau.puangbe.common.util.APIResponse;
import gdsc.cau.puangbe.common.util.ResponseCode;
import gdsc.cau.puangbe.photo.dto.request.UploadImageDto;
import gdsc.cau.puangbe.photo.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "photo", description = "사진 처리 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/photo")
public class PhotoController {

    private final PhotoService photoService;

    @Tag(name = "photo")
    @Operation(summary = "사진 URL 업로드", description = "결과 데이터에 대한 Image URL 업로드를 진행한다.", responses = {
            @ApiResponse(responseCode = "200", description = "사진 업로드 성공"),
            @ApiResponse(responseCode = "404", description = "photoResultId가 유효하지 않은 경우"),
            @ApiResponse(responseCode = "409", description = "이미 url이 업로드되어 종료 상태인 경우")
    })
    @PostMapping("/url")
    public APIResponse<Void> uploadImage(@RequestBody @Valid UploadImageDto uploadImageDto) {
        photoService.uploadPhoto(uploadImageDto.getPhotoResultId(), uploadImageDto.getImageUrl());
        return APIResponse.success(null, ResponseCode.PHOTO_RESULT_URL_UPLOADED.getMessage());
    }

    @Tag(name = "photo")
    @Operation(summary = "사진 URL 조회", description = "결과 데이터에 대한 Image URL을 조회한다.", responses = {
            @ApiResponse(responseCode = "200", description = "사진 URL 조회 성공"),
            @ApiResponse(responseCode = "404", description = "photoResultId가 유효하지 않은 경우")
    })
    @GetMapping("/{imageId}")
    public APIResponse<String> getImage(@PathVariable("imageId") Long photoRequestId) {
        return APIResponse.success(photoService.getPhotoUrl(photoRequestId), ResponseCode.PHOTO_RESULT_URL_FOUND.getMessage());
    }
}
