package gdsc.cau.puangbe.auth.controller;

import gdsc.cau.puangbe.auth.dto.LoginResponse;
import gdsc.cau.puangbe.auth.dto.ReissueResponse;
import gdsc.cau.puangbe.auth.service.AuthService;
import gdsc.cau.puangbe.common.util.ApiResponse;
import gdsc.cau.puangbe.common.util.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @GetMapping("/login/oauth/kakao")
  public ApiResponse<LoginResponse> loginWithKakao(@RequestParam("code") String code) {
    return ApiResponse.success(authService.loginWithKakao(code), ResponseCode.USER_LOGIN_SUCCESS.getMessage());
  }

  @GetMapping("/reissue")
  public ApiResponse<ReissueResponse> reissue(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    return ApiResponse.success(authService.reissue(authorizationHeader), ResponseCode.USER_TOKEN_REISSUE_SUCCESS.getMessage());
  }
}