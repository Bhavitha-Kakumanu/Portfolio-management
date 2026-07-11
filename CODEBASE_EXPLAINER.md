# Robinhood Clone — Complete Codebase Explainer

Everything built so far, explained in enough detail to teach it back.

---

## Table of Contents

1. [What Was Built](#what-was-built)
2. [Project Structure](#project-structure)
3. [Infrastructure — docker-compose.yml](#infrastructure)
4. [User Service — The Full Walk-Through](#user-service)
   - [Entry Point](#entry-point)
   - [pom.xml — Dependencies](#pomxml--dependencies)
   - [application.yml — Configuration](#applicationyml--configuration)
   - [The Data Layer: User Entity + Repository](#the-data-layer)
   - [DTOs — Request and Response Objects](#dtos)
   - [Service Layer — Business Logic](#service-layer)
   - [Controllers — HTTP Endpoints](#controllers)
   - [Security — JWT + Spring Security](#security)
   - [Exception Handling](#exception-handling)
   - [Tests](#tests)
5. [How a Request Flows End-to-End](#request-flow)
6. [Key Design Decisions](#key-design-decisions)
7. [What's Planned But Not Built Yet](#whats-planned-but-not-built-yet)

---

## What Was Built

Only the **user-service** microservice has been implemented so far. It handles:

- User registration (stores hashed password, returns a JWT)
- User login (verifies password, returns a JWT)
- Protected profile endpoints (require a valid JWT)

Everything else in the architecture — trading, portfolio, market data, Kafka, Eureka — is planned in ROADMAP.md but not yet coded.

---

## Project Structure

```
robinhood-clone/
├── ROADMAP.md                    ← architecture guide + learning plan
├── docker-compose.yml            ← spins up Postgres, Redis, Kafka
└── user-service/
    ├── pom.xml                   ← Maven build + dependencies
    └── src/
        ├── main/
        │   ├── java/com/robinhood/user/
        │   │   ├── UserServiceApplication.java       ← main()
        │   │   ├── model/User.java                   ← database entity
        │   │   ├── repository/UserRepository.java    ← DB queries
        │   │   ├── service/UserService.java          ← business logic
        │   │   ├── controller/
        │   │   │   ├── AuthController.java           ← /api/v1/auth/*
        │   │   │   └── UserController.java           ← /api/v1/users/*
        │   │   ├── dto/
        │   │   │   ├── RegisterRequest.java
        │   │   │   ├── LoginRequest.java
        │   │   │   ├── AuthResponse.java
        │   │   │   └── UserResponse.java
        │   │   ├── security/
        │   │   │   ├── JwtUtil.java                  ← create/parse JWTs
        │   │   │   ├── JwtAuthFilter.java            ← intercepts every request
        │   │   │   └── SecurityConfig.java           ← which routes need auth
        │   │   └── exception/
        │   │       ├── GlobalExceptionHandler.java   ← maps exceptions → HTTP codes
        │   │       ├── UserNotFoundException.java
        │   │       └── UserAlreadyExistsException.java
        │   └── resources/application.yml             ← dev config
        └── test/
            ├── java/com/robinhood/user/
            │   └── UserServiceTest.java              ← 4 integration tests
            └── resources/application.yml            ← test config (H2 DB)
```

---

## Infrastructure

**File:** [docker-compose.yml](docker-compose.yml)

Running `docker-compose up -d` starts everything the services need locally. Nothing in the app code changes for local vs. CI — only the environment variables differ.

### What's in it

| Container | Image | Port | Purpose |
|---|---|---|---|
| `postgres-users` | postgres:16-alpine | 5432 | user-service database |
| `postgres-trading` | postgres:16-alpine | 5433 | trading-service database (future) |
| `postgres-portfolio` | postgres:16-alpine | 5434 | portfolio-service database (future) |
| `redis` | redis:7-alpine | 6379 | cache for market prices (future) |
| `zookeeper` | confluentinc/cp-zookeeper | 2181 | required by Kafka for coordination |
| `kafka` | confluentinc/cp-kafka | 9092 | message bus between services (future) |
| `kafka-ui` | provectuslabs/kafka-ui | 8090 | visual UI to browse Kafka topics |

### Why three separate Postgres containers?

Each microservice owns its own database — this is the **database-per-service** pattern. The user-service cannot directly query the trading database. Services talk to each other only via HTTP or Kafka events. This means you can replace the trading-service's database without touching user-service.

### Volumes

The three `postgres_*_data` volumes persist database data across `docker-compose down` restarts. Without volumes, all data would be lost every time you stop the containers.

---

## User Service

### Entry Point

**File:** [user-service/src/main/java/com/robinhood/user/UserServiceApplication.java](user-service/src/main/java/com/robinhood/user/UserServiceApplication.java)

```java
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

`@SpringBootApplication` is a shortcut for three annotations:
- `@Configuration` — this class can define beans
- `@EnableAutoConfiguration` — Spring Boot reads your classpath and auto-configures things (e.g., sees postgres driver → configures a DataSource)
- `@ComponentScan` — scans this package and all sub-packages for `@Component`, `@Service`, `@Repository`, `@Controller` beans

`SpringApplication.run(...)` boots the embedded Tomcat server, connects to the database, and starts listening for HTTP on port 8081.

---

### pom.xml — Dependencies

**File:** [user-service/pom.xml](user-service/pom.xml)

The `parent` section pins all Spring dependency versions so you never have to specify version numbers yourself.

| Dependency | Why |
|---|---|
| `spring-boot-starter-web` | Embedded Tomcat + `@RestController` support |
| `spring-boot-starter-security` | Authentication/authorization framework |
| `spring-boot-starter-data-jpa` | ORM — maps Java classes to DB tables via Hibernate |
| `spring-boot-starter-validation` | `@NotBlank`, `@Email`, `@Size` annotations on request objects |
| `postgresql` | JDBC driver to talk to Postgres (runtime-only, not needed at compile time) |
| `h2` | In-memory database used in tests — no real Postgres needed when running tests |
| `jjwt-api/impl/jackson` | Library for creating and verifying JWT tokens |
| `lombok` | Generates `@Getter`, `@Setter`, `@NoArgsConstructor` at compile time — removes boilerplate |
| `spring-boot-starter-test` | JUnit 5 + Mockito + Spring test support |

---

### application.yml — Configuration

**File:** [user-service/src/main/resources/application.yml](user-service/src/main/resources/application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/userdb
  jpa:
    hibernate:
      ddl-auto: create-drop   # drops and recreates the schema every restart
    show-sql: true

server:
  port: 8081

jwt:
  secret: "robinhood-super-secret-jwt-key-..."
  expiration-ms: 86400000     # 24 hours
```

**`ddl-auto: create-drop`** means Hibernate reads your `@Entity` classes and generates the SQL `CREATE TABLE` statements automatically on startup, then drops them on shutdown. This is fine for development. In production you'd use `validate` (checks schema matches entities) and manage schema changes with a migration tool like Flyway.

**`jwt.secret`** is the signing key used to create and verify JWT tokens. The comment in the file explicitly warns to replace it in production and inject it via environment variable.

**Test config** ([user-service/src/test/resources/application.yml](user-service/src/test/resources/application.yml)) overrides the datasource to use H2 in-memory instead of Postgres. This means tests can run anywhere without Docker.

---

### The Data Layer

#### User Entity

**File:** [user-service/src/main/java/com/robinhood/user/model/User.java](user-service/src/main/java/com/robinhood/user/model/User.java)

An `@Entity` class maps 1:1 to a database table. Each field becomes a column.

```java
@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
    })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;
    private String email;
    private String passwordHash;   // never plaintext
    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private boolean enabled = true;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();   // auto-set on every save
    }

    public enum Role { USER, ADMIN }
}
```

Key decisions:
- **UUID primary key** instead of auto-increment integer. In a microservices system, multiple services need to create IDs independently. UUIDs can be generated in memory without a database round-trip and are globally unique across services.
- **`passwordHash` field** — the password is never stored in plaintext. BCrypt hash is stored. When a user logs in, BCrypt re-hashes the input and compares.
- **`@Enumerated(EnumType.STRING)`** — stores `"USER"` or `"ADMIN"` as text in the DB. Without this annotation, JPA would store the ordinal (0, 1) which breaks if you reorder the enum values.
- **`@PreUpdate`** — JPA lifecycle hook. Automatically stamps `updatedAt` every time Hibernate flushes a change.
- **`@Column(updatable = false)` on `createdAt`** — tells Hibernate to never include this column in UPDATE statements.

#### UserRepository

**File:** [user-service/src/main/java/com/robinhood/user/repository/UserRepository.java](user-service/src/main/java/com/robinhood/user/repository/UserRepository.java)

```java
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailAndEnabled(String email, boolean enabled);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
```

This is **Spring Data JPA** magic. You declare the interface and Spring generates the SQL implementation at startup by parsing the method name. `findByEmailAndEnabled` becomes `SELECT * FROM users WHERE email = ? AND enabled = ?`. You never write SQL for standard queries.

`JpaRepository<User, UUID>` also gives you `save()`, `findById()`, `findAll()`, `deleteById()`, etc. for free.

---

### DTOs

DTOs (Data Transfer Objects) separate what the API exposes from what the database stores. All DTOs use Java **records** — immutable, compact, no boilerplate.

#### RegisterRequest

**File:** [user-service/src/main/java/com/robinhood/user/dto/RegisterRequest.java](user-service/src/main/java/com/robinhood/user/dto/RegisterRequest.java)

```java
public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 30) String username,
    @NotBlank @Email                   String email,
    @NotBlank @Size(min = 8)           String password,
    @NotBlank                          String firstName,
    @NotBlank                          String lastName
) {}
```

The validation annotations (`@NotBlank`, `@Email`, `@Size`) are enforced when the controller uses `@Valid`. If any constraint fails, Spring throws `MethodArgumentNotValidException` which the `GlobalExceptionHandler` catches and turns into a 400 response.

#### LoginRequest

**File:** [user-service/src/main/java/com/robinhood/user/dto/LoginRequest.java](user-service/src/main/java/com/robinhood/user/dto/LoginRequest.java)

Simple — just email + password, both required.

#### AuthResponse

**File:** [user-service/src/main/java/com/robinhood/user/dto/AuthResponse.java](user-service/src/main/java/com/robinhood/user/dto/AuthResponse.java)

```java
public record AuthResponse(
    String token,       // the JWT
    String tokenType,   // always "Bearer"
    UUID userId,
    String username,
    String email
) {
    public AuthResponse(String token, UUID userId, String username, String email) {
        this(token, "Bearer", userId, username, email);
    }
}
```

Returned to the client after register or login. The client stores the JWT and sends it on every subsequent request as `Authorization: Bearer <token>`.

#### UserResponse

**File:** [user-service/src/main/java/com/robinhood/user/dto/UserResponse.java](user-service/src/main/java/com/robinhood/user/dto/UserResponse.java)

```java
public record UserResponse(UUID id, String username, String email,
                            String firstName, String lastName,
                            String role, Instant createdAt) {
    public static UserResponse from(User user) { ... }
}
```

The `from(User user)` factory method converts the JPA entity to a safe response. Crucially, `passwordHash` is **not** included — you never expose password hashes in API responses.

---

### Service Layer

**File:** [user-service/src/main/java/com/robinhood/user/service/UserService.java](user-service/src/main/java/com/robinhood/user/service/UserService.java)

All business logic lives here. Controllers just delegate.

```java
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    ...
}
```

`@Transactional` at the class level means every public method runs inside a database transaction. If anything throws an exception mid-method, the transaction rolls back automatically.

#### `register()`

1. Check for duplicate email and username upfront (fail fast with a clear message rather than hitting a DB constraint violation)
2. Create a `User` entity, store the BCrypt hash (never the plaintext password)
3. Save to DB
4. Generate and return a JWT immediately — user is logged in right after registering

#### `login()`

1. Find user by email where `enabled = true`
2. `passwordEncoder.matches(plaintext, hash)` — BCrypt re-computes the hash and compares. Returns false if wrong.
3. Generate and return a JWT

#### `getUser()`

Annotated `@Transactional(readOnly = true)` — this hint tells the database it can optimize (e.g., avoid row-level locks, use a read replica) since no writes will happen.

#### `updateName()`

No explicit `save()` call needed. When JPA loads an entity inside a transaction, it becomes a **managed entity**. Any field changes are automatically detected ("dirty checking") and flushed to the DB when the transaction commits.

---

### Controllers

#### AuthController

**File:** [user-service/src/main/java/com/robinhood/user/controller/AuthController.java](user-service/src/main/java/com/robinhood/user/controller/AuthController.java)

Public endpoints — no JWT required.

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Create account, returns JWT |
| `POST` | `/api/v1/auth/login` | Authenticate, returns JWT |

`@Valid` on the `@RequestBody` parameter triggers validation. If validation fails, Spring short-circuits the method and the `GlobalExceptionHandler` returns a 400.

#### UserController

**File:** [user-service/src/main/java/com/robinhood/user/controller/UserController.java](user-service/src/main/java/com/robinhood/user/controller/UserController.java)

Protected endpoints — require a valid JWT.

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/users/me` | Get your own profile |
| `GET` | `/api/v1/users/{id}` | Get any user by ID |

`@AuthenticationPrincipal String userId` — Spring injects whatever the `JwtAuthFilter` stored as the "principal" in the SecurityContext. In this case it's the user's UUID string extracted from the JWT. This avoids trusting the client to pass their own ID in the request body.

---

### Security

This is the most complex part. Three classes work together.

#### JwtUtil

**File:** [user-service/src/main/java/com/robinhood/user/security/JwtUtil.java](user-service/src/main/java/com/robinhood/user/security/JwtUtil.java)

Handles all JWT operations using the `jjwt` library.

**What a JWT looks like:**
```
eyJhbGciOiJIUzI1NiJ9  ← header (base64): {"alg":"HS256"}
.
eyJzdWIiOiJ1c2VyLWlkIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIiwicm9sZSI6IlVTRVIifQ
                        ← payload (base64): {"sub":"uuid","email":"...","role":"USER","iat":...,"exp":...}
.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
                        ← signature: HMAC-SHA256(header+"."+payload, secret)
```

The client stores this token (in localStorage or memory) and attaches it to every request: `Authorization: Bearer <token>`.

**How verification works:** The server re-computes the signature using its secret key and compares it to the signature in the token. If they match, the payload hasn't been tampered with. The server trusts the claims (`sub`, `email`, `role`) without hitting the database.

`generateToken()` — builds the JWT with claims: `sub` (user UUID), `email`, `role`, `iat` (issued-at), `exp` (expiry).

`parseToken()` — verifies the signature and decodes the claims. Throws an exception if the token is expired or tampered with.

`isValid()` — wraps `parseToken()` in a try/catch, returns boolean.

#### JwtAuthFilter

**File:** [user-service/src/main/java/com/robinhood/user/security/JwtAuthFilter.java](user-service/src/main/java/com/robinhood/user/security/JwtAuthFilter.java)

Runs on **every HTTP request** before the controller is called.

```java
protected void doFilterInternal(request, response, filterChain) {
    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);  // strip "Bearer "

        if (jwtUtil.isValid(token)) {
            String userId = jwtUtil.extractUserId(token);
            String role   = jwtUtil.extractRole(token);

            // Put the authenticated identity into the SecurityContext
            var auth = new UsernamePasswordAuthenticationToken(
                userId,   // ← this becomes @AuthenticationPrincipal in controllers
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    filterChain.doFilter(request, response);  // always continue the chain
}
```

If no token or an invalid token is present, the filter does nothing — the `SecurityContext` stays empty. When the request hits the controller, Spring Security sees no authentication and returns 401 automatically (because of the `anyRequest().authenticated()` rule in `SecurityConfig`).

#### SecurityConfig

**File:** [user-service/src/main/java/com/robinhood/user/security/SecurityConfig.java](user-service/src/main/java/com/robinhood/user/security/SecurityConfig.java)

Configures the overall security policy:

```java
http
    .csrf(AbstractHttpConfigurer::disable)         // no CSRF — using JWTs not cookies
    .sessionManagement(s ->
        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // no server sessions
    .authorizeHttpRequests(auth -> auth
        .requestMatchers(POST, "/api/v1/auth/register").permitAll()
        .requestMatchers(POST, "/api/v1/auth/login").permitAll()
        .anyRequest().authenticated()              // everything else needs a JWT
    )
    .addFilterBefore(jwtAuthFilter,
        UsernamePasswordAuthenticationFilter.class);  // run JWT filter first
```

**Why disable CSRF?** CSRF attacks exploit browser cookies. Because this API uses JWTs in the `Authorization` header (not cookies), there's no browser-side credential for an attacker to exploit, so CSRF protection is unnecessary.

**Why `STATELESS`?** Traditional web apps create a server-side session and store a session ID in a cookie. With JWTs, each request is self-contained — the token carries identity. No session is created or needed.

**`BCryptPasswordEncoder(12)`** — work factor 12 means ~300ms per hash on modern hardware. This is intentionally slow to make brute-force attacks expensive. BCrypt also automatically handles salting (adds random bytes to prevent rainbow table attacks).

---

### Exception Handling

**File:** [user-service/src/main/java/com/robinhood/user/exception/GlobalExceptionHandler.java](user-service/src/main/java/com/robinhood/user/exception/GlobalExceptionHandler.java)

`@RestControllerAdvice` is a centralized place to map exceptions to HTTP responses. Without this, exceptions would bubble up as 500 errors with stack traces.

| Exception | HTTP Status | When thrown |
|---|---|---|
| `UserAlreadyExistsException` | 409 Conflict | Register with existing email/username |
| `UserNotFoundException` | 404 Not Found | Get user that doesn't exist |
| `MethodArgumentNotValidException` | 400 Bad Request | `@Valid` fails on a request DTO |

Uses Spring's `ProblemDetail` format (RFC 7807) — a standard JSON error shape:
```json
{
  "status": 409,
  "detail": "Email already registered: john@example.com"
}
```

For validation errors, the response includes a map of which fields failed:
```json
{
  "status": 400,
  "detail": "Validation failed",
  "errors": {
    "email": "Must be a valid email address",
    "password": "Password must be at least 8 characters"
  }
}
```

The custom exception classes (`UserAlreadyExistsException`, `UserNotFoundException`) extend `RuntimeException` — meaning callers don't need try/catch. Spring's `@ExceptionHandler` intercepts them automatically.

---

### Tests

**File:** [user-service/src/test/java/com/robinhood/user/UserServiceTest.java](user-service/src/test/java/com/robinhood/user/UserServiceTest.java)

Four integration tests using the full Spring context with H2 in-memory database.

```java
@SpringBootTest          // boots the full application context
@Transactional           // rolls back every test — DB is clean for each test method
@ActiveProfiles("test")  // loads test/resources/application.yml (H2, not Postgres)
class UserServiceTest {
    @Autowired
    private UserService userService;
    ...
}
```

**Why `@Transactional` on tests?** Each test method runs inside a transaction that is rolled back at the end. This means tests don't pollute each other — you never have a "user already exists from the previous test" problem.

| Test | What it checks |
|---|---|
| `register_createsUserAndReturnsToken` | Happy path: register returns a non-null token |
| `register_throwsIfEmailAlreadyExists` | Duplicate email → `UserAlreadyExistsException` |
| `login_returnsTokenForValidCredentials` | Happy path: login returns a token |
| `login_throwsForWrongPassword` | Wrong password → `BadCredentialsException` |

---

## Request Flow

Here's what happens when a client calls `POST /api/v1/auth/register`:

```
Client
  │
  │  POST /api/v1/auth/register
  │  Body: { "username": "john", "email": "john@example.com",
  │           "password": "secret123", "firstName": "John", "lastName": "Doe" }
  │
  ▼
Embedded Tomcat (port 8081)
  │
  ▼
Spring Security Filter Chain
  │
  ├── JwtAuthFilter.doFilterInternal()
  │     → No Authorization header → does nothing → continues chain
  │
  ├── SecurityConfig authorization check
  │     → Path matches permitAll() rule → allows through
  │
  ▼
AuthController.register(@Valid @RequestBody RegisterRequest request)
  │
  ├── @Valid triggers validation
  │     → All fields pass → continues
  │     → Any field fails → GlobalExceptionHandler → 400 response
  │
  ▼
UserService.register(request)
  │
  ├── userRepository.existsByEmail() → false → continue
  ├── userRepository.existsByUsername() → false → continue
  ├── new User() → set fields
  ├── passwordEncoder.encode("secret123") → "$2a$12$..." (BCrypt hash)
  ├── userRepository.save(user) → INSERT INTO users ... → DB assigns UUID
  ├── jwtUtil.generateToken(uuid, email, "USER") → "eyJ..."
  │
  ▼
AuthController returns ResponseEntity (201 Created)
  Body: { "token": "eyJ...", "tokenType": "Bearer",
          "userId": "uuid", "username": "john", "email": "john@example.com" }
  │
  ▼
Client stores the JWT and uses it for all future requests
```

---

Here's what happens on a protected request: `GET /api/v1/users/me`:

```
Client
  │
  │  GET /api/v1/users/me
  │  Authorization: Bearer eyJ...
  │
  ▼
JwtAuthFilter
  │
  ├── Header present? → yes
  ├── jwtUtil.isValid(token) → true
  ├── Extract userId, role from token
  ├── Create UsernamePasswordAuthenticationToken(userId, null, [ROLE_USER])
  └── Store in SecurityContextHolder
  │
  ▼
SecurityConfig authorization check
  │
  └── anyRequest().authenticated() → SecurityContext has auth → allow through
  │
  ▼
UserController.getMe(@AuthenticationPrincipal String userId)
  │
  ├── Spring injects userId from SecurityContext principal
  │
  ▼
UserService.getUser(UUID.fromString(userId))
  │
  ├── userRepository.findById(uuid) → SELECT * FROM users WHERE id = ?
  ├── UserResponse.from(user) → maps entity to DTO (no passwordHash)
  │
  ▼
200 OK: { "id": "...", "username": "john", "email": "john@example.com", ... }
```

---

## Key Design Decisions

### 1. BCrypt with work factor 12
Passwords are never stored plaintext. BCrypt's work factor makes each hash computation take ~300ms, which makes brute-force and dictionary attacks impractical. It also automatically generates a unique salt per password, so two users with the same password get different hashes.

### 2. UUID primary keys
Using `UUID` instead of auto-increment integers means any service can generate an ID without needing to ask the database. This is critical in microservices where multiple services create related entities.

### 3. DTOs separate API surface from database model
The `User` entity (internal) and `UserResponse` (external) are separate classes. You control exactly what leaves the service. Adding a new column to the `User` table doesn't automatically expose it in the API.

### 4. Stateless JWT authentication
No server-side session state. The JWT carries all identity information. This means the service can run multiple instances behind a load balancer — any instance can verify any token without shared session storage.

### 5. `@Transactional(readOnly = true)` on reads
Read-only transactions are a hint to the DB driver and Hibernate. Hibernate skips dirty checking (no need to compare before/after state), and some JDBC drivers route read-only connections to read replicas.

### 6. `GlobalExceptionHandler` centralized error handling
Instead of try/catch blocks in every controller or service, exceptions propagate up naturally and are mapped to HTTP responses in one place. Adding a new error type is one method in one file.

### 7. H2 for tests, Postgres for dev/prod
Tests don't require Docker. The test `application.yml` overrides the datasource to H2 in-memory. Because JPA is the abstraction layer, the same entity and repository code works against both databases.

---

## What's Planned But Not Built Yet

From ROADMAP.md, the full system has 9 services. Only `user-service` is implemented.

| Service | Status | Key concepts it would teach |
|---|---|---|
| `user-service` | **Built** | REST, JPA, JWT, Spring Security |
| `account-service` | Not started | Service-to-service HTTP calls, financial transactions |
| `market-service` | Not started | External API integration, Redis caching, WebSockets |
| `trading-service` | Not started | Kafka event publishing, distributed transactions |
| `portfolio-service` | Not started | Kafka event consumption, aggregations |
| `api-gateway` | Not started | Spring Cloud Gateway, route config, JWT validation filter |
| `notification-service` | Not started | Kafka consumer, email sending |
| `discovery-server` | Not started | Eureka service registry |
| `config-server` | Not started | Spring Cloud Config, centralized YAML |

The intended Kafka event flow once trading is built:
```
trading-service  →  order.placed   →  account-service (debit funds)
account-service  →  account.debited  →  trading-service (execute order)
trading-service  →  order.filled   →  portfolio-service (update holdings)
                                   →  notification-service (send email)
```
