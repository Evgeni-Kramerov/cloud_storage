package org.ek.cloud_storage.integration.services;

import org.ek.cloud_storage.auth.domain.UserDetailsRequestDTO;
import org.ek.cloud_storage.auth.domain.User;
import org.ek.cloud_storage.auth.services.RegistrationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class RegistrationServiceIT {

    @Autowired
    private RegistrationService registrationService;

    private static PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:latest");

    @BeforeAll
    static void setUp() {
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Test
    void when_adding_new_user_then_record_in_db_is_created() {
        UserDetailsRequestDTO userDetailsRequestDTO =
                new UserDetailsRequestDTO("username", "password");
        Optional<User> user = registrationService.registerNewUser(userDetailsRequestDTO);

        assert user.isPresent();
        assert user.get().getUsername().equals("username");
    }

    @Test
    void when_adding_existing_user_then_exception_is_thrown() {
        UserDetailsRequestDTO userDetailsRequestDTO =
                new UserDetailsRequestDTO("username", "password");

        registrationService.registerNewUser(userDetailsRequestDTO);

        assertThrows(DataIntegrityViolationException.class, () -> {
            registrationService.registerNewUser(userDetailsRequestDTO);
        });
    }



}
