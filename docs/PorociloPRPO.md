# Booking System - Poročilo

- Skupina: 13 - OneManBand
- Člani: Uroš Tomić - ut4228
- Repozitorij: https://github.com/PRPO-2025-26/booking-system-monorepo
- Naslov URL/IP - `http://booking.34.107.164.168.nip.io/`
- Testni prijavni podatki: `alice@example.com` / `password` - username je `alice` (vloga uporabnik), `bob@example.com` / `password` - username je `bob` (vloga uporabnik), `admin@example.com` / `password` - username je `admin` (vloga admin)

---

## Kratek opis projekta

Gre za aplikacijo za rezervacije, kjer je omogočeno upravljanje objektov kot npr. dvorane, terminov, rezervacij in plačil. Sistem je razdeljen na mikrostoritve, in sicer take ki pokrivajo avtentikacijo, katalog objektov, rezervacije, koledar dogodkov, obvestila in plačila. Cilj te aplikacije je prikazati sodoben, v oblaku zasnovan sistem, ki vsebuje avtomatizirane gradnje in izdaje (CI/CD), orkestracijo kontejnerjev ter spremljanje delovanja (preverjanje zdravja, metrike). Uporabniški del je preprosta spletna aplikacija. Problem, ki ga ta aplikacija rešuje je ta da zna biti upravljanje rezervacij za objekte pogosto kar razdrobljeno, kar se pokaže pri ločenih prijavah, ročnem usklajevanju koledarjev, nepreglednimi plačilnimi postopki, zamujenih obvestilih, ki lahko povzročajo podvojene rezervacije itd. Moj projekt ta problem reši z mikrostoritvami - vsaka mikrostoritev ima svojo nalogo: auth za prijavo, facility za katalog objektov, booking za rezervacije, calendar za koledarsko sinhronizacijo, payment za plačila in notification za obvsetila.

## Ogrodje in razvojno okolje

- Programski jezik: Java 17 (Spring Boot 3.x), TypeScript/React (Vite)
- Ogrodja/knjižnice (izbor):
  - Spring Web, Spring Data JPA, Spring Security, Springdoc OpenAPI, Actuator, Lombok
  - PostgreSQL (persistenca), Redis (cache/session)
  - WebClient (medstoritevna komunikacija), Flyway (migracije)
- Razvoj in orkestracija:
  - Docker, Docker Compose (lokalni razvijalski stack)
  - Kubernetes (GKE) z Ingress in HPA
  - GitHub Actions (testi, build/push, deploy)
- Odjemalec: React SPA, produkcijski image z Nginx

## Shema arhitekture

```
[ Client (React SPA) ]
        |
        | HTTPS (Ingress)
        v
[ Ingress (GKE, host: booking.34.107.164.168.nip.io) ]
        |
        +-- /auth → auth-service (HTTP REST)
        +-- /facility → facility-service (HTTP REST)
        +-- /booking → booking-service (HTTP REST)
        +-- /calendar → calendar-service (HTTP REST)
        +-- /notification → notification-service (HTTP REST)
        +-- /payment → payment-service (HTTP REST)

DB & Infra:
- PostgreSQL 15  ←(JDBC via Spring Data JPA)→ services
- Redis 7        ←(cache/session)→ services

Opazovanje/robustnost:
- Liveness/Readiness/Startup probes (K8s)
- Actuator (/actuator) in OpenAPI (/swagger-ui)

Komunikacija med storitvami:
- HTTP REST (Spring WebClient) [npr. booking ↔ payment/calendar/notification]
- Zunanja integracija (demo): Bearer-protected HTTP endpoint (httpbin.org)
```

Končni diagram (PNG) in vir:

![Shema arhitekture](docs/diagrams/architecture.png)

Vir (Lucidchart): https://lucid.app/lucidchart/fb13f964-25a0-48a2-8736-e24843019ee4/edit?viewport_loc=-160%2C105%2C2988%2C1639%2C0_0&invitationId=inv_64cc9e93-7b2c-4685-8d05-5acbc4d8fbcd

Legenda protokolov:

- Client → Ingress: HTTPS
- Ingress → storitve: HTTP (cluster)
- Storitve → PostgreSQL: JDBC/TCP 5432
- auth-service → Redis: RESP/TCP 6379
- booking-service → payment/calendar/notification: HTTP (internal REST)
- notification-service → SMTP ponudnik: SMTP/TLS
- booking-service → zunanji API (httpbin): HTTPS (Bearer)

## Seznam funkcionalnosti mikrostoritev

- auth-service
  - Registracija in prijava uporabnika, izdaja JWT, zaščita preostalih storitev.
- facility-service
  - Upravljanje objektov (CRUD), osnovni katalog s kapacitetami.
- booking-service
  - Ustvarjanje/preklic/posodobitev rezervacij, vpogled v moje rezervacije (preteklost/prihodnost), preverjanje razpoložljivosti, orkestracija s plačili/koledarjem/obvestili.
- calendar-service
  - Upravljanje dogodkov/terminov, sinhronizacija rezervacij v koledar.
- notification-service
  - Pošiljanje obvestil (e-pošta/simulacija), ponovno pošiljanje, vpogled v status.
- payment-service
  - Plačilni tok: kreiranje »checkout« zahtevka, status plačila, demo webhook.

## Primeri uporabe

- Upravljanje z objekti: skrbnik doda/uredi izbiro objektov in njihove kapacitete.
- Rezervacija termina: uporabnik poišče prost termin in odda rezervacijo.
- Plačilo rezervacije: sistem ustvari plačilni zahtevek in spremlja status.
- Obveščanje: po potrjeni rezervaciji uporabnik prejme obvestilo.
- Koledar: potrjene rezervacije so vidne v koledarju.

Kompleksnejši primer: Rezervacija s plačilom in sinhronizacijo v koledar

1. Uporabnik se prijavi (JWT) in izbere objekt/termin.
2. booking-service ustvari rezervacijo in inicira plačilo v payment-service.
3. Po uspešnem plačilu booking-service potrdi rezervacijo, vpiše termin v calendar-service in pokliče notification-service za obvestilo.
4. Uporabnik vidi potrjeno rezervacijo in termin na koledarju.

## Seznam zahtev (izvedba v projektu)

- Repozitorij (Git)
  - GitHub: PRPO-2025-26/booking-system-monorepo. README opisuje projekt, namestitev in zagon.
- Razvojno okolje in struktura
  - IDE: Uporabljal sem VS Code. Monorepo z direktoriji za storitve, odjemalca, infra in docs. Testi, konfiguracije in Docker/K8s so datoteke ločene. Dokumentirana je tudi nastavitev.
- Mikrostoritve
  - 6 storitev (auth, facility, booking, calendar, notification, payment). Komunikacija prek REST (WebClient) in Ingress /api/\* poti.
- REST API
  - Vsaka storitev izpostavi CRUD in dodatne končne točke. Uporabljeni so bili HTTP statusi, validacije in obravnava napak (globalni handlerji, kjer relevantno).
- Dokumentacija API
  - Springdoc OpenAPI + Swagger UI za vsako storitev, Postman kolekcije za calendar in payment. Poti usklajene z Ingressom.
- Vsebniki (Docker)
  - Dockerfile sem uporabil za vsako storitev in za odjemalca. Slike se gradijo v CI in lokalno, objavljeni so tudi v Google Artifact Registry.
- Kubernetes
  - Manifesti: Deployments, Services, Ingress, HPA, ConfigMap/Secrets. Aplikacija deluje v GKE in je javno dostopna.
- CI/CD
  - GitHub Actions: testi, build, push, deploy na GKE (kubectl apply)
- Namestitev v oblak
  - Uporabil sem Google Cloud GKE, Ingress ima javni host booking.34.107.164.168.nip.io
- Poslovna logika
  - Vsaka storitev vsebuje svojo poslovno logiko
- Dokumentacija projekta
  - Dokumentacija je vidna v root README datoteki, v /docs je SERVICE_INTEGRATION.md in SMOKE.md.
- Zunanji API
  - Integriral sem httpbin.org (Bearer) v booking-service za demonstracijo avtoriziranih klicev.
- Podatkovna baza
  - Uporabljam PostgreSQL 15, storitve dostopajo prek JDBC. Tabele in začetni podatki sem implementiral prek Flyway migracij (db/migration/\*).
- ORM
  - Uporabil sem Spring Data JPA/Hibernate. Implementiral sem tudi CRUD operacije za vse mikrostoritve.
- Preverjanje zdravja
  - Preverjanje zdravja je implementirano preko Actuator health endpoints ter z K8s liveness/readiness/startup probes za vsako storitev.
- Skaliranje
  - Uporabil sem HPA: min 1, max 3 replike, cilj 70% CPU ter z Ingress, ki uravnoteži promet med replikami.
- Grafični vmesnik
  - Za frontend sem uporabil React (Vite) SPA, ki ima več podstrani (Login, Home, Bookings, Facilities, Payments, Calendar). Z mikrostoritvami komunicira prek REST API.
