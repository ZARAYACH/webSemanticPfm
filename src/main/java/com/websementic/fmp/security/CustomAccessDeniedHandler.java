package com.websementic.fmp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websementic.fmp.exeption.modal.ExceptionDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .message(accessDeniedException.getMessage())
                .status(HttpStatus.FORBIDDEN.value())
                .statusDescription(HttpStatus.FORBIDDEN.getReasonPhrase())
                .build();
        log.debug("Exception error id : {{}} : {}", exceptionDto.getErrorId(), accessDeniedException.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(exceptionDto));
    }
}
