package com.aistudio.service.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "old password is required")
    @JsonAlias("oldPassword")
    private String oldPassword;

    @NotBlank(message = "new password is required")
    @Size(min = 6, message = "new password must be at least 6 characters")
    @JsonAlias("newPassword")
    private String newPassword;
}
