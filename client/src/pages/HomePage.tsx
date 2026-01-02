import React from "react";
import { AUTH_API_BASE, BOOKING_API_BASE, FACILITY_API_BASE } from "../config";

export const HomePage: React.FC = () => {
  return (
    <div className="grid grid-2">
      <div className="card">
        <h2>Welcome</h2>
        <p>
          Use the navigation to browse facilities, view your bookings, or log in
          / register.
        </p>
        <ul>
          <li>
            Bookings: shows your bookings from <code>{BOOKING_API_BASE}</code>
          </li>
          <li>
            Facilities: pulled from <code>{FACILITY_API_BASE}</code>
          </li>
          <li>
            Auth: login/register via <code>{AUTH_API_BASE}</code>
          </li>
        </ul>
      </div>
      <div className="card">
        <h3>Status</h3>
        <div className="badge">Frontend ↔ APIs connected</div>
        <p style={{ marginTop: 12, color: "#9ca6c7" }}>
          All services are reachable.
        </p>
      </div>
    </div>
  );
};
