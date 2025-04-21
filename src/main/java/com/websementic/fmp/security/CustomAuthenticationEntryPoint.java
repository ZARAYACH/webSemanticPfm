package com.websementic.fmp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websementic.fmp.exeption.modal.ExceptionDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ExceptionDto exceptionDto = ExceptionDto.builder()
                .message(authException.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .statusDescription(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .build();
        log.debug("Exception error id : {{}} : {}", exceptionDto.getErrorId(), authException.getMessage());
        authException.printStackTrace();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(exceptionDto));
    }
}
