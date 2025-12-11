# Payment Service 💳

Mikrostoritev za upravljanje plačil z integracijo Stripe payment gateway.

## 🎯 Funkcionalnosti

- ✅ Kreiranje Stripe checkout session
- ✅ Sledenje statusu plačil
- ✅ Webhook integracija za avtomatsko posodabljanje
- ✅ Podpora za test in produkcijska okolja
- ✅ Multiple payment methods (cards, SEPA, etc.)
- ✅ Payment history tracking
- ✅ Refund support (planned)

## 🚀 Quick Start

### 1. Zagon s PostgreSQL

Prepričajte se, da PostgreSQL teče:

```bash
# Docker Compose iz root direktorija
cd c:\Users\Administrator\Documents\PRPO\booking-system-monorepo
docker-compose up -d postgres
```

### 2. Konfiguracija Stripe

1. Registrirajte se na [Stripe](https://dashboard.stripe.com/register)
2. V `src/main/resources/application.properties` zamenjajte:
   - `stripe.api.key` z vašim test API key
   - `stripe.webhook.secret` z webhook secret (če uporabljate webhooks)

### 3. Zagon Aplikacije

```bash
cd services/payment-service
mvnw spring-boot:run
```

Aplikacija bo dostopna na `http://localhost:8083`

## 📖 API Dokumentacija

Celotna API dokumentacija je na voljo v [`API_DOCUMENTATION.md`](./API_DOCUMENTATION.md)

### Osnovni Primeri

**Ustvari plačilo:**

```bash
curl -X POST http://localhost:8083/api/payments/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 1,
    "userId": 1,
    "amount": 30.00,
    "currency": "EUR",
    "description": "Booking #1"
  }'
```

**Preveri status:**

```bash
curl http://localhost:8083/api/payments/1/status
```

## 🗄️ Database Schema

Tabela `payments` se avtomatsko ustvari ob zagonu (Hibernate DDL auto).

```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    status VARCHAR(20) NOT NULL,
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    stripe_checkout_session_id VARCHAR(255) UNIQUE,
    checkout_url VARCHAR(500),
    description VARCHAR(500),
    payment_method VARCHAR(50),
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);
```

## 🔧 Konfiguracija

### application.properties

```properties
# Server
server.port=8083

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/booking_system
spring.datasource.username=admin
spring.datasource.password=admin123

# Stripe
stripe.api.key=sk_test_YOUR_KEY
stripe.webhook.secret=whsec_YOUR_SECRET
stripe.success.url=http://localhost:3000/payment/success
stripe.cancel.url=http://localhost:3000/payment/cancel
```

## 🧪 Testing

### Test Stripe Cards

| Card Number         | Result        |
| ------------------- | ------------- |
| 4242 4242 4242 4242 | Success       |
| 4000 0000 0000 9995 | Declined      |
| 4000 0025 0000 3155 | Requires auth |

## 🔗 Integracija z ostalimi servisi

### booking-service → payment-service

```java
// V booking-service, po uspešni kreaciji rezervacije
PaymentRequest paymentRequest = PaymentRequest.builder()
    .bookingId(booking.getId())
    .userId(userId)
    .amount(booking.getTotalPrice())
    .currency("EUR")
    .description("Booking #" + booking.getId())
    .build();

// REST call na payment-service
String checkoutUrl = restTemplate.postForObject(
    "http://localhost:8083/api/payments/checkout",
    paymentRequest,
    PaymentResponse.class
).getCheckoutUrl();

// Preusmeri uporabnika na Stripe checkout
return checkoutUrl;
```

## 📁 Struktura Projekta

```
payment-service/
├── src/main/java/si/fri/prpo/paymentservice/
│   ├── config/
│   │   └── StripeConfig.java           # Stripe inicializacija
│   ├── controller/
│   │   ├── PaymentController.java      # REST endpoints
│   │   └── WebhookController.java      # Stripe webhooks
│   ├── dto/
│   │   ├── PaymentRequest.java
│   │   ├── PaymentResponse.java
│   │   └── StripeWebhookEvent.java
│   ├── entity/
│   │   └── Payment.java                # JPA entiteta
│   ├── exception/
│   │   ├── PaymentException.java
│   │   ├── ErrorResponse.java
│   │   └── GlobalExceptionHandler.java
│   ├── repository/
│   │   └── PaymentRepository.java      # Spring Data JPA
│   ├── service/
│   │   ├── PaymentService.java         # Business logika
│   │   └── StripeService.java          # Stripe API calls
│   └── PaymentServiceApplication.java
└── src/main/resources/
    └── application.properties
```

## 🐳 Docker

### Dockerfile (prihodnost)

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/payment-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build

```bash
mvn clean package
docker build -t payment-service:latest .
```

## 📊 Monitoring

Health check endpoint:

```bash
curl http://localhost:8083/api/payments/health
```

## 🔐 Varnost

- ⚠️ **NIKOLI** ne commitajte production Stripe keys v Git
- Uporabljajte environment variables za občutljive podatke
- Webhook signature verification je vključen za varnost
- JWT avtentikacija bo dodana v prihodnosti

## 🚧 Next Steps

- [ ] Dodaj JWT avtentikacijo
- [ ] Implementiraj refund funkcionalnost
- [ ] Dodaj event publishing (RabbitMQ/Kafka) za obveščanje booking-service
- [ ] Dodaj email obvestila po plačilu
- [ ] Implementiraj retry logiko za failed payments
- [ ] Dodaj payment analytics

## 📝 Dependencies

- Spring Boot 3.4.12
- Spring Data JPA
- PostgreSQL Driver
- Lombok
- Stripe Java SDK 24.18.0
- Spring Validation

## 👤 Avtor

OneManBand (ut4228)

## 📄 Licenca

Educational project - PRPO 2025/26
