export type StoredUser = {
  id: number;
  username: string;
  email?: string;
  role?: string;
  token?: string;
};

const KEY = "prpo_auth";

export function saveUser(user: StoredUser) {
  localStorage.setItem(KEY, JSON.stringify(user));
}

export function loadUser(): StoredUser | null {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as StoredUser) : null;
  } catch (e) {
    return null;
  }
}

export function clearUser() {
  localStorage.removeItem(KEY);
}
