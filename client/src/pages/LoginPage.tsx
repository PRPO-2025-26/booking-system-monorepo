import React, { useState } from "react";
import { Link } from "react-router-dom";
import { AUTH_API_BASE } from "../config";
import { login } from "../api";
import { saveUser } from "../auth";

type LoginResponse = {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  role: string;
};

export const LoginPage: React.FC = () => {
  const [username, setUsername] = useState("uros");
  const [password, setPassword] = useState("password");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<LoginResponse | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setResult(null);
    setLoading(true);
    try {
      const data = await login(username, password);
      setResult(data);
      saveUser({
        id: data.id,
        username: data.username,
        email: data.email,
        role: data.role,
        token: data.token,
      });
    } catch (err: any) {
      setError(err.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card" style={{ maxWidth: 440 }}>
      <h2>Login</h2>
      <p style={{ color: "#9ca6c7" }}>
        Connects to auth-service at <code>{AUTH_API_BASE}</code>. Use
        username/password (not email).
      </p>
      <form className="grid" style={{ gap: 12 }} onSubmit={handleSubmit}>
        <div>
          <label>Username</label>
          <input
            className="input"
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="username"
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
            placeholder="••••••••"
            required
          />
        </div>
        <button className="button" type="submit" disabled={loading}>
          {loading ? "Signing in..." : "Sign in"}
        </button>
        <div style={{ fontSize: 14, color: "#9ca6c7" }}>
          No account? <Link to="/register">Register here</Link>
        </div>
        {error && <div style={{ color: "#ff9b9b", fontSize: 14 }}>{error}</div>}
        {result && (
          <div
            className="badge"
            style={{ background: "rgba(76, 175, 80, 0.2)", color: "#c8f7c5" }}
          >
            Token received for {result.username} ({result.role})
          </div>
        )}
      </form>
    </div>
  );
};
