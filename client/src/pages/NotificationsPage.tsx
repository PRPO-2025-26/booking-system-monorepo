import React, { useState } from "react";
import { sendNotification, type NotificationRequest } from "../api";

export const NotificationsPage: React.FC = () => {
  const [payload, setPayload] = useState<NotificationRequest>({
    userId: 1,
    bookingId: 1,
    type: "BOOKING_CONFIRMATION",
    channel: "EMAIL",
    recipient: "user@example.com",
    subject: "Booking confirmation",
    content: "Your booking is confirmed.",
  });
  const [status, setStatus] = useState<string>("");
  const [loading, setLoading] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setStatus("");
    try {
      await sendNotification(payload);
      setStatus("Notification sent (mock)");
    } catch (err: any) {
      setStatus(err.message || "Send failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>Notifications (mock)</h2>
      <p style={{ color: "#9ca6c7" }}>
        Calls notification-service `/api/notifications` to send a mock
        email/SMS.
      </p>
      <form className="grid grid-2" style={{ gap: 12 }} onSubmit={submit}>
        <label>
          User ID
          <input
            className="input"
            type="number"
            value={payload.userId}
            onChange={(e) =>
              setPayload((p) => ({ ...p, userId: Number(e.target.value) }))
            }
            required
          />
        </label>
        <label>
          Booking ID
          <input
            className="input"
            type="number"
            value={payload.bookingId ?? ""}
            onChange={(e) =>
              setPayload((p) => ({ ...p, bookingId: Number(e.target.value) }))
            }
            required
          />
        </label>
        <label>
          Type
          <input
            className="input"
            value={payload.type}
            onChange={(e) =>
              setPayload((p) => ({ ...p, type: e.target.value }))
            }
            required
          />
        </label>
        <label>
          Channel
          <input
            className="input"
            value={payload.channel}
            onChange={(e) =>
              setPayload((p) => ({ ...p, channel: e.target.value }))
            }
            required
          />
        </label>
        <label>
          Recipient
          <input
            className="input"
            value={payload.recipient}
            onChange={(e) =>
              setPayload((p) => ({ ...p, recipient: e.target.value }))
            }
            required
          />
        </label>
        <label>
          Subject
          <input
            className="input"
            value={payload.subject}
            onChange={(e) =>
              setPayload((p) => ({ ...p, subject: e.target.value }))
            }
            required
          />
        </label>
        <label style={{ gridColumn: "1 / -1" }}>
          Content
          <textarea
            className="input"
            rows={3}
            value={payload.content}
            onChange={(e) =>
              setPayload((p) => ({ ...p, content: e.target.value }))
            }
            required
          />
        </label>
        <div style={{ gridColumn: "1 / -1", display: "flex", gap: 8 }}>
          <button className="button" type="submit" disabled={loading}>
            {loading ? "Sending..." : "Send notification"}
          </button>
          {status && <div style={{ color: "#9ca6c7" }}>{status}</div>}
        </div>
      </form>
    </div>
  );
};
