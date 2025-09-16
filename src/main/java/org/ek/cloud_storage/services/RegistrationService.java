package org.ek.cloud_storage.services;

import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ek.cloud_storage.domain.UserDetailsRequestDTO;
import org.ek.cloud_storage.domain.User;
import org.ek.cloud_storage.exception.MinIoResourceException;
import org.ek.cloud_storage.mappers.UserMapper;
import org.ek.cloud_storage.repositories.UserRepository;
import org.ek.cloud_storage.exception.UserAlreadyExists;
import org.ek.cloud_storage.exception.UserStorageException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Principal;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final BucketService bucketService;

    public User registerNewUser(UserDetailsRequestDTO userDetailsRequestDTO) {
        log.debug("Received request to register new user: {}", userDetailsRequestDTO);

        User user = userMapper.registrationRequesttoUser(userDetailsRequestDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.error("User {} already exists", userDetailsRequestDTO.getUsername());
            throw new UserAlreadyExists(e.getMessage());
        }

        createFolderForNewUser(user);

        return user;
    }

    private void createFolderForNewUser(User  user)  {
        log.debug("Creating folder for new user: {}", user);
        String folderPathForNewUser = "user-" + user.getId() + "-files/";
        try {
            bucketService.createEmptyFolder((Principal) user, folderPathForNewUser);
        }
        catch (MinIoResourceException e) {
            log.error("Error while creating folder for new user: {}", e.getMessage());
            throw new UserStorageException("Error while creating folder for new user");
        }
    }


}
