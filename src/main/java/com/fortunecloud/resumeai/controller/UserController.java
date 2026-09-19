package com.fortunecloud.resumeai.controller;

import com.fortunecloud.resumeai.dto.user.PasswordConfirmRequest;
import com.fortunecloud.resumeai.dto.user.UpdateProfileRequest;
import com.fortunecloud.resumeai.dto.user.UserProfileResponse;
import com.fortunecloud.resumeai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * NEW. Everything here requires a valid JWT (see SecurityConfig) -- the
 * current user's email is read from the authenticated principal, never from
 * a client-supplied id, so one user can never read or edit another's data.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserProfileResponse me(Authentication auth) {
        return userService.getProfile(auth.getName());
    }

    @PutMapping("/me")
    public UserProfileResponse update(Authentication auth, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(auth.getName(), request);
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserProfileResponse uploadPhoto(Authentication auth, @RequestParam("file") MultipartFile file) {
        return userService.uploadPhoto(auth.getName(), file);
    }

    @DeleteMapping("/me")
    public Map<String, String> deleteAccount(Authentication auth, @Valid @RequestBody PasswordConfirmRequest request) {
        userService.deleteAccount(auth.getName(), request.getPassword());
        return Map.of("message", "Account permanently deleted");
    }
}
