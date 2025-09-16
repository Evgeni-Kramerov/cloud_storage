package org.ek.cloud_storage.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Auth - User Response DTO", description = "User Response DTO")
public class UserResponseDTO {
    @Schema(description = "Username", example = "John_Doe")
    private String username;
}
