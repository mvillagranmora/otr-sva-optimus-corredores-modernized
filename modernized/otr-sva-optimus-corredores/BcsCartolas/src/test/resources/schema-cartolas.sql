-- Schema H2 in-memory para tests de BcsCartolas
-- Equivalente H2-compatible de tablas HANA CAR.* y auxiliares

-- Configuración por empresa
CREATE TABLE IF NOT EXISTS car_config (
    id_config   BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
    id_empresa  BIGINT       NOT NULL,
    descripcion VARCHAR(255),
    tipo        VARCHAR(100),
    nombre      VARCHAR(255) NOT NULL,
    valor       VARCHAR(1000)
);

-- Parámetros de nómina
CREATE TABLE IF NOT EXISTS car_parametro (
    id_parametro BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
    id_nomina    BIGINT       NOT NULL,
    nombre       VARCHAR(255) NOT NULL,
    valor        VARCHAR(1000)
);

-- Nóminas de cartolas
CREATE TABLE IF NOT EXISTS car_nomina (
    id_nomina                   BIGINT       NOT NULL PRIMARY KEY AUTO_INCREMENT,
    id_empresa                  BIGINT       NOT NULL,
    id_usuario                  BIGINT       NOT NULL DEFAULT 0,
    id_plantilla                BIGINT       NOT NULL DEFAULT 0,
    periodo                     VARCHAR(7),
    fecha_desde                 DATE,
    fecha_hasta                 DATE,
    comentario                  VARCHAR(500),
    certificada                 VARCHAR(2)   NOT NULL DEFAULT 'NO',
    fecha_certificacion         DATE,
    id_usuario_certificador     BIGINT       NOT NULL DEFAULT 0,
    nombre_usuario_certificador VARCHAR(200),
    nota_certificacion          VARCHAR(500),
    tipo_proceso                VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',
    estado_proceso              VARCHAR(20)  NOT NULL DEFAULT 'INGRESADO',
    fecha_programada_proceso    DATE,
    fecha_inicio_proceso        DATE,
    fecha_termino_proceso       DATE,
    mensaje_proceso             VARCHAR(2000)
);

-- Cartolas individuales por portafolio/cliente
CREATE TABLE IF NOT EXISTS car_cartola (
    id_cartola     BIGINT      NOT NULL PRIMARY KEY AUTO_INCREMENT,
    id_nomina      BIGINT      NOT NULL,
    id_cliente     BIGINT      NOT NULL,
    id_portafolio  BIGINT      NOT NULL,
    reprocesar     VARCHAR(2)  NOT NULL DEFAULT 'NO',
    notificar      VARCHAR(2)  NOT NULL DEFAULT 'NO',
    estado_proceso VARCHAR(20),
    mensaje_proceso VARCHAR(2000)
);

-- Datos CSV por cartola y datasource
CREATE TABLE IF NOT EXISTS car_datos (
    id_datos   BIGINT        NOT NULL PRIMARY KEY AUTO_INCREMENT,
    id_cartola BIGINT        NOT NULL,
    nombre     VARCHAR(255)  NOT NULL,
    formato    VARCHAR(20)   NOT NULL,
    valor      CLOB
);

-- Trazas de proceso por nómina
CREATE TABLE IF NOT EXISTS car_traza (
    id_traza   BIGINT        NOT NULL PRIMARY KEY AUTO_INCREMENT,
    id_nomina  BIGINT        NOT NULL,
    mensaje    VARCHAR(2000),
    timestamp  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP()
);

-- Plantillas de generación
CREATE TABLE IF NOT EXISTS car_plantilla (
    id_plantilla  BIGINT      NOT NULL PRIMARY KEY AUTO_INCREMENT,
    id_empresa    BIGINT      NOT NULL,
    nombre        VARCHAR(255) NOT NULL,
    vigente_desde DATE,
    vigente       VARCHAR(2)  NOT NULL DEFAULT 'SI'
);

-- Recursos de plantilla
CREATE TABLE IF NOT EXISTS car_recurso (
    id_recurso   BIGINT        NOT NULL PRIMARY KEY AUTO_INCREMENT,
    id_plantilla BIGINT        NOT NULL,
    editable     VARCHAR(2)    NOT NULL DEFAULT 'NO',
    descripcion  VARCHAR(500),
    grupo        VARCHAR(100),
    nombre       VARCHAR(255),
    tipo         VARCHAR(50)   NOT NULL,
    valor        CLOB
);

-- Vista de clientes (NOR.ConsultarCliente)
CREATE TABLE IF NOT EXISTS vw_cliente (
    id_cliente         BIGINT       NOT NULL PRIMARY KEY,
    razon_social       VARCHAR(200),
    nombres            VARCHAR(200),
    apellido_paterno   VARCHAR(100),
    apellido_materno   VARCHAR(100),
    documento_identidad VARCHAR(30),
    email              VARCHAR(200)
);

-- Portafolios (POR.Portafolio)
CREATE TABLE IF NOT EXISTS por_portafolio (
    id_portafolio    BIGINT      NOT NULL PRIMARY KEY,
    codigo_portafolio VARCHAR(50) NOT NULL
);
