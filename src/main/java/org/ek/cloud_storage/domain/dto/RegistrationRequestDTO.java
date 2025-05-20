package org.ek.cloud_storage.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequestDTO {
    @Size(min = 4, max = 40, message = "Username must be from 4 to 40 characters long")
    private String username;

    @Size(min = 4, max = 40, message = "Password must be from 4 to 40 characters long")
    private String password;
}
