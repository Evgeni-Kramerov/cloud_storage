package org.ek.cloud_storage.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ek.cloud_storage.domain.UserResponseDTO;
import org.ek.cloud_storage.auth.security.CloudUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/user/me")
    public ResponseEntity<UserResponseDTO> currentUser(
            @AuthenticationPrincipal CloudUserDetails userDetails) {

        log.info("Received GET request for user {}", userDetails.getUsername());

        return ResponseEntity.ok(new UserResponseDTO(userDetails.getUsername()));
    }

}
