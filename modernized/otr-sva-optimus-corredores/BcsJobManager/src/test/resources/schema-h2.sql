-- H2 test schema for BcsJobManager integration tests
-- Mirrors the HANA BAT tables using H2-compatible syntax

CREATE TABLE IF NOT EXISTS bat_tipo_programa (
    id_tipo_programa    VARCHAR(20) PRIMARY KEY,
    tipo_clase_programa VARCHAR(100),
    nombre_clase_programa VARCHAR(255),
    maximo_intentos     INT NOT NULL DEFAULT 3
);

CREATE TABLE IF NOT EXISTS bat_agenda (
    id_agenda                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_tipo_programa           VARCHAR(20),
    es_demonio                 VARCHAR(1) DEFAULT 'N',
    habilitado                 VARCHAR(1) DEFAULT 'S',
    nombre_clase_programa      VARCHAR(255),
    tipo_clase_programa        VARCHAR(100),
    siguiente_fecha_ejecucion  VARCHAR(30),
    ultima_fecha_ejecucion     VARCHAR(30),
    dia_de_mes                 VARCHAR(10),
    mes                        VARCHAR(10),
    hora                       VARCHAR(10),
    minuto                     VARCHAR(10) DEFAULT '**',
    solo_dias_habiles          VARCHAR(1) DEFAULT 'N',
    solo_fin_de_mes            VARCHAR(1) DEFAULT 'N'
);

CREATE TABLE IF NOT EXISTS bat_programa (
    id_programa      BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_estado        VARCHAR(10) DEFAULT 'PEN',
    id_tipo_programa VARCHAR(20),
    intento          INT DEFAULT 0,
    bloqueo          VARCHAR(1) DEFAULT 'N',
    nodo             VARCHAR(50),
    fecha_creacion   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_ejecucion  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_fin        TIMESTAMP,
    fecha_sistema    TIMESTAMP,
    resultado        VARCHAR(2000),
    id_agenda        BIGINT
);

CREATE TABLE IF NOT EXISTS bat_param_programa (
    id_programa      BIGINT,
    nombre_parametro VARCHAR(100),
    valor            VARCHAR(2000),
    valor_blob       BINARY VARYING(65536)
);

CREATE TABLE IF NOT EXISTS bat_nodos (
    nodo                VARCHAR(50),
    job                 VARCHAR(50),
    pid                 INT,
    fecha_ultimo_ciclo  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reiniciar           VARCHAR(1) DEFAULT 'N',
    PRIMARY KEY (nodo, job)
);

CREATE TABLE IF NOT EXISTS pla_heartbeat (
    id              VARCHAR(100) PRIMARY KEY,
    last_timestamp  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
