# ADR 0001: Global `window.fetch` Interceptor for Keycloak / OAuth2 302 Redirects

## Status
Accepted

## Date
2026-09-27

## Context
The Ticketing System operates behind an enterprise API Gateway (`apigw.homedesk.dpdns.org`) that enforces Keycloak OIDC/OAuth2 authentication via an authorization proxy.

When an authenticated session expires:
1. The API Gateway intercepts requests to `/ticketing/api/...` and issues an `HTTP 302 Found` redirect with a `Location` header pointing to Keycloak's authorization endpoint (`/protocol/openid-connect/auth?...`).
2. In a Single Page Application (SPA), data calls are initiated programmatically via client-side `fetch()`.
3. Per the W3C Fetch specification, browsers automatically and transparently follow HTTP 302 redirects within the background network request pipeline. JavaScript cannot inspect or stop the 302 directly, nor does the browser update the top-level window URL (`window.location`).
4. Keycloak responds to the background request with `200 OK` and its HTML login page (`Content-Type: text/html`).
5. When the client-side code calls `res.json()`, it fails with `SyntaxError: Unexpected token '<' in JSON`. The user remains stranded on an unresponsive UI with failed requests instead of being navigated to Keycloak to log back in.

## Decision
We implemented a **Global `window.fetch` Interceptor** in the frontend client ([`frontend/src/main.tsx`](file:///c:/Users/jeshu/Projects/ticketing-system/frontend/src/main.tsx)), reinforced by defensive checks in the API response handler ([`frontend/src/api.ts`](file:///c:/Users/jeshu/Projects/ticketing-system/frontend/src/api.ts)):

1. **Monkey-Patching `window.fetch`**:
   Before mounting the React DOM tree, `window.fetch` is wrapped globally to intercept all incoming HTTP responses.
2. **Detecting Auth Redirects**:
   - Checks `response.redirected && (response.url.includes('/openid-connect/auth') || response.url.includes('keycloak'))`.
   - Checks if an API request unexpectedly received `Content-Type: text/html` pointing to Keycloak login routes.
3. **Promoting to Top-Level Window Navigation**:
   When an authentication redirect is detected, the interceptor executes:
   ```ts
   window.location.href = response.url;
   return new Promise(() => {}); // Halts promise chain while browser unloads the document
   ```
   Returning a pending promise ensures that calling components do not throw syntax errors or display error toasts in the brief window during which the browser unloads the page.

## Alternatives Considered
1. **Changing API Gateway to return 401 Unauthorized for `/api/**`**:
   - *Pros*: Follows standard REST conventions where API endpoints return status codes rather than HTML redirects.
   - *Cons*: Requires reconfiguring the external API Gateway and maintaining path-matching rules (differentiating page visits from AJAX). If the gateway is shared across multiple applications, changing this behavior could impact other systems.
2. **Handling 302 on each API call individually**:
   - *Pros*: None.
   - *Cons*: High boilerplate, error-prone, and fragile when new endpoints or components are added.

## Consequences

### Positive
- **Universal Coverage**: Protects 100% of network requests across the application, including any future endpoints or third-party libraries using `fetch()`.
- **Decoupled from Gateway**: Allows the API Gateway to preserve its standard OAuth2 302 redirect behavior without requiring custom 401 overrides.
- **Flawless UX**: As soon as a user returns to an expired session and performs an action, they are immediately and seamlessly transitioned to the Keycloak login screen.
- **Zero Syntax Errors**: The pending promise eliminates console `SyntaxError: Unexpected token '<'` warnings during navigation.

### Negative / Trade-offs
- **Unsaved Drafts**: Any unsubmitted form input in the browser's memory is discarded when navigating away to Keycloak (acceptable given that the session was already expired on the server).
