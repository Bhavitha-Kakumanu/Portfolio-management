# Robinhood Clone

<<<<<<< HEAD
This is a full-stack application that replicates core functionalities of Robinhood, including user authentication, data management, and a responsive frontend. The project is divided into two main parts: the **frontend** (React-based) and the **backend** (Java-based).

---

## Project Structure

- **`frontend/`**: Contains the React-based client-side code.
  - Built with Vite for fast development and build processes.
  - Includes reusable components, API modules, and global state management using React Context.
- **`user-service/`**: Contains the Java-based backend service.
  - Built with Maven for dependency management.
  - Implements REST APIs for user authentication and data handling.
- **`docker-compose.yml`**: Configures Docker containers for running the application locally.

---

## Dependencies

### Frontend
- **React**: For building the user interface.
- **Vite**: For development and build tooling.
- **Axios**: For making HTTP requests to the backend.
- **ESLint**: For code linting and maintaining code quality.

### Backend
- **Java 17**: The programming language used for the backend.
- **Spring Boot**: For building the REST API and managing application configuration.
- **Spring Security**: For implementing JWT-based authentication.
- **Hibernate**: For ORM (Object-Relational Mapping) with the database.
- **JUnit**: For writing and running unit tests.

---

## How to Run Locally

### Prerequisites
- **Node.js** (v16 or higher) and **npm** for the frontend.
- **Java 17** and **Maven** for the backend.
- **Docker** and **Docker Compose** for containerized deployment.

### Steps

1. **Clone the Repository**
   ```sh
   git clone <repository-url>
   cd <repository-folder>
=======
A learning project that rebuilds a simplified version of Robinhood using a microservices architecture. Only the **user-service** (registration, login, JWT auth, user profiles) and its **React frontend** are implemented so far. The rest of the planned services (trading, portfolio, market data, notifications, API gateway, service discovery) are documented in [ROADMAP.md](ROADMAP.md) but not yet built.

For a deep, line-by-line walkthrough of how the code works, see [CODEBASE_EXPLAINER.md](CODEBASE_EXPLAINER.md) (backend) and [FRONTEND_EXPLAINER.md](FRONTEND_EXPLAINER.md) (frontend).

## What's in this repo

```
robinhood-clone/
├── docker-compose.yml     # Postgres (x3), Redis, Kafka, Zookeeper, Kafka UI
├── user-service/          # Spring Boot backend (Java 21) — auth + user profiles
└── frontend/               # React 19 + Vite single-page app
```

### Backend — `user-service`

A Spring Boot REST API that handles account creation and authentication.

- **Registration & login** — passwords are hashed with BCrypt (never stored in plaintext); a successful register/login returns a **JWT** the client uses for all future requests.
- **Stateless auth** — a `JwtAuthFilter` runs on every request, validates the `Authorization: Bearer <token>` header, and populates Spring Security's context. No server-side sessions.
- **Layered structure**: `controller` (HTTP endpoints) → `service` (business logic, `@Transactional`) → `repository` (Spring Data JPA) → `model` (JPA entity mapped to the `users` table in Postgres).
- **DTOs** (`dto/`) keep the public API shape (e.g. `UserResponse`) separate from the internal `User` entity, so things like `passwordHash` never leak in a response.
- **Centralized error handling** via `GlobalExceptionHandler`, which maps exceptions (duplicate email, user not found, validation failures) to proper HTTP status codes.
- **Endpoints**:
  | Method | Path | Auth required | Purpose |
  |---|---|---|---|
  | POST | `/api/v1/auth/register` | No | Create an account, returns a JWT |
  | POST | `/api/v1/auth/login` | No | Authenticate, returns a JWT |
  | GET | `/api/v1/users/me` | Yes | Get your own profile |
  | GET | `/api/v1/users/{id}` | Yes | Get a user by ID |

  Runs on **port 8081**.

### Frontend — `frontend/`

A React single-page app (Vite dev server on **port 3000**) with pages for landing, login, register, and a dashboard. `AuthContext` holds the logged-in user/JWT (stored in `localStorage`), `ProtectedRoute` guards the dashboard, and `api/client.js` is an Axios instance that automatically attaches the JWT to every request and redirects to `/login` on a 401. In dev, Vite proxies any `/api/*` request to the backend at `http://localhost:8081`, so the frontend never needs to hardcode a backend URL.

### Infrastructure — `docker-compose.yml`

Spins up everything the services need locally, following a **database-per-service** pattern (each microservice gets its own Postgres instance so services stay decoupled):

| Container | Image | Port | Used by |
|---|---|---|---|
| `postgres-users` | postgres:16-alpine | 5432 | user-service (active) |
| `postgres-trading` | postgres:16-alpine | 5433 | trading-service (future) |
| `postgres-portfolio` | postgres:16-alpine | 5434 | portfolio-service (future) |
| `redis` | redis:7-alpine | 6379 | market-data caching (future) |
| `zookeeper` | confluentinc/cp-zookeeper | 2181 | required by Kafka |
| `kafka` | confluentinc/cp-kafka | 9092 | inter-service events (future) |
| `kafka-ui` | provectuslabs/kafka-ui | 8090 | browse Kafka topics in a browser |

Only `postgres-users` is required to run what's currently built; the rest exist for services planned in the roadmap.

## Dependencies

### Backend (`user-service/pom.xml`, managed by Maven)

- **Java 21**
- `spring-boot-starter-web` — embedded Tomcat + REST controllers
- `spring-boot-starter-security` — authentication/authorization
- `spring-boot-starter-data-jpa` — ORM (Hibernate) for Postgres access
- `spring-boot-starter-validation` — request validation (`@NotBlank`, `@Email`, etc.)
- `postgresql` — JDBC driver (runtime)
- `h2` — in-memory database used only for tests
- `io.jsonwebtoken:jjwt-api` / `jjwt-impl` / `jjwt-jackson` (v0.12.5) — JWT creation/verification
- `lombok` — removes getter/setter/constructor boilerplate
- `spring-boot-starter-test` + `spring-security-test` — JUnit 5, Mockito, Spring test support

### Frontend (`frontend/package.json`, managed by npm)

- `react` / `react-dom` (v19) — UI library
- `react-router-dom` (v7) — client-side routing
- `axios` — HTTP client
- Dev tooling: `vite`, `@vitejs/plugin-react`, `eslint` and plugins

### Infrastructure

- **Docker** and **Docker Compose** — to run Postgres, Redis, and Kafka locally

## Prerequisites

Install these before running the project:

1. **Java 21 (JDK)** — check with `java -version`. Install via [Homebrew](https://brew.sh): `brew install openjdk@21`
2. **Maven** — check with `mvn -version`. Install via: `brew install maven`
3. **Node.js 20+ and npm** — check with `node -v` and `npm -v`. Install via: `brew install node`
4. **Docker Desktop** (includes Docker Compose) — check with `docker -v`. Download from [docker.com](https://www.docker.com/products/docker-desktop/) if not installed.

## Step-by-step: running the project locally

### 1. Clone/open the project

```bash
cd /Users/bhavithakakumanu/javaProjects/robinhood-clone
```

### 2. Start the infrastructure (Postgres, Redis, Kafka) with Docker

```bash
docker-compose up -d
```

This starts all containers listed in `docker-compose.yml` in the background. Verify they're running:

```bash
docker ps
```

You should see `postgres-users`, `postgres-trading`, `postgres-portfolio`, `redis`, `zookeeper`, `kafka`, and `kafka-ui` listed. Only `postgres-users` is strictly required for the backend to start.

### 3. Install backend dependencies

```bash
cd user-service
mvn install -DskipTests
```

This downloads all Maven dependencies declared in `pom.xml` (Spring Boot, JWT libraries, Lombok, etc.) into your local `~/.m2` repository and compiles the project. `-DskipTests` skips running the test suite during this step (you can run tests separately in step 5).

### 4. Run the backend (user-service)

From inside the `user-service` directory:

```bash
mvn spring-boot:run
```

Wait for the log output to show the application has started (look for a line like `Started UserServiceApplication` and `Tomcat started on port 8081`). Leave this terminal running — the backend must stay up for the frontend to work.

The API is now available at `http://localhost:8081`.

### 5. (Optional) Run the backend tests

In a separate terminal, from inside `user-service`:

```bash
mvn test
```

Tests use an in-memory H2 database, so they don't require Docker/Postgres to be running.

### 6. Install frontend dependencies

Open a **new terminal window/tab** (keep the backend running in the first one), then:

```bash
cd /Users/bhavithakakumanu/javaProjects/robinhood-clone/frontend
npm install
```

This installs everything listed in `package.json` (React, React Router, Axios, Vite, ESLint, etc.) into `frontend/node_modules`.

### 7. Run the frontend dev server

Still inside the `frontend` directory:

```bash
npm run dev
```

Vite will start the dev server and print a local URL, typically `http://localhost:3000`.

### 8. Open the app in your browser

Navigate to:

```
http://localhost:3000
```

You should see the landing page. From there you can register a new account, log in, and view the dashboard. Requests to `/api/*` are automatically proxied by Vite to the backend running on port 8081.

### 9. Stopping everything

When you're done:

- Stop the frontend: `Ctrl+C` in the terminal running `npm run dev`
- Stop the backend: `Ctrl+C` in the terminal running `mvn spring-boot:run`
- Stop the infrastructure containers:
  ```bash
  cd /Users/bhavithakakumanu/javaProjects/robinhood-clone
  docker-compose down
  ```
  This stops and removes the containers but **keeps** the database volumes (your data persists). To also wipe all data, run `docker-compose down -v` instead.

## Troubleshooting

- **Backend fails to connect to Postgres**: make sure `docker-compose up -d` finished successfully and `postgres-users` is healthy (`docker ps`). The backend expects Postgres at `localhost:5432` with database `userdb`, user `postgres`, password `postgres` (see `user-service/src/main/resources/application.yml`).
- **Port already in use**: port 8081 (backend), 3000 (frontend), or 5432/5433/5434/6379/2181/9092/8090 (Docker services) may already be occupied by another process. Stop the conflicting process or change the port in `application.yml` / `vite.config.js` / `docker-compose.yml`.
- **`mvn` or `npm` command not found**: revisit the [Prerequisites](#prerequisites) section and confirm installation with the version-check commands.
>>>>>>> ff0607b (Adding a readme file. It has all the instructions)
