-- ============================================================
-- BcsFilters — H2 test seed data
-- ============================================================

-- -------- Users --------
-- Stored passwords: SHA-256 of "secret123"
-- SHA-256("secret123") = fcf730b6d95236ecd3c9fc2d92d7b6b2bb061514961aec041d6c7a7192f592e4
-- HybridPasswordEncoder.matches() accepts both SHA-256 (legacy) and BCrypt (new).

-- ACT user — normal login should succeed
INSERT INTO ACC_USUARIO (ID_USUARIO, CODIGO_USUARIO, ESTADO, FECHA_VIGENCIA, CLAVE, INTENTOS)
VALUES (1, 'usuario_act', 'ACT', DATEADD(DAY, 90, CURRENT_DATE),
        'fcf730b6d95236ecd3c9fc2d92d7b6b2bb061514961aec041d6c7a7192f592e4', 0);

-- BLO user — RULE-018: login must be denied even with correct password
INSERT INTO ACC_USUARIO (ID_USUARIO, CODIGO_USUARIO, ESTADO, FECHA_VIGENCIA, CLAVE, INTENTOS)
VALUES (2, 'usuario_blo', 'BLO', DATEADD(DAY, 90, CURRENT_DATE),
        'fcf730b6d95236ecd3c9fc2d92d7b6b2bb061514961aec041d6c7a7192f592e4', 0);

-- Expired account — login must be denied (FechaVigencia in past)
INSERT INTO ACC_USUARIO (ID_USUARIO, CODIGO_USUARIO, ESTADO, FECHA_VIGENCIA, CLAVE, INTENTOS)
VALUES (3, 'usuario_exp', 'ACT', DATEADD(DAY, -1, CURRENT_DATE),
        'fcf730b6d95236ecd3c9fc2d92d7b6b2bb061514961aec041d6c7a7192f592e4', 0);

-- Locked user — RULE-040: intentos=3, maxIntentos=3 → must be locked (>= fix)
INSERT INTO ACC_USUARIO (ID_USUARIO, CODIGO_USUARIO, ESTADO, FECHA_VIGENCIA, CLAVE, INTENTOS)
VALUES (4, 'usuario_max', 'ACT', DATEADD(DAY, 90, CURRENT_DATE),
        'fcf730b6d95236ecd3c9fc2d92d7b6b2bb061514961aec041d6c7a7192f592e4', 3);

-- -------- Sessions --------
-- Valid WEB session
INSERT INTO ACC_SESION (ID_TOKEN_SESION, TOKEN_VALIDO, ID_USUARIO, CANAL, FECHA_CREACION)
VALUES ('valid-token-123', 'S', 1, 'WEB', CURRENT_TIMESTAMP);

-- Valid COR session (recent — within 30 min timeout)
INSERT INTO ACC_SESION (ID_TOKEN_SESION, TOKEN_VALIDO, ID_USUARIO, CANAL, FECHA_CREACION)
VALUES ('valid-cor-token', 'S', 1, 'COR', CURRENT_TIMESTAMP);

-- Expired COR session (61 min ago — RULE-025: must be rejected)
INSERT INTO ACC_SESION (ID_TOKEN_SESION, TOKEN_VALIDO, ID_USUARIO, CANAL, FECHA_CREACION)
VALUES ('expired-cor-token', 'S', 1, 'COR', DATEADD(MINUTE, -61, CURRENT_TIMESTAMP));

-- Invalid / revoked session
INSERT INTO ACC_SESION (ID_TOKEN_SESION, TOKEN_VALIDO, ID_USUARIO, CANAL, FECHA_CREACION)
VALUES ('invalid-token', 'N', 1, 'WEB', CURRENT_TIMESTAMP);

-- -------- Services --------
-- Service 100: no access validation needed
INSERT INTO PLA_SERVICIO (ID_SERVICIO, VALIDA_ACCESO_SERVICIO) VALUES ('100', 'N');
-- Service 200: access validation required; valid-token-123 has a grant
INSERT INTO PLA_SERVICIO (ID_SERVICIO, VALIDA_ACCESO_SERVICIO) VALUES ('200', 'S');
-- Service 300: access validation required; valid-token-123 does NOT have a grant
INSERT INTO PLA_SERVICIO (ID_SERVICIO, VALIDA_ACCESO_SERVICIO) VALUES ('300', 'S');

-- -------- Configuration --------
INSERT INTO PLA_CONFIGURACION (CODIGO_PARAMETRO, VALOR) VALUES ('PLA.TipoValidaServicio', 'TODOS');
INSERT INTO PLA_CONFIGURACION (CODIGO_PARAMETRO, VALOR) VALUES ('COR_SESSION_TIMEOUT_MINUTES', '30');
INSERT INTO PLA_CONFIGURACION (CODIGO_PARAMETRO, VALOR) VALUES ('PLA.MaxIntentosPassword', '3');

-- -------- Access grants --------
-- valid-token-123 → service 200 (has access)
INSERT INTO ACC_ACCESO_SERVICIO (ID_TOKEN_SESION, ID_SERVICIO) VALUES ('valid-token-123', '200');
-- Note: no grant for service 300 → access denied (tests RULE-004 fix)
