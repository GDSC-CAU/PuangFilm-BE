package gdsc.cau.puangbe.common.exception;

import gdsc.cau.puangbe.auth.exception.AuthException;
import gdsc.cau.puangbe.common.util.ApiResponse;
import gdsc.cau.puangbe.user.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public ApiResponse<Void> handleUserException(UserException e) {
        log.info("UserException: {}", e.getMessage());
        return ApiResponse.fail(e.getResponseCode(), e.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public ApiResponse<Void> handleAuthException(AuthException e) {
        log.info("AuthException: {}", e.getMessage());
        return ApiResponse.fail(e.getResponseCode(), e.getMessage());
    }
}
