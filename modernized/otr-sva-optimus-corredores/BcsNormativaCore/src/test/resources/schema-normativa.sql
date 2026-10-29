-- H2 schema for BcsNormativaCore tests
-- HANA equivalents shown in comments

-- HANA: "BSCL.Normativa.BD::DOC.Esquema"
CREATE TABLE IF NOT EXISTS ESQUEMA (
    Esquema                 VARCHAR(100)    NOT NULL,
    Version                 VARCHAR(20)     NOT NULL,
    Modulo                  VARCHAR(50),
    FechaPresentacionLimite DATE,
    IdEntidadReguladora     VARCHAR(50),
    Objetivo                VARCHAR(500),
    Periodicidad            VARCHAR(50),
    PeriodoInicio           DATE,
    CodigoVersionSII        INTEGER         DEFAULT 0,
    FechaDatosDesde         DATE,
    FechaDatosHasta         DATE,
    PRIMARY KEY (Esquema, Version)
);

-- HANA: "BSCL.Normativa.BD::DOC.Campo"
CREATE TABLE IF NOT EXISTS CAMPO (
    Esquema             VARCHAR(100)    NOT NULL,
    Version             VARCHAR(20)     NOT NULL,
    Nombre              VARCHAR(100)    NOT NULL,
    CodigoSII           VARCHAR(20),
    Titulo              VARCHAR(200),
    TipoDato            VARCHAR(50),
    Largo               SMALLINT        DEFAULT 0,
    CantidadDecimales   SMALLINT        DEFAULT 0,
    ValorDefecto        VARCHAR(200),
    Obligatorio         CHAR(1)         DEFAULT 'N',
    Editable            CHAR(1)         DEFAULT 'S',
    ModoMasivo          CHAR(1)         DEFAULT 'N',
    Exportable          CHAR(1)         DEFAULT 'S',
    OcultoEnGrilla      CHAR(1)         DEFAULT 'N',
    OcultoEnMantenedor  CHAR(1)         DEFAULT 'N',
    Comun               CHAR(1)         DEFAULT 'N',
    Grupo               VARCHAR(100),
    Clave               CHAR(1)         DEFAULT 'N',
    Orden               SMALLINT        DEFAULT 0,
    OrdenCargaExterna   SMALLINT        DEFAULT 0,
    TipoDiccionario     VARCHAR(50),
    PRIMARY KEY (Esquema, Version, Nombre)
);

-- HANA: "BSCL.Normativa.BD::DOC.Documento"
CREATE TABLE IF NOT EXISTS DOCUMENTO (
    IdDocumento                         BIGINT          NOT NULL,
    IdTipoDocumento                     VARCHAR(50),
    Esquema                             VARCHAR(100),
    Version                             VARCHAR(20),
    IdEmpresa                           BIGINT,
    FechaLimite                         DATE,
    IdEstadoDocumento                   VARCHAR(20),
    FechaCreacion                       DATE,
    FechaPresentacion                   DATE,
    DocumentoIdentidadDeclarante        VARCHAR(20),
    DocumentoIdentidadRepresentante     VARCHAR(20),
    DireccionElectronicaDeclaracion     VARCHAR(200),
    DireccionFisicaDeclaracion          VARCHAR(200),
    ComunaDeclaracion                   VARCHAR(100),
    TelefonoDeclaracion                 VARCHAR(20),
    FolioDeclaracion                    VARCHAR(50),
    EnProceso                           CHAR(1)         DEFAULT 'N',
    FechaInicioUltimoProceso            TIMESTAMP,
    PRIMARY KEY (IdDocumento)
);

-- HANA: "BSCL.Normativa.BD::DOC.Validacion"
CREATE TABLE IF NOT EXISTS VALIDACION (
    Esquema     VARCHAR(100)    NOT NULL,
    Version     VARCHAR(20)     NOT NULL,
    Codigo      VARCHAR(50)     NOT NULL,
    Tipo        VARCHAR(50),
    Activa      CHAR(1)         DEFAULT 'S',
    Descripcion VARCHAR(500),
    Severidad   VARCHAR(20),
    Formula     VARCHAR(2000),
    PRIMARY KEY (Esquema, Version, Codigo)
);

-- HANA: "BSCL.Normativa.BD::DOC.Clasificador"
CREATE TABLE IF NOT EXISTS CLASIFICADOR (
    Esquema     VARCHAR(100)    NOT NULL,
    Version     VARCHAR(20)     NOT NULL,
    Tipo        VARCHAR(100)    NOT NULL,
    Descripcion VARCHAR(500),
    PRIMARY KEY (Esquema, Version, Tipo)
);

-- HANA: "BSCL.Normativa.BD::DOC.CampoClasificador"
CREATE TABLE IF NOT EXISTS CAMPO_CLASIFICADOR (
    Esquema             VARCHAR(100)    NOT NULL,
    Version             VARCHAR(20)     NOT NULL,
    Tipo                VARCHAR(100)    NOT NULL,
    IdDocumento         BIGINT          NOT NULL,
    NombreCampo         VARCHAR(100),
    ValorClasificador   VARCHAR(200)
);

-- HANA: "BSCL.Normativa.BD::DOC.Cuadratura"
CREATE TABLE IF NOT EXISTS CUADRATURA (
    Esquema     VARCHAR(100)    NOT NULL,
    Version     VARCHAR(20)     NOT NULL,
    Tipo        VARCHAR(100)    NOT NULL,
    Descripcion VARCHAR(500),
    PRIMARY KEY (Esquema, Version, Tipo)
);

-- HANA: "BSCL.Normativa.BD::DOC.CampoCuadratura"
CREATE TABLE IF NOT EXISTS CAMPO_CUADRATURA (
    Esquema         VARCHAR(100)    NOT NULL,
    Version         VARCHAR(20)     NOT NULL,
    NombreCuadratura VARCHAR(100)   NOT NULL,
    NombreCampo     VARCHAR(100)    NOT NULL,
    Orden           INTEGER         DEFAULT 0
);

-- HANA: "BSCL.Normativa.BD::NOR.Configuracion"
CREATE TABLE IF NOT EXISTS CONFIGURACION (
    IdEmpresa       BIGINT          NOT NULL,
    CodigoParametro VARCHAR(100)    NOT NULL,
    Valor           VARCHAR(500),
    PRIMARY KEY (IdEmpresa, CodigoParametro)
);

-- HANA: "BSCL.Normativa.BD::DOC.Parametro"
CREATE TABLE IF NOT EXISTS PARAMETRO (
    Esquema VARCHAR(100)    NOT NULL,
    Version VARCHAR(20)     NOT NULL,
    Codigo  VARCHAR(100)    NOT NULL,
    Valor   VARCHAR(500),
    PRIMARY KEY (Esquema, Version, Codigo)
);

-- HANA: "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"
CREATE TABLE IF NOT EXISTS EMPRESA (
    IdEmpresa       BIGINT          NOT NULL,
    Nombre          VARCHAR(200),
    RazonSocial     VARCHAR(200),
    Rut             VARCHAR(20),
    RutRepresentante VARCHAR(20),
    Codigo          VARCHAR(50),
    EsCorredora     CHAR(1)         DEFAULT 'N',
    EmailPrincipal  VARCHAR(200),
    FonoPrincipal   VARCHAR(20),
    PRIMARY KEY (IdEmpresa)
);

-- HANA: "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"
CREATE TABLE IF NOT EXISTS CORREDORA_EMPRESA (
    IdEmpresa       BIGINT          NOT NULL,
    IdBolsa         VARCHAR(20)     NOT NULL,
    CodigoCorredora VARCHAR(50),
    PRIMARY KEY (IdEmpresa, IdBolsa)
);

-- HANA: "BSCL.Normativa.BD::TRI.ResumenCarga"
CREATE TABLE IF NOT EXISTS RESUMEN_CARGA (
    ID                      BIGINT          NOT NULL,
    IdProceso               BIGINT,
    IdDocumento             BIGINT,
    IdUsuario               BIGINT,
    Fecha                   TIMESTAMP,
    Glosa                   VARCHAR(500),
    NombreArchivo           VARCHAR(500),
    TotalRegistrosLeidos    BIGINT          DEFAULT 0,
    TotalRegistrosCargados  BIGINT          DEFAULT 0,
    TotalRegistrosErroneos  BIGINT          DEFAULT 0,
    PRIMARY KEY (ID)
);

-- HANA: "BSCL.Normativa.BD::TRI.ResumenGeneracion"
CREATE TABLE IF NOT EXISTS RESUMEN_GENERACION (
    ID                      BIGINT          NOT NULL,
    IdProceso               BIGINT,
    IdDocumento             BIGINT,
    IdUsuario               BIGINT,
    Fecha                   TIMESTAMP,
    Glosa                   VARCHAR(500),
    NombreArchivo           VARCHAR(500),
    TotalRegistrosGenerados BIGINT          DEFAULT 0,
    UltimoCorrelativo       BIGINT          DEFAULT 0,
    PRIMARY KEY (ID)
);
