package com.websementic.fmp.exeption;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

@StandardException
public class AuthenticationInvalidTokenException extends AuthenticationException {

}
