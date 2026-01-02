import React, { useEffect, useMemo, useState } from "react";
import {
  createBooking,
  fetchFacilities,
  fetchMyBookings,
  type Booking,
  type Facility,
} from "../api";
import { loadUser } from "../auth";

export const BookingsPage: React.FC = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [facilityId, setFacilityId] = useState<number | null>(null);
  const [start, setStart] = useState<string>("");
  const [end, setEnd] = useState<string>("");
  const [notes, setNotes] = useState<string>("");

  useEffect(() => {
    const user = loadUser();
    if (!user) {
      setError("Please log in first to view your bookings.");
      return;
    }
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const [facData, bookData] = await Promise.all([
          fetchFacilities(),
          fetchMyBookings(user.id),
        ]);
        setFacilities(facData);
        setBookings(bookData);
        if (facData.length > 0 && facilityId === null) {
          setFacilityId(facData[0].id);
        }
      } catch (e: any) {
        setError(e.message || "Failed to load bookings");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const user = useMemo(() => loadUser(), []);

  const submitBooking = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) {
      setError("Please log in first.");
      return;
    }
    if (!facilityId || !start || !end) {
      setError("Pick facility, start, and end time.");
      return;
    }
    setSaving(true);
    setError(null);
    try {
      const created = await createBooking(user.id, {
        facilityId,
        startTime: new Date(start).toISOString(),
        endTime: new Date(end).toISOString(),
        notes,
      });
      setBookings((prev) => [created, ...prev]);
    } catch (e: any) {
      setError(e.message || "Failed to create booking");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="card">Loading bookings...</div>;
  if (error)
    return (
      <div className="card" style={{ color: "#ff9b9b" }}>
        Error: {error}
      </div>
    );

  return (
    <div className="card">
      <div style={{ display: "grid", gap: 16, marginBottom: 16 }}>
        <div>
          <h2>Bookings</h2>
          <p style={{ color: "#9ca6c7" }}>
            Showing your bookings from booking-service.
          </p>
        </div>
        <form
          className="grid grid-2"
          onSubmit={submitBooking}
          style={{ gap: 12 }}
        >
          <div>
            <label>Facility</label>
            <select
              className="input"
              value={facilityId ?? ""}
              onChange={(e) => setFacilityId(Number(e.target.value))}
              required
            >
              <option value="" disabled>
                Select facility
              </option>
              {facilities.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name} (#{f.id})
                </option>
              ))}
            </select>
          </div>
          <div>
            <label>Start time</label>
            <input
              className="input"
              type="datetime-local"
              value={start}
              onChange={(e) => setStart(e.target.value)}
              required
            />
          </div>
          <div>
            <label>End time</label>
            <input
              className="input"
              type="datetime-local"
              value={end}
              onChange={(e) => setEnd(e.target.value)}
              required
            />
          </div>
          <div>
            <label>Notes</label>
            <input
              className="input"
              type="text"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Optional"
            />
          </div>
          <div style={{ gridColumn: "1 / -1", display: "flex", gap: 8 }}>
            <button className="button" type="submit" disabled={saving}>
              {saving ? "Creating..." : "New booking"}
            </button>
            <div style={{ color: "#9ca6c7", alignSelf: "center" }}>
              Uses POST /api/bookings with X-User-Id.
            </div>
          </div>
        </form>
      </div>
      <table className="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Facility</th>
            <th>Status</th>
            <th>Start</th>
            <th>End</th>
          </tr>
        </thead>
        <tbody>
          {bookings.map((booking) => (
            <tr key={booking.id}>
              <td>{booking.id}</td>
              <td>{booking.facilityName ?? booking.facilityId}</td>
              <td>{booking.status}</td>
              <td>{new Date(booking.startTime).toLocaleString()}</td>
              <td>{new Date(booking.endTime).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {bookings.length === 0 && (
        <p style={{ color: "#9ca6c7", marginTop: 12 }}>No bookings yet.</p>
      )}
    </div>
  );
};
