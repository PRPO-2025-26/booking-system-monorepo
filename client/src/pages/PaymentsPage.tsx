import React, { useState } from "react";
import {
  createPaymentCheckout,
  completePaymentMock,
  type PaymentCheckoutRequest,
  type PaymentCheckoutResponse,
} from "../api";

export const PaymentsPage: React.FC = () => {
  const [payload, setPayload] = useState<PaymentCheckoutRequest>({
    bookingId: 1,
    userId: 1,
    amount: 50,
    currency: "EUR",
  });
  const [checkout, setCheckout] = useState<PaymentCheckoutResponse | null>(
    null
  );
  const [status, setStatus] = useState<string>("");
  const [loading, setLoading] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setStatus("");
    try {
      const res = await createPaymentCheckout(payload);
      setCheckout(res);
      setStatus(`Checkout created: ${res.sessionId}`);
    } catch (err: any) {
      setStatus(err.message || "Checkout failed");
    } finally {
      setLoading(false);
    }
  };

  const complete = async () => {
    if (!checkout?.sessionId) return;
    setLoading(true);
    setStatus("");
    try {
      await completePaymentMock(checkout.sessionId);
      setStatus(`Completed mock payment for ${checkout.sessionId}`);
    } catch (err: any) {
      setStatus(err.message || "Complete failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>Payments (mock demo)</h2>
      <p style={{ color: "#9ca6c7" }}>
        Calls payment-service `/api/payments/checkout` then `/mock/
        {checkout?.sessionId ?? "{sessionId}"}/complete`.
      </p>
      <form className="grid grid-2" style={{ gap: 12 }} onSubmit={submit}>
        <label>
          Booking ID
          <input
            className="input"
            type="number"
            value={payload.bookingId}
            onChange={(e) =>
              setPayload((p) => ({ ...p, bookingId: Number(e.target.value) }))
            }
            required
          />
        </label>
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
          Amount
          <input
            className="input"
            type="number"
            value={payload.amount}
            onChange={(e) =>
              setPayload((p) => ({ ...p, amount: Number(e.target.value) }))
            }
            required
          />
        </label>
        <label>
          Currency
          <input
            className="input"
            value={payload.currency}
            onChange={(e) =>
              setPayload((p) => ({ ...p, currency: e.target.value }))
            }
            required
          />
        </label>
        <div style={{ gridColumn: "1 / -1", display: "flex", gap: 8 }}>
          <button className="button" type="submit" disabled={loading}>
            {loading ? "Creating..." : "Create checkout"}
          </button>
          {checkout?.sessionId && (
            <button
              className="button"
              type="button"
              onClick={complete}
              disabled={loading}
            >
              Complete mock payment
            </button>
          )}
          {status && <div style={{ color: "#9ca6c7" }}>{status}</div>}
        </div>
      </form>
      {checkout?.checkoutUrl && (
        <div style={{ marginTop: 12, color: "#9ca6c7" }}>
          Checkout URL:{" "}
          <a href={checkout.checkoutUrl}>{checkout.checkoutUrl}</a>
        </div>
      )}
    </div>
  );
};
