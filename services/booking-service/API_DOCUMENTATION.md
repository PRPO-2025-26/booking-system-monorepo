# Booking Service API Documentation

## Overview

Booking Service upravlja z rezervacijami športnih objektov. Port: **8082**

## Features

- ✅ Ustvarjanje novih rezervacij
- ✅ Pregled vseh rezervacij uporabnika
- ✅ Pregled prihodnjih/preteklih rezervacij
- ✅ Posodabljanje statusa rezervacij
- ✅ Preklic rezervacij
- ✅ Pregled rezervacij po objektu
- ✅ Preprečevanje prekrivajočih se rezervacij
- ✅ Validacija časovnih okvirjev

## Booking Statuses

- `PENDING` - Rezervacija čaka na potrditev
- `CONFIRMED` - Rezervacija je potrjena
- `CANCELLED` - Rezervacija je preklicana
- `COMPLETED` - Rezervacija je zaključena

## API Endpoints

### 1. Create Booking

Ustvari novo rezervacijo za športni objekt.

**Request:**

```http
POST http://localhost:8082/api/bookings
Content-Type: application/json
X-User-Id: 1

{
  "facilityId": 1,
  "startTime": "2025-12-15T10:00:00",
  "endTime": "2025-12-15T12:00:00",
  "notes": "Team training session"
}
```

**Response (201 Created):**

```json
{
  "id": 1,
  "userId": 1,
  "facilityId": 1,
  "startTime": "2025-12-15T10:00:00",
  "endTime": "2025-12-15T12:00:00",
  "status": "PENDING",
  "totalPrice": 30.0,
  "notes": "Team training session",
  "createdAt": "2025-12-10T21:56:30",
  "updatedAt": "2025-12-10T21:56:30"
}
```

**Validation Rules:**

- `facilityId` - Required, must exist
- `startTime` - Required, must be in the future
- `endTime` - Required, must be after startTime
- Minimum duration: 1 hour
- Cannot overlap with existing bookings

---

### 2. Get My Bookings

Pridobi vse rezervacije prijavljenega uporabnika.

**Request:**

```http
GET http://localhost:8082/api/bookings/my
X-User-Id: 1
```

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "userId": 1,
    "facilityId": 1,
    "startTime": "2025-12-15T10:00:00",
    "endTime": "2025-12-15T12:00:00",
    "status": "PENDING",
    "totalPrice": 30.0,
    "notes": "Team training",
    "createdAt": "2025-12-10T21:56:30",
    "updatedAt": "2025-12-10T21:56:30"
  }
]
```

---

### 3. Get Upcoming Bookings

Pridobi samo prihodnje rezervacije uporabnika.

**Request:**

```http
GET http://localhost:8082/api/bookings/my/upcoming
X-User-Id: 1
```

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "userId": 1,
    "facilityId": 1,
    "startTime": "2025-12-15T10:00:00",
    "endTime": "2025-12-15T12:00:00",
    "status": "CONFIRMED",
    "totalPrice": 30.0,
    "notes": null,
    "createdAt": "2025-12-10T21:56:30",
    "updatedAt": "2025-12-10T21:56:30"
  }
]
```

---

### 4. Get Past Bookings

Pridobi pretekle rezervacije uporabnika.

**Request:**

```http
GET http://localhost:8082/api/bookings/my/past
X-User-Id: 1
```

**Response (200 OK):**

```json
[
  {
    "id": 2,
    "userId": 1,
    "facilityId": 2,
    "startTime": "2025-12-01T14:00:00",
    "endTime": "2025-12-01T16:00:00",
    "status": "COMPLETED",
    "totalPrice": 30.0,
    "notes": null,
    "createdAt": "2025-11-25T10:00:00",
    "updatedAt": "2025-12-01T16:05:00"
  }
]
```

---

### 5. Get Booking by ID

Pridobi podrobnosti določene rezervacije.

**Request:**

```http
GET http://localhost:8082/api/bookings/1
X-User-Id: 1
```

**Response (200 OK):**

```json
{
  "id": 1,
  "userId": 1,
  "facilityId": 1,
  "startTime": "2025-12-15T10:00:00",
  "endTime": "2025-12-15T12:00:00",
  "status": "PENDING",
  "totalPrice": 30.0,
  "notes": "Team training",
  "createdAt": "2025-12-10T21:56:30",
  "updatedAt": "2025-12-10T21:56:30"
}
```

**Error (403 Forbidden):**

```json
{
  "timestamp": "2025-12-10T21:56:30",
  "status": 403,
  "error": "Conflict",
  "message": "You are not authorized to view this booking"
}
```

---

### 6. Get Bookings by Facility

Pridobi vse rezervacije za določen športni objekt.

**Request:**

```http
GET http://localhost:8082/api/bookings/facility/1
```

**Response (200 OK):**

```json
[
  {
    "id": 1,
    "userId": 1,
    "facilityId": 1,
    "startTime": "2025-12-15T10:00:00",
    "endTime": "2025-12-15T12:00:00",
    "status": "CONFIRMED",
    "totalPrice": 30.0,
    "notes": null,
    "createdAt": "2025-12-10T21:56:30",
    "updatedAt": "2025-12-10T21:56:30"
  },
  {
    "id": 3,
    "userId": 2,
    "facilityId": 1,
    "startTime": "2025-12-15T14:00:00",
    "endTime": "2025-12-15T16:00:00",
    "status": "PENDING",
    "totalPrice": 30.0,
    "notes": "Birthday party",
    "createdAt": "2025-12-10T22:00:00",
    "updatedAt": "2025-12-10T22:00:00"
  }
]
```

---

### 7. Update Booking Status

Posodobi status rezervacije (PENDING → CONFIRMED, CONFIRMED → COMPLETED, itd.).

**Request:**

```http
PATCH http://localhost:8082/api/bookings/1/status
Content-Type: application/json
X-User-Id: 1

{
  "status": "CONFIRMED"
}
```

**Response (200 OK):**

```json
{
  "id": 1,
  "userId": 1,
  "facilityId": 1,
  "startTime": "2025-12-15T10:00:00",
  "endTime": "2025-12-15T12:00:00",
  "status": "CONFIRMED",
  "totalPrice": 30.0,
  "notes": "Team training",
  "createdAt": "2025-12-10T21:56:30",
  "updatedAt": "2025-12-10T22:05:00"
}
```

**Valid Status Transitions:**

- `PENDING` → `CONFIRMED`, `CANCELLED`
- `CONFIRMED` → `COMPLETED`, `CANCELLED`
- `CANCELLED` → ❌ No changes allowed
- `COMPLETED` → ❌ No changes allowed

---

### 8. Cancel Booking

Prekliči rezervacijo (soft delete - status se nastavi na CANCELLED).

**Request:**

```http
DELETE http://localhost:8082/api/bookings/1
X-User-Id: 1
```

**Response (204 No Content):**

```
(Empty response body)
```

**Cancellation Rules:**

- ✅ Lahko prekličeš samo svoje rezervacije
- ✅ Samo prihodnje rezervacije lahko prekličeš
- ❌ Ne moreš preklicati že preklicanih rezervacij
- ❌ Ne moreš preklicati že zaključenih rezervacij

---

## Error Responses

### 400 Bad Request - Validation Error

```json
{
  "timestamp": "2025-12-10T22:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Start time must be in the future"
}
```

### 409 Conflict - Booking Overlap

```json
{
  "timestamp": "2025-12-10T22:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Facility is not available at the selected time"
}
```

### 404 Not Found

```json
{
  "timestamp": "2025-12-10T22:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Booking not found"
}
```

---

## Business Logic

### Pricing Calculation

- **Fixed rate:** 15.00 EUR/hour
- Duration is calculated from `startTime` to `endTime`
- Example: 2 hours = 30.00 EUR

_(Future enhancement: Fetch dynamic pricing from facility-service)_

### Conflict Detection

The system prevents double-booking by checking for overlapping reservations:

- Query checks if any PENDING or CONFIRMED booking exists for the same facility
- Where the time ranges overlap: `(startTime < existingEndTime AND endTime > existingStartTime)`

### Minimum Booking Duration

- Minimum: **1 hour**
- System validates that `Duration.between(startTime, endTime).toHours() >= 1`

---

## Testing Flow

1. **Start services:**

   - Auth-service: Port 8080
   - Facility-service: Port 8081
   - Booking-service: Port 8082

2. **Create user:** POST to auth-service `/api/auth/register`

3. **Create facility:** POST to facility-service `/api/facilities`

4. **Create booking:** POST to booking-service `/api/bookings`

5. **View bookings:** GET `/api/bookings/my`

6. **Confirm booking:** PATCH `/api/bookings/1/status` with `{"status": "CONFIRMED"}`

7. **Test conflict:** Try creating overlapping booking (should fail with 409)

---

## Database Schema

```sql
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    facility_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    total_price DECIMAL(10,2) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

---

## Next Steps

- [ ] Integrate with facility-service to fetch dynamic pricing
- [ ] Add JWT authentication (replace X-User-Id header)
- [ ] Implement payment integration
- [ ] Add email notifications for booking confirmation
- [ ] Add calendar export functionality (iCal format)
