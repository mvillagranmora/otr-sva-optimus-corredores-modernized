-- H2 (MODE=LEGACY) stand-in for the SAP HANA objects read/written by the "transaccional" CuentaInversion services
-- (CrearCargoAbono, CertificarCargoAbono, AnularCargoAbono, ActualizarSaldoCargoAbono, SaldoCuentaInversion,
-- ConsultarSaldoCuentaInversion, ...). Quoted names are the exact HANA catalog names so the production SQL resources
-- (cl/bcs/cuentaInversion/sql/*.sql) run unmodified (through support/H2Hana.java). HANA views are modelled as tables:
-- the seeds insert the rows the view would return. Column types/scales are an ASSUMPTION (the HANA DDL of these tables
-- is not in the repository): money columns are DECIMAL(28,16) so nothing is rounded by the stand-in itself.
--
-- This file is executed by BOTH the legacy oracle runner and the modern tests.

-- ---------------------------------------------------------------------------------------------- HANA built-ins
CREATE TABLE DUMMY (X VARCHAR(1));
INSERT INTO DUMMY VALUES ('X');

CREATE ALIAS TO_NVARCHAR FOR "cl.bcs.cuentainversion.transaccional.support.H2Hana.toNvarchar";
CREATE ALIAS TO_VARCHAR FOR "cl.bcs.cuentainversion.transaccional.support.H2Hana.toVarchar";
CREATE ALIAS TO_BIGINT FOR "cl.bcs.cuentainversion.transaccional.support.H2Hana.toBigint";
CREATE ALIAS TO_DECIMAL FOR "cl.bcs.cuentainversion.transaccional.support.H2Hana.toDecimal";
CREATE ALIAS TO_DATE FOR "cl.bcs.cuentainversion.transaccional.support.H2Hana.toDate";
CREATE ALIAS TO_TIMESTAMP FOR "cl.bcs.cuentainversion.transaccional.support.H2Hana.toTimestamp";
CREATE ALIAS ADD_SECONDS FOR "cl.bcs.cuentainversion.transaccional.support.H2Hana.addSeconds";
CREATE ALIAS HANA_UTCNOW FOR "cl.bcs.cuentainversion.transaccional.support.H2Hana.utcNow";

-- ---------------------------------------------------------------------------------------------- sequences
CREATE SEQUENCE "BSCL.CuentaInversion.BD.Secuencias::CargoAbonoMovimiento" START WITH 5000;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::SecuenciaMovimiento" START WITH 100;

-- ---------------------------------------------------------------------------------------------- CTA tables
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CuentaInversion" (
  "IdCuentaInversion"        BIGINT NOT NULL PRIMARY KEY,
  "IdEmpresa"                BIGINT,
  "IdCliente"                BIGINT,
  "IdPortafolio"             BIGINT,
  "IdMoneda"                 VARCHAR(3),
  "IdEstado"                 VARCHAR(3),
  "Codigo"                   VARCHAR(50),
  "CuentaInversionPrincipal" VARCHAR(1)
);

CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" (
  "IdCargoAbonoMovimiento"          BIGINT NOT NULL PRIMARY KEY,
  "FechaSistema"                    DATE,
  "IdModulo"                        VARCHAR(10),
  "IdTipoMovimiento"                VARCHAR(10),
  "EsIngresoManual"                 VARCHAR(1),
  "IdCuentaInversion"               BIGINT,
  "NombreCliente"                   VARCHAR(200),
  "IdConcepto"                      VARCHAR(10),
  "Descripcion"                     VARCHAR(500),
  "IdCondicionDeLiquidacion"        VARCHAR(10),
  "FechaEfectivaMovimiento"         DATE,
  "Monto"                           DECIMAL(28,16),
  "Saldo"                           DECIMAL(28,16),
  "SaldoPorFechaMovimiento"         DECIMAL(28,16),
  "SaldoLibroInventario"            DECIMAL(28,16),
  "EsCertificadoAutomatico"         VARCHAR(1),
  "FechaCertificacion"              TIMESTAMP,
  "IdEjecutivo"                     BIGINT,
  "IdSucursal"                      BIGINT,
  "IdTipoComprobante"               VARCHAR(10),
  "IdComprobante"                   BIGINT,
  "FolioComprobante"                VARCHAR(50),
  "IdComprobanteDetalle"            BIGINT,
  "IdComprobanteFormaDePago"        BIGINT,
  "IdTipoMovimientoAsociado"        VARCHAR(10),
  "IdMovimientoAsociado"            BIGINT,
  "FechaComprobante"                DATE,
  "IdTipoProducto"                  VARCHAR(10),
  "IdTipoOperacion"                 VARCHAR(10),
  "IdOperacion"                     BIGINT,
  "FechaOperacion"                  DATE,
  "FechaLiquidacionOperacion"       DATE,
  "FechaVencimientoOperacion"       DATE,
  "IdMonedaOperacion"               VARCHAR(3),
  "MontoOperacion"                  DECIMAL(28,16),
  "TipoCambioOperacion"             DECIMAL(28,16),
  "IdInstrumento"                   BIGINT,
  "IdTipoInstrumento"               VARCHAR(10),
  "Nemo"                            VARCHAR(50),
  "Cantidad"                        DECIMAL(28,16),
  "EsCargoAbonoPorTraspaso"         VARCHAR(1),
  "IdCargoAbonoRelacionadoTraspaso" BIGINT,
  "EsIndicadorBilateral"            VARCHAR(1),
  "GeneroContraparteEnTesoreria"    VARCHAR(1),
  "IdContraparte"                   BIGINT,
  "IdPersonaPagoCobro"              BIGINT,
  "FechaIngresoRegistro"            TIMESTAMP,
  "IdUsuario"                       BIGINT,
  "IdUsuarioCertificador"           BIGINT,
  "IdEmpresa"                       BIGINT,
  "IdEstado"                        VARCHAR(3),
  "IdEvento"                        BIGINT,
  "SecuenciaMovimiento"             BIGINT
);

CREATE TABLE "BSCL.CuentaInversion.BD::GBL.ActualizarSaldoCargoAbono_Proceso_Paso" (
  "IdTrabajo"               BIGINT,
  "IdCuentaInversion"       BIGINT,
  "IdCargoAbonoMovimiento"  BIGINT,
  "IdTipoMovimiento"        VARCHAR(10),
  "Monto"                   DECIMAL(28,16),
  "Saldo"                   DECIMAL(28,16),
  "SaldoPorFechaMovimiento" DECIMAL(28,16),
  "FechaSistema"            DATE,
  "RankFechaMovimiento"     BIGINT,
  "FechaEfectivaMovimiento" DATE,
  "RankFechaEfectiva"       BIGINT,
  "FechaCertificacion"      TIMESTAMP
);

-- ---------------------------------------------------------------------------------------------- reference tables (Plataforma / SocioNegocio / Portafolio)
CREATE TABLE "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" (
  "IdCondicionDeLiquidacion" VARCHAR(10) NOT NULL PRIMARY KEY,
  "PlazoEnDias"              INT
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.TipoMovimiento" (
  "IdModulo"         VARCHAR(10),
  "IdTipoMovimiento" VARCHAR(10),
  "Nombre"           VARCHAR(100)
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.MonedaFormato" (
  "IdMonedaFormato"         BIGINT,
  "IdMoneda"                VARCHAR(3),
  "IdUsoMonedaFormato"      VARCHAR(3),
  "IdTipoInstrumento"       VARCHAR(10),
  "IdTipoOperacion"         VARCHAR(10),
  "PrecioDecimales"         INT,
  "PrecioTrucaRedondea"     VARCHAR(1),
  "TasaDecimales"           INT,
  "TasaTrucaRedondea"       VARCHAR(1),
  "CantidadDecimales"       INT,
  "CantidadTrucaRedondea"   VARCHAR(1),
  "MontoDecimales"          INT,
  "MontoTrucaRedondea"      VARCHAR(1),
  "TipoCambioDecimales"     INT,
  "TipoCambioTrucaRedondea" VARCHAR(1),
  "ParidadDecimales"        INT,
  "ParidadTrucaRedondea"    VARCHAR(1),
  "PuntosDecimales"         INT,
  "PuntosTrucaRedondea"     VARCHAR(1),
  "IdEstado"                VARCHAR(3)
);

CREATE TABLE "BSCL.Plataforma.BD::REG.DiaFeriado" (
  "IdFeriado"      BIGINT,
  "Tipo"           VARCHAR(10),
  "Fecha"          DATE,
  "Dia"            INT,
  "DescripcionDia" VARCHAR(100),
  "AgnoCarga"      INT,
  "IdPais"         BIGINT,
  "IdEstado"       VARCHAR(3)
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.ObtenerPlazo_Fechas" (
  "IdInterno"       BIGINT,
  "FechaDesde"      VARCHAR(10),
  "FechaHasta"      VARCHAR(10),
  "SoloDiasHabiles" VARCHAR(1),
  "Plazo"           BIGINT
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.Empresa" (
  "IdEmpresa"         BIGINT NOT NULL PRIMARY KEY,
  "IdSocioDeNegocio"  BIGINT,
  "IdPais"            BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" (
  "IdSocioDeNegocio" BIGINT NOT NULL PRIMARY KEY,
  "IdCliente"        BIGINT
);

CREATE TABLE "BSCL.Portafolio.BD::POR.Portafolio" (
  "IdPortafolio"          BIGINT NOT NULL PRIMARY KEY,
  "IdCliente"             BIGINT,
  "CodigoPortafolio"      VARCHAR(50),
  "IdEmpleadoEjecutivo"   BIGINT,
  "DescripcionPortafolio" VARCHAR(200),
  "Proposito"             VARCHAR(200),
  "IdMatrizAgrupadorAPV"  BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" (
  "IdCliente"     BIGINT NOT NULL PRIMARY KEY,
  "IdPersona"     BIGINT,
  "IdTipoCliente" VARCHAR(10)
);

CREATE TABLE "BSCL.SocioNegocio.BD::PER.Persona" (
  "IdPersona"                        BIGINT NOT NULL PRIMARY KEY,
  "Tipo"                             VARCHAR(1),
  "Nombres"                          VARCHAR(100),
  "ApellidoPaterno"                  VARCHAR(100),
  "ApellidoMaterno"                  VARCHAR(100),
  "RazonSocial"                      VARCHAR(200),
  "IdDocumentoIdentidad"             BIGINT,
  "IdDireccionElectronicaPrincipal"  BIGINT,
  "IdTelefonoPrincipal"              BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" (
  "IdDocumentoIdentidad"     BIGINT NOT NULL PRIMARY KEY,
  "DocumentoIdentidad"       VARCHAR(30),
  "IdTipoDocumentoIdentidad" VARCHAR(10)
);

CREATE TABLE "BSCL.SocioNegocio.BD::PER.Telefono" (
  "IdTelefono" BIGINT NOT NULL PRIMARY KEY,
  "Numero"     VARCHAR(30)
);

CREATE TABLE "BSCL.SocioNegocio.BD::PER.DireccionElectronica" (
  "IdDireccionElectronica" BIGINT NOT NULL PRIMARY KEY,
  "Direccion"              VARCHAR(200),
  "Borrado"                VARCHAR(1)
);

CREATE TABLE "BSCL.SocioNegocio.BD::EMP.Empleado" (
  "IdEmpleado"     BIGINT NOT NULL PRIMARY KEY,
  "IdPersona"      BIGINT,
  "CodigoEmpleado" VARCHAR(30),
  "IdSucursal"     BIGINT
);

CREATE TABLE "BSCL.Plataforma.BD::ACC.Usuario" (
  "IdUsuario"   BIGINT NOT NULL PRIMARY KEY,
  "IdEntidad"   BIGINT,
  "TipoEntidad" VARCHAR(3)
);

-- views modelled as tables
CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" (
  "IdConcepto"     VARCHAR(10) NOT NULL PRIMARY KEY,
  "NombreConcepto" VARCHAR(100)
);

CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" (
  "IdEmpleado"             BIGINT NOT NULL PRIMARY KEY,
  "IdPersona"              BIGINT,
  "IdDireccionElectronica" BIGINT
);

CREATE TABLE "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" (
  "IdEmpresa"   BIGINT NOT NULL PRIMARY KEY,
  "RazonSocial" VARCHAR(200),
  "Email"       VARCHAR(200),
  "RUT"         VARCHAR(30),
  "Telefono"    VARCHAR(30)
);

-- ---------------------------------------------------------------------------------------------- CertificarCargoAbono
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CertificarCargoAbono_IN_CargosAbonosACertificar" (
  "IdCargoAbonoMovimiento" BIGINT
);

-- ---------------------------------------------------------------------------------------------- AnularCargoAbono
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_IN_CargoAbonoAAnular" (
  "IdCargoAbonoMovimiento" BIGINT,
  "GlosaAnulacion"         VARCHAR(260)
);

-- column ORDER matters: AnularCargoAbono_Select_Insert_CargoAbono.sql inserts positionally (INSERT ... SELECT without column list)
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular" (
  "IdInterno"                       BIGINT,
  "IdCargoAbonoMovimiento"          BIGINT,
  "IdCuentaInversion"               BIGINT,
  "IdEstado"                        VARCHAR(3),
  "IdEvento"                        BIGINT,
  "IdTipoMovimiento"                VARCHAR(10),
  "IdTipoOperacion"                 VARCHAR(10),
  "IdOperacion"                     BIGINT,
  "IdMonedaOperacion"               VARCHAR(3),
  "TipoCambioOperacion"             DECIMAL(28,16),
  "MontoOperacion"                  DECIMAL(28,16),
  "FechaOperacion"                  DATE,
  "FechaLiquidacionOperacion"       DATE,
  "FechaVencimientoOperacion"       DATE,
  "IdContraparte"                   BIGINT,
  "IdTipoComprobante"               VARCHAR(10),
  "IdComprobante"                   BIGINT,
  "FolioComprobante"                VARCHAR(50),
  "IdComprobanteDetalle"            BIGINT,
  "IdComprobanteFormaDePago"        BIGINT,
  "IdTipoMovimientoAsociado"        VARCHAR(10),
  "IdMovimientoAsociado"            BIGINT,
  "FechaComprobante"                DATE,
  "IdPersonaPagoCobro"              BIGINT,
  "IdInstrumento"                   BIGINT,
  "IdTipoInstrumento"               VARCHAR(10),
  "Nemo"                            VARCHAR(50),
  "EsIndicadorBilateral"            VARCHAR(1),
  "GeneroContraparteEnTesoreria"    VARCHAR(1),
  "IdConcepto"                      VARCHAR(10),
  "FechaSistema"                    DATE,
  "FechaEfectivaMovimiento"         DATE,
  "IdCliente"                       BIGINT,
  "IdPortafolio"                    BIGINT,
  "IdTipoProducto"                  VARCHAR(10),
  "Monto"                           DECIMAL(28,16),
  "IdMoneda"                        VARCHAR(3),
  "IdEjecutivo"                     BIGINT,
  "IdSucursal"                      BIGINT,
  "IdEmpresa"                       BIGINT,
  "GlosaAnulacion"                  VARCHAR(260),
  "PERMITEANULARMOVIMIENTO"         VARCHAR(1),
  "ANULACIONREQUIEREAUTORIZACION"   VARCHAR(1),
  "EsCargoAbonoPorTraspaso"         VARCHAR(1),
  "IdCargoAbonoRelacionadoTraspaso" BIGINT,
  "Informado"                       VARCHAR(1)
);

CREATE TABLE "BSCL.CuentaInversion.BD::CTA.ServicioCumpleRequisitoAnularCargoAbono_IN_CargoAbonoAAnular" (
  "IdInterno"                       BIGINT,
  "IdCargoAbonoMovimiento"          BIGINT,
  "IdCuentaInversion"               BIGINT,
  "IdEstado"                        VARCHAR(3),
  "IdEvento"                        BIGINT,
  "IdTipoMovimiento"                VARCHAR(10),
  "IdTipoOperacion"                 VARCHAR(10),
  "IdOperacion"                     BIGINT,
  "IdMonedaOperacion"               VARCHAR(3),
  "TipoCambioOperacion"             DECIMAL(28,16),
  "MontoOperacion"                  DECIMAL(28,16),
  "FechaOperacion"                  DATE,
  "FechaLiquidacionOperacion"       DATE,
  "FechaVencimientoOperacion"       DATE,
  "IdContraparte"                   BIGINT,
  "IdTipoComprobante"               VARCHAR(10),
  "IdComprobante"                   BIGINT,
  "FolioComprobante"                VARCHAR(50),
  "IdComprobanteDetalle"            BIGINT,
  "IdComprobanteFormaDePago"        BIGINT,
  "IdTipoMovimientoAsociado"        VARCHAR(10),
  "IdMovimientoAsociado"            BIGINT,
  "FechaComprobante"                DATE,
  "IdPersonaPagoCobro"              BIGINT,
  "IdInstrumento"                   BIGINT,
  "IdTipoInstrumento"               VARCHAR(10),
  "Nemo"                            VARCHAR(50),
  "EsIndicadorBilateral"            VARCHAR(1),
  "GeneroContraparteEnTesoreria"    VARCHAR(1),
  "IdConcepto"                      VARCHAR(10),
  "FechaSistema"                    DATE,
  "FechaEfectivaMovimiento"         DATE,
  "IdCliente"                       BIGINT,
  "IdPortafolio"                    BIGINT,
  "IdTipoProducto"                  VARCHAR(10),
  "Monto"                           DECIMAL(28,16),
  "IdMoneda"                        VARCHAR(3),
  "IdEjecutivo"                     BIGINT,
  "IdSucursal"                      BIGINT,
  "IdEmpresa"                       BIGINT,
  "GlosaAnulacion"                  VARCHAR(260),
  "PERMITEANULARMOVIMIENTO"         VARCHAR(1),
  "ANULACIONREQUIEREAUTORIZACION"   VARCHAR(1),
  "EsCargoAbonoPorTraspaso"         VARCHAR(1),
  "IdCargoAbonoRelacionadoTraspaso" BIGINT,
  "Informado"                       VARCHAR(1)
);

CREATE TABLE "BSCL.CuentaInversion.BD::CTA.LibroInventarioDeSaldos" (
  "IdCargoAbonoMovimiento"       BIGINT,
  "IdCargoAbonoMovimientoCalce"  BIGINT,
  "MontoCalce"                   DECIMAL(28,16),
  "IdEstado"                     VARCHAR(3)
);

-- view ConsultarLibroInventarioDeSaldos modelled as a table (the seed inserts the rows the view would return)
CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultarLibroInventarioDeSaldos" (
  "IdCargoAbonoMovimiento"       BIGINT,
  "IdCargoAbonoMovimientoCalce"  BIGINT,
  "MontoCalce"                   DECIMAL(28,16)
);

CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultaConfiguracionCuentaInversion" (
  "IdEmpresa"                     BIGINT,
  "PERMITEANULARMOVIMIENTO"       VARCHAR(1),
  "ANULACIONREQUIEREAUTORIZACION" VARCHAR(1)
);

CREATE TABLE "BSCL.Plataforma.BD::AUT.Evento" (
  "IdEvento"  BIGINT NOT NULL PRIMARY KEY,
  "IdEstado"  VARCHAR(3)
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" (
  "IdConcepto"          VARCHAR(10),
  "RequiereAutorizacion" VARCHAR(1)
);

-- PLA.Identidad: identity matrix used by the "COALESCE("1"||'CODE1', "2"||'CODE2', ...)" validation pattern. The DDL is not in the
-- repository; the pattern only works if row k has '' in column "k" and NULL elsewhere, so every validation yields one row per
-- check (NULL||x is NULL). MODEL ASSUMPTION documented in the README.
CREATE TABLE "BSCL.Plataforma.BD::PLA.Identidad" (
  "1" VARCHAR(1), "2" VARCHAR(1), "3" VARCHAR(1), "4" VARCHAR(1), "5" VARCHAR(1), "6" VARCHAR(1)
);
INSERT INTO "BSCL.Plataforma.BD::PLA.Identidad" VALUES ('', NULL, NULL, NULL, NULL, NULL);
INSERT INTO "BSCL.Plataforma.BD::PLA.Identidad" VALUES (NULL, '', NULL, NULL, NULL, NULL);
INSERT INTO "BSCL.Plataforma.BD::PLA.Identidad" VALUES (NULL, NULL, '', NULL, NULL, NULL);
INSERT INTO "BSCL.Plataforma.BD::PLA.Identidad" VALUES (NULL, NULL, NULL, '', NULL, NULL);
INSERT INTO "BSCL.Plataforma.BD::PLA.Identidad" VALUES (NULL, NULL, NULL, NULL, '', NULL);
INSERT INTO "BSCL.Plataforma.BD::PLA.Identidad" VALUES (NULL, NULL, NULL, NULL, NULL, '');

-- ---------------------------------------------------------------------------------------------- CrearCargoAbono
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoAGenerar" (
  "NumeroCargoAbono"            BIGINT,
  "IdPortafolio"                BIGINT,
  "IdMoneda"                    VARCHAR(3),
  "IdTipoMovimiento"            VARCHAR(10),
  "IdConcepto"                  VARCHAR(10),
  "Descripcion"                 VARCHAR(500),
  "IdCondicionDeLiquidacion"    VARCHAR(10),
  "FechaCondicionDeLiquidacion" VARCHAR(10),
  "Monto"                       DECIMAL(28,16),
  "IdEjecutivo"                 BIGINT,
  "IdSucursal"                  BIGINT,
  "IdTipoProducto"              VARCHAR(10),
  "IdTipoOperacion"             VARCHAR(10),
  "IdOperacion"                 BIGINT,
  "FechaOperacion"              VARCHAR(10),
  "FechaLiquidacionOperacion"   VARCHAR(10),
  "FechaVencimientoOperacion"   VARCHAR(10),
  "IdMonedaOperacion"           VARCHAR(3),
  "MontoOperacion"              DECIMAL(28,16),
  "TipoCambioOperacion"         DECIMAL(28,16),
  "IdContraparte"               BIGINT,
  "IdTipoComprobante"           VARCHAR(10),
  "IdComprobante"               BIGINT,
  "FolioComprobante"            VARCHAR(50),
  "IdComprobanteDetalle"        BIGINT,
  "IdComprobanteFormaDePago"    BIGINT,
  "IdTipoMovimientoAsociado"    VARCHAR(10),
  "IdMovimientoAsociado"        BIGINT,
  "FechaComprobante"            VARCHAR(10),
  "IdPersonaPagoCobro"          BIGINT,
  "IdInstrumento"               BIGINT,
  "IdTipoInstrumento"           VARCHAR(10),
  "Cantidad"                    DECIMAL(28,16),
  "Nemo"                        VARCHAR(50),
  "EsIndicadorBilateral"        VARCHAR(1),
  "EsCargoAbonoPorTraspaso"     VARCHAR(1),
  "IdPortafolioTraspaso"        BIGINT,
  "GeneroContraparteEnTesoreria" VARCHAR(1),
  "CertificadoAutomatico"       VARCHAR(1)
);

CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_IN_CargoAbonoSumaSaldos" (
  "IdPortafolio" BIGINT,
  "IdMoneda"     VARCHAR(3),
  "FechaSaldo"   VARCHAR(10),
  "Saldo"        DECIMAL(28,16)
);

CREATE TABLE "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" (
  "IdConcepto" VARCHAR(10) NOT NULL PRIMARY KEY
);

CREATE TABLE "BSCL.Orden.BD::ORD.ConceptoOrden" (
  "IdConcepto" VARCHAR(10),
  "Uso"        VARCHAR(30)
);

CREATE TABLE "BSCL.Plataforma.BD::INS.TipoOperacion"   ("IdTipoOperacion"   VARCHAR(10) NOT NULL PRIMARY KEY);
CREATE TABLE "BSCL.Plataforma.BD::INS.TipoProducto"    ("IdTipoProducto"    VARCHAR(10) NOT NULL PRIMARY KEY);
CREATE TABLE "BSCL.Plataforma.BD::INS.TipoInstrumento" ("IdTipoInstrumento" VARCHAR(10) NOT NULL PRIMARY KEY);

CREATE TABLE "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" (
  "IdComprobanteDetalle"    BIGINT NOT NULL PRIMARY KEY,
  "IdComprobante"           BIGINT,
  "IdTipoComprobanteOrigen" VARCHAR(10)
);

-- views modelled as tables
CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicadoresPortafolioCuentaInversion" (
  "IdPortafolio"        BIGINT NOT NULL PRIMARY KEY,
  "OperaLineaSobregiro" VARCHAR(1),
  "OperaCuentaInversion" VARCHAR(1)
);

CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarMonedaTesoreria" (
  "IdMoneda"                VARCHAR(3) NOT NULL PRIMARY KEY,
  "IdEstadoMonedaTesoreria" VARCHAR(3)
);

CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" (
  "IdCliente"          BIGINT NOT NULL PRIMARY KEY,
  "IdPersona"          BIGINT,
  "IdTipoCliente"      VARCHAR(10),
  "NombreCompleto"     VARCHAR(300),
  "DocumentoIdentidad" VARCHAR(30)
);

CREATE TABLE "BSCL.Plataforma.BD::IND.TipoIndicador" (
  "IdTipoIndicador"     BIGINT,
  "IdTipoEntidad"       VARCHAR(10),
  "CodigoTipoIndicador" VARCHAR(10),
  "IdEstado"            VARCHAR(3)
);

CREATE TABLE "BSCL.Plataforma.BD::IND.Indicador" (
  "IdTipoIndicador" BIGINT,
  "IdTipoEntidad"   VARCHAR(10),
  "IdEntidad"       BIGINT,
  "ValorIndicador"  VARCHAR(1)
);

CREATE TABLE "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" (
  "IdPersona" BIGINT,
  "Nombre"    VARCHAR(200)
);

CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" (
  "IdPersona"          BIGINT NOT NULL PRIMARY KEY,
  "NombreCompleto"     VARCHAR(300),
  "DocumentoIdentidad" VARCHAR(30)
);

CREATE TABLE "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo" (
  "IdCondicionOperativa" VARCHAR(60),
  "IdTipoEtapa"          VARCHAR(30),
  "IdEmpresa"            BIGINT,
  "IdTipoCanalIngreso"   VARCHAR(10),
  "IdTipoComportamiento" VARCHAR(20),
  "IdEstado"             VARCHAR(3)
);

-- ---------------------------------------------------------------------------------------------- SaldoCuentaInversion
-- step table (GLOBAL TEMPORARY in HANA): rows are written at portfolio level (IdPortafolio, IdTrabajo) and at account level
CREATE TABLE "BSCL.CuentaInversion.BD::GBL.SaldoCuentaInversion_Proceso_Paso" (
  "IdCliente"         BIGINT,
  "IdPortafolio"      BIGINT,
  "IdCuentaInversion" BIGINT,
  "IdTrabajo"         BIGINT
);

-- ---------------------------------------------------------------------------------------------- ConsultarSaldoCuentaInversion
CREATE TABLE "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" (
  "IdTrabajo"    BIGINT,
  "IdEjecutivo"  BIGINT,
  "IdPortafolio" BIGINT,
  "IdMoneda"     VARCHAR(3)
);

CREATE TABLE "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Delete" (
  "IdTrabajo"    BIGINT,
  "IdPortafolio" BIGINT
);

CREATE TABLE "BSCL.CuentaInversion.BD::GBL.ConsultarUltimoMovimientoCuentaInversion_Paso" (
  "IdTrabajo"         BIGINT,
  "IdCuentaInversion" BIGINT,
  "FechaSaldo"        DATE
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.Moneda" (
  "IdMoneda" VARCHAR(3) NOT NULL PRIMARY KEY,
  "Codigo"   VARCHAR(3),
  "Nombre"   VARCHAR(100)
);

CREATE TABLE "BSCL.SocioNegocio.BD::EJE.Ejecutivo" (
  "IdEmpleadoEjecutivo" BIGINT NOT NULL PRIMARY KEY
);

CREATE TABLE "BSCL.APV.BD::APV.ProductoAgrupador" (
  "IdProductoAgrupador"     BIGINT NOT NULL PRIMARY KEY,
  "NombreProductoAgrupador" VARCHAR(100),
  "Codigo"                  VARCHAR(40)
);

CREATE TABLE "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" (
  "IdMatrizAgrupadorAPV" BIGINT NOT NULL PRIMARY KEY,
  "IdProductoAgrupador"  BIGINT,
  "IdRegimenTributario"  BIGINT,
  "EsADC"                VARCHAR(1)
);

CREATE TABLE "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" (
  "IdMatrizAgrupadorNOAPV" BIGINT NOT NULL PRIMARY KEY,
  "CodigoPortafolio"       VARCHAR(50),
  "EsADC"                  VARCHAR(1)
);

-- views modelled as tables
CREATE TABLE "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" (
  "IdSucursal"     BIGINT NOT NULL PRIMARY KEY,
  "CodigoSucursal" VARCHAR(20),
  "NombreSucursal" VARCHAR(100)
);

CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultarRelacionCorredora" (
  "IdPersonaDestino" BIGINT
);

CREATE TABLE "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" (
  "IdPersona"          BIGINT,
  "IdCliente"          BIGINT,
  "DocumentoIdentidad" VARCHAR(30)
);

-- FondoMutuo (solicitudes en transito) / Mesa
CREATE TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" (
  "IdFondo"    VARCHAR(20) NOT NULL,
  "IdEmpresa"  BIGINT,
  "IdMoneda"   VARCHAR(3),
  "MoneyMarket" VARCHAR(1)
);

CREATE TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" (
  "IdSerieFondo" VARCHAR(30) NOT NULL,
  "IdFondo"      VARCHAR(20),
  "IdEmpresa"    BIGINT,
  "IdInstrumento" BIGINT
);

CREATE TABLE "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" (
  "IdSolicitud"            BIGINT,
  "IdPortafolio"           BIGINT,
  "IdCliente"              BIGINT,
  "Monto"                  DECIMAL(28,16),
  "MontoMonedaSistema"     DECIMAL(28,16),
  "IdOperacionSpot"        BIGINT,
  "IdSerieFondo"           VARCHAR(30),
  "IdEmpresa"              BIGINT,
  "FechaEmision"           DATE,
  "Estado"                 VARCHAR(3),
  "TipoSolicitud"          VARCHAR(3),
  "CalculoPorMontoCuota"   VARCHAR(1),
  "IngresoAutomatico"      VARCHAR(1)
);

CREATE TABLE "BSCL.Mesa.BD::SPT.OperacionSpot" (
  "IdOperacionSpotOrigen" BIGINT,
  "MontoMonedaSecundaria" DECIMAL(28,16),
  "Estado"                VARCHAR(3)
);

-- Custodia / Tesoreria / garantias (other-balances queries)
CREATE TABLE "BSCL.Custodia.BD::GTA.Garantia" (
  "IdCliente"           BIGINT,
  "IdPortafolio"        BIGINT,
  "IdMonedaInstrumento" VARCHAR(3),
  "INGCantidad"         DECIMAL(28,16),
  "IdEstado"            VARCHAR(3),
  "IdOrigenGarantia"    VARCHAR(10),
  "IdEmpresa"           BIGINT,
  "FechaOperacion"      DATE
);

CREATE TABLE "BSCL.Custodia.BD::CUS.Cartera" (
  "IdCliente"                       BIGINT,
  "IdPortafolio"                    BIGINT,
  "IdClasificacionTipoInstrumento"  VARCHAR(5),
  "IdTipoReajuste"                  VARCHAR(5),
  "IdMonedaInstrumento"             VARCHAR(3),
  "IdTipoOperacion"                 VARCHAR(10),
  "IdTipoPrestamo"                  VARCHAR(10),
  "INGMontoATasaCompraActual"       DECIMAL(28,16),
  "INGMontoAPlazoMonedaInstrumento" DECIMAL(28,16),
  "INGMontoAPlazo"                  DECIMAL(28,16),
  "INGPrecioAPlazo"                 DECIMAL(28,16),
  "INGFechaLiquidacion"             DATE,
  "FechaLiquidacion"                DATE,
  "FechaVencimientoInstrumento"     DATE,
  "IdEmpresa"                       BIGINT,
  "IdEstado"                        VARCHAR(3),
  "Nemotecnico"                     VARCHAR(30),
  "INGCantidad"                     DECIMAL(28,16)
);

CREATE TABLE "BSCL.Custodia.BD::HIS.Cartera" (
  "IdCliente"                       BIGINT,
  "IdPortafolio"                    BIGINT,
  "IdClasificacionTipoInstrumento"  VARCHAR(5),
  "IdTipoReajuste"                  VARCHAR(5),
  "IdMonedaInstrumento"             VARCHAR(3),
  "IdTipoOperacion"                 VARCHAR(10),
  "IdTipoPrestamo"                  VARCHAR(10),
  "INGMontoATasaCompraActual"       DECIMAL(28,16),
  "INGMontoAPlazoMonedaInstrumento" DECIMAL(28,16),
  "INGMontoAPlazo"                  DECIMAL(28,16),
  "INGPrecioAPlazo"                 DECIMAL(28,16),
  "INGFechaLiquidacion"             DATE,
  "FechaLiquidacion"                DATE,
  "FechaVencimientoInstrumento"     DATE,
  "IdEmpresa"                       BIGINT,
  "IdEstado"                        VARCHAR(3),
  "Nemotecnico"                     VARCHAR(30),
  "INGCantidad"                     DECIMAL(28,16),
  "FechaRespaldo"                   DATE
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.IngresoEgreso" (
  "IdPortafolioTitularPago" BIGINT,
  "IdMoneda"                VARCHAR(3),
  "IdTipoMovimiento"        VARCHAR(10),
  "Monto"                   DECIMAL(28,16),
  "IdEstado"                VARCHAR(3),
  "FechaLiquidacion"        DATE,
  "FechaMovimiento"         DATE,
  "IdEmpresa"               BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.Comprobante" (
  "IdComprobante"           BIGINT,
  "IdPortafolioTitularPago" BIGINT,
  "IdMoneda"                VARCHAR(3),
  "IdTipoMovimiento"        VARCHAR(10),
  "Monto"                   DECIMAL(28,16),
  "IdEstado"                VARCHAR(3),
  "IdConcepto"              VARCHAR(10),
  "FechaLiquidacion"        DATE,
  "FechaMovimiento"         DATE,
  "IdEmpresa"               BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" (
  "IdComprobante"        BIGINT,
  "IdComprobanteDetalle" BIGINT
);

-- ---------------------------------------------------------------------------------------------- carga masiva (CAR.*)
-- DDL of CuentaInversion/BD/Scripts/CTA_Create_Tables.sql (NVARCHAR -> VARCHAR, SECONDDATE -> TIMESTAMP). The repository DDL of
-- CargaMasivaDetalle lacks the columns "Plan"/"ProductoAgrupador" that the legacy SQL writes: they are added here; IdPortafolio is text because
-- the legacy writes the portfolio CODE (CrearCargaMasiva.java:255, SelectDetalle.IdPortafolio is a String).
CREATE TABLE "BSCL.CuentaInversion.BD::CAR.CargaMasiva" (
  "IdCargaMasiva"         BIGINT NOT NULL,
  "CodigoTipoCargaMasiva" VARCHAR(40) NOT NULL DEFAULT '',
  "IdEmpresa"             BIGINT NOT NULL DEFAULT 0,
  "IdUsuario"             BIGINT NOT NULL DEFAULT 0,
  "CantidadRegistros"     INTEGER NOT NULL DEFAULT 0,
  "MontoTotal"            DECIMAL(26,8) NOT NULL DEFAULT 0,
  "NombreArchivo"         VARCHAR(100) NOT NULL DEFAULT '',
  "FechaCarga"            TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00',
  "FechaProceso"          TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00',
  "FechaIngresoRegistro"  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "IdEstado"              VARCHAR(3) NOT NULL DEFAULT '',
  "IdMoneda"              VARCHAR(3) NOT NULL DEFAULT '',
  "IdConcepto"            VARCHAR(6) NOT NULL DEFAULT '',
  PRIMARY KEY ("IdCargaMasiva")
);

CREATE TABLE "BSCL.CuentaInversion.BD::CAR.CargaMasivaDetalle" (
  "IdCargaMasivaDetalle"  BIGINT NOT NULL,
  "IdCargaMasiva"         BIGINT DEFAULT 0 NOT NULL,
  "DocumentoIdentidad"    VARCHAR(40) NOT NULL,
  "IdPortafolio"          VARCHAR(50) DEFAULT '0' NOT NULL,
  "IdMoneda"              VARCHAR(3) NOT NULL,
  "IdTipoMovimiento"      VARCHAR(6) NOT NULL,
  "IdConcepto"            VARCHAR(6) NOT NULL,
  "Monto"                 DECIMAL(26,8) DEFAULT 0 NOT NULL,
  "Descripcion"           VARCHAR(520) NOT NULL,
  "IdEstado"              VARCHAR(3) NOT NULL,
  "ObservacionValidacion" VARCHAR(520) NOT NULL,
  "Plan"                  VARCHAR(100),
  "ProductoAgrupador"     VARCHAR(40),
  PRIMARY KEY ("IdCargaMasivaDetalle")
);

CREATE TABLE "BSCL.CuentaInversion.BD::CAR.TipoCargaMasiva" (
  "IdTipoCargaMasiva"     BIGINT NOT NULL,
  "CodigoTipoCargaMasiva" VARCHAR(40) NOT NULL,
  "Nombre"                VARCHAR(100) NOT NULL,
  "IdEmpresa"             BIGINT DEFAULT 0 NOT NULL,
  "IdEstado"              VARCHAR(3) NOT NULL,
  "IdConcepto"            VARCHAR(6) NOT NULL,
  PRIMARY KEY ("IdTipoCargaMasiva")
);

CREATE TABLE "BSCL.APV.BD::REC.RegimenTributario" (
  "IdRegimenTributario" BIGINT NOT NULL PRIMARY KEY,
  "Codigo"              VARCHAR(50)
);

-- >>> otros (generated by scratch gen_schema_otros.py) >>>

-- columns added (INVISIBLE: positional INSERTs of the base seed keep working) for CrearCargoAbonoWS / ConsultarMovimientos
ALTER TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" ADD COLUMN "IdEstado" VARCHAR(3) INVISIBLE DEFAULT 'ACT';
ALTER TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" ADD COLUMN "FechaCreacion" TIMESTAMP INVISIBLE DEFAULT '2020-01-01 00:00:00';
ALTER TABLE "BSCL.SocioNegocio.BD::PER.Persona" ADD COLUMN "Borrado" VARCHAR(1) INVISIBLE DEFAULT 'N';
ALTER TABLE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" ADD COLUMN "Borrado" VARCHAR(1) INVISIBLE DEFAULT 'N';

-- view modelled as a table: ConsultarCartolaDeMovimientos (columns read by ConsultarMovimientoCuentaInversion*.sql and the Movimiento POJO)
CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" (
  "IdCargoAbonoMovimiento" BIGINT,
  "FechaSistema" DATE,
  "FechaIngresoRegistro" TIMESTAMP,
  "IdCuentaInversion" BIGINT,
  "CodigoCuentaInversion" VARCHAR(500),
  "NombreCuentaInversion" VARCHAR(500),
  "IdCliente" BIGINT,
  "NombreCliente" VARCHAR(500),
  "IdPortafolio" BIGINT,
  "IdMoneda" VARCHAR(50),
  "CodigoMoneda" VARCHAR(500),
  "NombreMoneda" VARCHAR(500),
  "SimboloMoneda" VARCHAR(500),
  "DecimalesMoneda" INT,
  "IdTipoMovimiento" VARCHAR(50),
  "TipoMovimiento" VARCHAR(500),
  "NombreTipoMovimiento" VARCHAR(500),
  "IdConcepto" VARCHAR(50),
  "NombreConcepto" VARCHAR(500),
  "OrigenConcepto" VARCHAR(500),
  "Descripcion" VARCHAR(500),
  "IdCondicionDeLiquidacion" VARCHAR(50),
  "CodigoCondicionDeLiquidacion" VARCHAR(500),
  "NombreCondicionDeLiquidacion" VARCHAR(500),
  "PlazoCondicionDeLiquidacion" INT,
  "FechaEfectivaMovimiento" DATE,
  "Monto" DECIMAL(28,16),
  "SaldoPorFechaEfectiva" DECIMAL(28,16),
  "SaldoPorFechaMovimiento" DECIMAL(28,16),
  "IdModulo" VARCHAR(50),
  "IdTipoProducto" VARCHAR(50),
  "CodigoTipoProducto" VARCHAR(500),
  "NombreTipoProducto" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(50),
  "CodigoTipoOperacion" VARCHAR(500),
  "NombreTipoOperacion" VARCHAR(500),
  "AccionTipoOperacion" VARCHAR(500),
  "IdOperacion" BIGINT,
  "FechaOperacion" TIMESTAMP,
  "FechaLiquidacionOperacion" DATE,
  "FechaVencimientoOperacion" DATE,
  "IdMonedaOperacion" VARCHAR(50),
  "MontoOperacion" DECIMAL(28,16),
  "TipoCambioOperacion" DECIMAL(28,16),
  "IdTipoInstrumento" VARCHAR(50),
  "NombreTipoInstrumento" VARCHAR(500),
  "IdClasificacionTipoInstrumento" VARCHAR(50),
  "NombreClasificacionTipoInstrumento" VARCHAR(500),
  "IdInstrumento" BIGINT,
  "Cantidad" DECIMAL(28,16),
  "NemotecnicoOriginal" VARCHAR(500),
  "EsCargoAbonoPorTraspaso" VARCHAR(5),
  "EsCargoAbonoPorTraspasoTraducido" VARCHAR(5),
  "IdCargoAbonoRelacionadoTraspaso" BIGINT,
  "NombreClienteRelacionado" VARCHAR(500),
  "CodigoPortafolioRelacionado" VARCHAR(500),
  "IdTipoComprobante" VARCHAR(50),
  "CodigoTipoComprobante" VARCHAR(500),
  "NombreTipoComprobante" VARCHAR(500),
  "IdComprobante" BIGINT,
  "FolioComprobante" VARCHAR(500),
  "EsVentaCorta" VARCHAR(5),
  "FechaComprobante" TIMESTAMP,
  "IdComprobanteDetalle" BIGINT,
  "IdPersonaPagoCobro" BIGINT,
  "EsCertificadoAutomatico" VARCHAR(5),
  "IdUsuarioCertificador" BIGINT,
  "FechaCertificacion" TIMESTAMP,
  "IdContraparte" BIGINT,
  "EsIndicadorBilateral" VARCHAR(5),
  "IdEmpresa" BIGINT,
  "CodigoEmpresa" VARCHAR(500),
  "NombreEmpresa" VARCHAR(500),
  "EsIngresoManual" VARCHAR(5),
  "IdEvento" BIGINT,
  "IdUsuario" BIGINT,
  "NombreUsuario" VARCHAR(500),
  "IdEstado" VARCHAR(50),
  "NombreEstado" VARCHAR(500),
  "NombreUsuarioCert" VARCHAR(500),
  "IdCarga" BIGINT,
  "IdEstadoSII" VARCHAR(50),
  "IdRespuestaDTE" BIGINT,
  "DocumentoIdentidad" VARCHAR(500),
  "CodigoPortafolio" VARCHAR(500)
);

-- view modelled as a table: ConsultarCargosAbonos (columns read by GenerarArchivoCargoAbono / ConsultaComparacionSaldo / CuadraturaCierreDiario)
CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" (
  "IdCargoAbonoMovimiento" BIGINT,
  "FechaSistema" DATE,
  "FechaIngresoRegistro" TIMESTAMP,
  "IdCuentaInversion" BIGINT,
  "CodigoCuentaInversion" VARCHAR(500),
  "NombreCuentaInversion" VARCHAR(500),
  "IdCliente" BIGINT,
  "NombreCliente" VARCHAR(500),
  "IdPortafolio" BIGINT,
  "IdMoneda" VARCHAR(50),
  "CodigoMoneda" VARCHAR(500),
  "NombreMoneda" VARCHAR(500),
  "SimboloMoneda" VARCHAR(500),
  "DecimalesMoneda" INT,
  "IdTipoMovimiento" VARCHAR(50),
  "TipoMovimiento" VARCHAR(500),
  "NombreTipoMovimiento" VARCHAR(500),
  "IdConcepto" VARCHAR(50),
  "NombreConcepto" VARCHAR(500),
  "OrigenConcepto" VARCHAR(500),
  "Descripcion" VARCHAR(500),
  "IdCondicionDeLiquidacion" VARCHAR(50),
  "CodigoCondicionDeLiquidacion" VARCHAR(500),
  "NombreCondicionDeLiquidacion" VARCHAR(500),
  "PlazoCondicionDeLiquidacion" INT,
  "FechaEfectivaMovimiento" DATE,
  "Monto" DECIMAL(28,16),
  "SaldoPorFechaEfectiva" DECIMAL(28,16),
  "SaldoPorFechaMovimiento" DECIMAL(28,16),
  "IdModulo" VARCHAR(50),
  "IdTipoProducto" VARCHAR(50),
  "CodigoTipoProducto" VARCHAR(500),
  "NombreTipoProducto" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(50),
  "CodigoTipoOperacion" VARCHAR(500),
  "NombreTipoOperacion" VARCHAR(500),
  "AccionTipoOperacion" VARCHAR(500),
  "IdOperacion" BIGINT,
  "FechaOperacion" TIMESTAMP,
  "FechaLiquidacionOperacion" DATE,
  "FechaVencimientoOperacion" DATE,
  "IdMonedaOperacion" VARCHAR(50),
  "MontoOperacion" DECIMAL(28,16),
  "TipoCambioOperacion" DECIMAL(28,16),
  "IdTipoInstrumento" VARCHAR(50),
  "NombreTipoInstrumento" VARCHAR(500),
  "IdClasificacionTipoInstrumento" VARCHAR(50),
  "NombreClasificacionTipoInstrumento" VARCHAR(500),
  "IdInstrumento" BIGINT,
  "Cantidad" DECIMAL(28,16),
  "NemotecnicoOriginal" VARCHAR(500),
  "EsCargoAbonoPorTraspaso" VARCHAR(5),
  "EsCargoAbonoPorTraspasoTraducido" VARCHAR(5),
  "IdCargoAbonoRelacionadoTraspaso" BIGINT,
  "NombreClienteRelacionado" VARCHAR(500),
  "CodigoPortafolioRelacionado" VARCHAR(500),
  "IdTipoComprobante" VARCHAR(50),
  "CodigoTipoComprobante" VARCHAR(500),
  "NombreTipoComprobante" VARCHAR(500),
  "IdComprobante" BIGINT,
  "FolioComprobante" VARCHAR(500),
  "EsVentaCorta" VARCHAR(5),
  "FechaComprobante" TIMESTAMP,
  "IdComprobanteDetalle" BIGINT,
  "IdPersonaPagoCobro" BIGINT,
  "EsCertificadoAutomatico" VARCHAR(5),
  "IdUsuarioCertificador" BIGINT,
  "FechaCertificacion" TIMESTAMP,
  "IdContraparte" BIGINT,
  "EsIndicadorBilateral" VARCHAR(5),
  "IdEmpresa" BIGINT,
  "CodigoEmpresa" VARCHAR(500),
  "NombreEmpresa" VARCHAR(500),
  "EsIngresoManual" VARCHAR(5),
  "IdEvento" BIGINT,
  "IdUsuario" BIGINT,
  "NombreUsuario" VARCHAR(500),
  "IdEstado" VARCHAR(50),
  "NombreEstado" VARCHAR(500),
  "NombreUsuarioCert" VARCHAR(500),
  "IdCarga" BIGINT,
  "IdEstadoSII" VARCHAR(50),
  "IdRespuestaDTE" BIGINT,
  "DocumentoIdentidad" VARCHAR(500),
  "CodigoPortafolio" VARCHAR(500),
  "Cargo" DECIMAL(28,16),
  "Abono" DECIMAL(28,16),
  "CodigoUsuarioCreador" VARCHAR(500),
  "CodigoUsuarioCertificador" VARCHAR(500),
  "GlosaAnulacion" VARCHAR(500),
  "AutorizacionEstado" VARCHAR(500),
  "NombreEstadoEvento" VARCHAR(500),
  "Producto" VARCHAR(500),
  "Relacion" VARCHAR(500),
  "CodigoNombreMoneda" VARCHAR(500),
  "Proposito" VARCHAR(500),
  "Saldo" DECIMAL(28,16),
  "IdEjecutivo" BIGINT
);

-- historic movements table (before PLA.FechaGoLive)
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimientoHistorico" (
  "FechaSistema" DATE,
  "FechaIngresoRegistro" TIMESTAMP,
  "FechaEfectivaMovimiento" DATE,
  "NombreConcepto" VARCHAR(500),
  "EsVentaCorta" VARCHAR(5),
  "NemotecnicoOriginal" VARCHAR(500),
  "IdCliente" BIGINT,
  "NombreCliente" VARCHAR(500),
  "IdPortafolio" BIGINT,
  "IdMoneda" VARCHAR(50),
  "CodigoMoneda" VARCHAR(500),
  "NombreMoneda" VARCHAR(500),
  "SimboloMoneda" VARCHAR(500),
  "DecimalesMoneda" INT,
  "DocumentoIdentidad" VARCHAR(500),
  "CodigoPortafolio" VARCHAR(500),
  "Descripcion" VARCHAR(500),
  "Cargo" DECIMAL(28,16),
  "Abono" DECIMAL(28,16),
  "SaldoPorFechaMovimiento" DECIMAL(28,16),
  "EsCargoAbonoPorTraspasoTraducido" VARCHAR(5),
  "IdEstado" VARCHAR(50),
  "NombreEstado" VARCHAR(500),
  "IdTipoComprobante" VARCHAR(50),
  "NombreTipoComprobante" VARCHAR(500),
  "IdComprobante" BIGINT,
  "FolioComprobante" VARCHAR(500),
  "FechaComprobante" TIMESTAMP,
  "IdTipoProducto" VARCHAR(50),
  "NombreTipoProducto" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(50),
  "NombreTipoOperacion" VARCHAR(500),
  "FechaOperacion" TIMESTAMP,
  "FechaVencimientoOperacion" DATE,
  "IdOperacion" BIGINT,
  "MontoOperacion" DECIMAL(28,16),
  "IdCargoAbonoMovimiento" BIGINT,
  "IdCargoAbonoRelacionadoTraspaso" BIGINT,
  "NombreClienteRelacionado" VARCHAR(500),
  "CodigoPortafolioRelacionado" VARCHAR(500),
  "FechaCertificacion" TIMESTAMP,
  "EsIngresoManual" VARCHAR(5),
  "IdCarga" BIGINT,
  "IdEstadoSII" VARCHAR(50),
  "IdRespuestaDTE" BIGINT
);

-- temporary/work tables
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.ConsultarMovimientoCuentaInversion_Proceso" (
  "IdPortafolio" BIGINT, "IdMoneda" VARCHAR(3), "IdEstado" VARCHAR(3), "SaldoInicial" DECIMAL(28,16), "FechaDesde" DATE, "FechaHasta" DATE,
  "IdMatrizAgrupador" BIGINT, "EsADC" VARCHAR(1), "SaldoPorFechaEfectiva" VARCHAR(1)
);

CREATE TABLE "BSCL.CuentaInversion.BD::CTA.Saldo_Cuenta_Proceso" (
  "IdCuentaInversion" BIGINT, "SaldoAnterior" DECIMAL(28,16), "FechaAnterior" DATE
);

-- PK on the process id is an ASSUMPTION (the legacy deletes by id then re-inserts)
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CuadraturaDiariaOperaciones" (
  "IdCuadraturaDiariaOperaciones"         VARCHAR(50) NOT NULL PRIMARY KEY,
  "IdEmpresa"                             BIGINT,
  "OperacionCuadraturaDiariaOperaciones"  VARCHAR(200),
  "FechaEjecucion"                        DATE,
  "TotalOperaciones"                      BIGINT,
  "OperacionesConProblemas"               BIGINT,
  "TipoOperacion"                         VARCHAR(50),
  "Resultado"                             VARCHAR(50),
  "Orden"                                 BIGINT,
  "DetalleCuadraturaJSON"                 VARCHAR(1000000),
  "IdUsuario"                             BIGINT
);

CREATE TABLE "BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion" (
  "IdEmpresa" BIGINT, "CodigoParametro" VARCHAR(100), "Valor" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.Configuracion" (
  "IdEmpresa" BIGINT, "CodigoParametro" VARCHAR(100), "Valor" VARCHAR(500)
);

CREATE TABLE "BSCL.Facturacion.BD::FAC.Comprobante" (
  "IdComprobante" BIGINT, "MayorMenorValor" DECIMAL(28,16), "MontoRetencionGananciaCap" DECIMAL(28,16)
);

CREATE TABLE "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" (
  "IdComprobanteDetalle" BIGINT, "IdComprobante" BIGINT, "NemotecnicoInstrumento" VARCHAR(100)
);

-- view modelled as a table: client/portfolio lookup of CrearCargoAbonoWS
CREATE TABLE "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" (
  "IdEmpresa" BIGINT, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "CodigoPortafolio" VARCHAR(50), "DocumentoIdentidad" VARCHAR(30),
  "NombreCompletoPortafolio" VARCHAR(300), "IdEstado" VARCHAR(3), "IdEstadoCliente" VARCHAR(3)
);

-- <<< otros <<<

-- >>> otros2 (generated by scratch gen_schema_otros2.py) >>>

-- FMM tables: columns read by ComunesSolicitudes_SelectSerieFondo.sql added INVISIBLE (types from the SerieFondoSolicitudes POJO);
-- the visible columns keep the positional INSERTs of the earlier cases working
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "IdEmisor" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "IdPaisEmisor" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "Nemotecnico" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "NombreFondo" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "IdPaisOrigen" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "CodigoSVS" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "FechaCreacion" DATE INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "FechaIngresoRegistro" TIMESTAMP INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "FechaConstitucion" DATE INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "FechaCierre" DATE INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "Vigencia" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "FondoExtranjero" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "IdTipoInstrumento" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "IdCategoriaPerfilFondoMutuo" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "UsaHorarioAporteDefault" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "UsaHorarioRescateDefault" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "UsaHorarioReinversionDefault" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "UsaHorarioLiquidezDefault" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "EstadoAporteComercial" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "EstadoRescateComercial" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "EstadoReinversionComercial" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "EstadoLiquidezComercial" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "EstadoAporteBackoffice" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "EstadoRescateBackoffice" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "EstadoReinversionBackoffice" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "EstadoLiquidezBackoffice" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "BloqueoAporte" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "BloqueoRescate" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "BloqueoIndefinido" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "FechaBloqueoDesde" DATE INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "FechaBloqueoHasta" DATE INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "FechaFondo" DATE INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "AperturaAutomaticaFondo" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "FondoNormativo" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "IdTipoDocumentoIdentidadFondo" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "DocumentoIdentidadFondo" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "IdMercadoFondoMutuo" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ADD COLUMN "IdTipoFondo" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "NombreSerieFondo" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "CodigoSerie" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "Alias" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "DecimalesValorCuota" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "DecimalesCuota" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "MontoMinimo" DECIMAL(28,16) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "MontoMaximo" DECIMAL(28,16) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "DiasRetencionesAporte" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "DiasRetencionesRescate" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "DiasValutaCurseAporte" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "DiasValutaCurseRescate" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "DiasValutaValorCuotaAporte" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "DiasValutaValorCuotaRescate" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "DiasValutaCertificacionAporte" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "DiasValutaCertificacionRescate" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "ExcluyeGarantia" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "CobraComision" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "Diferido" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "InstrumentoDCV" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "Omnibus" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "PatrimonioMinimo" DECIMAL(28,16) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "IdSerieFondoOrigen" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "FechaIngresoRegistro" TIMESTAMP INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "IdEstado" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "IdCliente" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "IdPortafolio" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "IdMedioDePagoAbono" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "IdMedioDePagoCargo" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "IdFamiliaFondo" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "CambioFondo" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "DiasComision" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "PorcentajeComision" DECIMAL(28,16) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "PorcentajeRemuneracion" DECIMAL(28,16) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "PorcentajeGastoOpera" DECIMAL(28,16) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "PorcentajeCostoAdm" DECIMAL(28,16) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "AportesAltos" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "PatrimonioAltos" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "EsAPV" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "RestringeAporte" VARCHAR(200) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ADD COLUMN "RestringeRescate" VARCHAR(200) INVISIBLE;
CREATE TABLE "BSCL.FondoMutuo.BD::FMM.TipoFondo" (
  "IdTipoFondo" VARCHAR(200),
  "NombreCorto" VARCHAR(200),
  "Descripcion" VARCHAR(200),
  "IndicadorMoneyMarket" VARCHAR(200)
);

-- outputs of the HANA stored procedure CrearCargoAbono (PLA.UsaCargoAbonoJava = N): the case seed plays the procedure
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_CargoAbonoGenerado" ("NumeroCargoAbono" BIGINT, "IdCargoAbonoMovimiento" BIGINT, "IdTipoMovimiento" VARCHAR(6));
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_InformacionDetalle" ("NumeroCargoAbono" BIGINT, "IdCargoAbonoMovimiento" BIGINT, "Codigo" VARCHAR(100), "Descripcion" VARCHAR(500));
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_AdvertenciasDetalle" ("NumeroCargoAbono" BIGINT, "Codigo" VARCHAR(100), "RequiereAutorizacion" VARCHAR(1), "Descripcion" VARCHAR(500));
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle" ("NumeroCargoAbono" BIGINT, "Codigo" VARCHAR(100), "Descripcion" VARCHAR(500));

-- <<< otros2 <<<
