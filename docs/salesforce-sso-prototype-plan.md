# Salesforce SSO Prototype Plan — SAP Commerce Storefront

## Goal
Turn this out-of-the-box SAP Commerce installation into a single-sign-on (SSO)
prototype where a **Salesforce sandbox acts as the SAML 2.0 Identity Provider (IdP)**
and the **SAP Commerce B2B storefront acts as the Service Provider (SP)**.

## Confirmed decisions
| Topic | Decision |
|-------|----------|
| Login surface | B2B storefront (`yb2bacceleratorstorefront`) |
| User type | `B2BCustomer` |
| Environment | localhost |
| Login mode | **SSO-only** (native username/password login disabled) |
| User provisioning | Just-in-time (JIT) creation of `B2BCustomer` on first login, matched by email |

## Context (current installation)
- **SAP Commerce 2211.14 (JDK21)** — build version `2211-jdk21.14`.
- B2B accelerator storefront (`yb2bacceleratorstorefront`, JSP-based).
- OOTB **`samlsinglesignon`** extension is present under
  `hybris/bin/modules/platform/samlsinglesignon` but **not yet enabled** in
  `hybris/config/localextensions.xml`.
- The extension is the modern **Spring Security SAML 2.0 SP**
  (registrationId-based, OpenSAML 5). Key default properties:
  - `sso.relyingPartyRegistration.registrationId=registrationId`
  - `sso.metadata.verification.enabled=true`
  - `sso.legacy.endpoints.enabled=true`
  - `samlsinglesignon.logout.redirect.url=/logout.jsp`
- No third-party SAML libraries required — the platform ships this capability.

## Authentication flow (SP-initiated SAML 2.0 Web Browser SSO)
```
Shopper → Storefront (SP-initiated)
   → redirect to Salesforce login
   → Salesforce authenticates user
   → signed SAML Response (assertion) POSTed to Commerce ACS endpoint
   → samlsinglesignon validates signature + consumes assertion
   → maps/creates B2BCustomer (JIT by email)
   → Spring Security session established
   → redirect back to storefront account/landing page
```
Prototype uses **signed assertions** and validates the IdP signature against the
imported Salesforce certificate.

---

## Implementation plan

### Phase 0 — Decisions & prep (DONE / confirmed)
- SP entityId / registrationId: use default `registrationId` unless a cleaner id is preferred.
- Base URL: localhost. Note the stable ACS URL the storefront exposes (must be
  reachable by the same browser that logs into Salesforce).
- Provisioning: JIT creation of `B2BCustomer`, UID = email.

### Phase 1 — Enable the SAML SP extension  ✅ DONE (2026-07-07)
1. Add `<extension name='samlsinglesignon' />` to `hybris/config/localextensions.xml`.
2. Generate an SP signing/encryption keystore (JKS/PKCS12) for the ACS and place
   it under the config; reference alias/password via extension properties.
3. Rebuild: `ant clean all`, then `ant updatesystem`
   (extension has `samlsinglesignon-items.xml`).

**Completed:**
- Enabled `samlsinglesignon` in `hybris/config/localextensions.xml`.
- Generated SP keystore (RSA 2048, self-signed, `CN=localhost`, valid 3650d) at
  `hybris/bin/modules/platform/samlsinglesignon/web/webroot/WEB-INF/security/samlKeystore.jks`
  — alias `hybris`, storepass/keypass `changeit` (matches extension defaults).
- Added SSO keystore + registrationId properties to `hybris/config/local.properties`.
- `ant clean all` — BUILD SUCCESSFUL (~4.5 min).
- `ant updatesystem` — BUILD SUCCESSFUL (~12 min); `SamlUserGroup` type +
  `SamlUserGroupToUserGroupRelation` created in the DB.
- Note: `updatesystem` stopped the Solr server on shutdown; it restarts with the
  next Commerce server boot.

### Phase 2 — Configure Salesforce as IdP (sandbox)
1. Salesforce Setup → **Identity Provider** → enable; download IdP metadata / X.509 cert.
2. Create a **Connected App** with SAML enabled:
   - **Entity Id** = SP entityId (matches Commerce registration).
   - **ACS URL** = Commerce SAML assertion-consumer endpoint (localhost URL).
   - **Subject / NameID** = email (or username).
   - SAML attributes: `email`, `firstName`, `lastName`, `groups`.
3. Assign a sandbox test user / profile to the Connected App.

### Phase 3 — Configure Commerce SP (relying party registration)
1. In `local.properties` (or a small config extension) set the relyingPartyRegistration:
   - IdP SSO URL, IdP entityId, IdP verification certificate.
   - SP entityId, ACS / assertion-consumer path.
   - SP keystore alias + password.
2. Load the Salesforce IdP cert so Commerce can verify the signature
   (`sso.metadata.verification.enabled=true`).
3. **Trust the SP in Salesforce.** Salesforce does *not* ingest an SP metadata
   XML file — you configure the SP by entering values manually. From the Commerce
   SP metadata, Salesforce needs: **Entity Id** and **ACS URL** (both entered in
   the Connected App). The SP **X.509 certificate** is only needed if you enable
   **Verify Request Signatures** or **Encrypt SAML Response** — for this prototype
   leave both **off**, so nothing extra is imported.

### Phase 4 — User mapping & JIT provisioning (B2BCustomer)  ✅ config applied
**No Java code needed** — the OOTB `DefaultSSOService` already JIT-creates any user
type from configuration. Key facts (verified by decompiling the extension):
- `DefaultSAMLService` reads uid/name/roles from SAML **attributes only** (no NameID
  fallback): `sso.userid.attribute.key`, `sso.firstname/lastname.attribute.key`,
  `sso.usergroup.attribute.key` (default `usergroup`).
- `DefaultSSOService.getOrCreateSSOUser(uid, name, roles)` looks up by uid; if absent
  creates the type from `sso.mapping.<role>.usertype` and assigns
  `sso.mapping.<role>.groups`. Requires uid **and** roles non-empty.

**Applied config (`local.properties`):**
```properties
sso.userid.attribute.key=email
sso.firstname.attribute.key=firstName
sso.lastname.attribute.key=lastName
sso.usergroup.attribute.key=usergroup
sso.mapping.b2bcustomer.usertype=B2BCustomer
sso.mapping.b2bcustomer.groups=b2bcustomergroup
```
Salesforce auto-sends `email`, `firstName`, `lastName`; you only add one custom
attribute `usergroup=b2bcustomer` (see values-and-checklist doc).

**Optional hardening (not required for login):** assign the JIT `B2BCustomer` a
default **B2BUnit** so full B2B storefront features (checkout, org management) work;
OOTB B2B customers belong to a unit (e.g. "Custom Retail") plus `b2bcustomergroup`.

### Phase 5 — Storefront integration (SSO-only)  ✅ DONE (2026-07-10)
1. Replace/redirect the storefront login page so it triggers the SP-initiated SAML
   flow (no native username/password form — SSO-only). ✅
2. Handle post-login redirect back to the storefront account/landing page. ✅
   (`/saml/` cookie-handoff → `sso.redirect.url` → custom `SamlSsoLoginFilter`.)
3. Handle **Single Logout (SLO)** — extension already wires `/saml/logout/**`;
   point storefront logout at it. ⚠ config in place, end-to-end logout not yet verified.

> Full write-up (flow, the cookie-handoff gotcha, why the custom filter, single-host
> requirement, files/props, verification): see **section 5c** of
> `salesforce-sso-values-and-checklist.md`.

### Phase 6 — Test & harden
1. End-to-end test with a Salesforce sandbox user: login → JIT `B2BCustomer`
   creation → session → logout.
2. Verify signature validation, ACS over the localhost URL, clock-skew tolerance,
   NameID/attribute mapping.
3. Negative tests: unsigned/tampered assertion rejected; unknown user handling;
   confirm native login is disabled (SSO-only).

---

## Key endpoints & files (reference)
- Extension: `hybris/bin/modules/platform/samlsinglesignon/`
- Security config: `.../web/webroot/WEB-INF/security/spring-security-config.xml`
- Spring wiring: `.../resources/samlsinglesignon-spring.xml`,
  `.../web/webroot/WEB-INF/samlsinglesignon-web-spring.xml`
- Items model: `.../resources/samlsinglesignon-items.xml`
- Config to edit: `hybris/config/localextensions.xml`, `hybris/config/local.properties`
- Logout filter path: `/saml/logout/**`

## Open items to nail down during implementation
- Exact localhost base URL / port + whether HTTPS is used for the ACS.
- Concrete SP entityId and registrationId naming.
- Which B2B unit / customer group new JIT users are assigned to.
- Keystore generation details (alias, validity, algorithm).

## Notes
- This is a real, supported OOTB path; `samlsinglesignon` is already in the platform.
- localhost prototype: Salesforce must be reachable from the same browser session;
  the ACS URL does not need to be publicly reachable by Salesforce servers for
  SP-initiated browser-POST SSO.
