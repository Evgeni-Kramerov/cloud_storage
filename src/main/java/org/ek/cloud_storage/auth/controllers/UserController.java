package org.ek.cloud_storage.auth.controllers;

import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.auth.domain.UserResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/user/me")
    public ResponseEntity<?> currentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        return ResponseEntity.ok(new UserResponseDTO(principal.getName()));
    }

}
