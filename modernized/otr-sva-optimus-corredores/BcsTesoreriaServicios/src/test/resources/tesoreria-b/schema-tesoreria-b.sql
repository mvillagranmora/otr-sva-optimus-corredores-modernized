-- H2 (MODE=LEGACY) stand-in for the SAP HANA objects read/written by the Tesoreria "family B" services (PAC mandates, mass loads,
-- clearing, BCP, bank balances, distribution, payment files, transfer nominas). Quoted names are the exact HANA catalog names so the
-- production SQL resources (cl/bcs/tesoreria/sql/*.sql, ported verbatim to the modern src/main/resources/sql) run unmodified through
-- support/H2Hana.java. HANA views are modelled as tables: the seeds insert the rows the view would return. Column types/scales are an
-- ASSUMPTION (the HANA DDL is not in the repository): money columns are DECIMAL(28,16) so the stand-in itself rounds nothing.
-- Executed by BOTH the legacy oracle runner and the modern tests.

CREATE TABLE DUMMY (X VARCHAR(1));
INSERT INTO DUMMY VALUES ('X');

CREATE ALIAS TO_NVARCHAR FOR "cl.bcs.tesoreria.familia_b.support.H2Hana.toNvarchar";
CREATE ALIAS TO_VARCHAR FOR "cl.bcs.tesoreria.familia_b.support.H2Hana.toVarchar";
CREATE ALIAS TO_BIGINT FOR "cl.bcs.tesoreria.familia_b.support.H2Hana.toBigint";
CREATE ALIAS TO_DECIMAL FOR "cl.bcs.tesoreria.familia_b.support.H2Hana.toDecimal";
CREATE ALIAS TO_DATE FOR "cl.bcs.tesoreria.familia_b.support.H2Hana.toDate";
CREATE ALIAS TO_TIMESTAMP FOR "cl.bcs.tesoreria.familia_b.support.H2Hana.toTimestamp";
CREATE ALIAS ADD_SECONDS FOR "cl.bcs.tesoreria.familia_b.support.H2Hana.addSeconds";
CREATE ALIAS ADD_DAYS FOR "cl.bcs.tesoreria.familia_b.support.H2Hana.addDays";
CREATE ALIAS HANA_UTCNOW FOR "cl.bcs.tesoreria.familia_b.support.H2Hana.utcNow";

-- ---------------------------------------------------------------------------------------------- Plataforma reference tables
CREATE TABLE "BSCL.Plataforma.BD::PLA.Moneda" (
  "IdMoneda" VARCHAR(3) NOT NULL PRIMARY KEY,
  "Nombre"   VARCHAR(100)
);
CREATE TABLE "BSCL.Plataforma.BD::BAN.Banco" (
  "IdBanco"     BIGINT NOT NULL PRIMARY KEY,
  "NombreBanco" VARCHAR(100)
);

-- ---------------------------------------------------------------------------------------------- Tesoreria bank accounts
CREATE TABLE "BSCL.Tesoreria.BD::TES.CuentaBanco" (
  "IdCuentaBanco"        BIGINT NOT NULL PRIMARY KEY,
  "IdEmpresa"            BIGINT,
  "IdBanco"              BIGINT,
  "IdMoneda"             VARCHAR(3),
  "NumeroCuentaBancaria" VARCHAR(30),
  "NombreCuentaBancaria" VARCHAR(100)
);

-- GBL = HANA global temporary tables (content per session; here plain tables, emptied by the rollback of a failed call)
CREATE TABLE "BSCL.Tesoreria.BD::GBL.ConsultarSaldoCuentaBanco_CuentasAConsultar" (
  "IdCuentaBanco" BIGINT,
  "IdBanco"       BIGINT,
  "IdMoneda"      VARCHAR(3)
);
CREATE TABLE "BSCL.Tesoreria.BD::GBL.ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco" (
  "IdCuentaBanco" BIGINT,
  "FechaSaldo"    VARCHAR(10),
  "Orden"         INTEGER
);

-- views modelled as tables (the seeds play the view)
CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" (
  "IdComprobanteFormaDePago" BIGINT NOT NULL PRIMARY KEY,
  "IdComprobante"            BIGINT,
  "IdCuentaBancariaCorredora" BIGINT,
  "IdEstado"                 VARCHAR(3),
  "IdEstadoComprobante"      VARCHAR(3),
  "AfectoSaldo"              VARCHAR(1),
  "FechaAfectaSaldo"         DATE,
  "FechaHoraAfectaSaldo"     TIMESTAMP,
  "FechaLiquidacion"         DATE,
  "FechaHoraLiquidacion"     TIMESTAMP,
  "MontoIngreso"             DECIMAL(28,16),
  "MontoEgreso"              DECIMAL(28,16),
  "MontoConSigno"            DECIMAL(28,16),
  "SaldoDisponible"          DECIMAL(28,16)
);
CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago" (
  "IdIngresoEgresoFormaDePago" BIGINT NOT NULL PRIMARY KEY,
  "IdCuentaBancariaCorredora"  BIGINT,
  "IdEstado"                   VARCHAR(3),
  "IdEstadoIngresoEgreso"      VARCHAR(3),
  "FechaLiquidacion"           DATE,
  "MontoIngreso"               DECIMAL(28,16),
  "MontoEgreso"                DECIMAL(28,16),
  "MontoConSigno"              DECIMAL(28,16)
);

-- ---------------------------------------------------------------------------------------------- ConsultarLibretaBanco
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
CREATE TABLE "BSCL.Plataforma.BD::PLA.Estado" (
  "IdEstado" VARCHAR(3) NOT NULL PRIMARY KEY,
  "Nombre"   VARCHAR(100)
);
CREATE TABLE "BSCL.Plataforma.BD::PLA.Concepto" (
  "IdConcepto" VARCHAR(10) NOT NULL PRIMARY KEY,
  "Nombre"     VARCHAR(100)
);
CREATE TABLE "BSCL.Portafolio.BD::POR.Portafolio" (
  "IdPortafolio"     BIGINT NOT NULL PRIMARY KEY,
  "IdCliente"        BIGINT,
  "IdEstado"         VARCHAR(3),
  "CodigoPortafolio" VARCHAR(30)
);
CREATE TABLE "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" (
  "IdTipoSocioDeNegocio"     VARCHAR(10) NOT NULL PRIMARY KEY,
  "NombreTipoSocioDeNegocio" VARCHAR(100)
);
CREATE TABLE "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" (
  "IdConcepto" VARCHAR(10) NOT NULL PRIMARY KEY,
  "Origen"     VARCHAR(10)
);
CREATE TABLE "BSCL.Plataforma.BD::MER.ParametroFinanciero" (
  "IdParametroFinanciero" VARCHAR(10),
  "Fecha"                 DATE,
  "Valor"                 DECIMAL(28,16)
);
CREATE TABLE "BSCL.Tesoreria.BD::TES.Comprobante" (
  "IdComprobante"            BIGINT NOT NULL PRIMARY KEY,
  "IdEmpresa"                BIGINT,
  "IdConcepto"               VARCHAR(10),
  "IdEstado"                 VARCHAR(3),
  "Monto"                    DECIMAL(28,16),
  "FechaLiquidacion"         DATE,
  "NombrePersona"            VARCHAR(200),
  "DocumentoIdentidad"       VARCHAR(30),
  "IdPortafolioTitularPago"  BIGINT,
  "IdTipoPersonaTitularPago" VARCHAR(10),
  "Glosa"                    VARCHAR(500)
);
CREATE TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" (
  "IdComprobanteFormaDePago"  BIGINT NOT NULL PRIMARY KEY,
  "IdComprobante"             BIGINT,
  "IdComprobantePartida"      BIGINT,
  "IdTipoMovimiento"          VARCHAR(10),
  "IdMedioDePago"             VARCHAR(10),
  "NombreMedioDePago"         VARCHAR(100),
  "NumeroDocumentoCorredora"  BIGINT,
  "IdMoneda"                  VARCHAR(3),
  "Monto"                     DECIMAL(28,16),
  "IdEstado"                  VARCHAR(3),
  "FechaAfectaSaldo"          TIMESTAMP,
  "SaldoDisponible"           DECIMAL(28,16),
  "SaldoContable"             DECIMAL(28,16),
  "IdCuentaBancariaCorredora" BIGINT,
  "Depositado"                VARCHAR(1),
  "DiasRetencion"             INT,
  "IdDeposito"                BIGINT
);
CREATE TABLE "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" (
  "IdComprobanteDetalle" BIGINT NOT NULL PRIMARY KEY,
  "IdComprobante"        BIGINT,
  "IdEstado"             VARCHAR(3),
  "Monto"                DECIMAL(28,16)
);
CREATE TABLE "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" (
  "IdComprobanteDetalle" BIGINT,
  "IdIngresoEgreso"      BIGINT
);
CREATE TABLE "BSCL.Tesoreria.BD::TES.IngresoEgreso" (
  "IdIngresoEgreso"        BIGINT NOT NULL PRIMARY KEY,
  "IdOperacion"            BIGINT,
  "IdConcepto"             VARCHAR(10),
  "IdEstado"               VARCHAR(3),
  "IdComprobanteOrigen"    BIGINT,
  "FechaComprobanteOrigen" DATE,
  "FechaHoraEmision"       TIMESTAMP,
  "IdUsuarioEmision"       BIGINT
);
CREATE TABLE "BSCL.Facturacion.BD::FAC.MovimientoComprobante" (
  "IdComprobante" BIGINT,
  "IdTransaccion" BIGINT
);
CREATE TABLE "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" (
  "IdOperacion"           BIGINT,
  "IdOperacionUI"         BIGINT,
  "IdMonedaPrincipal"     VARCHAR(3),
  "IdMonedaSecundaria"    VARCHAR(3),
  "MontoMonedaPrincipal"  DECIMAL(28,16),
  "MontoMonedaSecundaria" DECIMAL(28,16),
  "TipoCambioCierre"      DECIMAL(28,16),
  "ParidadCierre"         DECIMAL(28,16),
  "Nemotecnico"           VARCHAR(20),
  "OperacionFicticia"     VARCHAR(1),
  "FechaOperacion"        DATE
);

-- ---------------------------------------------------------------------------------------------- Clearing financiero (Consultar / Procesar)
CREATE TABLE "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" (
  "IdEmpresa" BIGINT NOT NULL PRIMARY KEY, "Codigo" VARCHAR(20), "Nombre" VARCHAR(100), "IdEstado" VARCHAR(3), "IdPais" BIGINT,
  "IdSocioDeNegocio" BIGINT, "NombreCorto" VARCHAR(50), "IdContraparte" BIGINT, "IdPersona" BIGINT, "FechaIngreso" DATE, "FechaCreacion" DATE,
  "FechaModificacion" DATE, "IdDocumentoIdentidad" BIGINT, "DocumentoIdentidad" VARCHAR(30), "TipoDocumentoIdentidad" VARCHAR(10),
  "DocumentoIdentidadAdicional" VARCHAR(30), "TipoDocumentoIdentidadAdicional" VARCHAR(10), "RazonSocial" VARCHAR(100), "CodigoContraparte" VARCHAR(20),
  "EsCorredora" VARCHAR(1), "DocumentoIdentidadRepresentante" VARCHAR(30), "DireccionElectronicaPrincipal" VARCHAR(100),
  "DireccionFisicaPrincipalCalle" VARCHAR(100), "DireccionFisicaPrincipalNumero" VARCHAR(20), "DireccionFisicaPrincipalDepartamento" VARCHAR(20),
  "DireccionFisicaPrincipalSector" VARCHAR(50), "DireccionFisicaPrincipalNombreCiudad" VARCHAR(50), "DireccionFisicaPrincipalNombreDivisionPais" VARCHAR(50),
  "DireccionFisicaPrincipalNombrePais" VARCHAR(50), "TelefonoPrincipal" VARCHAR(30)
);
CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreria" (
  "IdEmpresa" BIGINT, "CodigoParametro" VARCHAR(60), "Descripcion" VARCHAR(200), "Valor" VARCHAR(100)
);
CREATE TABLE "BSCL.Plataforma.BD::INS.TipoOperacion" (
  "IdTipoOperacion" VARCHAR(6) NOT NULL PRIMARY KEY, "AccionOperacion" VARCHAR(6)
);
CREATE TABLE "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" (
  "IdCondicionDeLiquidacion" VARCHAR(2) NOT NULL PRIMARY KEY, "Codigo" VARCHAR(2), "Dias" INT
);
CREATE TABLE "BSCL.Plataforma.BD::INS.TipoInstrumento" (
  "IdTipoInstrumento" VARCHAR(3) NOT NULL PRIMARY KEY, "IdClasificacionTipoInstrumento" VARCHAR(3)
);
CREATE TABLE "BSCL.Plataforma.BD::INS.Instrumento" (
  "IdInstrumento" BIGINT NOT NULL PRIMARY KEY, "IdTipoInstrumento" VARCHAR(3)
);
CREATE TABLE "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" (
  "IdCorredora" BIGINT NOT NULL PRIMARY KEY, "DocumentoIdentidad" VARCHAR(30), "NombreCorto" VARCHAR(50), "CodigoInscripcionRegulador" VARCHAR(20)
);
CREATE TABLE "BSCL.Plataforma.BD::INS.Corredora" (
  "IdCorredora" BIGINT NOT NULL PRIMARY KEY, "IdPersona" BIGINT
);
CREATE TABLE "BSCL.Orden.BD.Vistas::ConsultarTransaccion" (
  "IdTransaccion" BIGINT NOT NULL PRIMARY KEY, "IdEmpresa" BIGINT, "IdClasificacionTipoInstrumento" VARCHAR(3), "FechaSistemaCreacion" DATE,
  "IdTipoInstrumento" VARCHAR(3), "FolioTransaccion" VARCHAR(20), "IndicadorOrdenDirecta" VARCHAR(1), "IdBolsa" VARCHAR(5), "MontoTransaccion" DECIMAL(28,16),
  "IdTipoOperacion" VARCHAR(6), "AccionTipoOperacion" VARCHAR(6), "IdCondicionDeLiquidacion" VARCHAR(2), "CodigoCondicionDeLiquidacion" VARCHAR(2),
  "InstrumentoUI" VARCHAR(40), "IdInstrumento" BIGINT, "IdInstrumentoSubyacente" BIGINT, "IdCorredorContraparte" BIGINT, "CodigoOperadorContraparte" VARCHAR(20),
  "CodigoOperadorCorredor" VARCHAR(20), "FechaLiquidacion" DATE, "FechaVencimiento" DATE, "EsAPlazo" VARCHAR(1), "EsBilateral" VARCHAR(1), "IdCamara" VARCHAR(5),
  "IdComprobanteTesoreria" BIGINT, "CantidadTransaccion" DECIMAL(28,16), "PrecioTransaccion" DECIMAL(28,16), "Tasa" DECIMAL(28,16),
  "IdMonedaTransaccion" VARCHAR(3), "MontoRetencion" DECIMAL(28,16), "IdEstadoTransaccion" VARCHAR(3), "BloqueoLiquidacion" VARCHAR(1)
);
CREATE TABLE "BSCL.Orden.BD::ORD.Transaccion" (
  "IdTransaccion" BIGINT NOT NULL PRIMARY KEY, "IdBolsa" VARCHAR(5), "IdClasificacionTipoInstrumento" VARCHAR(3), "IdComprobanteTesoreria" BIGINT,
  "IdCorredorContraparte" BIGINT, "IdMoneda" VARCHAR(3)
);
CREATE TABLE "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente" (
  "IdEventoCapitalCliente" BIGINT NOT NULL PRIMARY KEY, "IdEventoCapital" BIGINT, "IdTipoEventoCapital" VARCHAR(10), "TipoEvento" VARCHAR(30), "NumeroDividendo" BIGINT,
  "Secuencia" BIGINT, "IdTipoInstrumento" VARCHAR(3), "Nemo" VARCHAR(20), "IdMoneda" VARCHAR(3), "NombreCliente" VARCHAR(100), "CantidadDerecho" DECIMAL(28,16),
  "Total" DECIMAL(28,16), "IdBoveda" BIGINT, "IdComprobanteTesoreria" BIGINT, "IdBolsa" VARCHAR(5), "DocumentoIdentidad" VARCHAR(30), "IdCorredorContraparte" BIGINT,
  "TipoOperacion" VARCHAR(1), "MontoRetencion" DECIMAL(28,16), "AfectoaClearing" VARCHAR(1), "ProcesadoClearing" VARCHAR(1), "FechaPago" DATE
);
CREATE TABLE "BSCL.Custodia.BD::CUS.EventoCapitalCliente" (
  "IdEventoCapitalCliente" BIGINT NOT NULL PRIMARY KEY, "IdComprobanteTesoreria" BIGINT
);
CREATE TABLE "BSCL.Custodia.BD::CUS.Cartera" (
  "IdCartera" BIGINT NOT NULL PRIMARY KEY, "FolioExternoReferencia" VARCHAR(20)
);
CREATE TABLE "BSCL.Custodia.BD.Vistas::SIM.ConsultarAnticipos" (
  "AccionOperacion" VARCHAR(6), "EGRCantidad" DECIMAL(28,16), "EGRFechaOperacion" DATE, "EGRFolioComprobante" BIGINT, "FolioComprobante" BIGINT,
  "IdEmpresa" BIGINT, "NombreOperacion" VARCHAR(50), "suby_Nemotecnico" VARCHAR(30)
);
CREATE TABLE "BSCL.Custodia.BD.Vistas::SIM.ConsultarAnticiposHis" (
  "AccionOperacion" VARCHAR(6), "EGRCantidad" DECIMAL(28,16), "EGRFechaOperacion" DATE, "EGRFolioComprobante" BIGINT, "FolioComprobante" BIGINT,
  "IdEmpresa" BIGINT, "NombreOperacion" VARCHAR(50), "suby_Nemotecnico" VARCHAR(30), "FechaRespaldo" DATE
);
CREATE TABLE "BSCL.Facturacion.BD::FAC.Comprobante" (
  "IdComprobante" BIGINT NOT NULL PRIMARY KEY
);
CREATE TABLE "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" (
  "IdComprobante" BIGINT, "IdTransaccion" BIGINT, "FolioTransaccion" VARCHAR(20)
);
CREATE TABLE "BSCL.Plataforma.BD::PLA.Empresa" (
  "IdEmpresa" BIGINT NOT NULL PRIMARY KEY, "IdSocioDeNegocio" BIGINT
);
CREATE TABLE "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" (
  "IdSocioDeNegocio" BIGINT NOT NULL PRIMARY KEY, "IdPersona" BIGINT, "IdCliente" BIGINT, "IdEmpresa" BIGINT, "Borrado" VARCHAR(1)
);
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" (
  "IdCargoAbonoMovimiento" BIGINT NOT NULL PRIMARY KEY, "IdComprobante" BIGINT, "IdComprobanteTesoreria" BIGINT, "MontoOperacion" DECIMAL(28,16)
);
CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" (
  "IdCargoAbonoMovimiento" BIGINT NOT NULL PRIMARY KEY, "NombreCliente" VARCHAR(100), "IdPortafolio" BIGINT, "FechaOperacion" DATE, "TipoMovimiento" VARCHAR(2),
  "NombreTipoMovimiento" VARCHAR(50), "IdMoneda" VARCHAR(3), "IdComprobanteTesoreria" BIGINT, "Monto" DECIMAL(28,16), "IdEstado" VARCHAR(3), "IdConcepto" VARCHAR(10),
  "IdOperacion" BIGINT
);
CREATE TABLE "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCamaraDeCompensacion" (
  "IdCamaraDeCompensacion" VARCHAR(10), "Codigo" VARCHAR(10), "Nombre" VARCHAR(50), "IdPersona" BIGINT, "IdPaisCamara" BIGINT, "IdEstadoCamara" VARCHAR(3),
  "DocumentoIdentidad" VARCHAR(30), "RazonSocial" VARCHAR(100)
);
CREATE TABLE "BSCL.Plataforma.BD::CAM.GrupoCamaraDeCompensacion" (
  "NombreGrupoCamara" VARCHAR(30), "Camaras" VARCHAR(10), "IdEstado" VARCHAR(3), "IdCamaraDeCompensacion" VARCHAR(10), "Descripcion" VARCHAR(100),
  "CodigoGrupo" VARCHAR(10), "SecuenciaAgrupacion" VARCHAR(10)
);
CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" (
  "IdTipoMovimiento" VARCHAR(6), "IdMedioDePago" VARCHAR(3), "IdModalidadDePago" VARCHAR(6), "IdCuentaBanco" BIGINT, "IdMoneda" VARCHAR(3), "IdConcepto" VARCHAR(6),
  "NombreEntidad" VARCHAR(40), "IdEstado" VARCHAR(3), "IdEmpresa" BIGINT, "CodigoTipoCargaMasiva" VARCHAR(50), "IdPersonaEntidadRecaudadora" BIGINT, "ProfundidadProceso" VARCHAR(10)
);
CREATE TABLE "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" (
  "IdPreferenciaPago" VARCHAR(30), "IdCuentaBancaria" BIGINT, "IdBancoCuentaBancaria" BIGINT, "NombreBancoCuentaBancaria" VARCHAR(100), "NumeroCuentaBancaria" VARCHAR(30),
  "IdEstado" VARCHAR(3), "IdTipoOperacion" VARCHAR(6), "TipoOperacion" VARCHAR(50), "NombreTipoCuentaBancaria" VARCHAR(50), "IdTipoCuentaBancaria" VARCHAR(4),
  "IdMedioDePago" VARCHAR(3), "MedioDePago" VARCHAR(50), "IdMoneda" VARCHAR(3), "RecibePagaPortafolio" VARCHAR(1), "IdPortafolio" BIGINT, "IdCliente" BIGINT, "IdEmpresa" BIGINT
);
CREATE TABLE "BSCL.Plataforma.BD::PLA.CamaraDeCompensacion" (
  "IdPersona" BIGINT, "IdPais" BIGINT, "CodigoPortafolio" VARCHAR(30)
);

-- ---------------------------------------------------------------------------------------------- RecaudacionBCP / ConsultaBCP
CREATE ALIAS BINTOSTR FOR "cl.bcs.tesoreria.familia_b.support.H2Hana.bintoStr";
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::LOG.MensajeTransaccionBCP" START WITH 700;
CREATE TABLE "BSCL.Tesoreria.BD::LOG.MensajeTransaccionBCP" (
  "IdMensajeTransaccion" BIGINT NOT NULL PRIMARY KEY,
  "RqUID"                VARCHAR(100),
  "TipoTrasaccion"       VARCHAR(10),
  "MensajeEntrada"       VARCHAR(100000),
  "MensajeSalida"        VARCHAR(100000),
  "FechaHoraRecepcion"   TIMESTAMP,
  "FechaSistema"         DATE,
  "IdEmpresa"            BIGINT
);
CREATE TABLE "BSCL.SocioNegocio.BD::PER.Persona" (
  "IdPersona" BIGINT NOT NULL PRIMARY KEY, "Tipo" VARCHAR(1), "Nombres" VARCHAR(100), "ApellidoPaterno" VARCHAR(100), "ApellidoMaterno" VARCHAR(100),
  "RazonSocial" VARCHAR(200), "IdEstado" VARCHAR(3)
);
CREATE TABLE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" (
  "IdPersona" BIGINT, "IdTipoDocumentoIdentidad" VARCHAR(10), "DocumentoIdentidad" VARCHAR(30)
);
CREATE TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" (
  "IdCliente" BIGINT NOT NULL PRIMARY KEY, "IdPersona" BIGINT, "IdEstado" VARCHAR(3), "IdPortafolioDefecto" BIGINT
);
CREATE TABLE "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" (
  "IdEmpresa" BIGINT, "CodigoParametro" VARCHAR(60), "Valor" VARCHAR(100), "ValorDefecto" VARCHAR(100), "Descripcion" VARCHAR(200)
);
CREATE TABLE "BSCL.Plataforma.BD::REG.DiaFeriado" (
  "IdFeriado" BIGINT, "Tipo" VARCHAR(10), "Fecha" DATE, "Dia" INT, "DescripcionDia" VARCHAR(100), "AgnoCarga" INT, "IdPais" BIGINT, "IdEstado" VARCHAR(3)
);
ALTER TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" ADD COLUMN "IdPreferenciasPagoCorredor" BIGINT INVISIBLE;
ALTER TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" ADD COLUMN "IdBanco" BIGINT INVISIBLE;
ALTER TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" ADD COLUMN "NombreCuentaBancaria" VARCHAR(100) INVISIBLE;
ALTER TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" ADD COLUMN "IdTipoCuentaBancaria" VARCHAR(4) INVISIBLE;
ALTER TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" ADD COLUMN "IdMonedaCuentaBanco" VARCHAR(3) INVISIBLE;
ALTER TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" ADD COLUMN "NumeroCuentaBancaria" VARCHAR(30) INVISIBLE;
ALTER TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" ADD COLUMN "IdEstadoCuentaBanco" VARCHAR(3) INVISIBLE;

-- ---------------------------------------------------------------------------------------------- PAC mandates (Ingresar / Modificar / Anulacion / Revocacion)
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::IdMandatoPAC" START WITH 5000;
CREATE TABLE "BSCL.Tesoreria.BD::PAC.MandatoPAC" (
  "IdMandatoPAC" BIGINT NOT NULL PRIMARY KEY, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "FolioCSC" VARCHAR(20), "CuentaCorriente" VARCHAR(30),
  "FolioSAAPV" VARCHAR(20), "FolioPAC" BIGINT, "Receptor" VARCHAR(50), "Fecha" DATE, "NumeroMandato" BIGINT, "Firma" VARCHAR(50), "IdentServicio" VARCHAR(30),
  "Estado" VARCHAR(3), "EstadoCargo" VARCHAR(3), "IdEmpresa" BIGINT, "Canal" VARCHAR(10), "EstadoCargoBanco" VARCHAR(3), "EstadoRendicion" VARCHAR(3),
  "IdCarga" BIGINT, "IdCuentaBancaria" BIGINT, "EstadoPorAutorizar" VARCHAR(3), "EstadoContratoPorAutorizar" VARCHAR(3), "DiaDescuentoPorAutorizar" BIGINT
);
CREATE TABLE "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" (
  "IdFolioCSC" BIGINT, "FolioCSC" VARCHAR(20), "IdCliente" BIGINT, "DiaDescuento" BIGINT, "TipoPago" VARCHAR(3), "Estado" VARCHAR(3), "IdBanco" BIGINT,
  "MontoPesos" DECIMAL(28,16), "MontoUF" DECIMAL(28,16)
);
CREATE TABLE "BSCL.APV.BD::SCS.ContratoSAAPV" (
  "IdContratoSAAPV" BIGINT, "FolioContratoSAAPV" VARCHAR(20), "IdPersona" BIGINT, "IdCliente" BIGINT, "IdEmpleador" BIGINT, "IdEstadoEnvioNotificacion" VARCHAR(3),
  "IdEstadoSolicitud" VARCHAR(3), "IdEstado" VARCHAR(3), "IdGlosa" BIGINT, "DescripcionGlosa" VARCHAR(200), "DiaDescuento" BIGINT
);
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" (
  "IdCuentaBancaria" BIGINT, "NumeroCuentaBancaria" VARCHAR(30), "CodigoBanco" VARCHAR(10), "IdBanco" BIGINT
);
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" (
  "IdCliente" BIGINT, "DocumentoIdentidad" VARCHAR(30), "NombreCompleto" VARCHAR(200), "NombreEstado" VARCHAR(50)
);

-- ---------------------------------------------------------------------------------------------- ProcesarCargaMasiva / CargaMasivaIngresoEgresoDAPFFMM
CREATE TABLE "BSCL.Tesoreria.BD::CAR.TipoCargaMasiva" (
  "CodigoTipoCargaMasiva" VARCHAR(50) NOT NULL PRIMARY KEY, "GeneraPartida" VARCHAR(1)
);
CREATE TABLE "BSCL.Tesoreria.BD::CAR.CargaMasiva" (
  "IdCargaMasiva" BIGINT NOT NULL PRIMARY KEY, "IdEstado" VARCHAR(3), "FechaProceso" TIMESTAMP, "IdMoneda" VARCHAR(3), "IdConcepto" VARCHAR(10)
);
CREATE TABLE "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle" (
  "IdCargaMasivaDetalle" BIGINT NOT NULL PRIMARY KEY, "IdCargaMasiva" BIGINT, "IdTipoMovimiento" VARCHAR(6), "DocumentoIdentidad" VARCHAR(30), "Monto" DECIMAL(28,16),
  "IdPersona" BIGINT, "IdPortafolio" BIGINT, "Glosa" VARCHAR(500), "BCP_CON_FechaTransaccion" VARCHAR(30), "BCP_CON_NumeroOperacion" BIGINT, "IdEstado" VARCHAR(3),
  "IdMovimientoComprobante" BIGINT, "ObservacionValidacion" VARCHAR(500)
);
CREATE TABLE "BSCL.Tesoreria.BD::GBL.ProcesarCargaMasiva_Proceso" (
  "IdCargaMasiva" BIGINT, "CodigoTipoCargaMasiva" VARCHAR(50), "IdMoneda" VARCHAR(3), "IdConcepto" VARCHAR(10), "IdMedioDePago" VARCHAR(3), "IdCuentaBanco" BIGINT
);
CREATE TABLE "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" (
  "IdMedioDePago" VARCHAR(3), "IdModalidadDePago" VARCHAR(6)
);
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" (
  "IdPersona" BIGINT, "NombreCompleto" VARCHAR(200), "DocumentoIdentidad" VARCHAR(30), "TipoDocumentoIdentidad" VARCHAR(10)
);
CREATE TABLE "BSCL.Plataforma.BD::PLA.TipoMovimiento" (
  "IdModulo" VARCHAR(5), "IdTipoMovimiento" VARCHAR(6), "IdEstado" VARCHAR(3)
);
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Cabecera" START WITH 800;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::Detalle" START WITH 9000;
CREATE TABLE "BSCL.Plataforma.BD::FTP.Cabecera" (
  "IdCabecera" BIGINT NOT NULL PRIMARY KEY, "IdCodigoTipo" VARCHAR(100), "IdEstado" VARCHAR(3), "FechaCreacion" TIMESTAMP, "IdUsuario" BIGINT, "NombreArchivo" VARCHAR(200)
);
CREATE TABLE "BSCL.Plataforma.BD::FTP.Detalle" (
  "IdDetalle" BIGINT NOT NULL PRIMARY KEY, "IdCabecera" BIGINT, "Linea" VARCHAR(4000), "IdTipoEntidad" VARCHAR(30), "IdEntidad" BIGINT
);
CREATE TABLE "BSCL.Tesoreria.BD::GBL.CargaMasivaIngresoEgresoDAPFFMM_TablaPaso" (
  "IdTrabajo" BIGINT, "IdTipoDocumentoIdentidad" VARCHAR(10), "DocumentoIdentidad" VARCHAR(30), "CodigoPortafolio" VARCHAR(30)
);
CREATE TABLE "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptosManuales" (
  "IdConcepto" VARCHAR(10), "IdEstado" VARCHAR(3), "ConfiguradoEnModuloTESTra" VARCHAR(2), "ConfiguradoEnModuloCTATra" VARCHAR(2), "UsoTES" VARCHAR(10)
);
CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarConceptosTesoreria" (
  "IdConcepto" VARCHAR(10), "TipoConceptoTesoreria" VARCHAR(2), "NombreEntidad" VARCHAR(40), "IdEstado" VARCHAR(3), "DesplegarEnComboBox" VARCHAR(1),
  "DisponibleIngresoManual" VARCHAR(1), "VisibleOptimus" VARCHAR(1), "VisiblePorEjecutivo" VARCHAR(1)
);

-- ---------------------------------------------------------------------------------------------- ConsultarFormatoArchivo / ActualizarSaldosCuentaBanco(+Corredora) / CrearCargaMasivaPac
CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo" (
  "IdFormatoArchivo" BIGINT, "CodigoFormatoArchivo" VARCHAR(30), "SeparadorCampos" VARCHAR(5), "SeparadorDecimales" VARCHAR(5), "RellenarCampo" VARCHAR(5),
  "IdBanco" BIGINT, "NombreBanco" VARCHAR(100), "IdMoneda" VARCHAR(3), "IdMedioDePago" VARCHAR(3), "FormatoSalida" VARCHAR(10), "Servicio" VARCHAR(100),
  "Descripcion" VARCHAR(200), "RutaTesoreria" VARCHAR(200), "RutaCompleta" VARCHAR(300), "NombreArchivoSalida" VARCHAR(100), "CantidadDeTiposDeRegistro" BIGINT
);
CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivoDetalle" (
  "IdFormatoArchivo" BIGINT, "IdFormatoArchivoDetalle" BIGINT, "CodigoFormatoArchivo" VARCHAR(30), "TipoRegistro" INT, "Campo" VARCHAR(50), "TipoDato" VARCHAR(10),
  "Largo" INT, "Decimales" INT, "ValorDefecto" VARCHAR(50), "Orden" INT, "DescripcionFormatoDetalle" VARCHAR(200), "Formato" VARCHAR(50)
);
CREATE TABLE "BSCL.Tesoreria.BD::GBL.ActualizarSaldosCuentaBanco_Proceso_Paso" (
  "IdCuentaBanco" BIGINT
);
ALTER TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" ADD COLUMN "IdTipoMovimiento" VARCHAR(6) INVISIBLE;
ALTER TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" ADD COLUMN "Monto" DECIMAL(28,16) INVISIBLE;
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::IdCargaMasivaDetallePac" START WITH 100;
CREATE TABLE "BSCL.Tesoreria.BD::CAR.CargaMasivaDetallePAC" (
  "IdCargaMasivaDetallePac" BIGINT NOT NULL PRIMARY KEY, "IdCargaMasivaPac" BIGINT, "CodBanco" VARCHAR(10), "CodEmpresa" VARCHAR(10), "CodConvenio" VARCHAR(10),
  "TipoRegistro" VARCHAR(2), "IdentServicio" VARCHAR(30), "InfAdicional" VARCHAR(100), "MontoCargo" DECIMAL(28,16), "FechaVencimiento" VARCHAR(30), "FechaCargo" VARCHAR(30),
  "EstadoCargo" VARCHAR(10), "CodigoRechazo" VARCHAR(10), "Estado" VARCHAR(3), "TipoArchivo" VARCHAR(3), "FechaGeneracion" VARCHAR(30), "FechaInicioServ" VARCHAR(30)
);

-- ---------------------------------------------------------------------------------------------- AnularNominasTransferencias / QuitarDetalle / GestionDeDepositos
CREATE TABLE "BSCL.Tesoreria.BD::TES.NominaTransferencia" (
  "IdNominaTransferencia" BIGINT NOT NULL PRIMARY KEY, "IdComprobantePartidaEgreso" BIGINT, "IdEstado" VARCHAR(3), "Monto" DECIMAL(28,16)
);
CREATE TABLE "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" (
  "IdNominaTransferenciaDetalle" BIGINT NOT NULL PRIMARY KEY, "IdNominaTransferencia" BIGINT, "IdComprobanteFormaDePago" BIGINT, "IdEstado" VARCHAR(3)
);
CREATE TABLE "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" (
  "IdIngresoEgresoFormaDePago" BIGINT NOT NULL PRIMARY KEY, "IdIngresoEgreso" BIGINT, "IdEstado" VARCHAR(3)
);
CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" (
  "IdCargoAbonoMovimiento" BIGINT, "IdComprobante" BIGINT, "IdModulo" VARCHAR(3)
);
CREATE TABLE "BSCL.Tesoreria.BD::TES.Deposito" (
  "IdDeposito" BIGINT NOT NULL PRIMARY KEY, "IdEstado" VARCHAR(3), "Monto" DECIMAL(28,16), "IdConcepto" VARCHAR(10), "Descripcion" VARCHAR(300),
  "IdUsuarioLiquidador" BIGINT, "FechaHoraLiquidacion" TIMESTAMP
);
CREATE TABLE "BSCL.Tesoreria.BD::TES.DepositoDetalle" (
  "IdDepositoDetalle" BIGINT NOT NULL PRIMARY KEY, "IdDeposito" BIGINT, "IdComprobanteFormaDePago" BIGINT, "IdEstado" VARCHAR(3), "IdConcepto" VARCHAR(10), "Descripcion" VARCHAR(300)
);
CREATE TABLE "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" (
  "IdMedioDePago" VARCHAR(10), "IdTipoMovimiento" VARCHAR(10), "EstadosPosibleTipoMovimientoMedioDePago" VARCHAR(100), "AfectaSaldoEnEstado" VARCHAR(100)
);
CREATE TABLE "BSCL.Tesoreria.BD::GBL.GestionDeDepositos_Proceso" (
  "Id" BIGINT, "Entidad" VARCHAR(300), "IdConcepto" VARCHAR(10), "Descripcion" VARCHAR(300)
);

-- ---------------------------------------------------------------------------------------------- ProcesarDistribucion / CrearCargaMasivaPacRen
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::PAC.IdLog" START WITH 1;
CREATE TABLE "BSCL.Tesoreria.BD::PAC.LogMandatoPAC" (
  "IdLog" BIGINT, "IdentServicio" VARCHAR(30), "IdMandatoPAC" BIGINT, "FechaProceso" DATE, "FechaSistema" DATE, "Usuario" VARCHAR(20),
  "EstadoProcesamiento" VARCHAR(3), "PreRendicion" VARCHAR(1), "Glosa" VARCHAR(500)
);
CREATE TABLE "BSCL.Tesoreria.BD::PAC.EstadoCargoRendicion" ("CodigoCargo" VARCHAR(3), "Descripcion" VARCHAR(100));
CREATE TABLE "BSCL.SocioNegocio.BD::PER.CuentaBancaria" ("IdCuentaBancaria" BIGINT, "IdBanco" BIGINT, "NumeroCuentaBancaria" VARCHAR(30));
ALTER TABLE "BSCL.Plataforma.BD::BAN.Banco" ADD COLUMN "IdPersona" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" ADD COLUMN "IdPortafolio" BIGINT INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" ADD COLUMN "Canal" VARCHAR(10) INVISIBLE;
ALTER TABLE "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" ADD COLUMN "Fecha" DATE INVISIBLE;
ALTER TABLE "BSCL.APV.BD::SCS.ContratoSAAPV" ADD COLUMN "FechaSuscripcion" DATE INVISIBLE;
ALTER TABLE "BSCL.APV.BD::SCS.ContratoSAAPV" ADD COLUMN "Canal" VARCHAR(10) INVISIBLE;
ALTER TABLE "BSCL.Plataforma.BD::INS.Instrumento" ADD COLUMN "IdEmisor" VARCHAR(10) INVISIBLE;
ALTER TABLE "BSCL.Plataforma.BD::INS.Instrumento" ADD COLUMN "IdMoneda" VARCHAR(3) INVISIBLE;
CREATE TABLE "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotasDetalle" ("IdFolioCSC" BIGINT, "Serie" VARCHAR(20), "Regimen" VARCHAR(10), "Monto" DECIMAL(28,16));
CREATE TABLE "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" ("IdContratoSAAPV" BIGINT, "IdPortafolio" BIGINT, "IdEstado" VARCHAR(3), "MontoPesos" DECIMAL(28,16), "MontoUF" DECIMAL(28,16));
CREATE TABLE "BSCL.APV.BD::SCS.DistribucionInversion" ("IdContratoSAAPV" BIGINT, "Nemotecnico" VARCHAR(30), "MontoPesos" DECIMAL(28,16), "IdInstrumento" BIGINT);
CREATE TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" ("IdSerieFondo" VARCHAR(20), "IdMoneda" VARCHAR(3), "IdInstrumento" BIGINT);
CREATE TABLE "BSCL.APV.BD::APV.Estado" ("Codigo" VARCHAR(3), "Nombre" VARCHAR(100), "NombreEntidad" VARCHAR(30));
CREATE TABLE "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" ("IdPersona" BIGINT, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "IdEmpresa" BIGINT, "EsPortafolioDefecto" VARCHAR(1));
CREATE TABLE "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario" BIGINT, "TipoEntidad" VARCHAR(3), "IdEntidad" BIGINT);
CREATE TABLE "BSCL.APV.BD::APV.OrigenDestinoTransicion" ("Operacion" VARCHAR(3), "IdEmpresa" BIGINT, "IdEstado" VARCHAR(3), "DestinoCtaBco" BIGINT);
CREATE TABLE "BSCL.Tesoreria.BD::TES.Caja" ("IdCaja" BIGINT, "Codigo" VARCHAR(10), "Nombre" VARCHAR(50), "IdEstado" VARCHAR(3), "IdEmpresa" BIGINT);
ALTER TABLE "BSCL.Tesoreria.BD::TES.Comprobante" ADD COLUMN "IdTipoMovimiento" VARCHAR(6) INVISIBLE;
