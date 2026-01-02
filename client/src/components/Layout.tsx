import React from "react";
import { Link, Outlet, useLocation } from "react-router-dom";
import "./Layout.css";

const navItems = [
  { to: "/", label: "Home" },
  { to: "/bookings", label: "Bookings" },
  { to: "/facilities", label: "Facilities" },
  { to: "/calendar", label: "Calendar" },
  { to: "/payments", label: "Payments" },
  { to: "/notifications", label: "Notifications" },
  { to: "/login", label: "Login" },
  { to: "/register", label: "Register" },
];

export const Layout: React.FC = () => {
  const { pathname } = useLocation();

  return (
    <div className="layout">
      <header className="topbar">
        <div className="brand">PRPO Booking</div>
        <nav>
          <ul>
            {navItems.map((item) => (
              <li
                key={item.to}
                className={pathname === item.to ? "active" : ""}
              >
                <Link to={item.to}>{item.label}</Link>
              </li>
            ))}
          </ul>
        </nav>
      </header>
      <main className="content">
        <Outlet />
      </main>
      <footer className="footer">© {new Date().getFullYear()} PRPO</footer>
    </div>
  );
};
