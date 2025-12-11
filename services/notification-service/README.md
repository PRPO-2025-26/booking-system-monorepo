# Notification Service

Email and SMS notification service for the Booking System.

## Features

- ✅ Email notifications (SMTP)
- ✅ Multiple notification types (booking, payment, event reminders)
- ✅ Notification history tracking
- ✅ Retry failed notifications
- ✅ Mock mode for development/testing
- ✅ PostgreSQL storage

## Prerequisites

- Java 17+
- PostgreSQL database
- Maven 3.9+
- (Optional) SMTP server credentials for real email sending

## Configuration

### application.properties

```properties
# Server
server.port=8085

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/booking_system
spring.datasource.username=admin
spring.datasource.password=admin123

# Email SMTP
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Mock Mode (true = no actual emails sent)
notification.mock-mode=true
```

## API Endpoints

| Method | Endpoint                                 | Description               |
| ------ | ---------------------------------------- | ------------------------- |
| GET    | `/api/notifications/health`              | Health check              |
| POST   | `/api/notifications`                     | Send notification         |
| GET    | `/api/notifications/{id}`                | Get notification by ID    |
| GET    | `/api/notifications/user/{userId}`       | Get user notifications    |
| GET    | `/api/notifications/booking/{bookingId}` | Get booking notifications |
| GET    | `/api/notifications/payment/{paymentId}` | Get payment notifications |
| GET    | `/api/notifications/event/{eventId}`     | Get event notifications   |
| GET    | `/api/notifications`                     | Get all notifications     |
| POST   | `/api/notifications/{id}/retry`          | Retry failed notification |

## Usage

### Start Service

```bash
cd services/notification-service
./mvnw.cmd spring-boot:run
```

Or with pre-built JAR:

```bash
java -jar target/notification-service-0.0.1-SNAPSHOT.jar
```

### Send Notification

```bash
POST http://localhost:8085/api/notifications
Content-Type: application/json

{
  "userId": 456,
  "bookingId": 123,
  "type": "BOOKING_CONFIRMATION",
  "channel": "EMAIL",
  "recipient": "user@example.com",
  "subject": "Booking Confirmation",
  "content": "Your booking has been confirmed. Booking ID: 123"
}
```

## Notification Types

- `BOOKING_CONFIRMATION` - Booking created/confirmed
- `BOOKING_CANCELLATION` - Booking cancelled
- `BOOKING_REMINDER` - Upcoming booking reminder
- `PAYMENT_CONFIRMATION` - Payment successful
- `PAYMENT_FAILED` - Payment failed
- `PAYMENT_REFUND` - Payment refunded
- `EVENT_REMINDER` - Calendar event reminder
- `EVENT_CANCELLATION` - Event cancelled
- `EVENT_RESCHEDULED` - Event rescheduled
- `CUSTOM` - Custom notification

## Notification Channels

- `EMAIL` - Email notification (currently supported)
- `SMS` - SMS notification (future)
- `PUSH` - Push notification (future)
- `IN_APP` - In-app notification (future)

## Notification Status

- `PENDING` - Waiting to be sent
- `SENT` - Successfully sent (real mode)
- `FAILED` - Failed to send
- `MOCK` - Sent in mock mode

## Mock Mode

When `notification.mock-mode=true`:

- No actual emails are sent
- Logs notification details to console
- Status set to `MOCK`
- Useful for development and testing

To enable real email sending:

1. Set `notification.mock-mode=false`
2. Configure valid SMTP credentials
3. Restart service

## Database Schema

### notification_logs Table

| Column        | Type      | Description          |
| ------------- | --------- | -------------------- |
| id            | BIGINT    | Primary key          |
| user_id       | BIGINT    | User ID              |
| booking_id    | BIGINT    | Related booking      |
| payment_id    | BIGINT    | Related payment      |
| event_id      | BIGINT    | Related event        |
| type          | VARCHAR   | Notification type    |
| channel       | VARCHAR   | Notification channel |
| recipient     | VARCHAR   | Email/phone number   |
| subject       | VARCHAR   | Notification subject |
| content       | TEXT      | Notification content |
| status        | VARCHAR   | Send status          |
| sent_at       | TIMESTAMP | When sent            |
| error_message | VARCHAR   | Error if failed      |
| retry_count   | INT       | Number of retries    |
| created_at    | TIMESTAMP | Created timestamp    |
| updated_at    | TIMESTAMP | Updated timestamp    |

## Testing

```bash
# Run tests
./mvnw.cmd test

# Build without tests
./mvnw.cmd clean package -DskipTests
```

## Integration with Other Services

### From Booking Service

```java
// After booking creation
notificationService.sendBookingConfirmation(bookingId, userEmail);
```

### From Payment Service

```java
// After successful payment
notificationService.sendPaymentConfirmation(paymentId, userEmail);
```

### From Calendar Service

```java
// Before event
notificationService.sendEventReminder(eventId, userEmail);
```

## Troubleshooting

### Emails not sending in real mode

1. Check SMTP credentials
2. Enable "Less secure app access" for Gmail
3. Use App Password for Gmail (not account password)
4. Check firewall/network settings

### Database connection error

```
Error: password authentication failed
```

Solution: Check database credentials in `application.properties`

### Port already in use

```
Error: Port 8085 already in use
```

Solution: Change port or stop conflicting process

## License

PRPO 2025/26 - Faculty of Computer and Information Science, University of Ljubljana
