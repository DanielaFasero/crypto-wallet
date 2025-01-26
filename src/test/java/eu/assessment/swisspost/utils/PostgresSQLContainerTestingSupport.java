package eu.assessment.swisspost.utils;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public interface PostgresSQLContainerTestingSupport {

  PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-bookworm"))
          .withDatabaseName("cryptoWallet");

  @DynamicPropertySource
  static void postgreSQLProperties(DynamicPropertyRegistry registry) {
    postgreSQLContainer.start();
    registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
  }
}
