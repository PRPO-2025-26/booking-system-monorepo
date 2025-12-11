# Calendar Service - API Documentation

## Base Information

- **Service Name**: Calendar Service
- **Base URL**: `http://localhost:8084/api/calendar`
- **Version**: 0.0.1-SNAPSHOT
- **Description**: Google Calendar integration service for booking system

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Endpoints](#endpoints)
4. [Data Models](#data-models)
5. [Error Handling](#error-handling)
6. [Mock Mode](#mock-mode)

---

## Overview

The Calendar Service provides Google Calendar integration for the booking system. It automatically syncs booking events to Google Calendar and supports full CRUD operations on calendar events.

### Features

- ✅ Create calendar events from bookings
- ✅ Update existing calendar events
- ✅ Cancel and delete events
- ✅ Query events by user, booking, or date range
- ✅ Google Calendar synchronization
- ✅ Mock mode for development/testing
- ✅ Automatic conflict detection

---

## Authentication

Currently, the service does not require authentication. In production, you should implement:

- JWT token validation
- OAuth2 integration
- User role-based access control

---

## Endpoints

### 1. Health Check

**GET** `/health`

Check if the service is running.

**Response:**

```json
{
  "status": "UP",
  "service": "calendar-service",
  "timestamp": "2025-12-11T20:00:00"
}
```

---

### 2. Create Calendar Event

**POST** `/events`

Create a new calendar event and sync to Google Calendar.

**Request Body:**

```json
{
  "bookingId": 123,
  "userId": 456,
  "facilityId": 789,
  "title": "Tennis Court Booking",
  "description": "Regular tennis court booking for doubles match",
  "location": "Sports Center - Court 1",
  "startTime": "2025-12-15T14:00:00",
  "endTime": "2025-12-15T16:00:00"
}
```

**Field Validations:**

- `bookingId`: **Required**, Long
- `userId`: **Required**, Long
- `facilityId`: **Required**, Long
- `title`: **Required**, String (max 200 chars)
- `description`: Optional, String (max 1000 chars)
- `location`: Optional, String (max 500 chars)
- `startTime`: **Required**, ISO 8601 datetime
- `endTime`: **Required**, ISO 8601 datetime (must be after startTime)

**Response:** `201 Created`

```json
{
  "id": 1,
  "bookingId": 123,
  "userId": 456,
  "facilityId": 789,
  "title": "Tennis Court Booking",
  "description": "Regular tennis court booking for doubles match",
  "location": "Sports Center - Court 1",
  "startTime": "2025-12-15T14:00:00",
  "endTime": "2025-12-15T16:00:00",
  "status": "SCHEDULED",
  "syncStatus": "MOCK",
  "googleEventId": "mock_event_1734123456789",
  "googleCalendarId": "primary",
  "googleEventLink": "https://calendar.google.com/mock/event/1",
  "createdAt": "2025-12-11T20:00:00",
  "updatedAt": "2025-12-11T20:00:00",
  "lastSyncedAt": "2025-12-11T20:00:00"
}
```

**Error Responses:**

- `400 Bad Request`: Invalid input data
- `409 Conflict`: Event already exists for booking
- `500 Internal Server Error`: Google Calendar API error

---

### 3. Get Event by ID

**GET** `/events/{id}`

Retrieve a calendar event by its ID.

**Path Parameters:**

- `id` (Long): Event ID

**Response:** `200 OK`

```json
{
  "id": 1,
  "bookingId": 123,
  "userId": 456,
  "facilityId": 789,
  "title": "Tennis Court Booking",
  "description": "Regular tennis court booking for doubles match",
  "location": "Sports Center - Court 1",
  "startTime": "2025-12-15T14:00:00",
  "endTime": "2025-12-15T16:00:00",
  "status": "SCHEDULED",
  "syncStatus": "MOCK",
  "googleEventId": "mock_event_1734123456789",
  "googleCalendarId": "primary",
  "googleEventLink": "https://calendar.google.com/mock/event/1",
  "createdAt": "2025-12-11T20:00:00",
  "updatedAt": "2025-12-11T20:00:00",
  "lastSyncedAt": "2025-12-11T20:00:00"
}
```

**Error Responses:**

- `404 Not Found`: Event not found

---

### 4. Get Event by Booking ID

**GET** `/events/booking/{bookingId}`

Retrieve a calendar event associated with a specific booking.

**Path Parameters:**

- `bookingId` (Long): Booking ID

**Response:** `200 OK`

```json
{
  "id": 1,
  "bookingId": 123,
  "userId": 456,
  ...
}
```

**Error Responses:**

- `404 Not Found`: Event not found for booking

---

### 5. Get Events by User

**GET** `/events/user/{userId}`

Retrieve all calendar events for a specific user.

**Path Parameters:**

- `userId` (Long): User ID

**Response:** `200 OK`

```json
[
  {
    "id": 1,
    "bookingId": 123,
    "userId": 456,
    "title": "Tennis Court Booking",
    ...
  },
  {
    "id": 2,
    "bookingId": 124,
    "userId": 456,
    "title": "Basketball Court Booking",
    ...
  }
]
```

---

### 6. Get Upcoming Events for User

**GET** `/events/user/{userId}/upcoming`

Retrieve upcoming events for a user (next 7 days by default).

**Path Parameters:**

- `userId` (Long): User ID

**Query Parameters:**

- `days` (Integer, optional): Number of days to look ahead (default: 7)

**Example:**

```
GET /events/user/456/upcoming?days=14
```

**Response:** `200 OK`

```json
[
  {
    "id": 1,
    "bookingId": 123,
    "userId": 456,
    "title": "Tennis Court Booking",
    "startTime": "2025-12-15T14:00:00",
    "endTime": "2025-12-15T16:00:00",
    "status": "SCHEDULED",
    ...
  }
]
```

---

### 7. Update Event

**PUT** `/events/{id}`

Update an existing calendar event. Only provided fields will be updated.

**Path Parameters:**

- `id` (Long): Event ID

**Request Body:**

```json
{
  "title": "Updated Tennis Court Booking",
  "description": "Updated: Tennis court booking with coach",
  "location": "Sports Center - Court 2",
  "startTime": "2025-12-15T15:00:00",
  "endTime": "2025-12-15T17:00:00"
}
```

**All Fields Optional:**

- `title`: String (max 200 chars)
- `description`: String (max 1000 chars)
- `location`: String (max 500 chars)
- `startTime`: ISO 8601 datetime
- `endTime`: ISO 8601 datetime

**Response:** `200 OK`

```json
{
  "id": 1,
  "bookingId": 123,
  "userId": 456,
  "facilityId": 789,
  "title": "Updated Tennis Court Booking",
  "description": "Updated: Tennis court booking with coach",
  "location": "Sports Center - Court 2",
  "startTime": "2025-12-15T15:00:00",
  "endTime": "2025-12-15T17:00:00",
  "status": "SCHEDULED",
  "syncStatus": "MOCK",
  "updatedAt": "2025-12-11T20:30:00",
  "lastSyncedAt": "2025-12-11T20:30:00",
  ...
}
```

**Error Responses:**

- `400 Bad Request`: Invalid input data
- `404 Not Found`: Event not found
- `500 Internal Server Error`: Google Calendar sync error

---

### 8. Cancel Event

**POST** `/events/{id}/cancel`

Cancel a calendar event (marks as CANCELLED and removes from Google Calendar).

**Path Parameters:**

- `id` (Long): Event ID

**Response:** `200 OK`

```json
{
  "id": 1,
  "bookingId": 123,
  "userId": 456,
  "title": "Tennis Court Booking",
  "status": "CANCELLED",
  "syncStatus": "MOCK",
  "updatedAt": "2025-12-11T21:00:00",
  ...
}
```

**Error Responses:**

- `404 Not Found`: Event not found
- `500 Internal Server Error`: Google Calendar error

---

### 9. Delete Event

**DELETE** `/events/{id}`

Permanently delete a calendar event from database and Google Calendar.

**Path Parameters:**

- `id` (Long): Event ID

**Response:** `204 No Content`

**Error Responses:**

- `404 Not Found`: Event not found
- `500 Internal Server Error`: Google Calendar error

---

## Data Models

### EventRequest

```typescript
{
  bookingId: number;        // Required
  userId: number;           // Required
  facilityId: number;       // Required
  title: string;            // Required, max 200 chars
  description?: string;     // Optional, max 1000 chars
  location?: string;        // Optional, max 500 chars
  startTime: string;        // Required, ISO 8601 datetime
  endTime: string;          // Required, ISO 8601 datetime
}
```

### EventResponse

```typescript
{
  id: number;
  bookingId: number;
  userId: number;
  facilityId: number;
  title: string;
  description: string | null;
  location: string | null;
  startTime: string; // ISO 8601 datetime
  endTime: string; // ISO 8601 datetime
  status: EventStatus; // SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED
  syncStatus: SyncStatus; // PENDING, SYNCED, FAILED, MOCK
  googleEventId: string | null;
  googleCalendarId: string | null;
  googleEventLink: string | null;
  syncErrorMessage: string | null;
  createdAt: string; // ISO 8601 datetime
  updatedAt: string; // ISO 8601 datetime
  lastSyncedAt: string | null; // ISO 8601 datetime
}
```

### EventStatus Enum

- `SCHEDULED`: Event is scheduled to happen
- `COMPLETED`: Event has been completed
- `CANCELLED`: Event has been cancelled
- `RESCHEDULED`: Event has been rescheduled to a different time

### SyncStatus Enum

- `PENDING`: Waiting for Google Calendar sync
- `SYNCED`: Successfully synced with Google Calendar
- `FAILED`: Failed to sync with Google Calendar
- `MOCK`: Running in mock mode (no actual Google Calendar sync)

---

## Error Handling

### Error Response Format

```json
{
  "timestamp": "2025-12-11T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "End time must be after start time",
  "path": "/api/calendar/events"
}
```

### HTTP Status Codes

| Status Code | Description                                |
| ----------- | ------------------------------------------ |
| 200         | OK - Request succeeded                     |
| 201         | Created - Resource created successfully    |
| 204         | No Content - Resource deleted successfully |
| 400         | Bad Request - Invalid input data           |
| 404         | Not Found - Resource not found             |
| 409         | Conflict - Resource already exists         |
| 500         | Internal Server Error - Server error       |

### Common Error Messages

| Error            | Message                                    |
| ---------------- | ------------------------------------------ |
| Validation Error | "Title is required"                        |
| Validation Error | "End time must be after start time"        |
| Conflict         | "Event already exists for this booking"    |
| Not Found        | "Event not found with id: 123"             |
| Not Found        | "Event not found for booking: 456"         |
| Sync Error       | "Failed to sync with Google Calendar: ..." |

---

## Mock Mode

### Configuration

Mock mode is enabled by default in `application.properties`:

```properties
google.calendar.mock-mode=true
```

### Mock Mode Behavior

When running in mock mode:

1. **No Google API Calls**: No actual calls to Google Calendar API
2. **Mock Event IDs**: Generated with prefix `mock_event_{timestamp}`
3. **Mock Calendar ID**: Uses `"primary"` as calendar ID
4. **Mock Event Links**: Returns fake Google Calendar links
5. **Sync Status**: All events marked as `MOCK` sync status
6. **Logging**: Console logs show "MOCK:" prefix for all operations

### Mock vs Real Mode

| Feature              | Mock Mode      | Real Mode                 |
| -------------------- | -------------- | ------------------------- |
| Google API Calls     | ❌ No          | ✅ Yes                    |
| Event IDs            | `mock_event_*` | Real Google IDs           |
| Sync Status          | `MOCK`         | `SYNCED`/`FAILED`         |
| Credentials Required | ❌ No          | ✅ Yes                    |
| Event Links          | Fake URLs      | Real Google Calendar URLs |

### Switching to Real Mode

To enable real Google Calendar integration:

1. Obtain Google Calendar API credentials
2. Place `credentials.json` in project root
3. Set `google.calendar.mock-mode=false` in `application.properties`
4. Restart the service

---

## Example Workflows

### Complete Event Lifecycle

```bash
# 1. Create Event
POST /events
{
  "bookingId": 123,
  "userId": 456,
  "facilityId": 789,
  "title": "Tennis Court Booking",
  "startTime": "2025-12-15T14:00:00",
  "endTime": "2025-12-15T16:00:00"
}
# Response: { "id": 1, "status": "SCHEDULED", ... }

# 2. Get Event
GET /events/1
# Response: { "id": 1, "status": "SCHEDULED", ... }

# 3. Update Event
PUT /events/1
{
  "title": "Updated Tennis Court Booking",
  "startTime": "2025-12-15T15:00:00"
}
# Response: { "id": 1, "title": "Updated...", ... }

# 4. Get User Events
GET /events/user/456
# Response: [{ "id": 1, ... }]

# 5. Cancel Event
POST /events/1/cancel
# Response: { "id": 1, "status": "CANCELLED", ... }

# 6. Delete Event
DELETE /events/1
# Response: 204 No Content
```

---

## Database Schema

### calendar_events Table

| Column             | Type          | Constraints                 |
| ------------------ | ------------- | --------------------------- |
| id                 | BIGINT        | PRIMARY KEY, AUTO_INCREMENT |
| booking_id         | BIGINT        | NOT NULL                    |
| user_id            | BIGINT        | NOT NULL                    |
| facility_id        | BIGINT        | NOT NULL                    |
| title              | VARCHAR(200)  | NOT NULL                    |
| description        | VARCHAR(1000) | NULL                        |
| location           | VARCHAR(500)  | NULL                        |
| start_time         | TIMESTAMP     | NOT NULL                    |
| end_time           | TIMESTAMP     | NOT NULL                    |
| status             | VARCHAR(20)   | NOT NULL                    |
| sync_status        | VARCHAR(20)   | NOT NULL                    |
| google_event_id    | VARCHAR(255)  | UNIQUE, NULL                |
| google_calendar_id | VARCHAR(500)  | NULL                        |
| google_event_link  | VARCHAR(1000) | NULL                        |
| sync_error_message | VARCHAR(500)  | NULL                        |
| created_at         | TIMESTAMP     | NOT NULL                    |
| updated_at         | TIMESTAMP     | NOT NULL                    |
| last_synced_at     | TIMESTAMP     | NULL                        |

---

## Performance Considerations

- **Pagination**: Consider implementing pagination for user events endpoints
- **Caching**: Implement caching for frequently accessed events
- **Async Sync**: Google Calendar sync could be made asynchronous for better performance
- **Rate Limiting**: Implement rate limiting to prevent API abuse
- **Indexing**: Database indexes on `booking_id`, `user_id`, `start_time` for faster queries

---

## Future Enhancements

- [ ] Pagination support for list endpoints
- [ ] Event recurrence/repeating events
- [ ] Event reminders and notifications
- [ ] Batch operations (create/update/delete multiple events)
- [ ] Search/filter events by date range, status, location
- [ ] Event attachments support
- [ ] Multiple calendar support per user
- [ ] Webhook support for Google Calendar changes
- [ ] Event conflict resolution
- [ ] iCalendar (.ics) export

---

## Support

For questions or issues, please contact the development team or create an issue in the repository.
