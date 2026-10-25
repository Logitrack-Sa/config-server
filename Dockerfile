# ETAPA 1: Construcción (Build) usando Eclipse Temurin (Reemplazo oficial de openjdk)
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copiamos el wrapper de Maven y el archivo de configuración
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Damos permisos de ejecución al wrapper de Maven en Linux
RUN chmod +x ./mvnw

# Descargamos las dependencias de Spring Boot
RUN ./mvnw dependency:go-offline -B

# Copiamos el código fuente
COPY src ./src

# Compilamos el proyecto para generar el archivo .jar
RUN ./mvnw clean package -DskipTests

# ETAPA 2: Producción (Runtime) con una imagen más ligera
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copiamos el .jar generado desde la etapa "builder"
COPY --from=builder /app/target/config-server-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto oficial asignado al Config Server
EXPOSE 8888

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]