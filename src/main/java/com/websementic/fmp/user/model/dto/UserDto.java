package com.websementic.fmp.user.model.dto;

import com.websementic.fmp.user.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UserDto(
        @NotNull
        Long id,
        @NotNull
        String email,
        String firstName,
        String lastName,
        LocalDate birthDate,
        User.Role role,
        String groupName
) {
    public record PostUserDto(
            Long id,
            @NotBlank
            String email,
            @NotBlank
            String password,
            String firstName,
            String lastName,
            LocalDate birthDate,
            @NotNull User.Role role
    ) {
    }
}
