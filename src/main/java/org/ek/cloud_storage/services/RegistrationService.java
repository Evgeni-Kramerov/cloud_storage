package org.ek.cloud_storage.services;

import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.domain.dto.RegistrationRequestDTO;
import org.ek.cloud_storage.domain.model.User;
import org.ek.cloud_storage.mappers.UserMapper;
import org.ek.cloud_storage.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public Optional<User> registerNewUser(RegistrationRequestDTO registrationRequestDTO) {

        User user = userMapper.registrationRequesttoUser(registrationRequestDTO);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        return Optional.of(user);
    }
}
