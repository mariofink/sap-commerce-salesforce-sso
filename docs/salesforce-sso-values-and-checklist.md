# Salesforce SSO — Exact Values & Setup Checklist

Companion to `salesforce-sso-prototype-plan.md`. Contains the concrete SP values,
endpoint URLs, the Salesforce Connected App checklist, and the Commerce-side
config for **Phase 2 & 3**.

> Prototype context: SAP Commerce 2211.14, `samlsinglesignon` SP (Spring Security
> SAML2 / OpenSAML5), B2B storefront, **localhost**, **SSO-only**, JIT `B2BCustomer`.

---

## 1. Fixed facts about this installation

| Item | Value | Source |
|------|-------|--------|
| Commerce HTTP port | `9001` | `bin/platform/project.properties` |
| Commerce SSL port | `9002` | `bin/platform/project.properties` |
| SAML SP webapp context root | `/samlsinglesignon` | `samlsinglesignon/extensioninfo.xml` (`webroot="/samlsinglesignon"`) |
| B2B storefront webapp | `/yb2bacceleratorstorefront` (site: `powertools`) | `local.properties` `website.powertools.*` |
| SP registrationId | `registrationId` | `sso.relyingPartyRegistration.registrationId` |
| SP Entity ID (default) | `urn:ssoextension:hybris:de` | `sso.entity.id` default |
| SP keystore | `.../samlsinglesignon/web/webroot/WEB-INF/security/samlKeystore.jks` | Phase 1 |
| SP keystore alias / pass | `hybris` / `changeit` | Phase 1 |

The SAML SP webapp is served on the Commerce Tomcat (port 9002) and is **not**
bound to a storefront vhost, so `localhost` works directly.

**Base URL used below:** `https://localhost:9002/samlsinglesignon`

---

## 2. Exact SP endpoint URLs (give these to Salesforce)

Endpoint URI patterns are hard-coded in `SamlObjectsFactory`
(`{registrationId}` = `registrationId`).

| Purpose | Full URL | Binding |
|---------|----------|---------|
| **SP metadata** (import into Salesforce) | `https://localhost:9002/samlsinglesignon/saml2/service-provider-metadata/registrationId` | — |
| **ACS** (Assertion Consumer Service — Salesforce POSTs the SAML Response here) | `https://localhost:9002/samlsinglesignon/login/saml2/sso/registrationId` | HTTP-POST |
| **SP-initiated login start** (entry point that redirects to Salesforce) | `https://localhost:9002/samlsinglesignon/saml2/authenticate/registrationId` | HTTP-Redirect |
| **Single Logout (SLO)** | `https://localhost:9002/samlsinglesignon/logout/saml2/slo` | HTTP-POST/Redirect |

> **These modern `/saml2/...` URLs only work with `sso.legacy.endpoints.enabled=false`**
> (see §5). With the extension default (`true`), only the legacy `/saml/*` URLs below
> are active and the modern metadata URL will instead redirect you to the IdP login.

Legacy endpoints (still enabled via `sso.legacy.endpoints.enabled=true`, provided
for older IdP configs — prefer the modern ones above):

| Purpose | Full URL |
|---------|----------|
| Legacy ACS | `https://localhost:9002/samlsinglesignon/saml/SSO` |
| Legacy metadata | `https://localhost:9002/samlsinglesignon/saml/metadata` |
| Legacy SLO | `https://localhost:9002/samlsinglesignon/saml/SingleLogout` |

---

## 3. SAML values summary for the Connected App

| Salesforce field | Value to enter |
|------------------|----------------|
| **Entity Id** | `urn:ssoextension:hybris:de` |
| **ACS URL** | `https://localhost:9002/samlsinglesignon/login/saml2/sso/registrationId` |
| **Subject Type / Name ID** | User's **email** (Name ID format: `emailAddress`) |
| **Name ID Format** | `urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress` |
| **Issuer** (Salesforce IdP) | from Salesforce Identity Provider page |
| **IdP Login URL** | from Salesforce Identity Provider page |
| **IdP Certificate** | download from Salesforce Identity Provider page |

**SAML attributes** (read by `DefaultSAMLService` — all values come from **attributes**,
NOT the NameID):

> **✅ Verified against a real Salesforce assertion:** Salesforce **automatically**
> emits these STANDARD attributes — no custom config needed: `userId` (SF internal
> id, *not* email), `username` (=email), `email`, `firstName`, `lastName`,
> `is_portal_user`, `amr`. This is also *why* `email`/`userId` are rejected as custom
> keys — they already exist. So reuse the standard `email`/`firstName`/`lastName`.
> There is **no group attribute** by default, so add **one custom** attribute
> `usergroup` (not reserved) with a static value to drive the role→type mapping.

| SAML attribute (in assertion) | Source | Commerce property | Commerce use |
|---|---|---|---|
| `email` (auto) | user's email | `sso.userid.attribute.key=email` | `B2BCustomer.uid` (login id) |
| `firstName` (auto) | `$User.FirstName` | `sso.firstname.attribute.key=firstName` | first name |
| `lastName` (auto) | `$User.LastName` | `sso.lastname.attribute.key=lastName` | last name |
| `usergroup` (**add as custom**) | static `"b2bcustomer"` | `sso.usergroup.attribute.key=usergroup` | role → mapping key (**required, non-empty**) |

The `usergroup` value (`b2bcustomer`) selects the mapping
`sso.mapping.b2bcustomer.usertype=B2BCustomer` + `.groups=b2bcustomergroup`, which
sets the JIT-created user's **type** and **Commerce user groups**.

---

## 4. Salesforce sandbox checklist (Phase 2)

Perform in the Salesforce **sandbox** org (Setup):

> **Note (Salesforce Spring '26+):** New **Connected App** creation is disabled by
> default in newer orgs — you'll see **External Client App Manager** instead of a
> "New Connected App" button. The SAML **IdP → external SP** fields (Enable SAML /
> Entity Id / ACS URL) we need live in the classic **Connected App** SAML section,
> so for this prototype re-enable classic Connected App creation (Option A). External
> Client Apps (Option B) also work but group the SAML settings differently and some
> SP options are still maturing.
>
> **Option A — re-enable classic Connected Apps (recommended):**
> Setup → Quick Find **"Connected Apps"** → enable the org setting that **allows
> creation of connected apps** (may appear as an org preference or a banner/toggle
> on the **App Manager** page). "New Connected App" then reappears in App Manager.
>
> **Option B — External Client App Manager:** Setup → **External Client App Manager**
> → **New External Client App** → enable SAML and enter the same values below.

- [ ] **Enable My Domain** (required before enabling the Identity Provider).
- [ ] **Setup → Identity → Identity Provider → Enable Identity Provider.**
      Choose/keep the self-signed or CA-signed cert.
- [ ] **Download** the IdP metadata (or at least the IdP **certificate**,
      **Issuer/Entity Id**, and **SSO login URL**). Save the metadata as
      `metadata.xml` — needed for Commerce Phase 3.
- [ ] Ensure you can create a SAML app (Option A or B above), then create the app:
  - [ ] Basic info: name, contact email.
  - [ ] Check **Enable SAML**.
  - [ ] **Entity Id** = `urn:ssoextension:hybris:de`
  - [ ] **ACS URL** = `https://localhost:9002/samlsinglesignon/login/saml2/sso/registrationId`
  - [ ] **Subject Type** = `Username` or **Custom Attribute → email**
        (must resolve to the user's email; Name ID format `emailAddress`).
  - [ ] **Name ID Format** = `emailAddress`.
  - [ ] **Custom Attributes:** Salesforce already sends `email`, `firstName`,
        `lastName` automatically — do NOT re-add them (reserved). Add only **one**
        custom attribute for the group:
        - `usergroup` = `"b2bcustomer"`  (static string, in quotes) → selects the mapping
  - [ ] (Optional but recommended) **Sign SAML Response / Assertion** = enabled,
        so Commerce can verify the signature with the IdP cert.
- [ ] **Save**, then **Manage → Profiles / Permission Sets**: assign the test
      user's profile so they're authorized for the app.
- [ ] Confirm at least one **sandbox test user** exists with a valid email.
- [ ] SP metadata is **not** uploaded to Salesforce — the SP is configured by the
      manually entered **Entity Id** + **ACS URL** (§3). Only enable/upload the SP
      certificate if you turn on **Verify Request Signatures** / **Encrypt SAML
      Response** — leave both **off** for this prototype.

---

## 5. Commerce-side config for Phase 3 (`hybris/config/local.properties`)

Add/adjust these (keystore props already added in Phase 1):

```properties
# --- SP identity ---
sso.entity.id=urn:ssoextension:hybris:de
sso.relyingPartyRegistration.registrationId=registrationId

# --- Endpoint style: modern vs legacy ---
# The extension defaults sso.legacy.endpoints.enabled=true, which activates the
# LEGACY /saml/* endpoints (ACS=/saml/SSO, metadata=/saml/metadata) and makes the
# modern /saml2/... URLs 404/redirect. Our Salesforce config + §2 URLs use the
# MODERN endpoints, so this MUST be false.
sso.legacy.endpoints.enabled=false

# --- IdP (Salesforce) metadata file ---
# Place the Salesforce IdP metadata.xml here (path relative to the samlsinglesignon webapp,
# i.e. .../samlsinglesignon/web/webroot/WEB-INF/security/metadata.xml)
sso.metadata.location=./WEB-INF/security/metadata.xml

# --- Metadata signature verification ---
# The extension validates the IdP metadata file signature against certs in the SP
# keystore. Salesforce IdP metadata is signed by the Salesforce cert, which is NOT
# in our SP keystore. For the localhost prototype, disable file-signature verification
# (assertion signatures are still validated via the IdP cert embedded in the metadata).
sso.metadata.verification.enabled=false

# --- Logout redirect target ---
samlsinglesignon.logout.redirect.url=/logout.jsp
```

### Phase 4 additions (attribute mapping + JIT B2BCustomer) — COMPLETED ✅

```properties
# SAML attribute keys the IdP must send (non-reserved keys). No NameID fallback!
sso.userid.attribute.key=email
sso.firstname.attribute.key=firstName
sso.lastname.attribute.key=lastName
sso.usergroup.attribute.key=usergroup

# Role -> user type + Commerce groups. Salesforce sends usergroup=b2bcustomer.
# NOTE: 'Rustic' (an existing B2BUnit) is included so the mandatory B2BUnit
# membership validation passes — see gotcha #1 below.
sso.mapping.b2bcustomer.usertype=B2BCustomer
sso.mapping.b2bcustomer.groups=b2bcustomergroup,Rustic
```

`DefaultSSOService.getOrCreateSSOUser(uid, name, roles)` then: looks up by uid via
`UserService.getUserForUID`; if absent, `modelService.create("B2BCustomer")`, sets
uid+name+password, assigns groups, and saves. Existing users are reused (groups
re-applied). Both `uid` and `roles` must be non-empty or it throws.

**IdP metadata file placement:**
```
hybris/bin/modules/platform/samlsinglesignon/web/webroot/WEB-INF/security/metadata.xml
```
Download this from Salesforce (Identity Provider page → Download Metadata) in Phase 2.

> If you prefer to keep `sso.metadata.verification.enabled=true`, you must import the
> Salesforce metadata-signing certificate into `samlKeystore.jks` so the file
> signature validates. For a prototype, disabling it is simpler.

---

## 5b. Phase 4 — full completion summary (what it actually took)

Getting a `B2BCustomer` created just-in-time from a successful Salesforce SAML
assertion required clearing **five** blockers, in the order the flow hits them.
End state: logging in as `mario.fink@goodyear.com` auto-creates a `B2BCustomer`.

### The five gotchas (in flow order)

1. **`AuthnFailed` from Salesforce** — the external/connected app had **no profile
   assigned**, so Salesforce refused to issue an assertion. Fix: assign the user's
   profile to the app in Salesforce. Symptom in SAML-tracer:
   `StatusCode Value=...status:AuthnFailed` with no assertion/attributes.

2. **Reserved attribute keys** — Salesforce rejects `email`/`userId` as *custom*
   attribute keys ("Reserved attribute key"). Salesforce already sends `email`,
   `firstName`, `lastName`, `username`, `userId` as **standard** attributes, so we
   simply point the `sso.*.attribute.key` properties at those standard names and add
   one genuinely custom attribute `usergroup`. (`sso.userid.attribute.key=email`,
   because `DefaultSAMLService` reads the uid from a SAML *attribute* by key and does
   **not** fall back to the NameID.)

3. **Assertion `Issuer` ≠ metadata `entityID` → "Invalid signature / Failed to
   establish trust".** The Salesforce app had an *Issuer override* of
   `https://localhost:9002`, but the IdP metadata `entityID` was the my.domain URL
   (`https://brave-otter-...my.salesforce.com`). OpenSaml selects the IdP signing
   cert by matching the assertion Issuer to a metadata `entityID`; a mismatch means
   no cert is found and trust fails. Fix: remove/align the Issuer override in
   Salesforce so the assertion Issuer equals the metadata `entityID`.

4. **`SessionIndex` NPE → "Authentication failed … since null".** OOTB
   `SamlObjectsFactory.adjustPrincipalWithSessionIndex` does
   `List.of(statement.getSessionIndex())`, which NPEs (with a null message) when the
   assertion carries **no** `SessionIndex`. Salesforce omits `SessionIndex` unless
   **Single Logout** / "Include Session Index in SAML Assertion" is enabled on the
   app. Fix: enable that toggle in Salesforce. (This is a platform bug we worked
   around via IdP config rather than patching the extension.)

5. **`MissingMandatoryAttributesException: [email]` on save.** `B2BCustomer.email` is
   a **mandatory** attribute (`MandatoryAttributesValidator`), but
   `DefaultSSOService.createNewUser` only sets `uid`, `name`, `password`, and groups —
   it never sets `email`. So the JIT save always failed *after* a fully successful
   SAML authentication. **This is the blocker that required custom code** (see below).

Also worth recording (not an error, but required): a `B2BCustomer` must belong to a
**B2BUnit** (`B2BCustomerModelValidateInterceptor`, key
`error.b2bcustomer.b2bunit.missing`). `B2BUnitService.getParent()` looks for a
B2BUnit among the customer's groups, so we add the existing B2BUnit uid **`Rustic`**
to `sso.mapping.b2bcustomer.groups`. Without it the save fails even with email set.

### The custom extension `sfssocustom` — and why it was necessary

The only thing that could not be solved by configuration or Salesforce settings was
setting the mandatory `email` on the JIT-created `B2BCustomer`. `DefaultSSOService`
is not configurable for extra attributes, so we need a **model `PrepareInterceptor`**
on `B2BCustomer` that defaults `email = uid` (the uid *is* the email/login):

```java
// sfssocustom/src/com/sap/sfssocustom/interceptor/B2BCustomerEmailPrepareInterceptor.java
public class B2BCustomerEmailPrepareInterceptor implements PrepareInterceptor<B2BCustomerModel> {
    public void onPrepare(B2BCustomerModel c, InterceptorContext ctx) {
        if (StringUtils.isBlank(c.getEmail()) && StringUtils.isNotBlank(c.getUid())) {
            c.setEmail(c.getUid());
        }
    }
}
```

**Why a brand-new extension instead of reusing `yb2bacceleratorstorefront`:**

- A **model interceptor must be registered in the platform-wide (global) Spring
  application context** so it fires on **every** `modelService.save(B2BCustomer)`
  regardless of which webapp/thread triggers it.
- The JIT save happens **inside the `samlsinglesignon` webapp**, *not* the storefront
  webapp.
- `yb2bacceleratorstorefront` is effectively **web-only**: its Spring config lives in
  `web/webroot/WEB-INF/...-web-spring.xml`, a **web application context scoped to the
  storefront webapp**. A bean placed there would never run during the save that
  occurs in the `samlsinglesignon` webapp — so the interceptor simply wouldn't fire.
- To reuse it we'd have to bolt a core module + a global
  `resources/yb2bacceleratorstorefront-spring.xml` onto an accelerator-generated
  extension, which is messier than a small dedicated extension.

So `sfssocustom` provides exactly what's needed: a **core module** whose
`resources/sfssocustom-spring.xml` is the **global** context, the correct home for a
platform-level model interceptor. It's also decoupled from the storefront web layer
and reusable by any webapp.

### Files/config that make up `sfssocustom`

| File | Purpose |
|---|---|
| `src/com/sap/sfssocustom/interceptor/B2BCustomerEmailPrepareInterceptor.java` | Sets `email = uid` when blank |
| `resources/sfssocustom-spring.xml` | Registers the interceptor **bean** + an `InterceptorMapping` (`typeCode=B2BCustomer`) in the global context |
| `extensioninfo.xml` | `<requires-extension name="b2bcommerce"/>` so `B2BCustomerModel` is on the classpath |
| `config/localextensions.xml` | `<extension name='sfssocustom' />` added after `samlsinglesignon` |

Build: `ant clean all` (no `updatesystem` needed — no new item types). Then restart.
The interceptor requires no data model change, so no system update.

### How Phase 4 success was verified

- SAML-tracer showed `StatusCode ...status:Success` with the expected attributes
  (`email`, `firstName`, `lastName`, `usergroup`, plus Salesforce standards).
- Server log (DEBUG on `de.hybris.platform.samlsinglesignon`,
  `org.springframework.security`, `org.opensaml`): the failing run logged
  `MissingMandatoryAttributesException: [email]`; the fixed run did **not**, and the
  immediate follow-up request no longer logged `Cannot find user with uid ...`
  (i.e. the customer now existed).
- Flexible search confirms the row (note: `B2BCustomer` has **no `email` column** in
  the query projection — select `uid`/`itemtype` only):
  ```sql
  SELECT {uid},{itemtype} FROM {User} WHERE {uid}='mario.fink@goodyear.com'
  ```

---

## 5c. Phase 5 — SSO-only storefront login + JIT session (what it actually took) — COMPLETED ✅

**Goal:** the `yb2bacceleratorstorefront` (powertools) storefront has no native
username/password form. Hitting `/login` bounces the user to Salesforce; on return the
user is logged into the storefront session as the JIT-provisioned `B2BCustomer`.

### The end-to-end flow (as built)

1. **Storefront `/login` (anonymous)** → `LoginPageController.doLogin` redirects to the
   SP-initiated SAML URL. Because the endpoint lives in **another webapp**
   (`/samlsinglesignon`), the redirect is built as an **absolute** `scheme://host:port/...`
   URL (`buildAbsoluteSsoLoginUrl`). A context-relative `redirect:/...` would get the
   storefront context path prepended and loop back to `/login`.
2. **SP-init → Salesforce → ACS** `/samlsinglesignon/login/saml2/sso/registrationId`.
   `Saml2WebSsoAuthenticationFilter` authenticates and immediately fires its success
   handler, which **redirects and stops the filter chain**.
3. **Success redirect → `/samlsinglesignon/saml/`** (via `sso.success.redirect.url=/saml/`;
   the handler prepends the `/samlsinglesignon` context). On *this* follow-up request the
   SAML auth is restored from session (`security-context-explicit-save="false"`), so it is
   authenticated and the request reaches:
   - **`saml2UserFilter`** (in the hybris `PlatformFilterChain`) → calls
     `DefaultSamlLoginService.storeLoginToken` → `UserManager.storeLoginTokenCookie`
     → writes the single-use **`samlPassThroughToken`** cookie (path `/`, Secure, 60s).
   - **`RedirectionController`** (`@RequestMapping /saml/**`) → redirects to
     **`sso.redirect.url`** = the storefront home.
4. **Storefront home** (`/yb2bacceleratorstorefront/powertools/de/USD/`) → the custom
   **`SamlSsoLoginFilter`** finds the `samlPassThroughToken` cookie, decodes it
   (`new CookieBasedLoginToken(cookie)` → `token.getUser().getUid()`), and logs the user
   into the storefront session. The single-use cookie is then erased.

### The critical gotcha — where the cookie is created

`saml2UserFilter` is the **only** thing that writes the pass-through cookie, and it runs
**only on a `/samlsinglesignon/...` request that carries the SAML auth in session** — never
on the ACS request itself (the SAML auth filter sends its success redirect and terminates
the chain before the platform filter chain runs). Therefore the success redirect **must**
target a samlsinglesignon URL first. Redirecting the ACS success **straight to the
storefront** skips cookie creation entirely → the storefront filter never sees a cookie →
infinite `login ⇄ Salesforce ⇄ storefront` loop. The fix is the two-hop
`/saml/` → `sso.redirect.url` flow above (the OOTB `RedirectionController` is active in
modern mode too — it is not gated by `sso.legacy.endpoints.enabled`).

### Why a custom `SamlSsoLoginFilter` (not an OOTB consumer)

The B2B accelerator storefront has **no** OOTB consumer for the `samlPassThroughToken`
cookie (its `AcceleratorRememberMeServices` uses a different token format). The filter
mirrors the ASM `AssistedServiceFilter` / `DefaultAutoLoginStrategy` pattern. Two subtle
requirements learned the hard way:

- **The authentication principal must be the username `String`, not the `UserDetails`
  object.** `AcceleratorRememberMeServices.retrievePassword` does
  `authentication.getPrincipal().toString()` and looks that up as a uid, exactly like the
  platform `CoreAuthenticationProvider.createSuccessAuthentication` (which uses
  `user.getUsername()`). Passing a `CoreUserDetails` object throws
  `Cannot find user with uid 'CoreUserDetails[...]'`.
- **Set the hybris session user explicitly** via
  `userService.setCurrentUser(userService.getUserForUID(uid))` (the `AuthenticationManager`
  is bypassed for SSO), then `SecurityContextHolder.setAuthentication`,
  `customerFacade.loginSuccess()`, `guidCookieStrategy.setCookie`,
  `rememberMeServices.loginSuccess(request, response, authentication)`.

### Single-host requirement

The powertools site is bound only to `powertools.local`
(`website.powertools.https=https://powertools.local:9002/yb2bacceleratorstorefront`). SAML
sessions and the `samlPassThroughToken` cookie are per-host, so the **entire** flow
(storefront + samlsinglesignon + Salesforce ACS + `sso.redirect.url`) must use
`powertools.local`. The Salesforce **ACS URL** was set to
`https://powertools.local:9002/samlsinglesignon/login/saml2/sso/registrationId`
accordingly. `sso.entity.id=urn:ssoextension:hybris:de` is a host-independent URN and is
unchanged. The TLS cert on :9002 is `CN=localhost`, so the browser warns on
`powertools.local` — accept it for the prototype.

### Files / config that make up Phase 5

| File | Purpose |
|---|---|
| `yb2bacceleratorstorefront/web/src/.../security/SamlSsoLoginFilter.java` | **NEW.** Consumes the `samlPassThroughToken` cookie and establishes the storefront session |
| `yb2bacceleratorstorefront/web/src/.../controllers/pages/LoginPageController.java` | SSO-only: anonymous → absolute SP-init URL; authenticated → success redirect |
| `yb2bacceleratorstorefront/web/webroot/WEB-INF/config/spring-security-config.xml` | Registers `samlSsoLoginFilter` (`before ANONYMOUS_FILTER`); configurable logout target |
| `samlsinglesignon/web/webroot/WEB-INF/security/spring-security-config.xml` | OOTB edit: `successRedirectHandler` `defaultTargetUrl` → `${sso.success.redirect.url:/}` |
| `config/local.properties` | `sso.success.redirect.url=/saml/`, `sso.redirect.url=<storefront home>`, `storefront.sso.login.url`, `storefront.sso.logout.url` |

Key properties:

```properties
# ACS success → samlsinglesignon /saml/ (writes the samlPassThroughToken cookie via
# saml2UserFilter), then RedirectionController forwards to sso.redirect.url.
sso.success.redirect.url=/saml/
sso.redirect.url=https://powertools.local:9002/yb2bacceleratorstorefront/
# Storefront /login → SP-initiated SAML (no native form)
storefront.sso.login.url=/samlsinglesignon/saml2/authenticate/registrationId
storefront.sso.logout.url=/?logout=true
```

Build: `ant build` in `yb2bacceleratorstorefront` (Java changed). The samlsinglesignon XML
+ property changes need only a **restart**. Editing the OOTB `samlsinglesignon` webapp is
acceptable for the prototype (it cannot be overridden from the storefront webapp — different
Spring context); the change is config-driven via a property placeholder.

### How Phase 5 success was verified

- Browser: after Salesforce sign-in the user lands on the powertools storefront home,
  **logged in** as the `B2BCustomer` (no redirect loop, no HAC).
- Server log (INFO on `de.hybris.platform.yb2bacceleratorstorefront.security`):
  ```
  SamlSsoLoginFilter  SAML SSO cookie 'samlPassThroughToken' present on request to
                      '/yb2bacceleratorstorefront/powertools/de/USD/' - attempting storefront login
  SamlSsoLoginFilter  Established storefront SSO session for user 'mario.fink@goodyear.com'
  ```

---



Once the Commerce server is running with the IdP metadata in place:

```bash
curl -k https://localhost:9002/samlsinglesignon/saml2/service-provider-metadata/registrationId
```
Should return the SP `EntityDescriptor` XML (entityID `urn:ssoextension:hybris:de`,
the ACS at `.../login/saml2/sso/registrationId`, and the SP signing certificate).
Use this output to configure/verify the Salesforce side.

---

## 7. Open items still owned by later phases

- **Phase 4** — ✅ **DONE.** JIT `B2BCustomer` creation works end-to-end via the
  `sso.mapping.*` config plus the `sfssocustom` email interceptor and the `Rustic`
  B2BUnit membership. See section 5b for the full write-up.
- **Phase 5** — ✅ **DONE.** SSO-only storefront login + JIT session established via the
  custom `SamlSsoLoginFilter` and the `/saml/` → `sso.redirect.url` cookie-handoff flow.
  See section 5c for the full write-up. Logout/SLO wiring is in place (config-driven) but
  end-to-end logout is **not yet verified**.
- **HTTPS on localhost** — Commerce ships a self-signed SSL cert on 9002; the browser
  must accept it. The SP keystore cert is `CN=localhost` (Phase 1).
