# EP2-IngDevOps - Microservicio Catálogo TCG

## Descripción del proyecto

Microservicio de catálogo de productos TCG (Trading Card Game) construido con Java 21 y Spring Boot, gestionado con Maven. El repositorio incluye la creación de contenedores de Docker, utilizando archivos como `Dockerfile` y `docker-compose.yml` para la orquestación de estos mismos. Incluye también un pipeline de CI/CD implementado con GitHub Actions que compila la aplicación, realiza un análisis de seguridad de dependencias (SCA) utilizando Snyk, ejecución de pruebas unitarias y cobertura con JaCoCo y el análisis de calidad y seguridad de código (SAST) con SonarCloud.

--- 


## Explicación Dockerfile

El archivo `tcg/Dockerfile` implementa un build **multi-stage** de tres etapas, con el objetivo de generar una imagen final ligera y reutilizar la caché de dependencias.

### Etapa 1: `dependencies`

```dockerfile
FROM eclipse-temurin:21-jdk AS dependencies
WORKDIR /app
COPY tcg/pom.xml .
COPY tcg/mvnw .
COPY tcg/.mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
```

Descarga todas las dependencias declaradas en el `pom.xml` usando `dependency:go-offline`. Al copiar primero solo el `pom.xml` (y no el código), Docker puede reutilizar esta capa mientras las dependencias no cambien, acelerando los builds posteriores.

### Etapa 2: `compile`

```dockerfile
FROM eclipse-temurin:21-jdk AS compile
WORKDIR /app
COPY tcg/ .
COPY --from=dependencies /root/.m2 /root/.m2
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests
```

Copia el código fuente y el repositorio local de Maven (`/root/.m2`) desde la etapa anterior, y compila/empaqueta la aplicación con `clean package`. Se omiten los tests (`-DskipTests`) porque estos se ejecutan en el pipeline, no durante la construcción de la imagen.

### Etapa 3: `prod`

```dockerfile
FROM eclipse-temurin:21-jre-alpine AS prod
WORKDIR /app
COPY --from=compile /app/target/*.jar /app/app.jar
EXPOSE 9005
CMD ["java", "-jar", "/app/app.jar"]
```

La imagen final solo incluye el JRE sobre Alpine y el JAR generado. No contiene código fuente, Maven ni el JDK, lo que reduce el tamaño de la imagen y la superficie de ataque. El puerto `9005` corresponde al configurado en `application.yml`.

> El contexto de build es la raíz del repositorio, por eso las rutas internas comienzan con `tcg/`.

---

## Explicación estrategia de orquestación de contenedores con Docker Compose

El archivo `docker-compose.yml` define dos servicios que se comunican a través de una red bridge y persisten los datos en un volumen.

### Servicio `mysql`

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: mysql-tcg
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: tcg_db
      MYSQL_USER: tcg_user
      MYSQL_PASSWORD: tcg_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - tcg-network
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h localhost -u root -prootpassword || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
```

- Usa la imagen oficial `mysql:8.0`.
- Crea automáticamente la base `tcg_db` y el usuario `tcg_user`.
- Expone el puerto `3306` al host.
- Persiste los datos en el volumen `mysql_data`.
- Define un `healthcheck` con `mysqladmin ping`, que permite saber cuándo la base está realmente lista para aceptar conexiones.

### Servicio `tcg-service`

```yaml
  tcg-service:
    build:
      context: .
      dockerfile: ./tcg/Dockerfile
    container_name: tcg-app
    restart: unless-stopped
    ports:
      - "9005:9005"
    environment:
      SPRING_APPLICATION_NAME: ms-productos-tcg
      DB_URL: jdbc:mysql://mysql:3306/tcg_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      DB_USERNAME: tcg_user
      DB_PASSWORD: tcg_password
      SERVER_PORT: 9005
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - tcg-network
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: "1.0"
        reservations:
          memory: 512M
          cpus: "0.5"
```

- Construye la imagen usando `tcg/Dockerfile` con la raíz del repositorio como contexto.
- Expone la aplicación en el puerto `9005`.
- Inyecta la configuración de conexión a MySQL mediante variables de entorno (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`), que `application.yml` consume con la sintaxis `${VARIABLE:valor_por_defecto}`.
- `depends_on` con `condition: service_healthy` garantiza que la aplicación no arranque hasta que MySQL esté saludable.
- Define límites y reservas de recursos (memoria y CPU).

### Red y volumen

```yaml
networks:
  tcg-network:
    driver: bridge

volumes:
  mysql_data:
```

La red `tcg-network` permite que `tcg-service` resuelva el host `mysql` por nombre. El volumen `mysql_data` conserva la información aunque los contenedores se eliminen.

### Comandos útiles

```bash
docker compose up -d --build   # Construir y levantar los servicios
docker compose ps              # Ver estado de los contenedores
docker compose logs tcg-service # Ver logs de la aplicación
docker compose down            # Detener y eliminar los contenedores
```

> `deploy.resources` está pensado para Docker Swarm. En `docker compose` local puede no aplicar los límites, pero se mantiene como declaración de recursos esperados.

---

## Explicación Pipeline CI/CD

El pipeline está definido en `.github/workflows/ci-seguridad.yml` y se ejecuta en GitHub Actions.

### Disparadores

- `push` a las ramas `main` y `feature/ep2-devsecops-docker`.
- `pull_request` hacia `main`.
- `workflow_dispatch` para ejecución manual.

Además, declara permisos mínimos (`contents: read`), siguiendo el principio de menor privilegio.

### Jobs

Los jobs se ejecutan en cadena mediante `needs`, de modo que cada uno solo comienza si el anterior fue exitoso:

1. **`snyk-scan`** - Análisis de seguridad de dependencias (SCA) con Snyk.
2. **`test`** - Compilación, pruebas unitarias y cobertura con JaCoCo.
3. **`sonar-analysis`** - Análisis de calidad y seguridad de código (SAST) con SonarCloud.
4. **`deploy-simulated`** - Despliegue simulado con Docker Compose.

### Job `snyk-scan`

- Configura JDK 21, Node 20 e instala Snyk CLI con `npm install -g snyk`.
- Ejecuta `snyk test` sobre `pom.xml` con `--severity-threshold=high`.
- Genera `snyk-report.json` y lo publica como artifact (`snyk-report`) con `if: always()`, para que quede disponible incluso si hay hallazgos.

### Job `test`

- Ejecuta `./mvnw -B clean verify`.
- JaCoCo genera el reporte de cobertura en `tcg/target/site/jacoco/`.
- El reporte se publica como artifact (`jacoco-report`) con `if: always()`.

### Job `sonar-analysis`

- Hace checkout con `fetch-depth: 0` para que SonarCloud pueda analizar el historial y la autoría de las líneas.
- Ejecuta el `sonar-maven-plugin` contra SonarCloud con `sonar.qualitygate.wait=true`.
- Usa el `SONAR_TOKEN` almacenado como secret.

### Job `deploy-simulated`

- Levanta los servicios con `docker compose up -d --build`.
- Espera 25 segundos para la inicialización.
- Verifica el estado con `docker compose ps` y muestra los logs de `tcg-service`.
- Siempre limpia con `docker compose down` (`if: always()`).

---

## Conexión y configuración de Snyk (SCA)

Snyk se utiliza para el análisis de seguridad de dependencias (Software Composition Analysis). El pipeline se conecta a Snyk mediante un token de autenticación:

1. Crear una cuenta en [Snyk](https://snyk.io) e iniciar sesión.
2. Obtener el token desde la configuración de la cuenta (`Account Settings → General → Auth Token`).
3. En GitHub, ir a `Settings → Secrets and variables → Actions` y crear el secret `SNYK_TOKEN` con ese valor.
4. El workflow lo inyecta como variable de entorno:

```yaml
env:
  SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
```

### ¿Por qué el pipeline sigue pasando aunque Snyk encuentre errores?

El paso de escaneo está configurado así:

```bash
snyk test --severity-threshold=high \
  --file=pom.xml \
  --json-file-output=snyk-report.json -d || echo "Vulnerabilidades registradas en el reporte JSON"
```

El operador `|| echo "..."` hace que, **sin importar el código de salida de Snyk**, el comando del paso termine exitosamente. Cuando Snyk encuentra vulnerabilidades, devuelve un código de salida distinto de cero; sin embargo, ese código es capturado por el `||`, se imprime el mensaje y el job continúa. Lo mismo ocurre ante cualquier otro error de Snyk (por ejemplo, un problema de autenticación).

Esto es **intencional**: Snyk actúa como un control informativo dentro del pipeline. Los hallazgos no bloquean la construcción, pero quedan registrados en `snyk-report.json`, que se publica como artifact para su revisión posterior.

Además:

- `--severity-threshold=high` hace que Snyk reporte solo vulnerabilidades de severidad alta y crítica.
- `--file=pom.xml` indica el archivo de dependencias a analizar.
- `--json-file-output=snyk-report.json` guarda el resultado estructurado.
- `-d` habilita la salida de depuración.

En resumen, Snyk **siempre permite que el pipeline avance**, y la evidencia del análisis queda disponible como artifact.

---

## Pruebas unitarias y cobertura con JaCoCo

Las pruebas están escritas con JUnit 5 y Mockito, y cubren las capas principales:

- `ProductoTcgControllerTest`: verifica la delegación de los endpoints al servicio.
- `ProductoTcgServiceTest`: valida la lógica de negocio, incluyendo casos de éxito, SKU duplicado y recursos no encontrados.
- `DataTest`: comprueba la carga inicial de datos según si la base está vacía o no.

JaCoCo está configurado en el `pom.xml` con dos ejecuciones:

```xml
<execution>
  <id>prepare-agent</id>
  <goals>
    <goal>prepare-agent</goal>
  </goals>
</execution>
<execution>
  <id>report</id>
  <phase>verify</phase>
  <goals>
    <goal>report</goal>
  </goals>
</execution>
```

- `prepare-agent` instrumenta la JVM durante las pruebas para registrar la cobertura.
- `report` genera el informe HTML/XML en `target/site/jacoco/` durante la fase `verify`.

El pipeline sube este reporte como artifact `jacoco-report`. Es importante notar que el `pom.xml` **no impone un porcentaje mínimo de cobertura**: el umbral real de calidad se evalúa en SonarCloud.

---

## Conexión y configuración de SonarCloud (SAST)

SonarCloud se utiliza para el análisis de calidad y seguridad de código (Static Application Security Testing). La conexión se realiza con un token y la configuración del proyecto:

1. Crear una cuenta en [SonarCloud](https://sonarcloud.io) e iniciar sesión con GitHub.
2. Importar el repositorio y crear/identificar la organización y el proyecto.
3. Generar un token en `My Account → Security`.
4. En GitHub, crear el secret `SONAR_TOKEN` con ese valor.
5. El workflow lo inyecta como variable de entorno y como parámetro:

```yaml
env:
  SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
run: |
  ./mvnw -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
    -Dsonar.projectKey=fernandocamus_EP2-IngDevops \
    -Dsonar.organization=fernandocamus \
    -Dsonar.host.url=https://sonarcloud.io \
    -Dsonar.token="${{ secrets.SONAR_TOKEN }}" \
    -Dsonar.qualitygate.wait=true
```

- `sonar.projectKey` identifica el proyecto en SonarCloud.
- `sonar.organization` indica la organización propietaria.
- `sonar.host.url` apunta a SonarCloud.
- `sonar.qualitygate.wait=true` hace que el pipeline **espere el resultado del Quality Gate y falle si no se cumple**.

### El umbral del 80%

A diferencia de Snyk, SonarCloud **sí bloquea el pipeline** gracias a `sonar.qualitygate.wait=true`. El Quality Gate por defecto de SonarCloud ("Sonar way") exige, entre otras condiciones:

- **Cobertura de al menos 80% sobre el código nuevo**.
- 0 bugs nuevos.
- 0 vulnerabilidades nuevas.
- 0 security hotspots sin revisar.
- Duplicación de código por debajo del umbral permitido.

Si el análisis no alcanza el **80% de cobertura sobre el código nuevo** (o incumple el resto de condiciones), el Quality Gate falla, el job `sonar-analysis` termina con error y, por la cadena de `needs`, **el despliegue simulado no se ejecuta**. De esta forma, SonarCloud actúa como una compuerta de calidad obligatoria.

---

## Explicación de cómo se garantiza la trazabilidad y calidad con el pipeline

El pipeline asegura trazabilidad y calidad mediante varias estrategias:

### Ejecución ligada a cada cambio

Cada `push` o `pull_request` dispara una ejecución asociada a un commit y una rama concretos, por lo que todo resultado es rastreable hasta el cambio que lo originó. La opción `workflow_dispatch` permite además ejecuciones manuales controladas.

### Evidencia persistente

Los reportes de Snyk y JaCoCo se publican como artifacts de GitHub Actions. Esto deja evidencia descargable de cada ejecución, incluso cuando el análisis reporta hallazgos.

### Compuertas secuenciales

Los jobs se encadenan con `needs`, de modo que el pipeline avanza solo si la etapa previa fue exitosa:

1. Snyk entrega información de seguridad sin bloquear.
2. Las pruebas unitarias deben pasar para continuar.
3. SonarCloud debe cumplir el Quality Gate (incluido el 80% de cobertura sobre código nuevo).
4. Solo entonces se ejecuta el despliegue simulado.

### Reproducibilidad

- Maven Wrapper (`./mvnw`) fija la versión de Maven.
- El `Dockerfile` multi-stage produce imágenes reproducibles y ligeras.
- Docker Compose define explícitamente versiones, variables, red y volumen.

### Arranque ordenado y verificación

El `healthcheck` de MySQL junto con `depends_on: condition: service_healthy` garantizan que la aplicación arranque solo cuando la base está lista. En el pipeline, el despliegue simulado verifica el estado con `docker compose ps` y revisa los logs, y siempre limpia los contenedores con `docker compose down`.

### Seguridad de credenciales

Los tokens de Snyk y SonarCloud se almacenan como secrets de GitHub y nunca se escriben en el repositorio. El workflow declara permisos mínimos (`contents: read`).

---

## Cómo ejecutar el proyecto

### Localmente

```bash
cd tcg
./mvnw spring-boot:run
```

La aplicación queda disponible en `http://localhost:9005`. La documentación Swagger está disponible en `http://localhost:9005/swagger-ui.html`.

### Con Docker Compose

```bash
docker compose up -d --build
```

- API: `http://localhost:9005/api/v1/productos`
- Swagger UI: `http://localhost:9005/swagger-ui.html`
- MySQL: `localhost:3306`

Para detener y limpiar:

```bash
docker compose down
```

### Secrets necesarios en GitHub

| Secret | Uso |
| --- | --- |
| `SNYK_TOKEN` | Autenticación con Snyk |
| `SONAR_TOKEN` | Autenticación con SonarCloud |

## Uso de IA

### Herramientas de IA que se usaron y cómo se aplicaron 

Se utilizó Gemini para resolver problemas con las pruebas unitarias que nos surgieron y mejorar la redacción de la documentación del README del proyecto.

### Reflexiones Personales

**Fernando Camus:** Gracias a este proyecto pude entender la importancia y lo complicado que es implementar pruebas unitarias y la seguridad automatizada. Me di cuenta que no es solo correr herramientas, si no que hay una complejidad total detrás de todo, diseñar pruebas, pasar las Quality Gate de SonarCloud y demás. Todo eso son barreras que nos dificultan al principio pero que a la larga nos ayudan a garantizar que no hay vulnerabilidades.
**Deymon González:** Como reflexión, este proyecto me ayudó a entender cómo funciona un flujo de CI/CD de forma completa, mientras también cubrimos su seguridad a la par con las tecnologías de Snyk, SonarCloud y las pruebas unitarias. Si bien ya había trabajado con Docker, esta experiencia me ayudó a seguir entendiéndolo más allá y practicar más.