import {
  AUTH_API_BASE,
  BOOKING_API_BASE,
  CALENDAR_API_BASE,
  FACILITY_API_BASE,
  NOTIFICATION_API_BASE,
  PAYMENT_API_BASE,
} from "./config";

export type LoginResponse = {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  role: string;
};

export type RegisterResponse = {
  id: number;
  username: string;
  email: string;
  role: string;
  createdAt?: string;
};

export type Facility = {
  id: number;
  name: string;
  type: string;
  address: string;
  description?: string;
  capacity: number;
  pricePerHour: number;
  ownerId: number;
  available: boolean;
};

export type Booking = {
  id: number;
  facilityId: number;
  facilityName?: string;
  userId: number;
  startTime: string;
  endTime: string;
  status: string;
};

export type CalendarEvent = {
  id?: number;
  bookingId: number;
  userId: number;
  facilityId: number;
  title: string;
  location?: string;
  description?: string;
  startTime: string;
  endTime: string;
  status?: string;
};

export async function fetchCalendarEventsByUser(
  userId: number
): Promise<CalendarEvent[]> {
  const res = await fetch(`${CALENDAR_API_BASE}/events/user/${userId}`);
  if (!res.ok) {
    throw new Error(
      (await res.text()) || `Failed to load calendar events (${res.status})`
    );
  }
  return (await res.json()) as CalendarEvent[];
}

export type CreateCalendarEventPayload = {
  bookingId: number;
  userId: number;
  facilityId: number;
  title: string;
  location?: string;
  description?: string;
  startTime: string;
  endTime: string;
};

export type PaymentCheckoutRequest = {
  bookingId: number;
  userId: number;
  amount: number;
  currency: string;
};

export type PaymentCheckoutResponse = {
  sessionId: string;
  checkoutUrl?: string;
  status?: string;
};

export type NotificationRequest = {
  userId: number;
  bookingId?: number;
  paymentId?: number;
  eventId?: number;
  type: string;
  channel: string;
  recipient: string;
  subject: string;
  content: string;
};

export type CreateBookingPayload = {
  facilityId: number;
  startTime: string; // ISO
  endTime: string; // ISO
  notes?: string;
};

export async function login(username: string, password: string) {
  const res = await fetch(`${AUTH_API_BASE}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  if (!res.ok) {
    throw new Error((await res.text()) || `Login failed (${res.status})`);
  }
  return (await res.json()) as LoginResponse;
}

export async function register(
  username: string,
  email: string,
  password: string
) {
  const res = await fetch(`${AUTH_API_BASE}/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, email, password }),
  });
  if (!res.ok) {
    throw new Error((await res.text()) || `Register failed (${res.status})`);
  }
  return (await res.json()) as RegisterResponse;
}

export async function fetchFacilities(): Promise<Facility[]> {
  const res = await fetch(FACILITY_API_BASE);
  if (!res.ok) {
    throw new Error(`Failed to load facilities (${res.status})`);
  }
  return (await res.json()) as Facility[];
}

export async function fetchMyBookings(userId: number): Promise<Booking[]> {
  const res = await fetch(`${BOOKING_API_BASE}/my`, {
    headers: {
      "X-User-Id": String(userId),
    },
  });
  if (!res.ok) {
    throw new Error(
      (await res.text()) || `Failed to load bookings (${res.status})`
    );
  }
  return (await res.json()) as Booking[];
}

export async function createBooking(
  userId: number,
  payload: CreateBookingPayload
): Promise<Booking> {
  const res = await fetch(`${BOOKING_API_BASE}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-User-Id": String(userId),
    },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(
      (await res.text()) || `Create booking failed (${res.status})`
    );
  }
  return (await res.json()) as Booking;
}

export async function updateBookingStatus(
  userId: number,
  bookingId: number,
  status: "PENDING" | "CONFIRMED" | "CANCELLED" | "COMPLETED"
): Promise<Booking> {
  const res = await fetch(`${BOOKING_API_BASE}/${bookingId}/status`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      "X-User-Id": String(userId),
    },
    body: JSON.stringify({ status }),
  });
  if (!res.ok) {
    throw new Error(
      (await res.text()) || `Update booking status failed (${res.status})`
    );
  }
  return (await res.json()) as Booking;
}

export async function createCalendarEvent(
  payload: CreateCalendarEventPayload
): Promise<CalendarEvent> {
  const res = await fetch(`${CALENDAR_API_BASE}/events`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(
      (await res.text()) || `Create calendar event failed (${res.status})`
    );
  }
  return (await res.json()) as CalendarEvent;
}

export async function createPaymentCheckout(
  payload: PaymentCheckoutRequest
): Promise<PaymentCheckoutResponse> {
  const res = await fetch(`${PAYMENT_API_BASE}/checkout`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(
      (await res.text()) || `Payment checkout failed (${res.status})`
    );
  }
  return (await res.json()) as PaymentCheckoutResponse;
}

export async function completePaymentMock(sessionId: string) {
  const res = await fetch(`${PAYMENT_API_BASE}/mock/${sessionId}/complete`, {
    method: "POST",
  });
  if (!res.ok) {
    throw new Error(
      (await res.text()) || `Mock completion failed (${res.status})`
    );
  }
  return await res.text();
}

export async function sendNotification(payload: NotificationRequest) {
  const res = await fetch(`${NOTIFICATION_API_BASE}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    throw new Error(
      (await res.text()) || `Send notification failed (${res.status})`
    );
  }
  return await res.json();
}
