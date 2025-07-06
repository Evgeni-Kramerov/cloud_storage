package org.ek.cloud_storage.auth.services;

import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.auth.domain.UserDetailsRequestDTO;
import org.ek.cloud_storage.auth.domain.User;
import org.ek.cloud_storage.auth.mappers.UserMapper;
import org.ek.cloud_storage.auth.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public Optional<User> registerNewUser(UserDetailsRequestDTO userDetailsRequestDTO) {

        User user = userMapper.registrationRequesttoUser(userDetailsRequestDTO);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        return Optional.of(user);
    }
}
