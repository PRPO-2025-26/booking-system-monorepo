const origin =
  typeof window !== "undefined" && window.location?.origin
    ? window.location.origin
    : "";

export const AUTH_API_BASE =
  import.meta.env.VITE_AUTH_API ?? `${origin}/api/auth`;

export const FACILITY_API_BASE =
  import.meta.env.VITE_FACILITY_API ?? `${origin}/api/facilities`;

export const BOOKING_API_BASE =
  import.meta.env.VITE_BOOKING_API ?? `${origin}/api/bookings`;

export const CALENDAR_API_BASE =
  import.meta.env.VITE_CALENDAR_API ?? `${origin}/api/calendar`;

export const PAYMENT_API_BASE =
  import.meta.env.VITE_PAYMENT_API ?? `${origin}/api/payments`;

export const NOTIFICATION_API_BASE =
  import.meta.env.VITE_NOTIFICATION_API ?? `${origin}/api/notifications`;
