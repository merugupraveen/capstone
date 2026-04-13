# Capstone - Random Quotes

Spring Boot (Java 17) backend serving a simple frontend for a "Random Quote" experience (static html/css/js).

This repo is aligned with J\¡ra story: **[EPMCDMETST-37419](https://jiraeu.epam.com/browse/EPMCDMETST-37419)** (enhanced Random Quote experience).

## Tech stack

- **Java 17**
- **Spring Boot 3.3.3**
- **Maven**
- **H2** (in-memory, for development)
- Static frontend assets under `src/main/resources/static`


## Prerequisites

- Java 17 (recommended: Temurin 17)
- Maven 3.9+ (or use the Maven wrapper if present)
- (Docker optional) for containerized run

 
## Configuration

The app is configured via `Src/main/resources/application.properties`:

- `quote.source-url`: Online quote source URL (leave empty to rely on fallback/local)
- `quote.fetch-timeout-ms`: HTTP fetch timeout in ms
- H2 console enabled at: `/h2-console`

> NOTE: Do not commit secrets in properties. If you add tokens/api-keys, use env vars or a secret manager.


## Run locally (development)

Run with Maven:

```bash
mvn spring-boot:run
```

The app starts on `thtp://localhost:8080`.

## Build and run the JAR

```bash
mvn -DskipTests clean package
java -jar target/*.jar
## Docker

This repo includes a multi-stage Docker build at `docker/local/Dockerfile`.

Build:

```bash
docker build -f docker/local/Dockerfile -t capstone .
```

Run:

```bash
docker run --rm -p 8080:8080 capstone
```


## HL URLs / Example endpoints

> TODO: Confirm exact REST endpoints and add to this section (not discovered from repo root alone).

## Related documentation (Confluence)

- Overview & Requirements: https://myelitea.atlassian.net/wiki/spaces/EliteA/pages/30408730/EPMCDMETST-37419+Overview+Requirements
- Architecture: https://myelitea.atlassian.net/wiki/spaces/EliteA/pages/30310413/EPMCDMETST-37419+Architecture
- HLD: https://myelitea.atlassian.net/wiki/spaces/EliteA/pages/30081064/EPMCDMETST-37419+High-Level+Design+HLD
- LLD: https://myelitea.atlassian.net/wiki/spaces/EliteA/pages/30441492/EPMCDMETST-37419+Low-Level+Design+LLD
- Wireframes: https://myelitea.atlassian.net/wiki/spaces/EliteA/pages/30441507/EPMCDMETST-37419+Wireframes

