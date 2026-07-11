# Robinhood Clone — Java + Spring Microservices

A full learning roadmap and architecture reference for building a Robinhood-like trading platform
using Java 21, Spring Boot, and microservices.

---

## Learning Phases

| Phase | Topic | Timeline |
|---|---|---|
| 1 | Java Fundamentals | Week 1–2 |
| 2 | Spring Boot Basics | Week 2–3 |
| 3 | Microservices Architecture | Week 3–5 |
| 4 | Build the App (iterative) | Week 5+ |

---

## System Architecture

```
                        ┌─────────────────┐
   Mobile/Web Client ──▶│   API Gateway   │ (Spring Cloud Gateway)
                        └────────┬────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
    ┌─────────▼──────┐  ┌────────▼───────┐  ┌──────▼──────────┐
    │  User Service  │  │ Trading Service│  │  Market Service  │
    │  (Auth/Profile)│  │ (Orders/Exec.) │  │  (Prices/Data)   │
    └────────────────┘  └────────┬───────┘  └──────────────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
    ┌─────────▼──────┐  ┌────────▼───────┐  ┌──────▼──────────┐
    │Account Service │  │  Notification  │  │ Portfolio Service│
    │(Balance/Cash)  │  │  Service       │  │ (Holdings/P&L)   │
    └────────────────┘  └────────────────┘  └──────────────────┘
              │
    ┌─────────▼──────────────────────────────────────┐
    │              Message Bus (Kafka)                │
    │  order.placed → account.debited → order.filled  │
    └─────────────────────────────────────────────────┘
```

---

## Services

| Service | Responsibilities | Database | Port |
|---|---|---|---|
| `api-gateway` | Routing, auth filter, rate limiting | — | 8080 |
| `user-service` | Register, login, JWT tokens | PostgreSQL | 8081 |
| `account-service` | Cash balance, deposits, withdrawals | PostgreSQL | 8082 |
| `market-service` | Stock prices (WebSocket + REST) | Redis | 8083 |
| `trading-service` | Place/cancel orders, order book | PostgreSQL | 8084 |
| `portfolio-service` | Holdings, P&L, history | PostgreSQL | 8085 |
| `notification-service` | Email/push on order fills | — | 8086 |
| `discovery-server` | Eureka service registry | — | 8761 |
| `config-server` | Centralized configuration | Git repo | 8888 |

---

## Build Order

Build services in this order — each one teaches a new pattern:

1. `user-service` — Spring Boot, REST, JPA, JWT auth
2. `account-service` — service-to-service calls, transactions
3. `market-service` — external API, Redis caching, WebSockets
4. `trading-service` — Kafka events, distributed transactions
5. `portfolio-service` — event-driven updates, aggregations
6. `api-gateway` — routing, JWT validation filter
7. `notification-service` — Kafka consumer, email

---

## Phase 1: Java Fundamentals

### Classes and Objects

```java
public class Stock {
    private String ticker;
    private double price;

    public Stock(String ticker, double price) {
        this.ticker = ticker;
        this.price = price;
    }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
```

### Interfaces

```java
// Interface = contract (what, not how)
public interface OrderExecutor {
    Order execute(OrderRequest request);
    void cancel(String orderId);
}

public class MarketOrderExecutor implements OrderExecutor {
    @Override
    public Order execute(OrderRequest request) {
        // implementation
    }
}
```

### Generics and Collections

```java
List<Stock> portfolio = new ArrayList<>();
Map<String, Double> prices = new HashMap<>();

Optional<Stock> found = portfolio.stream()
    .filter(s -> s.getTicker().equals("AAPL"))
    .findFirst();
```

### Records (Java 16+)

Immutable data carriers — replaces boilerplate POJOs. Used heavily in modern Spring.

```java
public record OrderRequest(
    String userId,
    String ticker,
    int quantity,
    OrderType type
) {}
```

### Enums

```java
public enum OrderType   { MARKET, LIMIT, STOP_LOSS }
public enum OrderStatus { PENDING, FILLED, CANCELLED, REJECTED }
```

---

## Phase 2: Spring Boot

### Core Concept: Dependency Injection

Spring manages object creation. You declare *what* you need; Spring wires it together.

```java
// Without Spring — manual, painful
OrderRepository repo = new OrderRepository(new DataSource(...));
OrderService service = new OrderService(repo, new PriceService(...));

// With Spring — declare dependencies, Spring injects them
@Service
public class OrderService {
    private final OrderRepository repo;
    private final PriceService priceService;

    // Spring sees this constructor and injects matching beans
    public OrderService(OrderRepository repo, PriceService priceService) {
        this.repo = repo;
        this.priceService = priceService;
    }
}
```

### Key Annotations

| Annotation | Purpose |
|---|---|
| `@SpringBootApplication` | Entry point; enables auto-configuration |
| `@RestController` | HTTP handler; returns JSON automatically |
| `@Service` | Business logic layer |
| `@Repository` | Data access layer |
| `@Component` | Generic Spring-managed bean |
| `@Autowired` | Inject a dependency (prefer constructor injection) |
| `@Value("${key}")` | Inject a config property |
| `@Transactional` | Wraps method in a DB transaction |

### A Complete REST Endpoint

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST /api/v1/orders
    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest request) {
        Order order = orderService.place(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // GET /api/v1/orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable String id) {
        return orderService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/orders?userId=abc
    @GetMapping
    public List<Order> getUserOrders(@RequestParam String userId) {
        return orderService.findByUser(userId);
    }
}
```

### Spring Layers

```
Request
  │
  ▼
Controller        ← HTTP in/out, input validation, delegates to service
  │
  ▼
Service           ← Business logic, orchestration, transactions
  │
  ▼
Repository        ← DB queries via Spring Data JPA
  │
  ▼
Database          ← PostgreSQL / H2
```

---

## Phase 3: Microservices Patterns

### 1. API Gateway

Single entry point. Routes requests to the correct service.

```yaml
# gateway/src/main/resources/application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service        # lb = load balanced via Eureka
          predicates:
            - Path=/api/users/**
        - id: trading-service
          uri: lb://trading-service
          predicates:
            - Path=/api/orders/**
```

### 2. Service Discovery (Eureka)

Services register themselves by name. Others find them by name, not IP.

```java
@SpringBootApplication
@EnableEurekaClient
public class TradingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradingServiceApplication.class, args);
    }
}
```

### 3. Synchronous Communication (WebClient)

Used when Service A needs an immediate answer from Service B.

```java
@Service
public class TradingService {
    private final WebClient webClient;

    public boolean hasSufficientFunds(String userId, double amount) {
        return webClient.get()
            .uri("http://account-service/api/accounts/{userId}/balance", userId)
            .retrieve()
            .bodyToMono(AccountBalance.class)
            .map(b -> b.available() >= amount)
            .block();
    }
}
```

### 4. Async Communication (Kafka)

Used when Service A needs to notify others but doesn't need an immediate reply.

```java
// Producer: trading-service publishes an event
@Service
public class OrderService {
    private final KafkaTemplate<String, OrderEvent> kafka;

    public Order fill(Order order) {
        order.setStatus(OrderStatus.FILLED);
        kafka.send("order.filled", new OrderEvent(order));
        return orderRepository.save(order);
    }
}

// Consumer: portfolio-service listens and updates holdings
@KafkaListener(topics = "order.filled")
public void onOrderFilled(OrderEvent event) {
    portfolioService.updateHoldings(event);
}
```

Kafka topics in this app:

| Topic | Published by | Consumed by |
|---|---|---|
| `order.placed` | trading-service | account-service |
| `account.debited` | account-service | trading-service |
| `order.filled` | trading-service | portfolio-service, notification-service |
| `order.cancelled` | trading-service | account-service, notification-service |

### 5. Circuit Breaker (Resilience4j)

Prevents cascading failures when a downstream service is unavailable.

```java
@CircuitBreaker(name = "marketData", fallbackMethod = "cachedPrice")
public double getLivePrice(String ticker) {
    return marketDataClient.getPrice(ticker);
}

// Called automatically when marketData circuit is open
public double cachedPrice(String ticker, Exception e) {
    return priceCache.getLastKnown(ticker);
}
```

States: `CLOSED` (normal) → `OPEN` (failing, use fallback) → `HALF_OPEN` (testing recovery)

### 6. Centralized Config (Spring Cloud Config)

All services read config from one Git-backed config server.

```
config-server/
  └── src/main/resources/config/
        ├── application.yml           ← shared by all services
        ├── trading-service.yml       ← trading-specific config
        └── user-service.yml          ← user-specific config
```

---

## Prerequisites

```bash
# Java 21 (LTS)
brew install openjdk@21

# Build tool (Maven)
brew install maven

# Docker (for PostgreSQL, Kafka, Redis, Eureka)
brew install --cask docker

# IDE: IntelliJ IDEA Community (best for Java, free)
brew install --cask intellij-idea-ce
```

---

## Project Directory Structure

```
robinhood-clone/
├── ROADMAP.md                   ← this file
├── docker-compose.yml           ← spins up all infrastructure
├── api-gateway/
├── user-service/
├── account-service/
├── market-service/
├── trading-service/
├── portfolio-service/
├── notification-service/
├── discovery-server/
└── config-server/
```

Each service follows this internal layout:

```
user-service/
├── pom.xml
└── src/main/
    ├── java/com/robinhood/user/
    │   ├── UserServiceApplication.java     ← main entry point
    │   ├── controller/
    │   │   └── UserController.java
    │   ├── service/
    │   │   └── UserService.java
    │   ├── repository/
    │   │   └── UserRepository.java
    │   ├── model/
    │   │   └── User.java                   ← JPA entity
    │   ├── dto/
    │   │   ├── RegisterRequest.java        ← records
    │   │   └── LoginRequest.java
    │   └── security/
    │       ├── JwtUtil.java
    │       └── SecurityConfig.java
    └── resources/
        └── application.yml
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Service mesh | Spring Cloud (Gateway, Eureka, Config) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Cache | Redis |
| Messaging | Apache Kafka |
| Auth | JWT (JSON Web Tokens) |
| Resilience | Resilience4j (circuit breaker) |
| Containers | Docker + Docker Compose |
| Build | Maven |
