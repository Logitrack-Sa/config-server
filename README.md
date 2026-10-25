
## Punto de partida del servidor

El backend de configuración será https://github.com/Logitrack-Sa/config-repo. El servidor expondrá propiedades para shipment-service, courier-service y security-service con perfiles dev/prod. No necesita base MySQL propia.

La implementación pendiente debe seleccionar una versión de Spring Cloud compatible con el Boot del equipo, habilitar EnableConfigServer y verificar respuestas mediante un Git de prueba. La imagen Java25 debe ejecutarse independientemente; credenciales Git por entorno si fueran necesarias.
# config-server

Repositorio del Sprint 2 de LogiTrack. **Servidor de configuración centralizada implementado y dockerizado.**

[Tablero del Sprint 2](https://github.com/orgs/Logitrack-Sa/projects/2) · [Contratos y puertos](https://github.com/Logitrack-Sa/shipment-service/blob/main/docs/sprint2/CONTRATOS.md) · [Workflow](https://github.com/Logitrack-Sa/shipment-service/blob/main/docs/sprint2/WORKFLOW.md)

Las issues del repositorio definen responsable, puntos, criterios y pruebas independientes. Usar fixtures y dobles para avanzar sin esperar servicios de otros integrantes. Se conserva el Sprint 1 en shipment-service.

Base actual: README, plantilla de PR, formulario de issues y revisor IA en GitHub Actions. La IA devuelve hallazgos sobre PR y sugerencias de pruebas; no planifica, no aprueba ni integra. CI de esta base verifica automatización.

No incorporar credenciales, claves JWT privadas ni archivos .env. Java 25 para aplicaciones; `config-repo` es únicamente Git de propiedades no sensibles dev/prod.

## Implementación del Servidor (Issue [S2-24])

El servidor de configuración ha sido implementado utilizando Spring Boot (3.5.16) y la anotación `@EnableConfigServer`. Cumple con los siguientes criterios operativos:

* **Backend de configuración:** Lee y expone dinámicamente las propiedades desde el repositorio central [config-repo](https://github.com/Logitrack-Sa/config-repo) para los clientes `shipment-service`, `courier-service` y `security-service` (perfiles `dev` y `prod`).
* **Puerto Oficial:** El servicio se expone en el puerto **8888** según el contrato del Sprint 2.
* **Resolución interna:** Se configuró `spring.main.allow-bean-definition-overriding=true` para garantizar el arranque sin conflictos de autoconfiguración en el cliente REST.
* **Dockerización Multietapa:** Contiene un `Dockerfile` optimizado de construcción multietapa (multi-stage) empleando imágenes oficiales de **Eclipse Temurin (Java 25)**. Esto permite compilar el código fuente sin dependencias locales y generar una imagen final JRE extremadamente ligera para su ejecución independiente.