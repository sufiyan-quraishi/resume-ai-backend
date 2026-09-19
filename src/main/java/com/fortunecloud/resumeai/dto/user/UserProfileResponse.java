package com.fortunecloud.resumeai.dto.user;

import com.fortunecloud.resumeai.entity.User;
import lombok.Getter;

@Getter
public class UserProfileResponse {
    private final Long id;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String headline;
    private final String photoUrl;
    private final boolean emailVerified;
    private final String createdAt;

    public UserProfileResponse(User u) {
        this.id = u.getId();
        this.fullName = u.getFullName();
        this.email = u.getEmail();
        this.phone = u.getPhone();
        this.headline = u.getHeadline();
        this.photoUrl = u.getPhotoUrl();
        this.emailVerified = u.isEmailVerified();
        this.createdAt = u.getCreatedAt().toString();
    }

	public Long getId() {
		return id;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getHeadline() {
		return headline;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public String getCreatedAt() {
		return createdAt;
	}
    
}
