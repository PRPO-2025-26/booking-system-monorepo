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
| **payment-service**      | 8083 | Plačilni sistem (Stripe + Mock)     | ✅ Running |
| **calendar-service**     | 8084 | Google Calendar integracija (Mock)  | ✅ Running |
| **notification-service** | 8085 | Email/SMS obvestila (Mock)          | ✅ Running |

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

#### Payment Service (Port 8083)

```bash
cd services/payment-service
./mvnw.cmd spring-boot:run
# Mock mode enabled by default - no Stripe credentials required
```

#### Calendar Service (Port 8084)

```bash
cd services/calendar-service
./mvnw.cmd spring-boot:run
# Mock mode enabled by default - no Google Calendar credentials required
```

#### Notification Service (Port 8085)

```bash
cd services/notification-service
./mvnw.cmd spring-boot:run
# Mock mode enabled by default - no SMTP credentials required
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

### Payment Service - `/api/payments`

- `POST /checkout` - Ustvari Stripe checkout session
- `GET /{id}` - Pridobi plačilo po ID
- `GET /booking/{bookingId}` - Plačilo za booking
- `GET /user/{userId}` - Vsa uporabnikova plačila
- `GET /session/{sessionId}` - Pridobi po session ID
- `GET /{id}/status` - Status plačila
- `POST /{id}/cancel` - Prekliči plačilo
- `POST /mock/{sessionId}/complete` - Mock: Potrdi plačilo (testing)
- `POST /mock/{sessionId}/fail` - Mock: Zavrni plačilo (testing)

[📖 Podrobna dokumentacija →](services/payment-service/API_DOCUMENTATION.md)

### Calendar Service - `/api/calendar`

- `POST /events` - Ustvari Google Calendar event
- `GET /events/{id}` - Pridobi event po ID
- `GET /events/booking/{bookingId}` - Event za booking
- `GET /events/user/{userId}` - Vsi uporabnikovi eventi
- `GET /events/user/{userId}/upcoming` - Prihajajoči eventi
- `PUT /events/{id}` - Posodobi event
- `POST /events/{id}/cancel` - Prekliči event
- `DELETE /events/{id}` - Izbriši event

[📖 Podrobna dokumentacija →](services/calendar-service/API_DOCUMENTATION.md)

### Notification Service - `/api/notifications`

- `POST /` - Pošlji obvestilo (email/SMS)
- `GET /{id}` - Pridobi obvestilo po ID
- `GET /user/{userId}` - Vsa uporabnikova obvestila
- `GET /booking/{bookingId}` - Obvestila za booking
- `GET /payment/{paymentId}` - Obvestila za plačilo
- `GET /event/{eventId}` - Obvestila za event
- `POST /{id}/retry` - Ponovno pošlji obvestilo

[📖 Podrobna dokumentacija →](services/notification-service/README.md)

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

### 6. Plačilo Rezervacije (Mock)

```http
POST http://localhost:8083/api/payments/checkout
Content-Type: application/json

{
  "bookingId": 1,
  "userId": 1,
  "amount": 100.00,
  "currency": "EUR"
}
```

**Response:** `201 Created` z `sessionId` in `checkoutUrl`

```http
POST http://localhost:8083/api/payments/mock/cs_test_123/complete
```

**Response:** `200 OK` - Plačilo označeno kot COMPLETED (mock mode)

---

### 7. Google Calendar Event (Mock)

```http
POST http://localhost:8084/api/calendar/events
Content-Type: application/json

{
  "bookingId": 1,
  "userId": 1,
  "summary": "Nogometno igrišče - Ekipni trening",
  "location": "Kardeljeva ploščad 1, Ljubljana",
  "description": "Rezervacija za ekipni trening",
  "startDateTime": "2025-12-15T10:00:00",
  "endDateTime": "2025-12-15T12:00:00",
  "timeZone": "Europe/Ljubljana",
  "attendeeEmails": ["janez.novak@example.com"]
}
```

**Response:** `201 Created` z `calendarEventId` (mock mode)

---

### 8. Pošlji Obvestilo (Mock)

```http
POST http://localhost:8085/api/notifications
Content-Type: application/json

{
  "userId": 1,
  "bookingId": 1,
  "type": "BOOKING_CONFIRMATION",
  "channel": "EMAIL",
  "recipient": "janez.novak@example.com",
  "subject": "Potrditev rezervacije",
  "content": "Vaša rezervacija za Nogometno igrišče Center je potrjena."
}
```

**Response:** `201 Created` - Obvestilo poslano (mock mode)

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

### Payments (payment-service)

```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    stripe_session_id VARCHAR(255) UNIQUE,
    stripe_payment_intent_id VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Calendar Events (calendar-service)

```sql
CREATE TABLE calendar_events (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    google_event_id VARCHAR(255) UNIQUE,
    summary VARCHAR(255) NOT NULL,
    location VARCHAR(500),
    description TEXT,
    start_date_time TIMESTAMP NOT NULL,
    end_date_time TIMESTAMP NOT NULL,
    time_zone VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Notifications (notification-service)

```sql
CREATE TABLE notification_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    booking_id BIGINT,
    payment_id BIGINT,
    event_id BIGINT,
    type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP,
    error_message TEXT,
    retry_count INT DEFAULT 0,
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
│   ├── booking-service/       # Port 8082 - Bookings
│   │   ├── src/
│   │   │   └── main/java/si/fri/prpo/bookingservice/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       ├── dto/
│   │   │       └── exception/
│   │   └── pom.xml
│   │
│   ├── payment-service/       # Port 8083 - Payments (Stripe + Mock)
│   │   ├── src/
│   │   │   └── main/java/si/fri/prpo/paymentservice/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       ├── dto/
│   │   │       └── exception/
│   │   └── pom.xml
│   │
│   ├── calendar-service/      # Port 8084 - Google Calendar (Mock)
│   │   ├── src/
│   │   │   └── main/java/si/fri/prpo/calendarservice/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       ├── dto/
│   │   │       └── exception/
│   │   └── pom.xml
│   │
│   └── notification-service/  # Port 8085 - Email/SMS (Mock)
│       ├── src/
│       │   └── main/java/si/fri/prpo/notificationservice/
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
- [x] Payment-service (Stripe integration + Mock mode)
- [x] Calendar-service (Google Calendar integration + Mock mode)
- [x] Notification-service (Email/SMS notifications + Mock mode)
- [x] API documentation for all services
- [x] Error handling & validation

### In Progress 🔄

- [ ] Service integration (Booking → Payment → Calendar → Notification flow)
- [ ] Docker Compose update (add new services)
- [ ] API Gateway implementation

### Planned 🔜

- [ ] React frontend
- [ ] Kubernetes deployment
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Full integration testing

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
