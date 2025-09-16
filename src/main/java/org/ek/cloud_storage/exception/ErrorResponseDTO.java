package org.ek.cloud_storage.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Error - Error Response DTO")
public class ErrorResponseDTO {
    @Schema(description = "error message", example = "Unauthorized")
    private String message;
}
