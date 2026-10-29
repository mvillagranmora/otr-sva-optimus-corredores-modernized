# Rule Reviews — otr-sva-optimus-corredores

20 flagged P0 rules reviewed on 2026-10-02. **3 confirmed**, **17 marked wrong** (defects to fix), **0 under discussion**.

All 17 wrong rules are open questions for the `brief` phase: they will be listed as defects, not anchored in the Behavior Contract, and `transform`/`reimagine` will not pin them as oracles.

| Rule | Verdict | When | Title | Note |
|------|---------|------|-------|------|
| RULE-004 | ❌ wrong | 2026-10-02 | ServiceFilter `ch != null` bypasses all role authorization | This is a bug — only WS, CLI, and AGE should bypass. Condition should be `noAuthChanels.contains(ch)` without the `ch != null` guard. |
| RULE-005 | ❌ wrong | 2026-10-02 | ServiceFilter fail-open — exceptions grant access | Should fail-closed: return HTTP 500 or HTTP 401 on exception, never grant access on error. |
| RULE-011 | ✅ confirmed | 2026-10-02 | Login — pending authorization (EsAutorizado=P) blocks login | Correct — only 'P' blocks. Other non-'S' values fall through; 'S' is authorized, anything else is treated as authorized. |
| RULE-018 | ❌ wrong | 2026-10-02 | CLI entity with BLO status can log in | Bug — BLO customers should not be able to log in. Condition should be `!IdEstado.equals("ACT")` like EMP and PER paths. |
| RULE-025 | ❌ wrong | 2026-10-02 | COR channel sessions never expire | COR should have a configurable idle timeout like the other channels. The exemption is a defect. |
| RULE-030 | ❌ wrong | 2026-10-02 | Password hash inconsistency — login vs change-password | Real bug — server receives raw passwords and the two hash schemes produce different outputs. Users cannot log in after changing their password via the change-password path. |
| RULE-032 | ❌ wrong | 2026-10-02 | Password complexity — digit characters ignored by scanner | Digits should count as special characters, or there should be a separate minimum-digit rule. The `\W` regex is wrong for the policy. |
| RULE-035 | ❌ wrong | 2026-10-02 | Password history — cannot reuse any prior password | Only the last N passwords should be checked. The "last 10" comment is the intended limit; the SQL should enforce it. |
| RULE-040 | ❌ wrong | 2026-10-02 | Provisional password — off-by-one in max attempts check | Bug — lockout should fire at exactly `PLA.ClaveUsuarioIntentos` attempts, not max+1. |
| RULE-045 | ❌ wrong | 2026-10-02 | SSO login does not check account expiry (FechaVigencia) | Unfinished implementation — SSO should check FechaVigencia. The `--todo` comment confirms it was planned but never done. |
| RULE-046 | ❌ wrong | 2026-10-02 | SSO OAuth2 CSRF state parameter is hardcoded | Security defect — state must be a per-request cryptographically random value, generated at redirect time and validated in the callback. |
| RULE-047 | ✅ confirmed | 2026-10-02 | SSO session creation hardcoded to company ID 1 | Correct — system is single-company in production; company ID 1 is always right. |
| RULE-051 | ❌ wrong | 2026-10-02 | CallBatch fallback to hardcoded token "CALLBATCH" | Security defect — if config can't be read, reject the request (HTTP 503), not authenticate with a known-public default. |
| RULE-052 | ❌ wrong | 2026-10-02 | CallBatch job queue is LIFO (last-in, first-out) | Defect — batch jobs should be processed in FIFO order. LIFO could cause financial sequencing issues. |
| RULE-057 | ❌ wrong | 2026-10-02 | Job lock race condition — FOR UPDATE removed | Defect — race condition must be fixed. Restore row-level locking or implement optimistic locking. |
| RULE-059 | ❌ wrong | 2026-10-02 | Job retry call commented out in poolDiary | Defect — retry call should be active. The comment-out was a temporary debug change accidentally left in. |
| RULE-068 | ❌ wrong | 2026-10-02 | SQL injection in TransactionManager.executionCount | Must be fixed regardless of callers — use parameterized queries. String-concatenated SQL is never acceptable. |
| RULE-076 | ❌ wrong | 2026-10-02 | Hardcoded HANA credentials in Sesion.java dead code | Credentials must be rotated immediately (treat as compromised) and the main() method removed from source. |
| RULE-077 | ❌ wrong | 2026-10-02 | Static3DES encryption uses publicly known key | Security defect — production credentials must not use Static3DES. Rotate and replace with AES-256 or a managed-key scheme. |
| RULE-078 | ✅ confirmed | 2026-10-02 | SSO JWT id_token signature not verified | Acceptable — token comes directly from provider's token endpoint over TLS. Not verifying signature is standard practice in this server-to-server OAuth2 flow. |
