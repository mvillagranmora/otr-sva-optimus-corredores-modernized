-- H2 copy of the real BAT.* columns the dispatcher reads (BAT_DDL_REAL.md, HANA QA 2026-10-21), with the real
-- quoted names so the production SQL of ProgramaJdbcAdapter runs unchanged on H2. SECONDDATE -> TIMESTAMP(0).

CREATE TABLE IF NOT EXISTS "BSCL.Plataforma.BD::BAT.TipoPrograma" (
    "IdTipoPrograma"      VARCHAR(40) PRIMARY KEY,
    "MaximoIntentos"      INTEGER,
    "NombreClasePrograma" VARCHAR(200),
    "TipoClasePrograma"   VARCHAR(40),
    "Nodo"                VARCHAR(1)
);

CREATE TABLE IF NOT EXISTS "BSCL.Plataforma.BD::BAT.Agenda" (
    "IdAgenda"       BIGINT PRIMARY KEY,
    "IdTipoPrograma" VARCHAR(40),
    "EsDemonio"      VARCHAR(1)
);

CREATE TABLE IF NOT EXISTS "BSCL.Plataforma.BD::BAT.Programa" (
    "IdPrograma"     BIGINT AUTO_INCREMENT PRIMARY KEY,
    "IdAgenda"       BIGINT DEFAULT 0,
    "IdTipoPrograma" VARCHAR(40),
    "Intento"        INTEGER DEFAULT 0,
    "idEstado"       VARCHAR(3),
    "Resultado"      VARCHAR(2000),
    "fechaCreacion"  TIMESTAMP(0),
    "fechaEjecucion" TIMESTAMP(0),
    "fechaFin"       TIMESTAMP(0),
    "Nodo"           VARCHAR(1),
    "Bloqueo"        VARCHAR(1)
);
