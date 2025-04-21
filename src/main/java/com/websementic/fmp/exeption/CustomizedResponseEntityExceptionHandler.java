package com.websementic.fmp.exeption;

import com.websementic.fmp.exeption.modal.ExceptionDto;
import jakarta.persistence.EntityExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestControllerAdvice
@Slf4j
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ExceptionDto> handleNotFoundException(Exception ex) {
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .statusDescription(HttpStatus.NOT_FOUND.getReasonPhrase())
                .build();
        log.debug("Exception error id : {{}} : {}", exceptionDto.getErrorId(), ex.getMessage());
        return new ResponseEntity<>(exceptionDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({BadArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionDto> handleBadRequestExceptions(Exception ex) {
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .statusDescription(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .build();
        log.debug("Exception error id : {{}} : {}", exceptionDto.getErrorId(), ex.getMessage(), ex);
        return new ResponseEntity<>(exceptionDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UnauthorizedException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ExceptionDto> handleUnauthorizedException(Exception ex) {
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .message(ex.getMessage())
                .status(HttpStatus.FORBIDDEN.value())
                .statusDescription(HttpStatus.FORBIDDEN.getReasonPhrase())
                .build();
        log.debug("Exception error id : {{}} : {}", exceptionDto.getErrorId(), ex.getMessage(), ex);
        return new ResponseEntity<>(exceptionDto, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler({AuthenticationException.class, TokenValidationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ExceptionDto> handleUnauthenticatedException(Exception ex) {
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .message(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .statusDescription(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .build();
        log.debug("Exception error id : {{}} : {}", exceptionDto.getErrorId(), ex.getMessage(), ex);
        return new ResponseEntity<>(exceptionDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EntityExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ExceptionDto> handleEntityAlreadyExists(Exception ex) {
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .message(ex.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .statusDescription(HttpStatus.CONFLICT.getReasonPhrase())
                .build();
        log.debug("Exception error id : {{}} : {}", exceptionDto.getErrorId(), ex.getMessage(), ex);
        return new ResponseEntity<>(exceptionDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ExceptionDto> handleAnyOtherException(Exception ex) {
        ex.printStackTrace();
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .message("INTERNAL_SERVER_ERROR")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .statusDescription(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .build();
        log.error("E&rror id: {}", exceptionDto.getErrorId(), ex);
        return new ResponseEntity<>(exceptionDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}



