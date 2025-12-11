# Calendar Service

Google Calendar integracija za Booking System - sinhronizacija rezervacij z Google Calendar.

## 🚀 Funkcionalnosti

- ✅ **Kreiranje dogodkov** - Avtomatsko dodajanje rezervacij v Google Calendar
- ✅ **Posodabljanje dogodkov** - Sync sprememb rezervacij
- ✅ **Brisanje dogodkov** - Odstranitev preklicanih rezervacij
- ✅ **Mock Mode** - Testiranje brez Google API credentials
- ✅ **Database Persistence** - Shranjanje v PostgreSQL
- ✅ **Error Handling** - Graceful degradation če Google API ni na voljo

## 📋 Predpogoji

- Java 17+
- PostgreSQL 15+
- Maven 3.8+
- Google Calendar API credentials (za production mode)

## ⚙️ Konfiguracija

### Application Properties

```properties
server.port=8084
google.calendar.mock-mode=true  # Set to false for real Google Calendar
```

###Mock Mode (Default)

Service deluje v **mock mode** po defaultu - ni potreben Google Account.

### Production Mode

Za pravo Google Calendar integracijo:

1. Ustvari Google Cloud projekt
2. Omogoči Google Calendar API
3. Ustvari OAuth2 credentials
4. Prenesi `credentials.json` v project root
5. Nastavi `google.calendar.mock-mode=false`

## 🏃 Zagon

```bash
# Build
.\mvnw.cmd clean package -DskipTests

# Run
.\mvnw.cmd spring-boot:run
```

Service bo dosegljiv na **http://localhost:8084**

## 📚 API Endpoints

### Health Check

```http
GET /api/calendar/health
```

### Create Event

```http
POST /api/calendar/events
Content-Type: application/json

{
  "bookingId": 1,
  "userId": 123,
  "facilityId": 5,
  "title": "Najem nogometnega igrišča",
  "description": "Rezervacija za 2 uri",
  "startTime": "2025-12-15T14:00:00",
  "endTime": "2025-12-15T16:00:00",
  "location": "Športni center Ljubljana"
}
```

### Get Event

```http
GET /api/calendar/events/{id}
GET /api/calendar/events/booking/{bookingId}
GET /api/calendar/events/user/{userId}
GET /api/calendar/events/user/{userId}/upcoming
```

### Update Event

```http
PUT /api/calendar/events/{id}
```

### Cancel/Delete Event

```http
POST /api/calendar/events/{id}/cancel
DELETE /api/calendar/events/{id}
```

## 🗄️ Database Schema

```sql
CREATE TABLE calendar_events (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    facility_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    location VARCHAR(500),
    google_event_id VARCHAR(255) UNIQUE,
    google_calendar_id VARCHAR(500),
    google_event_link VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    sync_status VARCHAR(20) NOT NULL,
    last_synced_at TIMESTAMP,
    sync_error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## 🧪 Testiranje

Mock mode omogoča testiranje brez Google API:

```bash
# Test create event
curl -X POST http://localhost:8084/api/calendar/events \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 1,
    "userId": 123,
    "facilityId": 5,
    "title": "Test Event",
    "startTime": "2025-12-15T14:00:00",
    "endTime": "2025-12-15T16:00:00"
  }'
```

## 📊 Status Values

### Event Status

- `SCHEDULED` - Dogodek načrtovan
- `COMPLETED` - Dogodek izveden
- `CANCELLED` - Dogodek preklican
- `RESCHEDULED` - Dogodek prestavljen

### Sync Status

- `PENDING` - Čaka na sinhronizacijo
- `SYNCED` - Uspešno sinhronizirano
- `FAILED` - Sinhronizacija neuspešna
- `MOCK` - Mock mode

## 🔗 Integracija z drugimi servisi

- **Booking Service** → Calendar Service (kreiranje dogodka po rezervaciji)
- **Payment Service** → Calendar Service (potrditev dogodka po plačilu)

## 📝 Logs

V mock mode boste videli:

```
MOCK: Created calendar event with ID: mock_event_1733950800000
MOCK Event details: Najem nogometnega igrišča - 2025-12-15T14:00 to 2025-12-15T16:00
```

## 🛠️ Troubleshooting

**Problem**: Google API error

- **Rešitev**: Preverite credentials.json ali preklopite na mock mode

**Problem**: Database connection error

- **Rešitev**: Preverite PostgreSQL connection v application.properties

**Problem**: Validation errors

- **Rešitev**: End time mora biti po start time, start time v prihodnosti
