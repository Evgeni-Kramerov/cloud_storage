package org.ek.cloud_storage.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.domain.dto.RegistrationRequestDTO;
import org.ek.cloud_storage.domain.dto.UserResponseDTO;
import org.ek.cloud_storage.domain.model.User;
import org.ek.cloud_storage.mappers.UserMapper;
import org.ek.cloud_storage.services.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserMapper userMapper;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDTO> signUp(
            @RequestBody @Valid RegistrationRequestDTO registrationRequest) {
        User user =  registrationService.registerNewUser(registrationRequest).orElse(null);
        UserResponseDTO userResponseDTO = userMapper.usertoUserResponseDTO(user);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }
}
