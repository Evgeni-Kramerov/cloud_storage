package org.ek.cloud_storage.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Auth - User Request DTO", description = "User Response DTO")
public class UserDetailsRequestDTO {
    @Size(min = 4, max = 40, message = "Username must be from 4 to 40 characters long")
    @Schema(description = "Username", example = "John_Doe")
    private String username;

    @Size(min = 4, max = 40, message = "Password must be from 4 to 40 characters long")
    @Schema(description = "Username", example = "password")
    private String password;
}
