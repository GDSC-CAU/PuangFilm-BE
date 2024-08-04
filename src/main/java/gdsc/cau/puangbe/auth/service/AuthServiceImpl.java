package gdsc.cau.puangbe.auth.service;

import gdsc.cau.puangbe.auth.dto.AuthPayload;
import gdsc.cau.puangbe.auth.dto.OAuthTokenResponse;
import gdsc.cau.puangbe.auth.dto.LoginResponse;
import gdsc.cau.puangbe.auth.dto.ReissueResponse;
import gdsc.cau.puangbe.auth.entity.Token;
import gdsc.cau.puangbe.auth.exception.AuthException;
import gdsc.cau.puangbe.auth.external.JwtProvider;
import gdsc.cau.puangbe.auth.external.KakaoProvider;
import gdsc.cau.puangbe.auth.external.OIDCProvider;
import gdsc.cau.puangbe.auth.repository.TokenRepository;
import gdsc.cau.puangbe.common.util.ResponseCode;
import gdsc.cau.puangbe.user.repository.UserRepository;
import gdsc.cau.puangbe.user.entity.User;
import io.jsonwebtoken.security.SignatureException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final TokenRepository tokenRepository;
  private final UserRepository userRepository;

  private final KakaoProvider kakaoProvider;
  private final OIDCProvider OIDCProvider;
  private final JwtProvider jwtProvider;

  private final String ISS = "https://kauth.kakao.com";

  @Override
  public LoginResponse loginWithKakao(String code) {
    // 카카오로부터 토큰 발급
    OAuthTokenResponse kakaoToken = kakaoProvider.getTokenByCode(code);

    // ID 토큰 유효성 검증
    AuthPayload authPayload;
    try {
      authPayload = OIDCProvider.verify(kakaoToken.getIdToken(), ISS, kakaoProvider.getOIDCPublicKeyList());
    } catch (SignatureException e) {
      authPayload = OIDCProvider.verify(kakaoToken.getIdToken(), ISS, kakaoProvider.getUpdatedOIDCPublicKeyList());
    }

    final String kakaoId = authPayload.getSub();
    final String userName = authPayload.getNickname();

    // JWT 토큰 발행 및 DB 업데이트 (가입 완료 or 로그인)
    String refreshTokenString = jwtProvider.createRefreshToken(kakaoId, userName);

    userRepository.findByKakaoId(kakaoId)
        .map(u -> {
          u.updateRequestDate(LocalDateTime.now());
          return userRepository.save(u);
        })
        .orElseGet(() -> userRepository.save(User.builder()
            .userName(userName)
            .createDate(LocalDateTime.now())
            .requestDate(LocalDateTime.now())
            .kakaoId(kakaoId)
            .build()
        ));

    Token refreshToken = Token.builder()
        .refreshToken(refreshTokenString)
        .kakaoId(kakaoId)
        .expiresAt(jwtProvider.getExpirationFromToken(refreshTokenString).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
        .build();
    Token savedRefreshToken = tokenRepository.save(refreshToken);
    System.out.println(savedRefreshToken.getId());

    String accessToken = jwtProvider.createAccessToken(authPayload.getSub(), savedRefreshToken.getId());

    return new LoginResponse(accessToken);
  }

  @Override
  public ReissueResponse reissue(String authorizationHeader) {
    String accessToken = jwtProvider.getTokenFromAuthorizationHeader(authorizationHeader);

    Long refreshTokenId = jwtProvider.getRefreshIdFromExpiredToken(accessToken);

    Token refreshToken = tokenRepository.findById(refreshTokenId)
        .orElseThrow(() -> new AuthException(ResponseCode.UNAUTHORIZED));

    String newAccessToken = jwtProvider.reissueAccessToken(refreshToken.getRefreshToken(), refreshToken.getId());

    return new ReissueResponse(newAccessToken);
  }
}
