-- H2 (MODE=LEGACY) stand-in for the HANA tables read/written by EnvioContabilidad, GenerarJsonAsientoSap and
-- ReversaPreComprobante. Quoted names are the exact HANA catalog names so the production SQL
-- resources (cl/bcs/contabilidad/sql/*.sql) run unmodified. Views are modelled as tables.

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.PreComprobante" (
  "IdPreComprobante"   BIGINT NOT NULL PRIMARY KEY,
  "IdTipoContabilidad" BIGINT,
  "IdEmpresa"          BIGINT,
  "Ejercicio"          BIGINT,
  "Tipo"               VARCHAR(20),
  "FechaContable"      VARCHAR(10),
  "Glosa"              VARCHAR(500),
  "FechaProceso"       VARCHAR(10),
  "HoraProceso"        VARCHAR(20),
  "CodigoUsuario"      VARCHAR(50),
  "FolioContabilidad"  BIGINT,
  "IdReversa"          BIGINT,
  "Estado"             VARCHAR(1)
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" (
  "IdPreComprobanteAsiento" BIGINT NOT NULL PRIMARY KEY,
  "IdEmpresa"               BIGINT,
  "IdPreComprobante"        BIGINT,
  "IdCuentaContable"        VARCHAR(50),
  "Ejercicio"               BIGINT,
  "Linea"                   BIGINT,
  "MontoDebe"               DECIMAL(28,2),
  "MontoHaber"              DECIMAL(28,2),
  "FechaContable"           VARCHAR(10),
  "Glosa"                   VARCHAR(500),
  "Estado"                  VARCHAR(1)
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" (
  "IdPreComprobanteAsiento" BIGINT,
  "IdEvento"                BIGINT,
  "Operador"                VARCHAR(5),
  "Linea"                   BIGINT,
  "Estado"                  VARCHAR(1),
  "GlosaDetalle"            VARCHAR(500)
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.Evento" (
  "IdEvento"      BIGINT NOT NULL PRIMARY KEY,
  "IdMovimiento"  BIGINT,
  "IdTipoEvento"  VARCHAR(50),
  "CodigoMoneda"  VARCHAR(5),
  "Monto"         DECIMAL(28,2)
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.Movimiento" (
  "IdMovimiento"       BIGINT NOT NULL PRIMARY KEY,
  "IdCentralizacion"   BIGINT,
  "DocumentoIdentidad" VARCHAR(30),
  "Cuenta"             BIGINT
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.Centralizacion" (
  "IdCentralizacion"     BIGINT NOT NULL PRIMARY KEY,
  "IdTipoCentralizacion" BIGINT
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" (
  "IdTipoContabilidad" BIGINT NOT NULL PRIMARY KEY,
  "IdEmpresa"          BIGINT,
  "Nombre"             VARCHAR(200),
  "Formato"            VARCHAR(1),
  "Estado"             VARCHAR(1)
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" (
  "IdTipoContabilidad" BIGINT,
  "IdCuentaContable"   VARCHAR(50),
  "Orden"              BIGINT
);

CREATE TABLE "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad" (
  "IdEstadoContabilidad" VARCHAR(1)
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" (
  "IdTipoContabilidad"   BIGINT,
  "IdTipoCentralizacion" BIGINT
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.HomologaCuenta" (
  "CuentaOptimus"      VARCHAR(50),
  "CuentaContabilidad" VARCHAR(50),
  "CodigoContabilidad" VARCHAR(50),
  "CentroCosto"        VARCHAR(50),
  "NombreCuenta"       VARCHAR(200)
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap" (
  "IdTipoCentralizacion"    BIGINT,
  "CampoSAP"                VARCHAR(100),
  "CampoTipoCentralizacion" VARCHAR(100)
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.MovimientoDato" (
  "IdMovimiento"     BIGINT,
  "IdTipoDatoEvento" VARCHAR(100),
  "ValorVarchar"     VARCHAR(500),
  "ValorDecimal"     DECIMAL(28,6),
  "Estado"           VARCHAR(1),
  "TipoEvento"       VARCHAR(50)
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" (
  "IdTipoDatoEvento" VARCHAR(100) NOT NULL PRIMARY KEY,
  "TipoDato"         VARCHAR(1)
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.PreComprobanteEnvioContabilidad" (
  "JdtNum"        BIGINT,
  "ReferenceDate" VARCHAR(10),
  "Memo"          VARCHAR(500),
  "DueDate"       VARCHAR(10),
  "TaxDate"       VARCHAR(10)
);

CREATE SEQUENCE "BSCL.Contabilidad.BD.Secuencias::IdPreComprobanteAsientoEnvioContabilidad" START WITH 1;

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsientoEnvioContabilidad" (
  "IdPreComprobanteAsientoEnvioContabilidad" BIGINT NOT NULL PRIMARY KEY,
  "IdTipoContabilidad"  BIGINT,
  "ParentKey"           BIGINT,
  "Debit"               DECIMAL(34,16),
  "Credit"              DECIMAL(34,16),
  "U_glosa"             VARCHAR(500),
  "LineMemo"            VARCHAR(500),
  "ProfitCode"          VARCHAR(50),
  "BPLId"               BIGINT,
  "AccountCode"         VARCHAR(50),
  "DueDate"             VARCHAR(10),
  "Formato"             VARCHAR(20),
  "TieneProject"        VARCHAR(5),
  "Project"             VARCHAR(50),
  "ProductoMovimiento"  VARCHAR(100),
  "RelacionCorredora"   VARCHAR(100),
  "CodSecuencia"        VARCHAR(100),
  "IdMovimiento"        BIGINT,
  "Reference1"          VARCHAR(100),
  "Reference2"          VARCHAR(100),
  "AdditionalReference" VARCHAR(100),
  "FCDebit"             DECIMAL(34,16),
  "FCCredit"            DECIMAL(34,16),
  "FCCurrency"          VARCHAR(5),
  "ShortName"           VARCHAR(50),
  "TipoSocioNegocio"    VARCHAR(20),
  "ReferenceDate1"      VARCHAR(10),
  "TaxDate"             VARCHAR(10),
  "CostingCode"         VARCHAR(50),
  "CostingCode2"        VARCHAR(50),
  "CostingCode3"        VARCHAR(50),
  "CostingCode4"        VARCHAR(50),
  "CostingCode5"        VARCHAR(50),
  "U_BCS_TipoPersona"   VARCHAR(50),
  "U_BCS_Rut"           VARCHAR(50),
  "U_BCS_Portafolio"    VARCHAR(50),
  "U_BCS_Nombre"        VARCHAR(200),
  "U_BCS_CtaDCV"        VARCHAR(50),
  "U_BCS_EmitidoA"      VARCHAR(200),
  "U_BCS_Comprobante"   VARCHAR(50),
  "U_BCS_Folio"         VARCHAR(50),
  "U_BCS_NDcto"         VARCHAR(50),
  "U_BCS_Concepto"      VARCHAR(200),
  "U_BCS_CV"            VARCHAR(10),
  "U_BCS_Nemo"          VARCHAR(50),
  "U_BCS_IFRSCtaPropia" VARCHAR(50),
  "U_BCS_CodigoProducto" VARCHAR(50),
  "U_BCS_Bolsa"         VARCHAR(50),
  "U_BCS_Relacion"      VARCHAR(50),
  "U_RelacionCodigoSap" VARCHAR(50),
  "U_BCS_FolioDTE"      VARCHAR(50)
);

CREATE TABLE "BSCL.Contabilidad.BD::ITZ.PreComprobanteCentralizacionTipo" (
  "IdPreComprobante"     BIGINT,
  "IdTipoCentralizacion" BIGINT
);

CREATE TABLE "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarPreComprobanteEnvioContabilidad" (
  "JdtNum"          BIGINT,
  "ReferenceDate"   VARCHAR(10),
  "Memo"            VARCHAR(500),
  "DueDate"         VARCHAR(10),
  "TaxDate"         VARCHAR(10),
  "U_BCS_IDPreComp" BIGINT,
  "U_BCS_TipoCen"   BIGINT
);

-- Read by legacy InterfazContable.java:44 (SELECT_MENSAJES).
CREATE TABLE "BSCL.Contabilidad.BD::ITZ.HomologaRespuestaSap" (
  "Codigo"  BIGINT,
  "Valor"   VARCHAR(500),
  "Mensaje" VARCHAR(500)
);
