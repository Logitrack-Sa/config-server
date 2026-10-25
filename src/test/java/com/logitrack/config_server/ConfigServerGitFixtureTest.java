package com.logitrack.config_server;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// 1. Configuramos el test usando una propiedad de sistema que setearemos antes de arrancar
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.config.server.git.uri=${fixture.git.uri}",
                "spring.cloud.config.server.git.clone-on-start=true"
        }
)
public class ConfigServerGitFixtureTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // 2. Antes de que arranque Spring Boot, creamos un repositorio Git real en una carpeta temporal
    @BeforeAll
    public static void setupFakeGitRepo(@TempDir Path tempDir) throws Exception {
        File gitDir = tempDir.toFile();

        // Inicializamos el repo Git
        try (Git git = Git.init().setDirectory(gitDir).call()) {

            // Creamos un archivo de propiedades de prueba
            File propertiesFile = new File(gitDir, "shipment-service-dev.properties");
            Files.writeString(propertiesFile.toPath(), "logitrack.fee.base=1000.00\n");

            // Hacemos commit del archivo
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit con fixture").call();
        }

        // Le pasamos la ruta de esta carpeta temporal a Spring Boot
        System.setProperty("fixture.git.uri", gitDir.toURI().toString());
    }

    @Test
    public void whenValidProfileRequested_thenReturnsProperties() {
        // 3. Hacemos la petición pidiendo las propiedades de desarrollo del servicio de envíos
        ResponseEntity<String> response = restTemplate.getForEntity("/shipment-service/dev", String.class);
        // Imprimimos en la consola para verlo con tus propios ojos
        System.out.println("CÓDIGO RECIBIDO: " + response.getStatusCode());
        System.out.println("CUERPO DE LA RESPUESTA: " + response.getBody());
        // 4. Verificamos que responda 200 OK y que contenga nuestra propiedad falsa
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Debe devolver 200 OK");
        assertTrue(response.getBody().contains("logitrack.fee.base"), "El JSON debe contener la propiedad inyectada");
        assertTrue(response.getBody().contains("1000.00"), "El JSON debe contener el valor inyectado");
    }
}


