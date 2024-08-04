package gdsc.cau.puangbe.auth.exception;

import gdsc.cau.puangbe.common.exception.BaseException;
import gdsc.cau.puangbe.common.util.ResponseCode;

public class AuthException extends BaseException {

  public AuthException(ResponseCode responseCode) {
    super(responseCode);
  }
}
