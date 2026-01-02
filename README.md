# Booking System Monorepo

[![CI/CD](https://github.com/PRPO-2025-26/booking-system-monorepo/actions/workflows/ci.yml/badge.svg)](https://github.com/PRPO-2025-26/booking-system-monorepo/actions/workflows/ci.yml)

Mikrostoritveni sistem za rezervacijo športnih objektov: Spring Boot (Java 17), React frontend, PostgreSQL, Docker/Kubernetes (GKE), CI/CD na GitHub Actions.

---

## 🏗️ Arhitektura (hitra tabela)

| Storitev             | Port | Namen                                     |
| -------------------- | ---- | ----------------------------------------- |
| auth-service         | 8080 | Avtentikacija, JWT                        |
| facility-service     | 8081 | Upravljanje športnih objektov             |
| booking-service      | 8082 | Rezervacije, orkestracija drugih storitev |
| payment-service      | 8083 | Mock plačila in webhooki                  |
| calendar-service     | 8084 | Google Calendar integracija (demo)        |
| notification-service | 8085 | Mock obvestila (email/SMS)                |
| frontend             | 80   | React UI (Nginx)                          |

Infrastruktura: PostgreSQL 15 (PVC), GKE namespace `bookig`, ingress `booking.34.107.164.168.nip.io`, Artifact Registry `europe-west1-docker.pkg.dev/.../booking`.

---

## 🌐 API & Swagger

- Auth: `http://booking.34.107.164.168.nip.io/auth/swagger-ui.html`
- Facility: `http://booking.34.107.164.168.nip.io/facility/swagger-ui.html`
- Booking: `http://booking.34.107.164.168.nip.io/booking/swagger-ui.html`
- Payment: `http://booking.34.107.164.168.nip.io/payment/swagger-ui.html`
- Calendar: `http://booking.34.107.164.168.nip.io/calendar/swagger-ui.html`
- Notification: `http://booking.34.107.164.168.nip.io/notification/swagger-ui.html`

Zunanji API demo (booking-service): `GET /api/bookings/external/auth-check` (delegira na `external.api.url` z bearer tokenom).

---

## 🚀 Lokalni zagon (osnovni koraki)

Predpogoji: Java 17, Maven 3.9+, Node 20+ (frontend), Docker za lokalno bazo.

1. Baza:

```bash
docker-compose up -d postgres
```

2. Servisi (ločeni terminali, primer):

```bash
cd services/auth-service && ./mvnw spring-boot:run
cd services/facility-service && ./mvnw spring-boot:run
cd services/booking-service && ./mvnw spring-boot:run
cd services/payment-service && ./mvnw spring-boot:run
cd services/calendar-service && ./mvnw spring-boot:run
cd services/notification-service && ./mvnw spring-boot:run
```

3. Frontend (dev):

```bash
cd client
npm install
npm run dev
```

Podrobnejše API opise glej v `services/*/API_DOCUMENTATION.md` tam, kjer obstaja.

---

## ☸️ Kubernetes (GKE)

- Manifesti: `infra/k8s/bookig.yaml` (Deployments/Services/Ingress), `infra/k8s/hpa.yaml` (HPA 1–3 replike, 70% CPU za auth/facility/booking/payment/calendar/notification)
- Namespace: `bookig`
- Ingress: `http://booking.34.107.164.168.nip.io/`

Ročni deploy (če ne uporabljaš CI/CD):

```bash
kubectl apply -f infra/k8s/bookig.yaml -n bookig
kubectl apply -f infra/k8s/hpa.yaml -n bookig
kubectl rollout status deployment/frontend -n bookig
```

### Konfiguracija (env/Secret)

- DB: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- JWT: `JWT_SECRET` (auth-service)
- External API: `EXTERNAL_API_URL`, `EXTERNAL_API_TOKEN` (booking-service)
- Optional per-service overrides: payment/calendar/notification URLs (`SERVICES_*`)

V K8s nastavi kot Secret/ConfigMap in referenciraj v `bookig.yaml`.

---

## 🔄 CI/CD (GitHub Actions)

- Workflow: `.github/workflows/ci.yml`
- Test: `./mvnw -pl services/auth-service -am test` (ponovi za facility, booking)
- Build & Push: Docker slike `auth/facility/booking/payment/calendar/notification/frontend` → GAR `booking`
- Deploy: `kubectl apply -f infra/k8s/bookig.yaml -n bookig` + `kubectl apply -f infra/k8s/hpa.yaml -n bookig`

Zahtevani GitHub Secrets: `GCP_SA_KEY`, `GCP_PROJECT`, `GKE_CLUSTER`, `GKE_LOCATION`.

---

## 🧪 Hitri cURL testi

```bash
# Health (primer booking-service)
curl -I http://booking.34.107.164.168.nip.io/booking/actuator/health

# Zunanji API check (zahteva token v podu)
curl http://booking.34.107.164.168.nip.io/booking/api/bookings/external/auth-check
```

---

## 📝 Opombe

- Zunanja integracija: booking-service → `external.api.url` z bearer tokenom.
- Skaliranje: HPAs za auth/facility/booking/payment/calendar/notification (1–3 replike, 70% CPU), Ingress na GCE LB.
- CI/CD: test → build/push GAR → deploy na GKE na `main`.

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
- [x] **Service integration** (Booking → Payment → Calendar → Notification flow)
- [x] API documentation for all services
- [x] Error handling & validation

### In Progress 🔄

- [ ] Docker Compose update (add all 6 services)
- [ ] API Gateway implementation
- [ ] Payment webhooks (Stripe callbacks)

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
