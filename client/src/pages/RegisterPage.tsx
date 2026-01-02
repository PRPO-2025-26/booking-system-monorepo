import React, { useState } from "react";
import { Link } from "react-router-dom";
import { AUTH_API_BASE } from "../config";
import { register as registerApi } from "../api";

type RegisterResponse = {
  id: number;
  username: string;
  email: string;
  role: string;
  createdAt?: string;
};

export const RegisterPage: React.FC = () => {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<RegisterResponse | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setResult(null);
    setLoading(true);
    try {
      const data = await registerApi(username, email, password);
      setResult(data);
    } catch (err: any) {
      setError(err.message || "Register failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card" style={{ maxWidth: 520 }}>
      <h2>Register</h2>
      <p style={{ color: "#9ca6c7" }}>
        Creates a user via auth-service at <code>{AUTH_API_BASE}</code>.
      </p>
      <form className="grid" style={{ gap: 12 }} onSubmit={handleSubmit}>
        <div>
          <label>Username</label>
          <input
            className="input"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="yourusername"
            required
            minLength={3}
          />
        </div>
        <div>
          <label>Email</label>
          <input
            className="input"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
            required
          />
        </div>
        <div>
          <label>Password</label>
          <input
            className="input"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="minimum 6 characters"
            required
            minLength={6}
          />
        </div>
        <button className="button" type="submit" disabled={loading}>
          {loading ? "Registering..." : "Register"}
        </button>
        <div style={{ fontSize: 14, color: "#9ca6c7" }}>
          Already have an account? <Link to="/login">Log in</Link>
        </div>
        {error && <div style={{ color: "#ff9b9b", fontSize: 14 }}>{error}</div>}
        {result && (
          <div
            className="badge"
            style={{ background: "rgba(76, 175, 80, 0.2)", color: "#c8f7c5" }}
          >
            Registered {result.username} ({result.email})
          </div>
        )}
      </form>
    </div>
  );
};
