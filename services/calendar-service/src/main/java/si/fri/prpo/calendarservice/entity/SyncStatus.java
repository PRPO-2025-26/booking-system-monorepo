package si.fri.prpo.calendarservice.entity;

public enum SyncStatus {
    PENDING, // Čaka na sinhronizacijo
    SYNCED, // Uspe šno sinhronizirano
    FAILED, // Sinhronizacija neuspešna
    MOCK // Mock mode (brez Google API)
}
