package com.websementic.fmp.user.controller;

import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.user.UserMapper;
import com.websementic.fmp.user.model.User;
import com.websementic.fmp.user.model.dto.UserDto;
import com.websementic.fmp.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/signup")
@Tag(name = "signup")
@RequiredArgsConstructor
public class SignUpController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    private UserDto signUp(@RequestBody UserDto.PostUserDto userDto) throws BadArgumentException {
        return userMapper.toUserDto(userService.create(userDto, User.Role.USER));
    }
}
