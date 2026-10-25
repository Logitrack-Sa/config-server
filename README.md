# config-server

Servidor Spring Cloud Config sobre el trabajo inicial de Damián (PR #5, issue #1). Java 25, Spring Boot 3.5.16 y Spring Cloud 2025.0.3. No usa MySQL ni espera que arranquen las aplicaciones cliente.

## Ejecutar

`./mvnw spring-boot:run` o abrir pom.xml con JDK 25. CONFIG_GIT_URI selecciona el repositorio (por defecto https://github.com/Logitrack-Sa/config-repo.git), CONFIG_GIT_LABEL la rama (main) y SERVER_PORT el puerto (8888). Git se clona al iniciar; un backend inaccesible impide el arranque. Si el repo es privado, CONFIG_GIT_USERNAME y CONFIG_GIT_PASSWORD solo por entorno seguro; no guardarlos en config-repo ni en argumentos.

GET /shipment-service/dev/main, /courier-service/prod/main, etc. entregan Environment con versión Git. Los perfiles dev/prod de los tres clientes provienen de config-repo. Una aplicación sin propiedades devuelve 404.

Solo /actuator/health está expuesto, sin detalles/componentes. Las propiedades contienen placeholders para secretos de los clientes; no cargar valores secretos en Git. El servicio local escucha 127.0.0.1. Docker escucha la red del contenedor: mantenerlo en red privada y publicar solo en loopback para pruebas. No ofrece autenticación de red en esta issue.

## Pruebas y Docker

`./mvnw clean verify`: Git temporal local con rama main, seis combinaciones de servicio/perfil, placeholders conservados, health mínimo y configuración inexistente. Sin GitHub ni bases de otros repositorios.

`docker build -t config-server:local .`; ejecutar con CONFIG_GIT_URI y CONFIG_GIT_LABEL. Para Git de prueba montado: file:/fixture/ con montaje de solo lectura. La imagen separa build/runtime, excluye tests/secretos y usa UID 10001. CI construye la imagen y verifica HTTP con un Git local, sin servicios consumidores. La publicación de imágenes pertenece a otra issue.

El PR #6 de Damián aborda la CI del servicio; esta corrección conserva su rama y limita cambios a los criterios iniciales de #1. [Contrato](https://github.com/Logitrack-Sa/shipment-service/blob/main/docs/sprint2/CONTRATOS.md) · [Tablero](https://github.com/orgs/Logitrack-Sa/projects/2).
