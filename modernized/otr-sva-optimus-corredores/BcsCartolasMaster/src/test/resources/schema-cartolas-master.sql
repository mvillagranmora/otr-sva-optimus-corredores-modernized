-- Schema H2 in-memory para tests de BcsCartolasMaster
-- Equivalente H2-compatible de BSCL.Normativa.BD::CAR.Config y CAR.Parametro

CREATE TABLE IF NOT EXISTS car_config (
    id_config  BIGINT       NOT NULL PRIMARY KEY,
    id_empresa BIGINT       NOT NULL,
    descripcion VARCHAR(255),
    tipo        VARCHAR(100),
    nombre      VARCHAR(255) NOT NULL,
    valor       VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS car_parametro (
    id_parametro BIGINT       NOT NULL PRIMARY KEY,
    id_nomina    BIGINT       NOT NULL,
    nombre       VARCHAR(255) NOT NULL,
    valor        VARCHAR(1000)
);
