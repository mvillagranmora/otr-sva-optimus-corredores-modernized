-- H2 (MODE=LEGACY) stand-in for the SAP HANA objects read/written by the reporting / aging / commission / patrimony
-- classes of BcsCuentaInversionServicios. Quoted names are the exact HANA catalog names so the production SQL
-- resources (cl/bcs/cuentaInversion/sql/*.sql) run through H2Dialect with no other change. HANA views are modelled
-- as plain tables (the tests seed the rows the view would return). Types are the narrowest H2 types that hold the legacy
-- POJO field types (BIGINT = Long, DECIMAL(34,16) = BigDecimal written with setScale(16) by SQL.batch).

-- ---------------------------------------------------------------- HANA scalar functions used by the SQL
CREATE ALIAS IF NOT EXISTS TO_DATE FOR "cl.bcs.cuentainversion.reportes.support.H2Functions.toDate";
CREATE ALIAS IF NOT EXISTS TO_NVARCHAR FOR "cl.bcs.cuentainversion.reportes.support.H2Functions.toNvarchar";
CREATE ALIAS IF NOT EXISTS CURRENT_UTCTIMESTAMP FOR "cl.bcs.cuentainversion.reportes.support.H2Functions.currentUtcTimestamp";
CREATE ALIAS IF NOT EXISTS CURRENT_UTCDATE FOR "cl.bcs.cuentainversion.reportes.support.H2Functions.currentUtcDate";

-- ---------------------------------------------------------------- platform
CREATE TABLE "BSCL.Plataforma.BD::PLA.Configuracion" (
  "IdEmpresa" BIGINT NOT NULL, "CodigoParametro" VARCHAR(100) NOT NULL, "Valor" VARCHAR(500));
CREATE TABLE "BSCL.Plataforma.BD::PLA.Diccionario" (
  "CodLenguaje" VARCHAR(10), "CodDiccionario" VARCHAR(100), "Descripcion" VARCHAR(1000));
CREATE TABLE "BSCL.Plataforma.BD::REG.DiaFeriado" (
  "IdFeriado" BIGINT, "Tipo" VARCHAR(10), "Fecha" DATE, "Dia" INTEGER, "DescripcionDia" VARCHAR(50),
  "AgnoCarga" INTEGER, "IdPais" BIGINT, "IdEstado" VARCHAR(3));
CREATE TABLE "BSCL.Plataforma.BD::PLA.MonedaFormato" (
  "IdMonedaFormato" BIGINT, "IdMoneda" VARCHAR(3), "IdUsoMonedaFormato" VARCHAR(3), "IdTipoInstrumento" VARCHAR(10),
  "IdTipoOperacion" VARCHAR(10), "PrecioDecimales" INTEGER, "PrecioTrucaRedondea" VARCHAR(1), "TasaDecimales" INTEGER,
  "TasaTrucaRedondea" VARCHAR(1), "CantidadDecimales" INTEGER, "CantidadTrucaRedondea" VARCHAR(1), "MontoDecimales" INTEGER,
  "MontoTrucaRedondea" VARCHAR(1), "TipoCambioDecimales" INTEGER, "TipoCambioTrucaRedondea" VARCHAR(1), "ParidadDecimales" INTEGER,
  "ParidadTrucaRedondea" VARCHAR(1), "PuntosDecimales" INTEGER, "PuntosTrucaRedondea" VARCHAR(1), "IdEstado" VARCHAR(3));
CREATE TABLE "BSCL.Plataforma.BD::PLA.Archivo" ("IdArchivo" BIGINT, "CodigoArchivo" VARCHAR(50), "IdEmpresa" BIGINT);
CREATE TABLE "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivo" BIGINT, "IdFormato" BIGINT);
CREATE TABLE "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato" BIGINT, "CodigoCampo" VARCHAR(50), "Posicion" INTEGER);
CREATE TABLE "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" ("IdEmpresa" BIGINT, "IdBolsa" VARCHAR(10), "CodigoCorredora" VARCHAR(10));
CREATE TABLE "BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero" (
  "IdIndiceFinanciero" VARCHAR(20) NOT NULL, "Periodo" VARCHAR(10), "Nombre" VARCHAR(100), "Descripcion" VARCHAR(200),
  "IdMonedaParametro" VARCHAR(3), "IdMonedaValor" VARCHAR(3),
  -- ASSUMPTION flagged for SME: ProcesaIndicesAtributtionAnalisys.java:104-113 turns an INSERT failure into UI_PROCESO_EJECUTADO_ERROR
  -- (UI_PROCESO_EJECUTADO_ERROR_INDENTIFICAR_INDICE), which only happens if HANA has a primary key on IdIndiceFinanciero.
  PRIMARY KEY ("IdIndiceFinanciero"));
CREATE TABLE "BSCL.Plataforma.BD::MER.IndiceFinanciero" ("IdIndiceFinanciero" VARCHAR(20), "Fecha" DATE, "Valor" DECIMAL(34,16));
CREATE TABLE "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" ("IdParametroFinanciero" VARCHAR(20));
CREATE TABLE "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" (
  "IdArbolClasificacionActivo" BIGINT, "Padre" BIGINT, "Descripcion" VARCHAR(100), "Indice" VARCHAR(20));
CREATE TABLE "BSCL.Plataforma.BD::COM.Cobro" ("IdCobro" VARCHAR(20), "ValorCobro" DECIMAL(34,16));
CREATE TABLE "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado" VARCHAR(3), "Nombre" VARCHAR(100));
CREATE TABLE "BSCL.Plataforma.BD::PLA.ConfiguracionPatrimonioCaja" (
  "CodigoItem" VARCHAR(50), "FuncionOperador" VARCHAR(5), "IdEstado" VARCHAR(3));
CREATE TABLE "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" ("IdCondicionDeLiquidacion" VARCHAR(10));
CREATE TABLE "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" (
  "IdPortafolio" BIGINT, "CodigoTipoIndicador" VARCHAR(50), "ValorIndicador" VARCHAR(50));
CREATE TABLE "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" (
  "IdPersona" BIGINT, "IdTipoEntidad" VARCHAR(20), "CodigoTipoIndicador" VARCHAR(50), "ValorIndicador" VARCHAR(50));
CREATE TABLE "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" (
  "IdCliente" BIGINT, "IdTipoEntidad" VARCHAR(20), "CodigoTipoIndicador" VARCHAR(50), "ValorIndicador" VARCHAR(50));
CREATE TABLE "BSCL.Plataforma.BD::IND.Indicador" (
  "IdIndicador" BIGINT, "IdEntidad" BIGINT, "IdTipoEntidad" VARCHAR(20), "IdTipoIndicador" BIGINT, "ValorIndicador" VARCHAR(50));
CREATE TABLE "BSCL.Plataforma.BD::IND.TipoIndicador" (
  "IdTipoIndicador" BIGINT, "IdTipoEntidad" VARCHAR(20), "CodigoTipoIndicador" VARCHAR(50));
CREATE TABLE "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" (
  "IdInstrumento" BIGINT, "IdClasificacionTipoInstrumento" VARCHAR(10), "CobroComisionesCustodia" VARCHAR(1));

-- ---------------------------------------------------------------- socio de negocio / portafolio / apv
CREATE TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" (
  "IdCliente" BIGINT, "IdEmpresa" BIGINT, "IdPersona" BIGINT, "IdPersonaEjecutivo" BIGINT);
CREATE TABLE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" ("IdPersona" BIGINT, "DocumentoIdentidad" VARCHAR(30));
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" (
  "IdPersona" BIGINT, "Tipo" VARCHAR(1), "NombreCompleto" VARCHAR(200), "RazonSocial" VARCHAR(200),
  "DocumentoIdentidad" VARCHAR(30));
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" (
  "IdCliente" BIGINT, "IdPersona" BIGINT, "DocumentoIdentidad" VARCHAR(30), "NombreCompleto" VARCHAR(200));
CREATE TABLE "BSCL.Portafolio.BD::POR.Portafolio" (
  "IdPortafolio" BIGINT, "IdCliente" BIGINT, "CodigoPortafolio" VARCHAR(50), "Proposito" VARCHAR(100),
  "IdMatrizAgrupadorAPV" BIGINT);
CREATE TABLE "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" (
  "IdPortafolio" BIGINT, "CodigoPortafolio" VARCHAR(50), "Proposito" VARCHAR(100), "TienePropositoAPV" VARCHAR(1));
CREATE TABLE "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupadorDetalle" (
  "IdPortafolio" BIGINT, "IdProductoAgrupador" BIGINT);
CREATE TABLE "BSCL.APV.BD.Vistas::ConsultarOtroProductoAgrupador" (
  "IdProductoAgrupador" BIGINT, "NombreProductoAgrupador" VARCHAR(100));
CREATE TABLE "BSCL.Facturacion.BD::FAC.Comprobante" (
  "IdComprobante" BIGINT, "IdClasificacionTipoInstrumento" VARCHAR(10), "IdCondicionLiquidacion" VARCHAR(10));

-- ---------------------------------------------------------------- cuenta inversion
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion" (
  "IdEmpresa" BIGINT NOT NULL, "CodigoParametro" VARCHAR(100) NOT NULL, "Valor" VARCHAR(500));
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CuentaInversion" (
  "IdCuentaInversion" BIGINT, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "IdMoneda" VARCHAR(3), "IdEstado" VARCHAR(3));
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" (
  "IdCargoAbonoMovimiento" BIGINT, "IdCuentaInversion" BIGINT, "IdEmpresa" BIGINT, "IdTipoMovimiento" VARCHAR(10),
  "IdEstado" VARCHAR(3), "IdModulo" VARCHAR(3), "IdComprobante" BIGINT, "IdConcepto" VARCHAR(40),
  "Monto" DECIMAL(34,16), "FechaSistema" DATE, "FechaEfectivaMovimiento" DATE, "FechaCertificacion" TIMESTAMP);
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad" (
  "FechaSistema" DATE, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "IdMoneda" VARCHAR(3), "TipoSaldo" VARCHAR(1),
  "Saldo" DECIMAL(34,16));
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" ("IdConcepto" VARCHAR(40));
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto" (
  "IdExclusionConcepto" BIGINT, "IdConcepto" VARCHAR(40), "FechaVigenciaDesde" DATE);
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" (
  "IdOpcionCobro" BIGINT, "OpcionCobro" BIGINT, "PortafolioAPV" VARCHAR(1), "IdClasificacionTipoInstrumento" VARCHAR(10),
  "PorcentajeCobro" DECIMAL(34,16), "IdEstado" VARCHAR(3), "FechaVigencia" DATE, "FechaIngreso" VARCHAR(30),
  "FechaModificacion" TIMESTAMP,
  -- ASSUMPTION flagged for SME: legacy EditarCobroComisionOpcionesCobro.java:109-117 catches an UPDATE failure and reports
  -- UI_ADVERTENCIA_OPCION_DUPLICADA, which only happens if HANA enforces uniqueness of (OpcionCobro, PortafolioAPV, Mercado).
  CONSTRAINT "UQ_OpcionCobro" UNIQUE ("OpcionCobro", "PortafolioAPV", "IdClasificacionTipoInstrumento"));
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.RegistrosEditadosComisiones" (
  "IdRegistro" BIGINT, "IdPortafolio" BIGINT, "Periodo" BIGINT, "MontoComision" DECIMAL(34,16),
  "FechaModificacion" VARCHAR(30), "EstadoComision" VARCHAR(3));
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.PeriodosComisiones" (
  "IdPeriodo" BIGINT, "FechaDesde" DATE, "FechaHasta" DATE);
CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultarPeriodosComisiones" (
  "IdPeriodo" BIGINT, "FechaDesde" DATE, "FechaHasta" DATE);
CREATE TABLE "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" (
  "IdPatrimonioCaja" BIGINT, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "FechaPatrimonio" DATE,
  "TipoFechaConsulta" VARCHAR(3), "IdMoneda" VARCHAR(3), "IdMonedaSistema" VARCHAR(3),
  "MontoPatrimonio" DECIMAL(34,16), "SaldoDisponible" DECIMAL(34,16), "SaldoEnGarantia" DECIMAL(34,16),
  "SaldoRetiro" DECIMAL(34,16), "SaldoAPlazoSimultanea" DECIMAL(34,16), "SaldoAPlazoVentaCorta" DECIMAL(34,16),
  "SaldoAPlazoCompromiso" DECIMAL(34,16), "IdEstado" VARCHAR(3), "MontoPatrimonioAfectoComision" DECIMAL(34,16),
  "MontoComision" DECIMAL(34,16), "PorcentajeCobroComision" DECIMAL(34,16), "IdEstadoComision" VARCHAR(3));
CREATE TABLE "BSCL.Custodia.BD::CUS.PatrimonioCustodia" (
  "IdPatrimonioCustodia" BIGINT, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "IdInstrumento" BIGINT,
  "FechaPatrimonio" DATE, "TipoFechaConsulta" VARCHAR(3), "MontoComision" DECIMAL(34,16),
  "IdEstadoComision" VARCHAR(3), "MontoPatrimonio" DECIMAL(34,16), "Nemotecnico" VARCHAR(30),
  "PorcentajeCobroComision" DECIMAL(34,16), "CantidadCustodia" DECIMAL(34,16), "CantidadGarantia" DECIMAL(34,16),
  "PrecioMercadoCustodia" DECIMAL(34,16));

-- staging ("tablas de paso") the legacy classes fill and clean
CREATE TABLE "BSCL.CuentaInversion.BD::GBL.ConsultarAntiguedadDeudasCuentaInversion_Cuenta_Paso" (
  "IdTrabajo" BIGINT, "IdCuentaInversion" BIGINT, "IdPortafolio" BIGINT, "IdMoneda" VARCHAR(3),
  "SaldoFinal" DECIMAL(34,16), "IdCliente" BIGINT);
CREATE TABLE "BSCL.CuentaInversion.BD::GBL.ConsultaReporteAntiguedadBolsa_Cuenta_Paso" (
  "IdTrabajo" BIGINT, "IdCuentaInversion" BIGINT, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "IdMoneda" VARCHAR(3),
  "SaldoFinal" DECIMAL(34,16));
CREATE TABLE "BSCL.CuentaInversion.BD::GBL.ConsultaReporteAcreedores_Cuenta_Paso" (
  "IdTrabajo" BIGINT, "IdCuentaInversion" BIGINT, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "IdMoneda" VARCHAR(3),
  "SaldoFinal" DECIMAL(34,16));

-- sequences (HANA "name".NEXTVAL; MODE=LEGACY accepts the same syntax)
CREATE SEQUENCE "BSCL.CuentaInversion.BD.Secuencias::CobroComisionOpcionesCobro" START WITH 7000;
CREATE SEQUENCE "BSCL.CuentaInversion.BD.Secuencias::PatrimonioCaja" START WITH 9000;
CREATE SEQUENCE "BSCL.CuentaInversion.BD.Secuencias::RegistrosEditadosComisiones" START WITH 5000;
CREATE SEQUENCE "BSCL.CuentaInversion.BD.Secuencias::CobroComisionExclusionConcepto" START WITH 6000;

-- ================================================================ family B, second batch (ClientesPorPagarYCobrar, Anexo16, InformeAtributtionAnalisys)
-- (the extra columns of CTA.CargoAbonoMovimiento are added by seed/cargoabono-ext.sql, listed first in the cases that need them,
--  so that the positional INSERTs of the earlier cases keep working)
CREATE ALIAS IF NOT EXISTS TO_VARCHAR FOR "cl.bcs.cuentainversion.reportes.support.H2Functions.toVarchar";

CREATE TABLE "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClienteAnalitica" (
  "IdCliente" BIGINT, "CodigoTipoIndicador" VARCHAR(30), "ValorIndicador" VARCHAR(10));
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" ("IdPersona" BIGINT, "NombreCompleto" VARCHAR(200));
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" (
  "IdEmpleado" BIGINT, "IdPersona" BIGINT, "NombreCompleto" VARCHAR(200), "DocumentoIdentidad" VARCHAR(30));
CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" ("IdCliente" BIGINT, "IdPersona" BIGINT);
CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultarRelacionCorredora" ("IdPersonaDestino" BIGINT);
CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" (
  "IdCuentaInversion" BIGINT, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "IdMoneda" VARCHAR(3));
CREATE TABLE "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto" VARCHAR(40));
CREATE TABLE "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen" ("NombreImagen" VARCHAR(100), "Version" INTEGER, "Binario" VARBINARY(100000));

CREATE TABLE "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" (
  "IdTrabajo" BIGINT, "IdEjecutivo" BIGINT, "IdPortafolio" BIGINT, "IdMoneda" VARCHAR(3));
CREATE TABLE "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Delete" ("IdTrabajo" BIGINT);
CREATE TABLE "BSCL.CuentaInversion.BD::GBL.GenerarArchivoCuentasPorCobrarAnexo16_Paso" ("IdTrabajo" BIGINT, "IdCuentaInversion" BIGINT);

CREATE TABLE "BSCL.Custodia.BD::CUS.Cartera" (
  "IdEmpresa" BIGINT, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "Nemotecnico" VARCHAR(40), "NemoSVS" VARCHAR(40),
  "IdMonedaInstrumento" VARCHAR(3), "IdTipoReajuste" VARCHAR(3), "INGCantidad" DECIMAL(34,16), "IdEstado" VARCHAR(3),
  "IdClasificacionTipoInstrumento" VARCHAR(10), "IdTipoOperacion" VARCHAR(10), "INGMontoAPlazo" DECIMAL(34,16),
  "FechaLiquidacion" DATE, "FechaVencimientoInstrumento" DATE,
  "FechaOperacion" DATE, "INGFechaOperacion" DATE, "FechaIngresoRegistro" TIMESTAMP, "INGIdTipoOperacion" VARCHAR(10),
  "EGRIdTipoOperacion" VARCHAR(10), "FechaVencimientoOperacion" DATE, "INGMontoATasaMercadoActual" DECIMAL(34,16),
  "INGMontoMercadoActualMonedaInstrumento" DECIMAL(34,16), "IdInstrumento" BIGINT, "IdTipoInstrumento" VARCHAR(10),
  "INGPrecioCompra" DECIMAL(34,16), "EGRPrecio" DECIMAL(34,16), "EGRMonto" DECIMAL(34,16), "EGRFechaLiquidacion" DATE,
  "MontoCompraMonedaInstrumento" DECIMAL(34,16));
CREATE TABLE "BSCL.Custodia.BD::GTA.Garantia" (
  "IdEmpresa" BIGINT, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "IdMonedaInstrumento" VARCHAR(3), "INGCantidad" DECIMAL(34,16),
  "IdEstado" VARCHAR(3), "IdOrigenGarantia" VARCHAR(10), "FechaOperacion" DATE);
CREATE TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" ("IdFondo" BIGINT, "MoneyMarket" VARCHAR(1));
CREATE TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ("IdSerieFondo" VARCHAR(40), "IdFondo" BIGINT, "IdInstrumento" BIGINT);
CREATE TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuota" (
  "IdEmpresa" BIGINT, "IdSerieFondo" VARCHAR(40), "ValorCuota" DECIMAL(34,16), "FechaCierre" DATE);
CREATE TABLE "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistorico" (
  "IdEmpresa" BIGINT, "IdSerieFondo" VARCHAR(40), "ValorCuota" DECIMAL(34,16), "FechaVigencia" DATE, "FechaIngresoRegistro" TIMESTAMP);

CREATE TABLE "BSCL.Plataforma.BD::INS.TipoOperacion" (
  "IdTipoOperacion" VARCHAR(10), "Nombre" VARCHAR(60), "Accion" VARCHAR(10), "AccionOrden" VARCHAR(10),
  "EsSimultanea" VARCHAR(1), "EsVentaCorta" VARCHAR(1), "EsAnticipo" VARCHAR(1), "EsVencimiento" VARCHAR(1));
CREATE TABLE "BSCL.Plataforma.BD::INS.Instrumento" ("IdInstrumento" BIGINT, "Nemotecnico" VARCHAR(40));
CREATE TABLE "BSCL.Normativa.BD.Vistas::CAR.ConsultarClasificacionActivos" (
  "IdInstrumento" BIGINT, "Mercado" VARCHAR(60), "SubMercado" VARCHAR(60));
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TAS.IdRegistroEntregarPreciosTasas" START WITH 1000 INCREMENT BY 1;
