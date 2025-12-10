# Booking System Monorepo

**Rezervacijski sistem športnih objektov** (Sports Facility Booking System)

Mikrostoritvena aplikacija za rezervacijo športnih objektov, zgrajena z Spring Boot, PostgreSQL, Redis in Docker.

---

## 🏗️ Arhitektura

### Mikrostoritve

| Service                  | Port | Opis                                | Status     |
| ------------------------ | ---- | ----------------------------------- | ---------- |
| **auth-service**         | 8080 | Avtentikacija uporabnikov (JWT)     | ✅ Running |
| **facility-service**     | 8081 | Upravljanje športnih objektov       | ✅ Running |
| **booking-service**      | 8082 | Rezervacije in upravljanje terminov | ✅ Running |
| **payment-service**      | 8083 | Plačilni sistem (Stripe)            | 🔜 Planned |
| **notification-service** | 8084 | Email/SMS obvestila                 | 🔜 Planned |
| **calendar-service**     | 8085 | Integracija s koledarji             | 🔜 Planned |

### Infrastruktura

- **PostgreSQL 15** - Port 5432 (Skupna baza: `booking_system`)
- **Redis 7** - Port 6379 (JWT token storage & caching)
- **Docker Compose** - Lokalno razvojno okolje

---

## 🚀 Quick Start

### Predpogoji

- Java 17+
- Maven 3.9+
- Docker Desktop
- PostgreSQL 15 (via Docker)

### 1. Zagon Infrastrukture

```bash
cd booking-system-monorepo
docker-compose up -d
```

### 2. Preveri Docker Containers

```bash
docker ps
```

Morali bi videti:

- `booking-postgres` (port 5432)
- `booking-redis` (port 6379)

### 3. Zagon Mikroservisov

#### Auth Service (Port 8080)

```bash
cd services/auth-service
mvn spring-boot:run
```

#### Facility Service (Port 8081)

```bash
cd services/facility-service
mvn spring-boot:run
```

#### Booking Service (Port 8082)

```bash
cd services/booking-service
mvn spring-boot:run
```

---

## 📚 API Dokumentacija

### Auth Service - `/api/auth`

- `POST /register` - Registracija uporabnika
- `POST /login` - Prijava (vrne JWT token)

[📖 Podrobna dokumentacija →](services/auth-service/API_DOCUMENTATION.md)

### Facility Service - `/api/facilities`

- `GET /` - Seznam vseh objektov
- `GET /{id}` - Podrobnosti objekta
- `POST /` - Dodaj nov objekt
- `PUT /{id}` - Posodobi objekt
- `DELETE /{id}` - Izbriši objekt
- `GET /type/{type}` - Objekti po tipu
- `GET /owner/{ownerId}` - Objekti po lastniku

[📖 Podrobna dokumentacija →](services/facility-service/API_DOCUMENTATION.md)

### Booking Service - `/api/bookings`

- `POST /` - Ustvari rezervacijo
- `GET /my` - Moje rezervacije
- `GET /my/upcoming` - Prihodnje rezervacije
- `GET /my/past` - Pretekle rezervacije
- `GET /{id}` - Podrobnosti rezervacije
- `GET /facility/{facilityId}` - Rezervacije po objektu
- `PATCH /{id}/status` - Posodobi status
- `DELETE /{id}` - Prekliči rezervacijo

[📖 Podrobna dokumentacija →](services/booking-service/API_DOCUMENTATION.md)

---

## 🧪 Testiranje (Postman)

### 1. Registracija Uporabnika

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "janez",
  "email": "janez@example.com",
  "password": "geslo123",
  "role": "USER"
}
```

**Response:** `201 Created` z `userId`

---

### 2. Prijava

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "janez",
  "password": "geslo123"
}
```

**Response:** `200 OK` z JWT `token`

---

### 3. Dodaj Športni Objekt

```http
POST http://localhost:8081/api/facilities
Content-Type: application/json
X-User-Id: 1

{
  "name": "Nogometno igrišče Center",
  "type": "FOOTBALL_FIELD",
  "address": "Kardeljeva ploščad 1, Ljubljana",
  "description": "Prvorazredno nogometno igrišče z umetno travo",
  "capacity": 22,
  "pricePerHour": 50.00
}
```

**Response:** `201 Created` z `facilityId`

---

### 4. Ustvari Rezervacijo

```http
POST http://localhost:8082/api/bookings
Content-Type: application/json
X-User-Id: 1

{
  "facilityId": 1,
  "startTime": "2025-12-15T10:00:00",
  "endTime": "2025-12-15T12:00:00",
  "notes": "Ekipni trening"
}
```

**Response:** `201 Created`

---

### 5. Potrdi Rezervacijo

```http
PATCH http://localhost:8082/api/bookings/1/status
Content-Type: application/json
X-User-Id: 1

{
  "status": "CONFIRMED"
}
```

**Response:** `200 OK`

---

## 🗄️ Database Schema

### Users (auth-service)

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

### Facilities (facility-service)

```sql
CREATE TABLE facilities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    address VARCHAR(200) NOT NULL,
    description TEXT,
    capacity INT NOT NULL,
    price_per_hour DECIMAL(10,2) NOT NULL,
    owner_id BIGINT NOT NULL,
    available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Bookings (booking-service)

```sql
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    facility_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

---

## 🔧 Tehnologije

### Backend

- **Spring Boot 3.4.12** - Main framework
- **Java 17** - Programming language
- **Maven 3.9.11** - Build tool
- **Spring Data JPA** - ORM
- **Hibernate** - JPA implementation
- **Jakarta Validation** - Request validation
- **Lombok** - Boilerplate reduction

### Database & Cache

- **PostgreSQL 15** - Relational database
- **Redis 7** - Cache & session storage

### Security

- **JJWT 0.12.6** - JWT tokens
- **BCrypt** - Password hashing

### DevOps

- **Docker & Docker Compose** - Containerization
- **Spring Boot DevTools** - Hot reload

---

## 📁 Struktura Projekta

```
booking-system-monorepo/
├── docker-compose.yml          # Infrastructure setup
├── README.md                   # This file
│
├── services/
│   ├── auth-service/          # Port 8080 - Authentication
│   │   ├── src/
│   │   │   └── main/java/si/fri/prpo/authservice/
│   │   │       ├── controller/   # REST endpoints
│   │   │       ├── service/      # Business logic
│   │   │       ├── repository/   # Data access
│   │   │       ├── entity/       # JPA entities
│   │   │       ├── dto/          # Data transfer objects
│   │   │       ├── security/     # JWT & BCrypt
│   │   │       └── util/         # Utilities
│   │   └── pom.xml
│   │
│   ├── facility-service/      # Port 8081 - Facilities
│   │   ├── src/
│   │   │   └── main/java/si/fri/prpo/facilityservice/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       └── dto/
│   │   └── pom.xml
│   │
│   └── booking-service/       # Port 8082 - Bookings
│       ├── src/
│       │   └── main/java/si/fri/prpo/bookingservice/
│       │       ├── controller/
│       │       ├── service/
│       │       ├── repository/
│       │       ├── entity/
│       │       ├── dto/
│       │       └── exception/
│       └── pom.xml
│
└── client/                    # Frontend (React) - Planned
```

---

## ✅ Current Progress

### Completed ✅

- [x] Monorepo structure
- [x] Docker Compose setup (PostgreSQL + Redis)
- [x] Auth-service (User registration, login, JWT)
- [x] Facility-service (CRUD for sports facilities)
- [x] Booking-service (Reservation system with conflict detection)
- [x] API documentation for all services
- [x] Error handling & validation

### In Progress 🔄

- [ ] JWT integration across all services
- [ ] Payment-service (Stripe integration)
- [ ] Notification-service (Email/SMS)

### Planned 🔜

- [ ] Calendar-service (iCal export)
- [ ] React frontend
- [ ] Kubernetes deployment
- [ ] CI/CD pipeline (GitHub Actions)

---

## 🐛 Troubleshooting

### Maven "spring-boot plugin not found"

**Problem:** Running `mvn spring-boot:run` from wrong directory

**Rešitev:**

```bash
cd C:\Users\Administrator\Documents\PRPO\booking-system-monorepo\services\booking-service
mvn spring-boot:run
```

### Docker containers not running

**Problem:** PostgreSQL not accessible

**Rešitev:**

```bash
docker-compose down
docker-compose up -d
docker ps  # Verify containers are running
```

### VS Code Java errors

**Problem:** Red squiggly lines in Java files

**Rešitev:**

1. `Ctrl+Shift+P` → "Java: Clean Java Language Server Workspace"
2. Run `mvn clean install`
3. Restart VS Code

---

## 📞 Support

Za vprašanja ali težave odprite GitHub Issue.

---

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.
