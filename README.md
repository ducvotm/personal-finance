# Budget MVP

Minimal full-stack budgeting app: **Spring Boot 3** (REST, JPA/Hibernate, Spring Security + JWT), **MySQL**, and **React** (Vite, Chakra UI). Users register, log expenses against categories, and set per-category budgets with spend progress.

## Prerequisites

- **JDK 17** and **Maven 3.9+**
- **Node.js 20+** and npm (for the frontend)
- **MySQL 8** (local install or Docker)

## Environment variables

Defaults match local development so you can run without setting anything. **Override in production** (and never commit real secrets).

| Variable | Purpose | Default (local) |
|----------|---------|-----------------|
| `SPRING_DATASOURCE_URL` | JDBC URL | MySQL `personal_finance` on `localhost:3306` |
| `SPRING_DATASOURCE_USERNAME` | DB user | `root` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `Admin@123` |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME` | JDBC driver class | `com.mysql.cj.jdbc.Driver` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Schema mode | `update` |
| `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT` | Hibernate dialect | `org.hibernate.dialect.MySQLDialect` |
| `JWT_SECRET` | HS256 signing key (long random string) | Dev default in `application.yml` |
| `JWT_ACCESS_EXPIRATION` | Access token TTL (ms) | `14400000` |
| `JWT_REFRESH_EXPIRATION` | Documented TTL (refresh rows use 7 days in code) | `604800000` |
| `JWT_ROLE_DEFAULT` | Default role string | `USER` |
| `SERVER_PORT` | HTTP port | `8080` |

## Run with Docker Compose

From the repo root:

```bash
docker compose up --build
```

- API: `http://localhost:8080`
- Frontend (nginx): `http://localhost:3000`

Compose passes `SPRING_DATASOURCE_*` and `JWT_*` into the app container; adjust values in [`docker-compose.yml`](docker-compose.yml) or use an override file for secrets.

## Run the backend locally

1. Create database `personal_finance` (or rely on `createDatabaseIfNotExist` in the default URL).
2. Export overrides if needed, then:

```bash
mvn spring-boot:run
```

3. **API docs (Springdoc)**  
   - OpenAPI JSON: `http://localhost:8080/v3/api-docs`  
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Run the frontend locally

```bash
cd frontend
npm ci
npm run dev
```

Vite dev server defaults to `http://localhost:5173`. The app calls `/api` (configure your dev proxy or nginx as needed).

Production build:

```bash
cd frontend
npm ci
npm run test:run
npm run build
```

## Tests and formatting

```bash
mvn spotless:check
mvn test
```

## Oracle profile (optional)

To align with environments that use **Oracle** instead of MySQL:

1. Build/run with the Maven profile so **ojdbc** is on the classpath:

   ```bash
   mvn -Poracle spring-boot:run -Dspring-boot.run.profiles=oracle
   ```

2. Set JDBC settings (for example Oracle XE / Free):

   - `SPRING_DATASOURCE_URL` — e.g. `jdbc:oracle:thin:@//localhost:1521/FREEPDB1`
   - `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`

3. Profile-specific defaults live in [`src/main/resources/application-oracle.yml`](src/main/resources/application-oracle.yml) (dialect and driver). Validate once against a real Oracle instance; day-to-day dev can stay on MySQL.

## Struts vs Spring (interview note)

**Apache Struts** (1.x/2.x) historically mapped URLs to *Actions*, used XML-heavy configuration, and managed form beans and interceptors in a servlet-centric model. **Spring MVC / Spring Boot** uses `@Controller` / `@RestController`, declarative validation, a unified `DispatcherServlet`, and first-class **dependency injection** and **test slices**. This repo is **Spring Boot only**; Struts knowledge is best shown through maintenance experience or a separate small example, not by mixing frameworks here.

## License

See repository settings or add a `LICENSE` file if you publish publicly.
