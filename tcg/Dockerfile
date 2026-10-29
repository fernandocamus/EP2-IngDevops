# 1: Descarga de dependencias
FROM eclipse-temurin:21-jdk AS dependencies
WORKDIR /app
COPY tcg/pom.xml .
COPY tcg/mvnw .
COPY tcg/.mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# 2: Compilación y empaquetado del artefacto
FROM eclipse-temurin:21-jdk AS compile
WORKDIR /app
COPY tcg/ .
COPY --from=dependencies /root/.m2 /root/.m2
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# 3: Imagen final ligera
FROM eclipse-temurin:21-jre-alpine AS prod
WORKDIR /app
# Copiar únicamente el archivo JAR generado
COPY --from=compile /app/target/*.jar /app/app.jar
# Puerto configurado en application.yml (9005)
EXPOSE 9005
CMD ["java", "-jar", "/app/app.jar"]