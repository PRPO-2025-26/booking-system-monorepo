import React, { useState } from "react";
import { createCalendarEvent, type CalendarEvent } from "../api";
import { loadUser } from "../auth";

export const CalendarPage: React.FC = () => {
  const user = loadUser();
  const [form, setForm] = useState<CalendarEvent>({
    bookingId: 1,
    userId: user?.id ?? 1,
    summary: "Training",
    location: "",
    description: "",
    startDateTime: new Date().toISOString(),
    endDateTime: new Date(Date.now() + 60 * 60 * 1000).toISOString(),
    timeZone: "Europe/Ljubljana",
  });
  const [status, setStatus] = useState<string>("");
  const [loading, setLoading] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setStatus("");
    try {
      const created = await createCalendarEvent(form);
      setStatus(`Created event ${created.id ?? ""} (${created.summary})`);
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
          User ID
          <input
            className="input"
            type="number"
            value={form.userId}
            onChange={(e) =>
              setForm((f) => ({ ...f, userId: Number(e.target.value) }))
            }
            required
          />
        </label>
        <label>
          Summary
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
          <button className="button" type="submit" disabled={loading}>
            {loading ? "Creating..." : "Create event"}
          </button>
          {status && <div style={{ color: "#9ca6c7" }}>{status}</div>}
        </div>
      </form>
    </div>
  );
};
