import React, { useEffect, useState } from "react";
import {
  createCalendarEvent,
  type CalendarEvent,
  type CreateCalendarEventPayload,
  fetchCalendarEventsByUser,
} from "../api";
import { loadUser } from "../auth";

export const CalendarPage: React.FC = () => {
  const user = loadUser();
  const currentUserId = user?.id;
  const [form, setForm] = useState({
    bookingId: 1,
    userId: currentUserId ?? 0,
    facilityId: 1,
    summary: "Training",
    location: "",
    description: "",
    startDateTime: new Date().toISOString(),
    endDateTime: new Date(Date.now() + 60 * 60 * 1000).toISOString(),
    timeZone: "Europe/Ljubljana",
  });
  const [status, setStatus] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const [eventsLoading, setEventsLoading] = useState(false);
  const [events, setEvents] = useState<CalendarEvent[]>([]);
  const [eventsError, setEventsError] = useState<string>("");

  const loadEvents = async (targetUserId: number) => {
    setEventsLoading(true);
    setEventsError("");
    try {
      const data = await fetchCalendarEventsByUser(targetUserId);
      setEvents(data);
      if (!data.length) {
        setEventsError("No events found for this user.");
      }
    } catch (err: any) {
      setEventsError(err.message || "Failed to load events");
    } finally {
      setEventsLoading(false);
    }
  };

  useEffect(() => {
    if (currentUserId) {
      loadEvents(currentUserId);
    } else {
      setEventsError("You must be logged in to view your events.");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setStatus("");
    try {
      if (!currentUserId) {
        throw new Error("You must be logged in to create events.");
      }
      const payload: CreateCalendarEventPayload = {
        bookingId: form.bookingId,
        userId: currentUserId,
        facilityId: form.facilityId,
        title: form.summary,
        description: form.description,
        location: form.location,
        startTime: form.startDateTime,
        endTime: form.endDateTime,
      };

      const created: CalendarEvent = await createCalendarEvent(payload);
      setStatus(`Created event ${created.id ?? ""} (${created.title})`);
    } catch (err: any) {
      setStatus(err.message || "Failed to create event");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>Calendar (demo)</h2>
      <p style={{ color: "#9ca6c7" }}>
        Calls calendar-service `/api/calendar/events` to create a mock event.
      </p>
      <form className="grid grid-2" style={{ gap: 12 }} onSubmit={submit}>
        <label>
          Booking ID
          <input
            className="input"
            type="number"
            value={form.bookingId}
            onChange={(e) =>
              setForm((f) => ({ ...f, bookingId: Number(e.target.value) }))
            }
            required
          />
        </label>
        <label>
          User
          <input
            className="input"
            value={currentUserId ? `User #${currentUserId}` : "Not logged in"}
            disabled
          />
        </label>
        <label>
          Facility ID
          <input
            className="input"
            type="number"
            value={form.facilityId}
            onChange={(e) =>
              setForm((f) => ({ ...f, facilityId: Number(e.target.value) }))
            }
            required
          />
        </label>
        <label>
          Title
          <input
            className="input"
            value={form.summary}
            onChange={(e) =>
              setForm((f) => ({ ...f, summary: e.target.value }))
            }
            required
          />
        </label>
        <label>
          Location
          <input
            className="input"
            value={form.location}
            onChange={(e) =>
              setForm((f) => ({ ...f, location: e.target.value }))
            }
            placeholder="Optional"
          />
        </label>
        <label>
          Description
          <input
            className="input"
            value={form.description}
            onChange={(e) =>
              setForm((f) => ({ ...f, description: e.target.value }))
            }
            placeholder="Optional"
          />
        </label>
        <label>
          Start
          <input
            className="input"
            type="datetime-local"
            value={form.startDateTime.slice(0, 16)}
            onChange={(e) =>
              setForm((f) => ({
                ...f,
                startDateTime: new Date(e.target.value).toISOString(),
              }))
            }
            required
          />
        </label>
        <label>
          End
          <input
            className="input"
            type="datetime-local"
            value={form.endDateTime.slice(0, 16)}
            onChange={(e) =>
              setForm((f) => ({
                ...f,
                endDateTime: new Date(e.target.value).toISOString(),
              }))
            }
            required
          />
        </label>
        <label>
          Time zone
          <input
            className="input"
            value={form.timeZone}
            onChange={(e) =>
              setForm((f) => ({ ...f, timeZone: e.target.value }))
            }
            required
          />
        </label>
        <div style={{ gridColumn: "1 / -1", display: "flex", gap: 8 }}>
          <button
            className="button"
            type="submit"
            disabled={loading || !currentUserId}
          >
            {loading ? "Creating..." : "Create event"}
          </button>
          {!currentUserId && (
            <div style={{ color: "#ffb4b4" }}>
              Login required to create an event.
            </div>
          )}
          {status && <div style={{ color: "#9ca6c7" }}>{status}</div>}
        </div>
      </form>

      <div style={{ marginTop: 24 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <h3 style={{ margin: 0 }}>My events</h3>
          <button
            className="button"
            type="button"
            onClick={() => currentUserId && loadEvents(currentUserId)}
            disabled={eventsLoading || !currentUserId}
          >
            {eventsLoading ? "Refreshing..." : "Refresh"}
          </button>
        </div>
        {eventsError && (
          <div style={{ color: "#ffb4b4", marginTop: 8 }}>{eventsError}</div>
        )}
        {!eventsLoading && !eventsError && events.length === 0 && (
          <div style={{ color: "#9ca6c7", marginTop: 8 }}>No events yet.</div>
        )}
        <div style={{ marginTop: 12, display: "grid", gap: 8 }}>
          {events.map((ev) => (
            <div
              key={ev.id ?? `${ev.bookingId}-${ev.startTime}`}
              style={{
                border: "1px solid #2c3352",
                borderRadius: 8,
                padding: 12,
                background: "#0f1424",
              }}
            >
              <div style={{ fontWeight: 600 }}>{ev.title}</div>
              <div style={{ color: "#9ca6c7", fontSize: 13 }}>
                Booking #{ev.bookingId} • Facility #{ev.facilityId} • User #
                {ev.userId}
              </div>
              <div style={{ color: "#9ca6c7", fontSize: 13, marginTop: 4 }}>
                {new Date(ev.startTime).toLocaleString()} —{" "}
                {new Date(ev.endTime).toLocaleString()} ({ev.status ?? ""})
              </div>
              {ev.description && (
                <div style={{ color: "#cfd6f1", marginTop: 4 }}>
                  {ev.description}
                </div>
              )}
              {ev.location && (
                <div style={{ color: "#cfd6f1", marginTop: 2 }}>
                  📍 {ev.location}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
