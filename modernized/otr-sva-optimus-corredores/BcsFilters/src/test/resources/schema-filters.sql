-- ============================================================
-- BcsFilters — H2 test schema
-- Modernized table names (replaces HANA catalog notation).
-- ============================================================

CREATE TABLE IF NOT EXISTS ACC_SESION (
    ID_TOKEN_SESION  VARCHAR(255) PRIMARY KEY,
    TOKEN_VALIDO     CHAR(1)      NOT NULL DEFAULT 'N',
    ID_USUARIO       BIGINT,
    CANAL            VARCHAR(20),
    FECHA_CREACION   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ACC_USUARIO (
    ID_USUARIO       BIGINT PRIMARY KEY,
    CODIGO_USUARIO   VARCHAR(100) NOT NULL,
    ESTADO           VARCHAR(10)  NOT NULL,
    FECHA_VIGENCIA   DATE,
    CLAVE            VARCHAR(255),
    INTENTOS         INT          NOT NULL DEFAULT 0
);

-- Service registry
CREATE TABLE IF NOT EXISTS PLA_SERVICIO (
    ID_SERVICIO              VARCHAR(20) PRIMARY KEY,
    VALIDA_ACCESO_SERVICIO   CHAR(1)     NOT NULL DEFAULT 'N'
);

-- Platform configuration
CREATE TABLE IF NOT EXISTS PLA_CONFIGURACION (
    CODIGO_PARAMETRO  VARCHAR(100) PRIMARY KEY,
    VALOR             VARCHAR(500)
);

-- Service access grants (simplified — legacy used role/profile tree resolved per HANA query)
CREATE TABLE IF NOT EXISTS ACC_ACCESO_SERVICIO (
    ID_TOKEN_SESION  VARCHAR(255) NOT NULL,
    ID_SERVICIO      VARCHAR(20)  NOT NULL,
    PRIMARY KEY (ID_TOKEN_SESION, ID_SERVICIO)
);

-- SSO CSRF state store (RULE-046)
CREATE TABLE IF NOT EXISTS SSO_STATE (
    STATE_KEY   VARCHAR(36)  PRIMARY KEY,
    CREATED_AT  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
