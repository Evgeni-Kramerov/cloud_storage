package org.ek.cloud_storage.integration.controllers;

import org.ek.cloud_storage.domain.dto.UserDetailsRequestDTO;
import org.ek.cloud_storage.services.RegistrationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RegistrationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistrationService registrationService;

    private static PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:latest");

    @BeforeAll
    public static void setUp() {
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }


    @Test
    void when_registration_data_is_ok_then_return_201() throws Exception {
        String json = """
            {
                "username": "user_123",
                "password": "password123"
            }
        """;

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void when_username_too_short_then_return_400() throws Exception {
        String json = """
            {
                "username": "use",
                "password": "password123"
            }
        """;

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void when_password_too_short_then_return_400() throws Exception {
        String json = """
            {
                "username": "user_345",
                "password": "pas"
            }
        """;

        mockMvc.perform(post("/api/v1/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_allow_access_with_correct_credentials() throws Exception {
        registrationService.registerNewUser(new UserDetailsRequestDTO("username", "password"));

        String json = """
            {
                "username": "username",
                "password": "password"
            }
        """;

        mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

    }

    @Test
    void should_not_allow_access_to_user_me_without_auth()  throws Exception {
        mockMvc.perform(get("/api/v1/auth/user/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_allow_access_to_user_me_with_auth_and_session()  throws Exception {
        registrationService.registerNewUser(new UserDetailsRequestDTO("username", "password"));

        String json = """
            {
                "username": "username",
                "password": "password"
            }
        """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        mockMvc.perform(get("/api/v1/auth/user/me").
                session((MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("username"));

    }

    @Test
    void should_not_allow_logout_without_auth()  throws Exception {
        mockMvc.perform(post("/api/v1/auth/sign-out"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_allow_logout_with_auth()  throws Exception {
        registrationService.registerNewUser(new UserDetailsRequestDTO("username", "password"));

        String json = """
            {
                "username": "username",
                "password": "password"
            }
        """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        mockMvc.perform(post("/api/v1/auth/sign-out").
                        session((MockHttpSession) session))
                .andExpect(status().isNoContent());

    }
}
