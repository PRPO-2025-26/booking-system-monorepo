# Payment Service API Documentation

## Overview

Payment Service handles all payment operations using Stripe integration. Supports creating checkout sessions, tracking payment status, and processing webhooks.

**Base URL:** `http://localhost:8083`  
**Port:** 8083  
**Database:** PostgreSQL (shared: `booking_system`)

---

## 🔑 API Endpoints

### 1. Create Payment Checkout

**Endpoint:** `POST /api/payments/checkout`

**Description:** Creates a new payment and Stripe checkout session.

**Request Body:**

```json
{
  "bookingId": 1,
  "userId": 1,
  "amount": 30.0,
  "currency": "EUR",
  "description": "Booking #1 - Football Field"
}
```

**Response:** `201 Created`

```json
{
  "id": 1,
  "bookingId": 1,
  "userId": 1,
  "amount": 30.0,
  "currency": "EUR",
  "status": "PENDING",
  "checkoutUrl": "https://checkout.stripe.com/c/pay/cs_test_...",
  "stripePaymentIntentId": "pi_...",
  "stripeCheckoutSessionId": "cs_test_...",
  "description": "Booking #1 - Football Field",
  "paymentMethod": null,
  "errorMessage": null,
  "createdAt": "2025-12-11T20:00:00",
  "updatedAt": "2025-12-11T20:00:00",
  "completedAt": null
}
```

---

### 2. Get Payment by ID

**Endpoint:** `GET /api/payments/{id}`

**Description:** Retrieves payment details by payment ID.

**Response:** `200 OK`

```json
{
  "id": 1,
  "bookingId": 1,
  "userId": 1,
  "amount": 30.0,
  "currency": "EUR",
  "status": "COMPLETED",
  "checkoutUrl": "https://checkout.stripe.com/...",
  "stripePaymentIntentId": "pi_...",
  "stripeCheckoutSessionId": "cs_test_...",
  "description": "Booking #1",
  "paymentMethod": "card",
  "errorMessage": null,
  "createdAt": "2025-12-11T20:00:00",
  "updatedAt": "2025-12-11T20:05:00",
  "completedAt": "2025-12-11T20:05:00"
}
```

---

### 3. Get Payment by Booking ID

**Endpoint:** `GET /api/payments/booking/{bookingId}`

**Description:** Retrieves payment for a specific booking.

**Response:** `200 OK` (same structure as above)

---

### 4. Get Payment Status

**Endpoint:** `GET /api/payments/{id}/status`

**Description:** Returns the current status of a payment.

**Response:** `200 OK`

```
COMPLETED
```

**Possible Statuses:**

- `PENDING` - Waiting for payment
- `PROCESSING` - Payment in progress
- `COMPLETED` - Payment successful
- `FAILED` - Payment failed
- `CANCELLED` - User cancelled
- `REFUNDED` - Refunded
- `PARTIALLY_REFUNDED` - Partially refunded

---

### 5. Get Payments by User ID

**Endpoint:** `GET /api/payments/user/{userId}`

**Description:** Retrieves all payments for a specific user.

**Response:** `200 OK`

```json
[
  {
    "id": 1,
    "bookingId": 1,
    "userId": 1,
    "amount": 30.00,
    "status": "COMPLETED",
    ...
  },
  {
    "id": 2,
    "bookingId": 5,
    "userId": 1,
    "amount": 45.00,
    "status": "PENDING",
    ...
  }
]
```

---

### 6. Get Completed Payments by User

**Endpoint:** `GET /api/payments/user/{userId}/completed`

**Description:** Retrieves only completed payments for a user.

**Response:** `200 OK` (array of completed payments)

---

### 7. Cancel Payment

**Endpoint:** `DELETE /api/payments/{id}`

**Description:** Cancels a pending payment (cannot cancel completed payments).

**Response:** `204 No Content`

---

### 8. Stripe Webhook

**Endpoint:** `POST /api/payments/webhook`

**Description:** Receives Stripe webhook events (checkout completed, payment failed, etc.).

**Headers:**

```
Stripe-Signature: t=...,v1=...
```

**Response:** `200 OK`

```
Webhook received
```

**Supported Events:**

- `checkout.session.completed` - Payment successful
- `checkout.session.expired` - Session expired
- `payment_intent.succeeded` - Payment intent succeeded
- `payment_intent.payment_failed` - Payment failed

---

### 9. Health Check

**Endpoint:** `GET /api/payments/health`

**Description:** Service health check.

**Response:** `200 OK`

```
Payment Service is running
```

---

## 🗄️ Database Schema

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
    completed_at TIMESTAMP,

    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED', 'PARTIALLY_REFUNDED'))
);

CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
```

---

## 🧪 Testing with Postman/cURL

### Create a Payment

```bash
curl -X POST http://localhost:8083/api/payments/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 1,
    "userId": 1,
    "amount": 30.00,
    "currency": "EUR",
    "description": "Football Field Booking"
  }'
```

### Check Payment Status

```bash
curl http://localhost:8083/api/payments/1/status
```

### Get User's Payments

```bash
curl http://localhost:8083/api/payments/user/1
```

---

## 🔧 Stripe Test Cards

Use these test cards in Stripe checkout:

| Card Number         | Description             |
| ------------------- | ----------------------- |
| 4242 4242 4242 4242 | Successful payment      |
| 4000 0000 0000 9995 | Payment declined        |
| 4000 0025 0000 3155 | Requires authentication |

**Expiry:** Any future date (e.g., 12/34)  
**CVC:** Any 3 digits (e.g., 123)  
**ZIP:** Any 5 digits (e.g., 12345)

---

## 🔗 Integration Flow

### Booking → Payment Flow

1. **User creates booking** in `booking-service`
2. **Booking-service calls** `POST /api/payments/checkout` with booking details
3. **Payment-service creates** Stripe checkout session and returns checkout URL
4. **User is redirected** to Stripe checkout page
5. **User completes payment** on Stripe
6. **Stripe sends webhook** to `/api/payments/webhook`
7. **Payment status updated** to `COMPLETED`
8. **Booking-service is notified** (future implementation)

---

## 📝 Configuration

### Environment Variables

```properties
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/booking_system
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin123

# Stripe
STRIPE_API_KEY=sk_test_your_stripe_test_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret
STRIPE_SUCCESS_URL=http://localhost:3000/payment/success
STRIPE_CANCEL_URL=http://localhost:3000/payment/cancel
```

### Get Stripe API Keys

1. Go to [https://dashboard.stripe.com/test/apikeys](https://dashboard.stripe.com/test/apikeys)
2. Copy **Secret key** (starts with `sk_test_`)
3. Configure webhook endpoint in Stripe Dashboard
4. Copy **Webhook signing secret** (starts with `whsec_`)

---

## ✅ Next Steps

- [ ] Integrate with booking-service for automatic payment creation
- [ ] Add refund functionality
- [ ] Implement payment notifications to users
- [ ] Add payment analytics/reporting
- [ ] Support multiple payment methods (SEPA, Klarna, etc.)
- [ ] Add payment retry logic for failed payments

---

## 🐛 Error Responses

### Payment Already Exists

```json
{
  "timestamp": "2025-12-11T20:00:00",
  "status": 400,
  "error": "Payment Error",
  "message": "Payment already exists for booking 1"
}
```

### Payment Not Found

```json
{
  "timestamp": "2025-12-11T20:00:00",
  "status": 400,
  "error": "Payment Error",
  "message": "Payment not found with ID: 999"
}
```

### Validation Error

```json
{
  "timestamp": "2025-12-11T20:00:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Invalid input data",
  "validationErrors": {
    "amount": "Amount must be greater than 0",
    "bookingId": "Booking ID is required"
  }
}
```
