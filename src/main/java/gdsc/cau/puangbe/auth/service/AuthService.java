package gdsc.cau.puangbe.auth.service;

import gdsc.cau.puangbe.auth.dto.LoginResponse;
import gdsc.cau.puangbe.auth.dto.ReissueResponse;

public interface AuthService {
  LoginResponse loginWithKakao(String code);
  ReissueResponse reissue(String authorizationHeader);
}
