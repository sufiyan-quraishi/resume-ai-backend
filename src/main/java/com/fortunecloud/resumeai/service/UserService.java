package com.fortunecloud.resumeai.service;

import com.fortunecloud.resumeai.dto.user.UpdateProfileRequest;
import com.fortunecloud.resumeai.dto.user.UserProfileResponse;
import com.fortunecloud.resumeai.entity.User;
import com.fortunecloud.resumeai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg", "image/jpg", "image/webp");
    private static final long MAX_PHOTO_BYTES = 5L * 1024 * 1024;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String uploadDir;
    private final String publicPath;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.upload.dir:./uploads}") String uploadDir,
                       @Value("${app.upload.public-path:/uploads}") String publicPath) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.uploadDir = uploadDir;
        this.publicPath = publicPath;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public UserProfileResponse getProfile(String email) {
        return new UserProfileResponse(getByEmail(email));
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest req) {
        User user = getByEmail(email);
        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName().trim());
        }
        if (req.getPhone() != null) {
            user.setPhone(req.getPhone().trim());
        }
        if (req.getHeadline() != null) {
            user.setHeadline(req.getHeadline().trim());
        }
        userRepository.save(user);
        return new UserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse uploadPhoto(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose an image to upload");
        }
        if (file.getContentType() == null || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only PNG, JPG, or WEBP images are allowed");
        }
        if (file.getSize() > MAX_PHOTO_BYTES) {
            throw new IllegalArgumentException("Image must be smaller than 5MB");
        }

        User user = getByEmail(email);
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String ext = switch (file.getContentType()) {
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> ".jpg";
            };
            
            String filename = "user-" + user.getId() + "-" + UUID.randomUUID() + ext;
            Path target = dir.resolve(filename);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            user.setPhotoUrl(publicPath + "/" + filename);
            userRepository.save(user);
            return new UserProfileResponse(user);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save the uploaded photo, please try again");
        }
    }

    @Transactional
    public void deleteAccount(String email, String passwordConfirmation) {
        User user = getByEmail(email);
        if (!passwordEncoder.matches(passwordConfirmation, user.getPassword())) {
            throw new IllegalArgumentException("Password is incorrect");
        }
        userRepository.delete(user);
    }
}