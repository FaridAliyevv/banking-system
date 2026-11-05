package com.example.ms_user.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpdateUserRequest {

    private String firstName;
    private String lastName;
    @Email
    private String email;
    private String password;
}
