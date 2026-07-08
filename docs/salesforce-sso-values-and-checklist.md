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

### Phase 4 additions (attribute mapping + JIT B2BCustomer) — already applied

```properties
# SAML attribute keys the IdP must send (non-reserved keys). No NameID fallback!
sso.userid.attribute.key=email
sso.firstname.attribute.key=firstName
sso.lastname.attribute.key=lastName
sso.usergroup.attribute.key=usergroup

# Role -> user type + Commerce groups. Salesforce sends usergroup=b2bcustomer.
sso.mapping.b2bcustomer.usertype=B2BCustomer
sso.mapping.b2bcustomer.groups=b2bcustomergroup
```

`DefaultSSOService.getOrCreateSSOUser(uid, name, roles)` then: looks up by uid via
`UserService.getUserForUID`; if absent, `modelService.create("B2BCustomer")`, sets
uid+name+password, assigns groups `[b2bcustomergroup]`, and saves. Existing users are
reused (groups re-applied). Both `uid` and `roles` must be non-empty or it throws.

**IdP metadata file placement:**
```
hybris/bin/modules/platform/samlsinglesignon/web/webroot/WEB-INF/security/metadata.xml
```
Download this from Salesforce (Identity Provider page → Download Metadata) in Phase 2.

> If you prefer to keep `sso.metadata.verification.enabled=true`, you must import the
> Salesforce metadata-signing certificate into `samlKeystore.jks` so the file
> signature validates. For a prototype, disabling it is simpler.

---

## 6. Verifying the SP metadata endpoint (after Phase 3 config + server boot)

Once the Commerce server is running with the IdP metadata in place:

```bash
curl -k https://localhost:9002/samlsinglesignon/saml2/service-provider-metadata/registrationId
```
Should return the SP `EntityDescriptor` XML (entityID `urn:ssoextension:hybris:de`,
the ACS at `.../login/saml2/sso/registrationId`, and the SP signing certificate).
Use this output to configure/verify the Salesforce side.

---

## 7. Open items still owned by later phases

- **Phase 4** — JIT `B2BCustomer` creation & mapping. The OOTB `sso.mapping.*`
  properties target `Employee`/ASM agents; storefront B2B customers need custom
  mapping logic (assertion `email` → `B2BCustomer.uid`, assign to a B2B unit/group).
- **Phase 5** — SSO-only storefront: redirect the storefront login to the
  SP-initiated URL (`.../saml2/authenticate/registrationId`) and disable the native
  login form; wire logout to the SLO endpoint.
- **HTTPS on localhost** — Commerce ships a self-signed SSL cert on 9002; the browser
  must accept it. The SP keystore cert is `CN=localhost` (Phase 1).
