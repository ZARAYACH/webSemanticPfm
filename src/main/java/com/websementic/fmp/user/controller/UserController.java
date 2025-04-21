package com.websementic.fmp.user.controller;

import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.exeption.NotFoundException;
import com.websementic.fmp.user.UserMapper;
import com.websementic.fmp.user.model.User;
import com.websementic.fmp.user.model.dto.UserDto;
import com.websementic.fmp.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    private List<UserDto> listUsers() {
        return userMapper.toUser(userService.list());
    }

    @GetMapping("/me")
    private UserDto getMe(@AuthenticationPrincipal UserDetails userDetails) throws NotFoundException {
        return userMapper.toUserDto(userService.findByEmail(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    private UserDto getUserById(@PathVariable long id) throws NotFoundException {
        return userMapper.toUserDto(userService.findById(id));
    }

    @PostMapping
    private UserDto createUser(@RequestBody UserDto.PostUserDto userDto) throws BadArgumentException {
        return userMapper.toUserDto(userService.create(userDto, User.Role.USER));
    }

    @PutMapping("/{id}")
    private UserDto modifyUser(@RequestBody UserDto.PostUserDto userDto, @PathVariable Long id) throws NotFoundException, BadArgumentException {
        User user = userService.findById(id);
        return userMapper.toUserDto(userService.modify(user, userDto));
    }

    @DeleteMapping("/{id}")
    private Map<String, Boolean> deleteUser(@PathVariable Long id) throws NotFoundException {
        User user = userService.findById(id);
        userService.delete(user);
        return Collections.singletonMap("deleted", true);
    }
}
