package org.ek.cloud_storage.auth.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.ErrorResponseDTO;
import org.ek.cloud_storage.auth.domain.UserDetailsRequestDTO;
import org.ek.cloud_storage.auth.domain.UserResponseDTO;
import org.ek.cloud_storage.auth.domain.User;
import org.ek.cloud_storage.auth.mappers.UserMapper;
import org.ek.cloud_storage.auth.services.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserMapper userMapper;

    @Operation(summary = "Sign up for the service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "user created"),
            @ApiResponse(responseCode = "400", description = "validation error", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation =  ErrorResponseDTO.class)
            )),
            @ApiResponse(responseCode = "409", description = "user with this username already exists", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation =  ErrorResponseDTO.class)
            )),
            @ApiResponse(responseCode = "500", description = "unknown error" , content = @Content()),
    })
    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDTO> signUp(
            @RequestBody @Valid UserDetailsRequestDTO registrationRequest) {
        User user =  registrationService.registerNewUser(registrationRequest).orElse(null);
        UserResponseDTO userResponseDTO = userMapper.usertoUserResponseDTO(user);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }

}
