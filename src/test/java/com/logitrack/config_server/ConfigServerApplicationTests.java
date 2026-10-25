package com.logitrack.config_server;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigServerApplicationTests {
    @TempDir static Path fixture;
    @Autowired TestRestTemplate http;

    @DynamicPropertySource
    static void gitFixture(DynamicPropertyRegistry properties) throws Exception {
        Path repo = Files.createDirectory(fixture.resolve("repository"));
        try (Git git = Git.init().setInitialBranch("main").setDirectory(repo.toFile()).call()) {
            for (String service : new String[]{"shipment-service", "courier-service", "security-service"}) {
                Files.writeString(repo.resolve(service+".properties"), "fixture.service="+service+"\n");
                for (String profile : new String[]{"dev", "prod"}) {
                    Files.writeString(repo.resolve(service+"-"+profile+".properties"),
                            "fixture.profile="+profile+"\nspring.datasource.password=${DB_PASSWORD}\n");
                }
            }
            git.add().addFilepattern(".").call();
            git.commit().setAuthor("Fixture", "fixture@example.test").setMessage("Configuración de prueba").call();
        }
        properties.add("spring.cloud.config.server.git.uri", () -> repo.toUri().toString());
        properties.add("spring.cloud.config.server.git.basedir", () -> fixture.resolve("clone").toString());
    }

    @ParameterizedTest
    @CsvSource({"shipment-service,dev", "shipment-service,prod", "courier-service,dev", "courier-service,prod", "security-service,dev", "security-service,prod"})
    void servesAllServiceProfilesFromGit(String application, String profile) {
        var response = http.getForEntity("/"+application+"/"+profile+"/main", JsonNode.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        var body = response.getBody();
        assertThat(body.path("name").asText()).isEqualTo(application);
        assertThat(body.path("profiles").get(0).asText()).isEqualTo(profile);
        assertThat(body.path("version").asText()).hasSize(40);
        var sources = body.path("propertySources");
        assertThat(sources.toString()).contains(application, profile, "${DB_PASSWORD}");
    }
    @Test void healthDoesNotExposeGitOrCredentials() {
        var response = http.getForEntity("/actuator/health", JsonNode.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().toString()).isEqualTo("{\"status\":\"UP\"}");
        assertThat(http.getForEntity("/actuator/env", String.class).getStatusCode().value()).isEqualTo(404);
    }
    @Test void unknownApplicationDoesNotPretendToHaveConfiguration() {
        assertThat(http.getForEntity("/unknown/dev/main", String.class).getStatusCode().value()).isEqualTo(404);
    }
}
