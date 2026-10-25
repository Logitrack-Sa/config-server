package com.logitrack.config_server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 1. Configuramos el entorno de prueba aislando la aplicación
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.config.server.git.uri=https://github.com/Logitrack-Sa/repo-falso-inexistente",
                "spring.cloud.config.server.git.clone-on-start=false"
        }
)
public class ConfigServerGitFailureTest {

    // 2. Cliente HTTP inyectado automáticamente para hacer peticiones al servidor de prueba
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void whenGitIsUnavailable_thenReturnsNotFound() {
        // 3. Simulamos a un microservicio (ej. shipment-service) pidiendo su configuración
        ResponseEntity<String> response = restTemplate.getForEntity("/shipment-service/dev", String.class);
        // Imprimimos en la consola para verlo con tus propios ojos
        System.out.println("CÓDIGO RECIBIDO: " + response.getStatusCode());
        System.out.println("CUERPO DE LA RESPUESTA: " + response.getBody());
        // 4. Verificamos que el servidor maneje el error y devuelva un 404 en lugar de crashear
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(),
                "El servidor debe devolver 404 cuando el repositorio Git no existe o está caído");
    }
}