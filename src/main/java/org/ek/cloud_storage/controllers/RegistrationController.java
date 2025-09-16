package org.ek.cloud_storage.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ek.cloud_storage.api.docs.annotations.SignUpApiDocs;
import org.ek.cloud_storage.domain.UserDetailsRequestDTO;
import org.ek.cloud_storage.domain.UserResponseDTO;
import org.ek.cloud_storage.domain.User;
import org.ek.cloud_storage.mappers.UserMapper;
import org.ek.cloud_storage.services.AuthService;
import org.ek.cloud_storage.services.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserMapper userMapper;
    private final AuthService authService;

    @PostMapping("/sign-up")
    @Tag(name = "Auth")
    @SignUpApiDocs
    public ResponseEntity<UserResponseDTO> signUp(
            @RequestBody @Valid UserDetailsRequestDTO registrationRequest,
            HttpServletRequest request) throws IOException {

        log.info("Received POST registration request for user {}", registrationRequest.getUsername());

        User user= registrationService.registerNewUser(registrationRequest);

        authService.authenticateUser(registrationRequest, request);

        UserResponseDTO userResponseDTO = userMapper.usertoUserResponseDTO(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }

}
