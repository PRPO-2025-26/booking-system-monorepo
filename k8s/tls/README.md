# TLS for local ingress (booking.local)

This document explains how to create a locally-trusted TLS certificate for `booking.local` and use it with the Kubernetes Ingress you already added TLS to (`k8s/ingress.yaml`). It uses `mkcert` (recommended) because it automates trust on Windows and is developer-friendly. A fallback OpenSSL self-signed flow is provided.

## Overview

- Use mkcert to generate a certificate for `booking.local` that Windows and browsers will trust.
- Create a Kubernetes TLS Secret containing the cert and key (must be in the same namespace as the Ingress resource).
- Apply the updated Ingress and test HTTPS locally (port-forward if needed).

## Prerequisites

- `kubectl` configured to the cluster (Docker Desktop Kubernetes in your environment).
- `mkcert` installed (recommended). You can install via Chocolatey:

```powershell
choco install mkcert -y
```

- If you can't use mkcert, OpenSSL is a fallback (not trusted by default).

## mkcert (recommended)

1. Install and register mkcert CA (run PowerShell as Administrator):

```powershell
# installs mkcert CA into Windows Trusted Root
mkcert -install
```

- Windows will show a Security Warning dialog asking to install a certificate authority (the screenshot in the repo). Choose "Yes" to allow mkcert to add the local CA to the Windows Trusted Root store.

2. Generate a certificate for `booking.local` (run from any folder):

```powershell
mkcert booking.local
```

- mkcert produces two files in the current directory, e.g. `booking.local.pem` and `booking.local-key.pem`.

3. Create a Kubernetes TLS secret. Ensure you create it in the same namespace as the Ingress (the Ingress in this repo is in the default namespace unless you moved it):

```powershell
# default namespace
kubectl create secret tls booking-local-tls \
  --cert=booking.local.pem \
  --key=booking.local-key.pem -n default

# If your Ingress is in another namespace, set -n <your-namespace>
```

4. Apply the Ingress manifest (we already added the TLS section and `secretName: booking-local-tls`):

```powershell
kubectl apply -f k8s/ingress.yaml
```

5. Test HTTPS

- If your ingress controller exposes 443 on the host (some setups), you can test directly:

```powershell
Invoke-WebRequest https://booking.local/auth/actuator/health
```

- If ingress is not exposed on host 443, port-forward ingress controller's 443 locally and test on `:8443`:

```powershell
# forward 8443 on localhost to ingress-nginx controller 443
kubectl port-forward -n ingress-nginx service/ingress-nginx-controller 8443:443

# then test
Invoke-WebRequest https://booking.local:8443/auth/actuator/health
```

Because mkcert installed and trusted the CA, the cert will be trusted in Windows and browsers and you should not need to skip verification.

## OpenSSL fallback (self-signed)

If you cannot use mkcert, you can create a self-signed certificate. Note: browsers/PowerShell will complain unless you import the cert into the Trusted Root store.

```powershell
# Create key and cert
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout booking.local.key -out booking.local.crt \
  -subj "/CN=booking.local/O=booking.local"

# Create the k8s secret (same namespace as Ingress)
kubectl create secret tls booking-local-tls \
  --cert=booking.local.crt \
  --key=booking.local.key -n default

kubectl apply -f k8s/ingress.yaml
```

Test with verification skipped (PowerShell):

```powershell
kubectl port-forward -n ingress-nginx service/ingress-nginx-controller 8443:443
Invoke-WebRequest -Uri https://booking.local:8443/auth/actuator/health -SkipCertificateCheck

# or with curl.exe
curl.exe -k https://booking.local:8443/auth/actuator/health
```

To avoid skipping verification, import `booking.local.crt` into Windows Trusted Root Certificates (local machine or current user) and then test again normally.

## Notes and troubleshooting

- Namespace: The TLS secret must exist in the same namespace as the `Ingress`. If the Ingress is not in `default`, add `-n <namespace>` to the `kubectl create secret` command.

- Ingress controller name/namespace: Commonly `ingress-nginx` namespace and service name `ingress-nginx-controller`. Adjust the port-forward command if yours differs.

- mkcert CA install failure: re-run PowerShell as Administrator. mkcert will show the Security Warning (click Yes). If mkcert can't install, you can manually import the generated root CA (`~/.local/share/mkcert` or `%LocalAppData%\mkcert`) into Windows Trusted Root.

- Renewals: mkcert-generated certs are static files. Re-run `mkcert booking.local` to re-generate and update the Kubernetes secret (delete and recreate secret or use `kubectl create secret tls --dry-run=client -o yaml | kubectl apply -f -` to replace).

- Automated production TLS: For real workloads or CI, consider `cert-manager` with ACME (Let's Encrypt) to automate certificate issuance and renewal. mkcert is for local developer environments only.

## Example: replace TLS secret atomically

```powershell
# Generate certs using mkcert in temp folder
mkcert -cert-file .\booking.local.crt -key-file .\booking.local.key booking.local

# Apply secret with kubectl apply (atomic replacement)
kubectl create secret tls booking-local-tls \
  --cert=booking.local.crt --key=booking.local.key -n default --dry-run=client -o yaml | kubectl apply -f -

# Re-apply ingress (if needed)
kubectl apply -f k8s/ingress.yaml
```

## Example smoke test (PowerShell snippet)

You can create a quick `smoke-test.ps1` that checks endpoints over HTTPS:

```powershell
$endpoints = @(
  'https://booking.local/auth/actuator/health',
  'https://booking.local/bookings/actuator/health',
  'https://booking.local/facilities/api/facilities',
  'https://booking.local/payments/actuator/health',
  'https://booking.local/calendar/actuator/health',
  'https://booking.local/notifications/actuator/health'
)

foreach ($url in $endpoints) {
  try {
    $r = Invoke-WebRequest -Uri $url -TimeoutSec 10
    Write-Host "$url -> $($r.StatusCode)"
  } catch {
    Write-Host "$url -> FAILED: $($_.Exception.Message)"
  }
}
```

Save this file under `scripts/` or similar and run after port-forward (or if your ingress exposes 443 on host).

---

If you'd like, I can add the `scripts/smoke-test.ps1` file to the repo next, or create a brief `k8s/tls/README-short.md` for quick copy-paste. Which would you prefer?
