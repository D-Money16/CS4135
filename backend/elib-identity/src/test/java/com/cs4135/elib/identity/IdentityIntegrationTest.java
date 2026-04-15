package com.cs4135.elib.identity;

import com.cs4135.elib.identity.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Identity service
 *
 * - Real Postgres via Testcontainers
 * - Full Spring Boot context (real HTTP)
 * - Covers register + login flows
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IdentityIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @LocalServerPort
    int port;

    @Autowired
    UserRepository userRepository;

    private RestClient client;
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private String registerJson(String username, String password) {
        return """
                {
                    "username": "%s",
                    "email": "%s@email.com",
                    "password": "%s"
                }
                """.formatted(username, username, password);
    }

    private String loginJson(String username, String password) {
        return """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> register(String username, String password) {
        return client.post()
                .uri("/api/user/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(registerJson(username, password))
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> login(String username, String password) {
        return client.post()
                .uri("/api/user/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginJson(username, password))
                .retrieve()
                .body(Map.class);
    }

    @Test
    @Order(1)
    @DisplayName("Register user - success")
    void registerUser_success() {
        Map<String, Object> response = register(USERNAME, PASSWORD);
        assertThat(response).containsEntry("username", USERNAME);
        assertThat(response).containsKey("id");
        assertThat(response).doesNotContainKey("password");
        assertThat(userRepository.findByUsername(USERNAME)).isPresent();
    }

    @Test
    @Order(2)
    @DisplayName("Register user — duplicate username rejected (409)")
    void registerUser_duplicate() {
        register(USERNAME, PASSWORD);

        ResponseEntity<String> resp = client.post()
                .uri("/api/user/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(registerJson(USERNAME, PASSWORD))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(3)
    @DisplayName("Login — success path returns JWT token")
    void login_success() {
        register(USERNAME, PASSWORD);

        Map<String, Object> response = login(USERNAME, PASSWORD);

        assertThat(response).containsKey("token");
        assertThat(response).containsKey("userId");

        String token = (String) response.get("token");
        assertThat(token).isNotBlank();
    }

    @Test
    @Order(4)
    @DisplayName("Login — invalid password returns 401")
    void login_invalidPassword() {
        register(USERNAME, PASSWORD);

        ResponseEntity<String> resp = client.post()
                .uri("/api/user/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginJson(USERNAME, "wrongpassword"))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(5)
    @DisplayName("Login — user not found returns 401")
    void login_userNotFound() {
        ResponseEntity<String> resp = client.post()
                .uri("/api/user/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginJson("unknown", PASSWORD))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(6)
    @DisplayName("JWT token — structure validation")
    void jwtToken_structure() {
        register(USERNAME, PASSWORD);
        Map<String, Object> response = login(USERNAME, PASSWORD);

        String token = (String) response.get("token");
        assertThat(token.split("\\.")).hasSize(3);
    }
}
