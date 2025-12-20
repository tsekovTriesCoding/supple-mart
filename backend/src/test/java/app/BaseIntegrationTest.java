package app;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests that require a full Spring context
 * with a real MySQL database running in a Docker container.
 *
 * Usage:
 * - Extend this class for integration tests that need database access
 * - The MySQL container is automatically started and configured via @ServiceConnection
 * - Flyway migrations are applied to create the schema
 * - Container is reused across tests for performance (singleton pattern)
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("supplemart_test_db")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
}

