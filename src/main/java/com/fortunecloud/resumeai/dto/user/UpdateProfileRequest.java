package com.fortunecloud.resumeai.dto.user;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 120)
    private String fullName;

    private String phone;

    @Size(max = 120)
    private String headline;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String fullName, String phone, String headline) {
        this.fullName = fullName;
        this.phone = phone;
        this.headline = headline;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }
}