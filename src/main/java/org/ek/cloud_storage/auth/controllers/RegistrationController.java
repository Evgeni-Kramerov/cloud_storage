package org.ek.cloud_storage.auth.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserMapper userMapper;
    private final BucketService bucketService;
    private final AuthenticationManager authenticationManager;


    @PostMapping("/sign-up")
    @Tag(name = "Auth")
    @SignUpApiDocs
    public ResponseEntity<UserResponseDTO> signUp(
            @RequestBody @Valid UserDetailsRequestDTO registrationRequest,
            HttpServletRequest request) throws IOException {
        User user =  registrationService.registerNewUser(registrationRequest).orElse(null);

        String folderPathForNewUser = "user-" + user.getId() + "-files/";

        bucketService.createEmptyFolder(folderPathForNewUser);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        registrationRequest.getUsername(), registrationRequest.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        UserResponseDTO userResponseDTO = userMapper.usertoUserResponseDTO(user);

        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }

}
