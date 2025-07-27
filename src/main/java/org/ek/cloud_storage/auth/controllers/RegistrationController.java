package org.ek.cloud_storage.auth.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.api.docs.annotations.SignUpApiDocs;
import org.ek.cloud_storage.auth.domain.UserDetailsRequestDTO;
import org.ek.cloud_storage.auth.domain.UserResponseDTO;
import org.ek.cloud_storage.auth.domain.User;
import org.ek.cloud_storage.auth.mappers.UserMapper;
import org.ek.cloud_storage.auth.services.RegistrationService;
import org.ek.cloud_storage.minio.services.bucket.BucketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserMapper userMapper;
    private final BucketService bucketService;


    @PostMapping("/sign-up")
    @Tag(name = "Auth")
    @SignUpApiDocs
    public ResponseEntity<UserResponseDTO> signUp(
            @RequestBody @Valid UserDetailsRequestDTO registrationRequest) throws IOException {
        User user =  registrationService.registerNewUser(registrationRequest).orElse(null);

        String folderPathForNewUser = "user-" + user.getId() + "-files/";

        bucketService.createEmptyFolder(folderPathForNewUser);

        UserResponseDTO userResponseDTO = userMapper.usertoUserResponseDTO(user);

        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }

}
