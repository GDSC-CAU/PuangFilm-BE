package gdsc.cau.puangbe.auth.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdsc.cau.puangbe.auth.config.KakaoLoginProperties;
import gdsc.cau.puangbe.auth.dto.AuthPayload;
import gdsc.cau.puangbe.auth.dto.KakaoIdTokenPublicKey;
import gdsc.cau.puangbe.auth.dto.KakaoJWK;
import gdsc.cau.puangbe.auth.exception.AuthException;
import gdsc.cau.puangbe.common.util.ResponseCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.security.SignatureException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OIDCProvider {
  private final KakaoLoginProperties kakaoLoginProperties;

  public AuthPayload verify(String token, String iss, KakaoIdTokenPublicKey kakaoIdTokenPublicKey) {
    verifyPayload(token, iss);
    return verifySignature(token, kakaoIdTokenPublicKey);
  }

  private void verifyPayload(String token, String iss) {
    AuthPayload authPayload = extractPayloadFromTokenString(token);

    if (!authPayload.getIss().equals(iss)) {
      throw new AuthException(ResponseCode.UNAUTHORIZED);
    }
    if (!authPayload.getAud().equals(kakaoLoginProperties.getClientId())) {
      throw new AuthException(ResponseCode.UNAUTHORIZED);
    }
    if (authPayload.getExp().compareTo(System.currentTimeMillis() / 1000) < 0) {
      throw new AuthException(ResponseCode.UNAUTHORIZED);
    }
  }

  private AuthPayload extractPayloadFromTokenString(String token) {
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      throw new AuthException(ResponseCode.BAD_REQUEST); // Invalid JWT token
    }
    byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);

    try {
      return new ObjectMapper().readValue(payloadBytes, AuthPayload.class);
    } catch (IOException e) {
      throw new AuthException(ResponseCode.INTERNAL_SERVER_ERROR);
    }
  }

  private String extractHeaderKidFromTokenString(String token)  {
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      throw new AuthException(ResponseCode.BAD_REQUEST); // Invalid JWT token
    }

    byte[] headerBytes = Base64.getUrlDecoder().decode(parts[0]);

    try {
      return new ObjectMapper().readTree(headerBytes).get("kid").asText();
    } catch (IOException e) {
      throw new AuthException(ResponseCode.INTERNAL_SERVER_ERROR);
    }
  }

  private AuthPayload verifySignature(String token, KakaoIdTokenPublicKey kakaoIdTokenPublicKey) {
    KakaoJWK kakaoJWK = getOIDCPublicKey(extractHeaderKidFromTokenString(token), kakaoIdTokenPublicKey);

    Claims payload;
    AuthPayload authPayload = new AuthPayload();
    try {
      payload = Jwts.parser()
          .verifyWith(getRSAPublicKey(kakaoJWK))
          .build()
          .parseSignedClaims(token)
          .getPayload();
      authPayload.setSub(payload.getSubject());
      authPayload.setNickname(payload.get("nickname").toString());
      return authPayload;
    } catch (SignatureException e) {
      throw e;
    } catch (SecurityException e) {
      throw new AuthException(ResponseCode.UNAUTHORIZED);
    }
  }

  private KakaoJWK getOIDCPublicKey(String kid, KakaoIdTokenPublicKey kakaoIdTokenPublicKey) {
    return kakaoIdTokenPublicKey.getKeys().stream()
        .filter(kakaoJWK -> kakaoJWK.getKid().equals(kid))
        .findFirst()
        .orElseThrow(() -> new AuthException(ResponseCode.BAD_REQUEST)); // 일치하는 PK 없음
  }

  private PublicKey getRSAPublicKey(KakaoJWK kakaoJWK) {
    byte[] decodeN = Base64.getUrlDecoder().decode(kakaoJWK.getN());
    byte[] decodeE = Base64.getUrlDecoder().decode(kakaoJWK.getE());
    BigInteger n = new BigInteger(1, decodeN);
    BigInteger e = new BigInteger(1, decodeE);

    RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(n, e);
    KeyFactory keyFactory;
    try {
      keyFactory = KeyFactory.getInstance("RSA");
    } catch (NoSuchAlgorithmException ex) {
      throw new AuthException(ResponseCode.INTERNAL_SERVER_ERROR);
    }

    try {
      return keyFactory.generatePublic(rsaPublicKeySpec);
    } catch (InvalidKeySpecException ex) {
      throw new AuthException(ResponseCode.INTERNAL_SERVER_ERROR);
    }
  }
}
