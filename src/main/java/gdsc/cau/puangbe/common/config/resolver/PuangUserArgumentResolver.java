package gdsc.cau.puangbe.common.config.resolver;

import gdsc.cau.puangbe.auth.external.JwtProvider;
import gdsc.cau.puangbe.common.annotation.PuangUser;
import gdsc.cau.puangbe.common.exception.AuthException;
import gdsc.cau.puangbe.common.util.ResponseCode;
import gdsc.cau.puangbe.user.entity.User;
import gdsc.cau.puangbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class PuangUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PuangUser.class) && parameter.getParameterType().equals(User.class);
    }

    @Override
    public User resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String accessToken = jwtProvider.getTokenFromAuthorizationHeader(webRequest.getHeader("Authorization"));
        jwtProvider.validateToken(accessToken);
        String kakaoId = jwtProvider.getKakaoIdFromToken(accessToken);
        return userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new AuthException(ResponseCode.UNAUTHORIZED));
    }
}
