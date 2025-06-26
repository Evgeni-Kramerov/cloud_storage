package org.ek.cloud_storage.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.domain.dto.UserDetailsRequestDTO;
import org.ek.cloud_storage.domain.dto.UserResponseDTO;
import org.ek.cloud_storage.domain.model.User;
import org.ek.cloud_storage.mappers.UserMapper;
import org.ek.cloud_storage.services.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserMapper userMapper;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDTO> signUp(
            @RequestBody @Valid UserDetailsRequestDTO registrationRequest) {
        User user =  registrationService.registerNewUser(registrationRequest).orElse(null);
        UserResponseDTO userResponseDTO = userMapper.usertoUserResponseDTO(user);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }


    @GetMapping("/user/me")
    public ResponseEntity<?> currentUser(Principal principal, HttpServletRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        return ResponseEntity.ok(new UserResponseDTO(principal.getName()));
    }
}
