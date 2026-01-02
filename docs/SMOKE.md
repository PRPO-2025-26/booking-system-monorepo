# API Smoke Examples

Base URLs (docker-compose defaults):

- Auth: http://localhost:8080
- Facility: http://localhost:8081
- Booking: http://localhost:8082
- Payment: http://localhost:8083
- Calendar: http://localhost:8084
- Notification: http://localhost:8085

## 1) Auth

### Login (seed user `alice@example.com` / `password`)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password"}'
```

### Register (new user)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"charlie","email":"charlie@example.com","password":"password"}'
```

PowerShell-friendly (avoids line breaks/escaping issues):

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/register `
  -ContentType 'application/json' `
  -Body '{"username":"charlie","email":"charlie@example.com","password":"password"}'
```

## 2) Facilities

### List facilities

```bash
curl http://localhost:8081/api/facilities
```

PowerShell:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8081/api/facilities
```

### Create facility (uses `X-User-Id` header, defaults to owner 1 if omitted)

```bash
curl -X POST http://localhost:8081/api/facilities \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "name":"Workshop Room B",
    "type":"MEETING_ROOM",
    "address":"123 Center St, Ljubljana",
    "description":"Cozy room with whiteboard",
    "capacity":12,
    "pricePerHour":35.0,
    "available":true
  }'
```

PowerShell-friendly:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8081/api/facilities `
  -ContentType 'application/json' `
  -Headers @{ "X-User-Id" = "1" } `
  -Body '{"name":"Workshop Room B","type":"MEETING_ROOM","address":"123 Center St, Ljubljana","description":"Cozy room with whiteboard","capacity":12,"pricePerHour":35.0,"available":true}'
```

## 3) Bookings

_Booking endpoints require `X-User-Id`._

### Create booking (user 2 = alice) for facility 1

```bash
curl -X POST http://localhost:8082/api/bookings \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 2" \
  -d '{
    "facilityId":1,
    "startTime":"2026-01-05T10:00:00",
    "endTime":"2026-01-05T12:00:00",
    "notes":"Team sync"
  }'
```

PowerShell:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8082/api/bookings `
  -ContentType 'application/json' `
  -Headers @{ "X-User-Id" = "2" } `
  -Body '{"facilityId":1,"startTime":"2026-01-05T10:00:00","endTime":"2026-01-05T12:00:00","notes":"Team sync"}'
```

### List my bookings

```bash
curl -H "X-User-Id: 2" http://localhost:8082/api/bookings/my
```

PowerShell:

```powershell
Invoke-RestMethod -Method Get `
  -Uri http://localhost:8082/api/bookings/my `
  -Headers @{ "X-User-Id" = "2" }
```

### Get bookings for a facility

```bash
curl http://localhost:8082/api/bookings/facility/1
```

PowerShell:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8082/api/bookings/facility/1
```

## 4) Payments

### Create payment (checkout) for a booking

ℹ️ A payment can be created only if that booking has no existing payment. Seed data already includes payments for booking IDs 1 and 2. Create a new booking first (e.g., booking 3) and use that ID below.

```bash
curl -X POST http://localhost:8083/api/payments/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId":3,
    "userId":2,
    "amount":300.00,
    "currency":"EUR",
    "description":"Payment for Main Hall booking"
  }'
```

PowerShell:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8083/api/payments/checkout `
  -ContentType 'application/json' `
  -Body '{"bookingId":3,"userId":2,"amount":300.00,"currency":"EUR","description":"Payment for Main Hall booking"}'
```

### Payment status by ID

```bash
curl http://localhost:8083/api/payments/1/status
```

PowerShell:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8083/api/payments/3/status
```

## 5) Calendar

### List calendar events (seeded IDs 1,2)

```bash
curl http://localhost:8084/api/calendar
```

PowerShell:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8084/api/calendar
```

## 6) Notifications

### Get notification by ID (seeded IDs 1,2)

```bash
curl http://localhost:8085/api/notifications/1
```

PowerShell:

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8085/api/notifications/1
```

## 7) Health checks

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
```

PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8082/actuator/health
Invoke-RestMethod http://localhost:8083/actuator/health
Invoke-RestMethod http://localhost:8084/actuator/health
Invoke-RestMethod http://localhost:8085/actuator/health
```

### Happy-path flow (order)

1. **Login**: use `alice/password` to verify auth.
2. **Create facility (optional)** or use seeded facility 1.
3. **Create booking**: user 2, facility 1; note the returned `id` (e.g., 3).
4. **Create payment**: use the new booking `id` (not 1 or 2) in checkout.
5. **Check calendar**: `GET /api/calendar` should list events (seeded + any new if you add them later).
6. **Check notifications**: fetch seeded notification 1.
7. **Health**: verify all `/actuator/health` endpoints are 200.

Notes:

- Headers `X-User-Id` are used by booking and facility flows for authorization context.
- Seeded sample data: users (admin/alice/bob), facilities (1=Main Hall, 2=Conference Room A), bookings (1,2), payments (1,2), calendar events (1,2), notification logs (1,2).
- Timestamps in create requests must be future ISO-8601 (e.g., `YYYY-MM-DDTHH:MM:SS`).
