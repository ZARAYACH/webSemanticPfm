package com.websementic.fmp.user.service;

import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.exeption.NotFoundException;
import com.websementic.fmp.user.model.User;
import com.websementic.fmp.user.model.dto.UserDto;
import com.websementic.fmp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final EmailValidator emailValidator = EmailValidator.getInstance();

    public List<User> list() {
        return userRepository.findAll();
    }

    public User findById(long id) throws NotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }

    public User findByEmail(String email) throws NotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User with email " + email + " not found"));
    }

    public User create(UserDto.PostUserDto postUserDto, User.Role role) throws BadArgumentException {
        validateUserDto(postUserDto);
        return userRepository.save(new User(postUserDto.email(),
                passwordEncoder.encode(postUserDto.password()),
                postUserDto.firstName(),
                postUserDto.lastName(),
                postUserDto.birthDate(), role));

    }

    private void validateUserDto(UserDto userDto) throws BadArgumentException {
        try {
            //TODO
        } catch (Exception e) {
            throw new BadArgumentException(e);
        }
    }

    private void validateUserDto(UserDto.PostUserDto postUserDto) throws BadArgumentException {
        try {
            validateAuthCredentials(postUserDto.email(), postUserDto.password());
        } catch (IllegalArgumentException | BadArgumentException e) {
            throw new BadArgumentException(e);
        }
    }

    private void validateAuthCredentials(String email, String password) throws BadArgumentException {
        try {
            Assert.hasText(email, "Email is required");
            Assert.isTrue(emailValidator.isValid(email), "Email is not valid");
            Assert.isTrue(!userRepository.existsByEmail(email), "Email Already Exists");
            validatePassword(password);
        } catch (IllegalArgumentException e) {
            throw new BadArgumentException(e);
        }
    }

    private void validatePassword(String password) {
        Assert.isTrue(passwordValidator.validate(new PasswordData(password)).isValid(), "Password should be at least be 8 character with one uppercase, one special and one digit characters");
    }
    @PreAuthorize("hasRole('ADMIN') or #user.id == authentication.principal.id ")
    public User modify(User user, UserDto.PostUserDto postUserDto) throws BadArgumentException {
        user.setBirthDate(postUserDto.birthDate());
        user.setFirstName(postUserDto.firstName());
        user.setLastName(postUserDto.lastName());
        if (StringUtils.hasText(postUserDto.password())) {
            try {
                validatePassword(postUserDto.password());
            } catch (IllegalArgumentException e) {
                throw new BadArgumentException(e);
            }
            user.setPassword(passwordEncoder.encode(postUserDto.password()));
        }

        return userRepository.save(user);
    }

    public void delete(User user) throws NotFoundException {
        userRepository.delete(user);
    }
}
