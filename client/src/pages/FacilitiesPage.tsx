import React, { useEffect, useState } from "react";
import { fetchFacilities, type Facility } from "../api";

export const FacilitiesPage: React.FC = () => {
  const [facilities, setFacilities] = useState<Facility[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await fetchFacilities();
        setFacilities(data);
      } catch (e: any) {
        setError(e.message || "Failed to load facilities");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return <div className="card">Loading facilities...</div>;
  if (error)
    return (
      <div className="card" style={{ color: "#ff9b9b" }}>
        Error: {error}
      </div>
    );

  return (
    <div className="grid grid-2">
      {facilities.map((facility) => (
        <div key={facility.id} className="card">
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              marginBottom: 8,
            }}
          >
            <h3 style={{ margin: 0 }}>{facility.name}</h3>
            <span className="badge">{facility.capacity} ppl</span>
          </div>
          <p style={{ color: "#9ca6c7" }}>ID: {facility.id}</p>
          <p style={{ color: "#9ca6c7" }}>{facility.address}</p>
          <p style={{ color: "#dfe5ff" }}>{facility.description}</p>
          <div
            style={{
              marginTop: 8,
              color: facility.available ? "#aef7c5" : "#ffb3b3",
            }}
          >
            {facility.available ? "Available" : "Unavailable"}
          </div>
        </div>
      ))}
    </div>
  );
};
