# Payment Service Setup

## Stripe Configuration (Mock Mode vs Production)

### Mock Mode (Default - No Stripe Account Required)

By default, the Payment Service runs in **mock mode**, which simulates payment processing without requiring Stripe credentials. This is perfect for development and testing.

**No setup required!** Just run the service and it will work.

---

### Production Mode (Requires Stripe Account)

To enable real Stripe payments:

#### 1. Get Stripe API Keys

1. Create a Stripe account at https://stripe.com
2. Go to https://dashboard.stripe.com/test/apikeys
3. Copy your **Publishable key** and **Secret key** (use test keys for development)

#### 2. Configure Local Environment

**Option A: Using application-local.properties (Recommended)**

1. Copy the example file:

   ```bash
   cd services/payment-service/src/main/resources
   cp application-local.properties.example application-local.properties
   ```

2. Edit `application-local.properties` and add your keys:
   ```properties
   stripe.api.key=sk_test_your_actual_test_key_here
   stripe.webhook.secret=whsec_your_actual_webhook_secret_here
   ```

**Option B: Using Environment Variables**

```bash
# Windows PowerShell
$env:STRIPE_API_KEY="sk_test_your_actual_test_key_here"
$env:STRIPE_WEBHOOK_SECRET="whsec_your_actual_webhook_secret_here"

# Linux/Mac
export STRIPE_API_KEY="sk_test_your_actual_test_key_here"
export STRIPE_WEBHOOK_SECRET="whsec_your_actual_webhook_secret_here"
```

#### 3. Disable Mock Mode

Set in `application.properties` or environment:

```properties
payment.mock-mode=false
```

---

## Security Notes

⚠️ **NEVER commit real API keys to git!**

- `application-local.properties` is ignored by git
- Use environment variables for production deployments
- Use Stripe test keys (`sk_test_...`) for development
- Use Stripe live keys (`sk_live_...`) only in production

---

## Testing Payments

### Mock Mode

```http
POST http://localhost:8083/api/payments/checkout
POST http://localhost:8083/api/payments/mock/{sessionId}/complete
POST http://localhost:8083/api/payments/mock/{sessionId}/fail
```

### Stripe Test Mode

Use Stripe test card numbers:

- **Success:** `4242 4242 4242 4242`
- **Decline:** `4000 0000 0000 0002`
- **3D Secure:** `4000 0027 6000 3184`

[More test cards →](https://stripe.com/docs/testing)
