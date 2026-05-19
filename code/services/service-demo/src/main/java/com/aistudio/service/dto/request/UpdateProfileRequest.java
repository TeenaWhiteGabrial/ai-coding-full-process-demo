package com.aistudio.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 50, message = "real name must not exceed 50 characters")
    private String realName;

    @Email(message = "email is invalid")
    @Size(max = 100, message = "email must not exceed 100 characters")
    private String email;

    @Size(max = 500, message = "avatar url must not exceed 500 characters")
    private String avatar;
}
