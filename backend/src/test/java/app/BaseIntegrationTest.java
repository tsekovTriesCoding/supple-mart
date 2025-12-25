package app;

import app.config.TestMailConfig;
import app.security.jwt.JwtService;
import app.testutil.JwtTestUtils;
import app.user.model.User;
import app.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Base class for integration tests that require a full Spring context
 * with a real MySQL database running in a Docker container.
 *
 * Usage:
 * - Extend this class for integration tests that need database access
 * - The MySQL container is automatically started via static block (singleton pattern)
 * - Flyway migrations are applied to create the schema
 * - Container is shared across ALL test classes for Testcontainers Cloud stability
 *
 * Features:
 * - MockMvc pre-configured with Spring Security
 * - ObjectMapper for JSON serialization
 * - JwtService for generating test tokens
 * - Utility methods for authenticated requests
 *
 * Note: Uses manual container lifecycle (not @Testcontainers/@Container) for better
 * control with Testcontainers Cloud, where connection stability is critical.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestMailConfig.class)
public abstract class BaseIntegrationTest {

    // Singleton container - started once and shared across ALL test classes
    // Using static block instead of @Container for manual lifecycle control
    private static final MySQLContainer<?> mysqlContainer;

    static {
        mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("supplemart_test_db")
                .withUsername("test")
                .withPassword("test")
                // Extended connection settings for Testcontainers Cloud stability
                .withUrlParam("connectTimeout", "120000")
                .withUrlParam("socketTimeout", "120000")
                // Disable SSL to avoid certificate issues with Testcontainers Cloud
                .withUrlParam("sslMode", "DISABLED")
                .withUrlParam("allowPublicKeyRetrieval", "true")
                .withUrlParam("useSSL", "false")
                // Auto-reconnect settings for connection stability
                .withUrlParam("autoReconnect", "true")
                .withUrlParam("failOverReadOnly", "false")
                .withUrlParam("maxReconnects", "10")
                // Wait strategy - ensure MySQL is fully ready before tests start
                .waitingFor(Wait.forLogMessage(".*ready for connections.*", 2)
                        .withStartupTimeout(Duration.ofMinutes(5)))
                // Startup timeout for container itself
                .withStartupTimeout(Duration.ofMinutes(5));

        // Start the container once - it will remain running for all tests
        mysqlContainer.start();
    }

    /**
     * Configure Spring datasource properties dynamically from the running container.
     * This ensures all test classes use the same container instance.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected UserRepository userRepository;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    /**
     * Generates a valid JWT token for the given user.
     */
    protected String generateToken(User user) {
        return jwtService.generateToken(JwtTestUtils.createUserDetails(user));
    }

    /**
     * Returns the Authorization header value for the given token.
     */
    protected String bearerToken(String token) {
        return "Bearer " + token;
    }

    /**
     * Returns the base URL for the test server.
     */
    protected String baseUrl() {
        return "http://localhost:" + port;
    }
}

