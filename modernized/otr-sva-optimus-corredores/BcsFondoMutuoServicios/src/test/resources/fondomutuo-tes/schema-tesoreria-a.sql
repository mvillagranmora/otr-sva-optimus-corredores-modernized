-- H2 (MODE=LEGACY) stand-in for the SAP HANA objects read/written by the Tesoreria "family A" legacy services
-- (CrearIngresoEgreso, LiquidarComprobante, CrearComprobante*, AnularRegistros, ...). Quoted names are the exact HANA catalog names so the
-- production SQL resources (cl/bcs/tesoreria/sql/*.sql) run unmodified (through support/H2Hana.java). HANA views are modelled as tables:
-- the seeds insert the rows the view would return. The HANA DDL of these tables is NOT in the repository: the column list of each table
-- was derived from the columns the legacy SQL reads/writes, types from the legacy POJO field types (Long -> BIGINT, BigDecimal ->
-- DECIMAL(28,16) so nothing is rounded by the stand-in itself, Date/Timestamp -> DATE/TIMESTAMP, anything else VARCHAR). Assumption, not legacy fact.
-- No primary keys or constraints are modelled (the legacy relies on the HANA ones only where a case says so).
--
-- This file is executed by BOTH the legacy oracle runner and the modern tests.

CREATE TABLE DUMMY (X VARCHAR(1));
INSERT INTO DUMMY VALUES ('X');

CREATE ALIAS TO_NVARCHAR FOR "cl.bcs.fondomutuo.tesoreria.H2Hana.toNvarchar";
CREATE ALIAS TO_VARCHAR FOR "cl.bcs.fondomutuo.tesoreria.H2Hana.toVarchar";
CREATE ALIAS TO_BIGINT FOR "cl.bcs.fondomutuo.tesoreria.H2Hana.toBigint";
CREATE ALIAS TO_DECIMAL FOR "cl.bcs.fondomutuo.tesoreria.H2Hana.toDecimal";
CREATE ALIAS TO_DATE FOR "cl.bcs.fondomutuo.tesoreria.H2Hana.toDate";
CREATE ALIAS TO_TIMESTAMP FOR "cl.bcs.fondomutuo.tesoreria.H2Hana.toTimestamp";
CREATE ALIAS ADD_SECONDS FOR "cl.bcs.fondomutuo.tesoreria.H2Hana.addSeconds";
CREATE ALIAS HANA_UTCNOW FOR "cl.bcs.fondomutuo.tesoreria.H2Hana.utcNow";
CREATE ALIAS HANA_NEXTVAL FOR "cl.bcs.fondomutuo.tesoreria.H2Hana.nextVal";
CREATE ALIAS ADD_DAYS FOR "cl.bcs.fondomutuo.tesoreria.H2Hana.addDays";
CREATE ALIAS "BSCL.Plataforma.BD.Utilitarios::FormatearNumero" FOR "cl.bcs.fondomutuo.tesoreria.H2Hana.formatearNumero";

-- sequences read directly by the SQL (fixed start values: ids of rows created by a case are part of the goldens)
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::Comprobante" START WITH 7000;
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::ComprobanteDetalle" START WITH 7100;
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::ComprobanteDetalleIngresoEgreso" START WITH 7200;
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::ComprobanteFormaDePago" START WITH 7300;
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::NominaTransferencia" START WITH 7400;
CREATE SEQUENCE "BSCL.Tesoreria.BD.Secuencias::NominaTransferenciaDetalle" START WITH 7500;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::SecuenciaMovimiento" START WITH 7600;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::AUTSecuenciasGenerales" START WITH 7700;

-- HANA system view read by ModificarComprobanteTesoreria ('does the temp table exist?'): empty stand-in (the drop-if-exists branch is unreachable here)
CREATE TABLE M_TEMPORARY_TABLES (TABLE_NAME VARCHAR(200));

CREATE TABLE "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" (
  "IdCargoAbonoMovimiento" BIGINT,
  "IdComprobante" BIGINT,
  "IdModulo" VARCHAR(500)
);

CREATE TABLE "BSCL.Custodia.BD.Vistas::CUS.ConsultarOperacionesPendientes" (
  "Monto" DECIMAL(28,16),
  "IdTipoOperacion" VARCHAR(500),
  "FolioOperacion" VARCHAR(100),
  "IdCliente" BIGINT
);

CREATE TABLE "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" (
  "FolioComprobante" BIGINT,
  "Cantidad" DECIMAL(28,16),
  "IdMovimientoCarteraEnTransito" BIGINT,
  "IdTipoOperacion" VARCHAR(500)
);

CREATE TABLE "BSCL.Custodia.BD::CUS.OperacionesPendientes" (
  "FolioOperacion" VARCHAR(100),
  "Clave" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(500)
);

CREATE TABLE "BSCL.Facturacion.BD::FAC.Comprobante" (
  "IdCondicionLiquidacion" VARCHAR(500),
  "IdComprobante" BIGINT,
  "IdTipoOperacion" VARCHAR(500),
  "FolioComprobante" BIGINT,
  "MontoLiquidacionFinal" DECIMAL(28,16),
  "Cantidad" DECIMAL(28,16)
);

CREATE TABLE "BSCL.Orden.BD::ORD.Transaccion" (
  "IdTransaccion" BIGINT,
  "Folio" VARCHAR(100),
  "Nemotecnico" VARCHAR(500),
  "Cantidad" DECIMAL(28,16),
  "Precio" DECIMAL(28,16),
  "Monto" DECIMAL(28,16),
  "FechaSistemaCreacion" DATE,
  "FechaLiquidacion" DATE,
  "IdCamara" VARCHAR(500),
  "EsBilateral" VARCHAR(500),
  "IdEmpresa" BIGINT
);

CREATE TABLE "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" (
  "IdPortafolio" BIGINT,
  "CodigoTipoIndicador" VARCHAR(500),
  "ValorIndicador" VARCHAR(100),
  "CodigoValorIndicador" VARCHAR(500),
  "IdIndicador" BIGINT
);

CREATE TABLE "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" (
  "IdPersona" BIGINT,
  "Nombre" VARCHAR(200)
);

CREATE TABLE "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" (
  "IdConcepto" VARCHAR(500),
  "Nombre" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD.Vistas::REQ.ConsultarRequisitoOperativo" (
  "IdCondicionOperativa" VARCHAR(500),
  "IdTipoEtapa" VARCHAR(500),
  "IdTipoCanalIngreso" VARCHAR(500),
  "IdTipoComportamiento" VARCHAR(500),
  "IdEstado" VARCHAR(500),
  "IdEmpresa" BIGINT
);

CREATE TABLE "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento" (
  "IdAutorizadoEtapaEvento" BIGINT,
  "IdEtapaEvento" BIGINT,
  "IdUsuario" BIGINT,
  "IdRol" BIGINT,
  "Observaciones" VARCHAR(500),
  "FechaHoraCambioEstado" TIMESTAMP,
  "IdEstado" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::AUT.EtapaEvento" (
  "IdEtapaEvento" BIGINT,
  "IdEvento" BIGINT,
  "IdEtapa" BIGINT,
  "Codigo" VARCHAR(100),
  "Nombre" VARCHAR(500),
  "Orden" INT,
  "TipoAutorizacion" VARCHAR(500),
  "MinimoAutorizan" INT,
  "IdUsuarioCambioEstado" BIGINT,
  "IdRolCambioEstado" BIGINT,
  "FechaHoraCambioEstado" TIMESTAMP,
  "IdEstado" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::AUT.Evento" (
  "IdEstado" VARCHAR(500),
  "IdEvento" BIGINT,
  "NumeroOperacionConcepto" VARCHAR(500),
  "IdTipoEvento" VARCHAR(500),
  "IdUsuarioCambioEstado" BIGINT,
  "FechaHoraCambioEstado" TIMESTAMP
);

CREATE TABLE "BSCL.Plataforma.BD::AUT.EventoDetalle" (
  "IdEvento" BIGINT,
  "NombreCampo" VARCHAR(500),
  "Valor" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::BAN.Banco" (
  "IdBanco" BIGINT,
  "IdPersona" BIGINT
);

CREATE TABLE "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" (
  "NombreTipoCuentaBancaria" VARCHAR(500),
  "IdTipoCuentaBancaria" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::CTT.ContratoDatosGenerales" (
  "IdEmpresa" BIGINT,
  "Telefono" VARCHAR(100),
  "NombreCorredora" VARCHAR(200)
);

CREATE TABLE "BSCL.Plataforma.BD::IND.Indicador" (
  "IdEntidad" BIGINT,
  "ValorIndicador" VARCHAR(100),
  "IdTipoIndicador" BIGINT,
  "IdTipoEntidad" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::IND.TipoIndicador" (
  "IdTipoIndicador" BIGINT,
  "IdTipoEntidad" VARCHAR(500),
  "CodigoTipoIndicador" VARCHAR(500),
  "IdEstado" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::INS.Corredora" (
  "NombreCorto" VARCHAR(500),
  "IdCorredora" BIGINT,
  "IdPersona" BIGINT
);

CREATE TABLE "BSCL.Plataforma.BD::INS.CorredoraCodigos" (
  "CodigoCorredora" VARCHAR(500),
  "IdBolsa" VARCHAR(500),
  "IdCorredora" BIGINT
);

CREATE TABLE "BSCL.Plataforma.BD::INS.Instrumento" (
  "IdInstrumento" BIGINT,
  "Nemotecnico" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::INS.TipoOperacion" (
  "IdTipoOperacion" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::INS.TipoProducto" (
  "IdTipoProducto" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::PAG.MedioDePago" (
  "IdMedioDePago" VARCHAR(500),
  "Nombre" VARCHAR(500),
  "TipoMedioDePago" VARCHAR(500),
  "NombreMedioDePago" VARCHAR(500),
  "NombreModalidadDePago" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" (
  "IdModalidadDePago" VARCHAR(500),
  "IdMedioDePago" VARCHAR(500),
  "IdEstado" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::PAG.ModalidadDePago" (
  "Nombre" VARCHAR(500),
  "IdModalidadDePago" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.Concepto" (
  "Nombre" VARCHAR(500),
  "IdConcepto" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" (
  "IdCondicionDeLiquidacion" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.Configuracion" (
  "Valor" VARCHAR(500),
  "CodigoParametro" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.Empresa" (
  "Nombre" VARCHAR(500),
  "IdEmpresa" BIGINT,
  "IdSocioDeNegocio" BIGINT
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.Moneda" (
  "IdMoneda" VARCHAR(500),
  "IdEstado" VARCHAR(500),
  "Nombre" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.MonedaFormato" (
  "MontoDecimales" INT,
  "MontoTrucaRedondea" VARCHAR(1),
  "IdMoneda" VARCHAR(500),
  "IdUsoMonedaFormato" VARCHAR(500),
  "IdTipoInstrumento" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" (
  "IdConcepto" VARCHAR(500),
  "IdEmpresa" BIGINT,
  "RequiereAutorizacion" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::PLA.TipoMovimiento" (
  "IdTipoMovimiento" VARCHAR(500),
  "Nombre" VARCHAR(500)
);

CREATE TABLE "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" (
  "IdPortafolio" BIGINT
);

CREATE TABLE "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasTesoreria" (
  "IdPortafolio" BIGINT,
  "IdCliente" BIGINT,
  "IdTipoCliente" VARCHAR(500),
  "TES_NIE" VARCHAR(500)
);

CREATE TABLE "BSCL.Portafolio.BD::POR.Portafolio" (
  "IdEstado" VARCHAR(500),
  "CodigoPortafolio" VARCHAR(100),
  "IdPortafolio" BIGINT,
  "IdCliente" BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" (
  "IdPortafolioDefecto" BIGINT,
  "IdCliente" BIGINT,
  "NombreCompleto" VARCHAR(500),
  "Tipo" VARCHAR(500),
  "DocumentoIdentidad" VARCHAR(500)
);

CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" (
  "NombreCompleto" VARCHAR(500),
  "DocumentoIdentidad" VARCHAR(500),
  "IdPersona" BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaEstado" (
  "IdEstado" VARCHAR(500),
  "IdPersona" BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" (
  "NombreCompleto" VARCHAR(500),
  "IdPersona" BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" (
  "IdTipoTarjetaBancaria" VARCHAR(500),
  "NombreTipoTarjetaBancaria" VARCHAR(500),
  "NumeroTarjetaBancaria" VARCHAR(500),
  "IdTarjetaBancaria" BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" (
  "IdSocioDeNegocio" BIGINT,
  "IdCliente" BIGINT,
  "IdPersona" BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" (
  "NombreCompleto" VARCHAR(500),
  "DocumentoIdentidadDefecto" VARCHAR(500),
  "CodigoTipoPersona" VARCHAR(500),
  "IdPersona" BIGINT,
  "IdEmpresa" BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD::CLI.Cliente" (
  "IdPersona" BIGINT,
  "IdCliente" BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD::EJE.Ejecutivo" (
  "IdEmpleadoEjecutivo" BIGINT,
  "IdEstado" VARCHAR(500)
);

CREATE TABLE "BSCL.SocioNegocio.BD::EMP.Empleado" (
  "CodigoEmpleado" VARCHAR(500),
  "IdEmpleado" BIGINT,
  "IdEstado" VARCHAR(500)
);

CREATE TABLE "BSCL.SocioNegocio.BD::PER.CuentaBancaria" (
  "IdCuentaBancaria" BIGINT,
  "IdTipoCuentaBancaria" VARCHAR(500),
  "NumeroCuentaBancaria" VARCHAR(500),
  "IdBanco" BIGINT,
  "IdPersona" BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD::PER.DireccionElectronica" (
  "Direccion" VARCHAR(500),
  "IdDireccionElectronica" BIGINT,
  "Predeterminado" VARCHAR(1)
);

CREATE TABLE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" (
  "IdTipoDocumentoIdentidad" VARCHAR(500),
  "DocumentoIdentidad" VARCHAR(500),
  "IdDocumentoIdentidad" BIGINT,
  "IdPersona" BIGINT,
  "Borrado" VARCHAR(500)
);

CREATE TABLE "BSCL.SocioNegocio.BD::PER.Persona" (
  "IdPersona" BIGINT,
  "IdDireccionElectronicaPrincipal" BIGINT,
  "IdDocumentoIdentidad" BIGINT,
  "IdTelefonoPrincipal" VARCHAR(500),
  "Tipo" VARCHAR(500)
);

CREATE TABLE "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" (
  "IdTarjetaBancaria" BIGINT
);

CREATE TABLE "BSCL.SocioNegocio.BD::PER.Telefono" (
  "Numero" VARCHAR(500),
  "IdPersona" BIGINT,
  "IdTelefono" VARCHAR(500),
  "Borrado" VARCHAR(500)
);

CREATE TABLE "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" (
  "IdPersona" BIGINT,
  "IdCliente" BIGINT,
  "IdSocioDeNegocio" BIGINT,
  "IdEmpresa" BIGINT,
  "Borrado" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarCajaMonedaUsuario" (
  "IdCaja" BIGINT,
  "IdMoneda" VARCHAR(3),
  "IdTipoMovimiento" VARCHAR(10),
  "IdUsuario" BIGINT,
  "IdEmpresa" BIGINT,
  "MaxIdCaja" BIGINT,
  "IdEstadoCaja" VARCHAR(3),
  "IdEstadoCajaMoneda" VARCHAR(3),
  "IdEstadoCajaUsuario" VARCHAR(3),
  "FechaInicio" DATE,
  "FechaTermino" DATE
);

CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" (
  "IdComprobante" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "IdPersona" BIGINT,
  "IdPortafolioTitularPago" BIGINT,
  "IdCaja" BIGINT,
  "IdMoneda" VARCHAR(500),
  "IdEstado" VARCHAR(500),
  "IdConcepto" VARCHAR(500),
  "FechaLiquidacion" DATE,
  "IdTipoPersona" VARCHAR(500),
  "IdMedioDePago" VARCHAR(500),
  "NombreBancoSocio" VARCHAR(500),
  "NombreBancoCorredora" VARCHAR(500),
  "AliasBanco" VARCHAR(500),
  "NumeroCuentaBancariaCorredora" VARCHAR(500),
  "IdClasificacionTipoInstrumento" VARCHAR(500),
  "IdTipoInstrumento" VARCHAR(500),
  "FechaComprobanteOrigen" TIMESTAMP,
  "NombreCaja" VARCHAR(500),
  "NumeroCuentaBancariaSocio" VARCHAR(500),
  "CuentaDCVOrigen" VARCHAR(500),
  "CuentaDCVDestino" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "NombrePersona" VARCHAR(500),
  "DocumentoIdentidad" VARCHAR(500),
  "NombreConcepto" VARCHAR(500),
  "NombreEstado" VARCHAR(500),
  "Nemotecnico" VARCHAR(500),
  "IdEmpresa" BIGINT,
  "NombreEmpresa" VARCHAR(500),
  "IdUsuario" BIGINT,
  "TipoComprobante" VARCHAR(500),
  "NombreTipoMovimiento" VARCHAR(500),
  "CodigoCaja" VARCHAR(500),
  "NombreMoneda" VARCHAR(500),
  "MontoEgreso" DECIMAL(28,16),
  "MontoIngreso" DECIMAL(28,16),
  "MontoConSigno" DECIMAL(28,16),
  "FechaIngresoRegistro" TIMESTAMP,
  "FechaMovimiento" TIMESTAMP,
  "NombreTipoSocioDeNegocio" VARCHAR(500),
  "IdTipoDocumentoIdentidad" VARCHAR(500),
  "TitularPago" VARCHAR(500),
  "IdTipoPersonaTitularPago" VARCHAR(500),
  "IdPersonaTitularPago" BIGINT,
  "NombrePersonaTitularPago" VARCHAR(500),
  "MueveCuentaInversion" VARCHAR(500),
  "NombrePortafolioTitularPago" VARCHAR(500),
  "SecuenciaAgrupacion" VARCHAR(500),
  "EsBilateral" VARCHAR(500),
  "ComprobanteNeteado" VARCHAR(500),
  "SeparadoPorConcepto" VARCHAR(500),
  "IdUsuarioBloqueo" BIGINT,
  "FechaHoraBloqueo" TIMESTAMP,
  "IdCarga" BIGINT,
  "IdTipoEvento" VARCHAR(500),
  "Autorizacion" VARCHAR(500),
  "SolicitudAutorizacion" VARCHAR(500),
  "MotivoAutorizacion" VARCHAR(500),
  "IdEstadoAutorizacion" VARCHAR(500),
  "NombreEstadoAutorizacion" VARCHAR(500),
  "NombreMedioDePago" VARCHAR(500),
  "IdModalidadDePago" VARCHAR(500),
  "NombreModalidadDePago" VARCHAR(500),
  "IdCuentaBancoCorredora" BIGINT,
  "IdChequera" BIGINT,
  "NombreTipoCuentaBancariaCorredora" VARCHAR(500),
  "NumeroSerieSocio" VARCHAR(500),
  "NumeroDocumentoSocio" BIGINT,
  "FechaDocumentoPagoSocio" TIMESTAMP,
  "IdBancoSocio" BIGINT,
  "NombreTipoCuentaBancariaSocio" VARCHAR(500),
  "NombreUsuarioLiquidacion" VARCHAR(500),
  "IdUsuarioAnulacion" BIGINT,
  "FechaHoraAnulacion" TIMESTAMP,
  "IdComprobanteOrigen" BIGINT,
  "FolioComprobanteDTE" BIGINT,
  "IdPortafolio" BIGINT,
  "NombrePortafolio" VARCHAR(500),
  "IdTipoCanalIngreso" VARCHAR(500),
  "Alias" VARCHAR(500),
  "IdComprobantePartidaEgreso" BIGINT,
  "NombreEstadoNomina" VARCHAR(500),
  "CondicionDeLiquidacion" VARCHAR(500),
  "Glosa" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDetalle" (
  "IdComprobanteDetalle" BIGINT,
  "NroLinea" INT,
  "IdTipoMovimiento" VARCHAR(500),
  "TipoMovimiento" VARCHAR(500),
  "NombreTipoMovimiento" VARCHAR(500),
  "IdConcepto" VARCHAR(500),
  "NombreConcepto" VARCHAR(500),
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "IdTipoComprobanteOrigen" VARCHAR(500),
  "IdComprobanteOrigen" BIGINT,
  "FechaComprobanteOrigen" TIMESTAMP,
  "IdTipoProducto" VARCHAR(500),
  "NombreTipoProducto" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(500),
  "NombreTipoOperacion" VARCHAR(500),
  "IdInstrumento" BIGINT,
  "Nemotecnico" VARCHAR(500),
  "IdTipoInstrumento" VARCHAR(500),
  "IdClasificacionTipoInstrumento" VARCHAR(500),
  "IdComprobante" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" (
  "IdComprobante" BIGINT,
  "IdMedioDePago" VARCHAR(500),
  "IdModalidadDePago" VARCHAR(500),
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "IdComprobanteFormaDePago" BIGINT,
  "ClaveDCV" VARCHAR(500),
  "NombreMedioDePago" VARCHAR(500),
  "TipoMedioDePago" VARCHAR(500),
  "NombreModalidadDePago" VARCHAR(500),
  "IdCuentaBancariaCorredora" BIGINT,
  "IdTipoCuentaBancariaCorredora" VARCHAR(500),
  "NombreTipoCuentaBancariaCorredora" VARCHAR(500),
  "IdBancoCorredora" BIGINT,
  "NombreBancoCorredora" VARCHAR(500),
  "NombreCuentaBancariaCorredora" VARCHAR(500),
  "NumeroCuentaBancariaCorredora" VARCHAR(500),
  "IdTarjetaBancariaCorredora" BIGINT,
  "IdChequera" BIGINT,
  "NumeroSerieCorredora" VARCHAR(500),
  "NumeroDocumentoCorredora" BIGINT,
  "FechaDocumentoPagoCorredora" TIMESTAMP,
  "IdCuentaBancariaSocio" BIGINT,
  "IdTipoCuentaBancariaSocio" VARCHAR(500),
  "NombreTipoCuentaBancariaSocio" VARCHAR(500),
  "NombreCuentaBancariaSocio" VARCHAR(500),
  "IdBancoSocio" BIGINT,
  "NombreBancoSocio" VARCHAR(500),
  "NumeroCuentaBancariaSocio" VARCHAR(500),
  "IdTarjetaBancariaSocio" BIGINT,
  "NumeroSerieSocio" VARCHAR(500),
  "NumeroDocumentoSocio" BIGINT,
  "FechaDocumentoPagoSocio" TIMESTAMP,
  "IdEstado" VARCHAR(500),
  "NombreEstado" VARCHAR(500),
  "IdUsuarioGiro" BIGINT,
  "NombreUsuarioGiro" VARCHAR(500),
  "IdComprobantePartida" BIGINT,
  "IdUsuarioCertificacion" BIGINT,
  "NombreUsuarioCertificacion" VARCHAR(500),
  "IdUsuarioLiquidacion" BIGINT,
  "NombreUsuarioLiquidacion" VARCHAR(500),
  "FechaLiquidacion" DATE,
  "NombreConcepto" VARCHAR(500),
  "NombreTipoMovimiento" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreriaPivot" (
  "SEPARAPORCONCEPTO" VARCHAR(500),
  "SEPARAPORAGENTE" VARCHAR(500),
  "SEPARAPORSUCURSAL" VARCHAR(500),
  "SEPARAPORCORREDORACONTRAPARTE" VARCHAR(500),
  "SEPARAPORTIPOCOMPROBANTE" VARCHAR(500),
  "SEPARAPORTIPOPRODUCTO" VARCHAR(500),
  "SEPARAPORTIPOOPERACION" VARCHAR(500),
  "SEPARAPORBOLSA" VARCHAR(500),
  "SEPARAPORCONDICIONLIQUIDACION" VARCHAR(500),
  "IdEmpresa" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias" (
  "IdPersona" BIGINT,
  "IdCuentaBanco" BIGINT,
  "IdBanco" BIGINT,
  "IdPlaza" BIGINT,
  "PlazoRetencionMismoBanco" BIGINT,
  "PlazoRetencionOtroBanco" BIGINT,
  "NombreCuentaBancaria" VARCHAR(500),
  "NumeroCuentaBancaria" VARCHAR(500),
  "IdTipoCuentaBancaria" VARCHAR(500),
  "IdEmpresa" BIGINT,
  "NombreTipoCuentaBancaria" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" (
  "IdIngresoEgreso" BIGINT,
  "IdEmpresa" BIGINT,
  "NombreEmpresa" VARCHAR(500),
  "IdModulo" VARCHAR(500),
  "IdUsuario" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "NombreTipoMovimiento" VARCHAR(500),
  "IdConcepto" VARCHAR(500),
  "NombreConcepto" VARCHAR(500),
  "IdCaja" BIGINT,
  "NombreCaja" VARCHAR(500),
  "IdMoneda" VARCHAR(500),
  "NombreMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "MontoEgreso" DECIMAL(28,16),
  "MontoIngreso" DECIMAL(28,16),
  "MontoConSigno" DECIMAL(28,16),
  "FechaLiquidacion" DATE,
  "FechaIngresoRegistro" TIMESTAMP,
  "FechaMovimiento" TIMESTAMP,
  "IdTipoPersona" VARCHAR(500),
  "NombreTipoSocioDeNegocio" VARCHAR(500),
  "IdPersona" BIGINT,
  "IdCliente" BIGINT,
  "IdSocioDeNegocio" BIGINT,
  "NombrePersona" VARCHAR(500),
  "IdPortafolio" BIGINT,
  "NombrePortafolio" VARCHAR(500),
  "IdTipoDocumentoIdentidad" VARCHAR(500),
  "DocumentoIdentidad" VARCHAR(500),
  "IdTipoPersonaTitularPago" VARCHAR(500),
  "IdPersonaTitularPago" BIGINT,
  "NombrePersonaTitularPago" VARCHAR(500),
  "MueveCuentaInversion" VARCHAR(500),
  "IdPortafolioTitularPago" BIGINT,
  "IdTipoDocumentoIdentidadTitularPago" VARCHAR(500),
  "DocumentoIdentidadTitularPago" VARCHAR(500),
  "NombrePortafolioTitularPago" VARCHAR(500),
  "IdClienteTitularPago" BIGINT,
  "SecuenciaAgrupacion" VARCHAR(500),
  "SecuenciaRelacionada" BIGINT,
  "Glosa" VARCHAR(500),
  "IdEmpleadoDigitador" BIGINT,
  "IdSucursalEmpleadoDigitador" BIGINT,
  "IdTipoComprobanteOrigen" VARCHAR(500),
  "IdComprobanteOrigen" BIGINT,
  "FolioComprobanteDTE" BIGINT,
  "FechaComprobanteOrigen" TIMESTAMP,
  "FechaLiquidacionComprobanteOrigen" TIMESTAMP,
  "IdTipoProducto" VARCHAR(500),
  "NombreTipoProducto" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(500),
  "NombreTipoOperacion" VARCHAR(500),
  "IdBolsa" VARCHAR(500),
  "IdMonedaOperacion" VARCHAR(500),
  "MontoOperacion" DECIMAL(28,16),
  "FechaParidad" TIMESTAMP,
  "IdTipoReajuste" VARCHAR(500),
  "ValorParidad" DECIMAL(28,16),
  "FolioCartera" BIGINT,
  "IdInstrumento" BIGINT,
  "Nemotecnico" VARCHAR(500),
  "IdTipoInstrumento" VARCHAR(500),
  "IdClasificacionTipoInstrumento" VARCHAR(500),
  "EsBilateral" VARCHAR(500),
  "TitularPago" VARCHAR(500),
  "IdUsuarioEmision" BIGINT,
  "NombreUsuarioEmision" VARCHAR(500),
  "FechaHoraEmision" TIMESTAMP,
  "IdUsuarioBloqueo" BIGINT,
  "FechaHoraBloqueo" TIMESTAMP,
  "IdCarga" BIGINT,
  "IdEstado" VARCHAR(500),
  "NombreEstado" VARCHAR(500),
  "IdComprobante" BIGINT,
  "FechaOperacion" TIMESTAMP,
  "IdCondicionLiquidacion" VARCHAR(500),
  "IdOperacion" BIGINT,
  "EsIngresoAutomatico" VARCHAR(500),
  "AgrupaComprobante" VARCHAR(500),
  "NeteaComprobante" VARCHAR(500),
  "NombreVentana" VARCHAR(500),
  "IdMedioDePago" VARCHAR(500),
  "NombreMedioDePago" VARCHAR(500),
  "IdModalidadDePago" VARCHAR(500),
  "NombreModalidadDePago" VARCHAR(500),
  "IdCuentaBancoCorredora" BIGINT,
  "IdChequera" BIGINT,
  "NombreBancoCorredora" VARCHAR(500),
  "NumeroCuentaBancariaCorredora" VARCHAR(500),
  "NombreTipoCuentaBancariaCorredora" VARCHAR(500),
  "NombreBancoSocio" VARCHAR(500),
  "NumeroCuentaBancariaSocio" VARCHAR(500),
  "NombreTipoCuentaBancariaSocio" VARCHAR(500),
  "NumeroSerieSocio" VARCHAR(500),
  "NumeroDocumentoSocio" BIGINT,
  "FechaDocumentoPagoSocio" TIMESTAMP,
  "IdBancoSocio" BIGINT,
  "IdEstadoComprobante" VARCHAR(500),
  "NombreEstadoComprobante" VARCHAR(500),
  "IdUsuarioAnulacion" BIGINT,
  "FechaHoraAnulacion" TIMESTAMP,
  "IdTipoEvento" VARCHAR(500),
  "Autorizacion" VARCHAR(500),
  "SolicitudAutorizacion" VARCHAR(500),
  "MotivoAutorizacion" VARCHAR(500),
  "IdEstadoAutorizacion" VARCHAR(500),
  "NombreEstadoAutorizacion" VARCHAR(500),
  "RegimenMatrizAgrupadorTitularPago" VARCHAR(500),
  "AliasBanco" VARCHAR(500),
  "IdTipoCanalIngreso" VARCHAR(500),
  "CuentaDCVOrigen" VARCHAR(500),
  "CuentaDCVDestino" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.ConsultarIngresoEgreso_Proceso" (
  "IdPortafolio" BIGINT,
  "IdTrabajo" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante" (
  "NumeroReferencia" BIGINT,
  "IdComprobante" BIGINT,
  "TipoComprobante" VARCHAR(500),
  "IdTipoMovimiento" VARCHAR(500),
  "IdConcepto" VARCHAR(500),
  "IdCaja" BIGINT,
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "FechaLiquidacion" DATE,
  "IdTipoPersona" VARCHAR(500),
  "IdPersona" BIGINT,
  "NombrePersona" VARCHAR(500),
  "IdTipoDocumentoIdentidad" VARCHAR(500),
  "DocumentoIdentidad" VARCHAR(500),
  "IdTipoPersonaTitularPago" VARCHAR(500),
  "IdPersonaTitularPago" BIGINT,
  "NombrePersonaTitularPago" VARCHAR(500),
  "IdPortafolioTitularPago" BIGINT,
  "TitularPago" VARCHAR(500),
  "SecuenciaAgrupacion" VARCHAR(500),
  "EsBilateral" VARCHAR(500),
  "ComprobanteAgrupado" VARCHAR(500),
  "ComprobanteNeteado" VARCHAR(500),
  "SeparaPorConcepto" VARCHAR(500),
  "GeneradoDesdeMovimientos" VARCHAR(500),
  "MueveCuentaInversion" VARCHAR(500),
  "IdCondicionDeLiquidacion" VARCHAR(500),
  "Glosa" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalle" (
  "IdComprobanteDetalle" BIGINT,
  "IdComprobante" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "IdConcepto" VARCHAR(500),
  "NroLinea" INT,
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "SecuenciaAgrupacion" VARCHAR(500),
  "SecuenciaRelacionada" BIGINT,
  "IdEmpleadoDigitador" BIGINT,
  "IdSucursalEmpleadoDigitador" BIGINT,
  "IdTipoComprobanteOrigen" VARCHAR(500),
  "IdComprobanteOrigen" BIGINT,
  "FechaComprobanteOrigen" TIMESTAMP,
  "FechaLiquidacionComprobanteOrigen" TIMESTAMP,
  "IdTipoProducto" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(500),
  "IdBolsa" VARCHAR(500),
  "IdMonedaOperacion" VARCHAR(500),
  "MontoOperacion" DECIMAL(28,16),
  "FechaParidad" TIMESTAMP,
  "IdTipoReajuste" VARCHAR(500),
  "ValorParidad" DECIMAL(28,16),
  "FolioCartera" BIGINT,
  "IdInstrumento" BIGINT,
  "IdTipoInstrumento" VARCHAR(500),
  "IdClasificacionTipoInstrumento" VARCHAR(500),
  "IdCondicionDeLiquidacion" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalleOrigen" (
  "IdComprobanteDetalle" BIGINT,
  "IdIngresoEgreso" BIGINT,
  "IdComprobante" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago" (
  "IdComprobante" BIGINT,
  "NumeroReferencia" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "IdMedioDePago" VARCHAR(500),
  "NombreMedioDePago" VARCHAR(500),
  "IdModalidadDePago" VARCHAR(500),
  "NombreModalidadDePago" VARCHAR(500),
  "IdCuentaBancariaCorredora" BIGINT,
  "NombreCuentaBancariaCorredora" VARCHAR(500),
  "NumeroCuentaBancariaCorredora" VARCHAR(500),
  "IdTipoCuentaBancariaCorredora" VARCHAR(500),
  "NombreTipoCuentaBancariaCorredora" VARCHAR(500),
  "IdTarjetaBancariaCorredora" BIGINT,
  "IdTipoTarjetaBancariaCorredora" VARCHAR(500),
  "NombreTipoTarjetaBancariaCorredora" VARCHAR(500),
  "NumeroTarjetaBancariaCorredora" VARCHAR(500),
  "IdChequera" BIGINT,
  "NumeroDocumentoCorredora" BIGINT,
  "FechaDocumentoPagoCorredora" TIMESTAMP,
  "IdCuentaBancariaSocio" BIGINT,
  "IdBancoSocio" BIGINT,
  "NombreCuentaBancariaSocio" VARCHAR(500),
  "NumeroCuentaBancariaSocio" VARCHAR(500),
  "IdTipoCuentaBancariaSocio" VARCHAR(500),
  "NombreTipoCuentaBancariaSocio" VARCHAR(500),
  "IdTarjetaBancariaSocio" BIGINT,
  "IdTipoTarjetaBancariaSocio" VARCHAR(500),
  "NombreTipoTarjetaBancariaSocio" VARCHAR(500),
  "NumeroTarjetaBancariaSocio" VARCHAR(500),
  "NumeroSerieSocio" VARCHAR(500),
  "NumeroDocumentoSocio" BIGINT,
  "FechaDocumentoPagoSocio" TIMESTAMP,
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "IdPlaza" BIGINT,
  "DiasRetencion" BIGINT,
  "FechaAfectaSaldo" TIMESTAMP
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar" (
  "NumeroReferencia" BIGINT,
  "IdComprobante" BIGINT,
  "TipoComprobante" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_FormaDePago" (
  "NumeroReferencia" BIGINT,
  "IdMedioDePago" VARCHAR(500),
  "IdModalidadDePago" VARCHAR(500),
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "IdCuentaBancariaCorredora" BIGINT,
  "IdTarjetaBancariaCorredora" BIGINT,
  "IdChequera" BIGINT,
  "NumeroDocumentoCorredora" BIGINT,
  "FechaDocumentoPagoCorredora" TIMESTAMP,
  "IdCuentaBancariaSocio" BIGINT,
  "IdBancoSocio" BIGINT,
  "NombreCuentaBancariaSocio" VARCHAR(500),
  "NumeroCuentaBancariaSocio" VARCHAR(500),
  "IdTipoCuentaBancariaSocio" VARCHAR(500),
  "NombreTipoCuentaBancariaSocio" VARCHAR(500),
  "IdTarjetaBancariaSocio" BIGINT,
  "IdTipoTarjetaBancariaSocio" VARCHAR(500),
  "NombreTipoTarjetaBancariaSocio" VARCHAR(500),
  "NumeroTarjetaBancariaSocio" VARCHAR(500),
  "NumeroSerieSocio" VARCHAR(500),
  "NumeroDocumentoSocio" BIGINT,
  "FechaDocumentoPagoSocio" TIMESTAMP
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar" (
  "NumeroReferencia" BIGINT,
  "IdIngresoEgreso" BIGINT,
  "TipoComprobante" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_GruposDeComprobantes" (
  "Grupo" VARCHAR(500),
  "IdComprobante" BIGINT,
  "NumeroReferencia" BIGINT,
  "TipoComprobante" VARCHAR(500),
  "IdPersonaTitularPago" BIGINT,
  "IdPortafolioTitularPago" BIGINT,
  "IdMoneda" VARCHAR(500),
  "IdCaja" BIGINT,
  "FechaLiquidacion" DATE,
  "SecuenciaAgrupacion" VARCHAR(500),
  "MueveCuentaInversion" VARCHAR(500),
  "NeteaComprobante" VARCHAR(500),
  "IdTipoMovimiento" VARCHAR(500),
  "AgrupaComprobante" VARCHAR(500),
  "IdInternoTes" VARCHAR(500),
  "IdConcepto" VARCHAR(500),
  "IdEmpleadoDigitador" BIGINT,
  "IdSucursalEmpleadoDigitador" BIGINT,
  "IdTipoComprobanteOrigen" VARCHAR(500),
  "IdTipoProducto" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(500),
  "IdBolsa" VARCHAR(500),
  "IdCondicionDeLiquidacion" VARCHAR(500),
  "Glosa" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_AdvertenciasDetalle" (
  "Codigo" VARCHAR(100),
  "RequiereAutorizacion" VARCHAR(500),
  "Descripcion" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ErroresDetalle" (
  "Codigo" VARCHAR(100),
  "Descripcion" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_InformacionDetalle" (
  "Codigo" VARCHAR(100),
  "Descripcion" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_Salida_ResultadoProceso" (
  "Campo" VARCHAR(500),
  "Id" BIGINT,
  "Servicio" VARCHAR(500),
  "Resultado" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearComprobante_TablaCodigosMensajes" (
  "Dummy" VARCHAR(1)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgresoFormaDePago_Proceso" (
  "NumeroReferencia" BIGINT,
  "NumeroReferenciaFormaDePago" BIGINT,
  "IdMedioDePago" VARCHAR(500),
  "IdModalidadDePago" VARCHAR(500),
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "IdCuentaBancariaCorredora" BIGINT,
  "IdCuentaBancariaSocio" BIGINT,
  "IdTarjetaBancariaCorredora" BIGINT,
  "IdTarjetaBancariaSocio" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CrearIngresoEgreso_Proceso" (
  "NumeroReferencia" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "IdPersona" BIGINT,
  "IdMoneda" VARCHAR(500),
  "IdConcepto" VARCHAR(500),
  "IdPersonaTitularPago" BIGINT,
  "IdCaja" BIGINT,
  "IdCondicionLiquidacion" VARCHAR(500),
  "IdTipoProducto" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(500),
  "SecuenciaAgrupacion" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "IdTipoCanalIngreso" VARCHAR(500),
  "IdPortafolio" BIGINT,
  "IdPortafolioTitularPago" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.CuentaBancariaCorredoraGenerarComprobante" (
  "IdPersona" BIGINT,
  "NombreCompleto" VARCHAR(500),
  "DocumentoIdentidadDefecto" VARCHAR(500),
  "CodigoTipoPersona" VARCHAR(500),
  "IdCuentaBanco" BIGINT,
  "IdBanco" BIGINT,
  "IdPlaza" BIGINT,
  "PlazoRetencionMismoBanco" BIGINT,
  "PlazoRetencionOtroBanco" BIGINT,
  "NombreCuentaBancaria" VARCHAR(500),
  "NumeroCuentaBancaria" VARCHAR(500),
  "IdTipoCuentaBancaria" VARCHAR(500),
  "NombreTipoCuentaBancaria" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_AdvertenciasDetalle" (
  "Codigo" VARCHAR(100),
  "RequiereAutorizacion" VARCHAR(1),
  "Descripcion" VARCHAR(2000)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.EdicionMultiple_Salida_ErroresDetalle" (
  "Codigo" VARCHAR(100),
  "Descripcion" VARCHAR(2000)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.FormaDePagoGenerarComprobante_Proceso" (
  "NumeroReferencia" BIGINT,
  "IdMedioDePago" VARCHAR(500),
  "IdModalidadDePago" VARCHAR(500),
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "IdCuentaBancariaCorredora" BIGINT,
  "IdTarjetaBancariaCorredora" BIGINT,
  "IdChequera" BIGINT,
  "NumeroDocumentoCorredora" BIGINT,
  "FechaDocumentoPagoCorredora" TIMESTAMP,
  "IdCuentaBancariaSocio" BIGINT,
  "IdBancoSocio" BIGINT,
  "NombreCuentaBancariaSocio" VARCHAR(500),
  "NumeroCuentaBancariaSocio" VARCHAR(500),
  "IdTipoCuentaBancariaSocio" VARCHAR(500),
  "NombreTipoCuentaBancariaSocio" VARCHAR(500),
  "IdTarjetaBancariaSocio" BIGINT,
  "IdTipoTarjetaBancariaSocio" VARCHAR(500),
  "NombreTipoTarjetaBancariaSocio" VARCHAR(500),
  "NumeroTarjetaBancariaSocio" VARCHAR(500),
  "NumeroSerieSocio" VARCHAR(500),
  "NumeroDocumentoSocio" BIGINT,
  "FechaDocumentoPagoSocio" TIMESTAMP,
  "NombreMedioDePago" VARCHAR(500),
  "NombreModalidadDePago" VARCHAR(500),
  "IdPersona" BIGINT,
  "NombreCompleto" VARCHAR(500),
  "DocumentoIdentidadDefecto" VARCHAR(500),
  "CodigoTipoPersona" VARCHAR(500),
  "IdCuentaBanco" BIGINT,
  "IdBanco" BIGINT,
  "IdPlaza" BIGINT,
  "PlazoRetencionMismoBanco" BIGINT,
  "PlazoRetencionOtroBanco" BIGINT,
  "NombreCuentaBancaria" VARCHAR(500),
  "NumeroCuentaBancaria" VARCHAR(500),
  "IdTipoCuentaBancaria" VARCHAR(500),
  "NombreTipoCuentaBancaria" VARCHAR(500),
  "IdTipoTarjetaBancaria" VARCHAR(500),
  "NombreTipoTarjetaBancaria" VARCHAR(500),
  "NumeroTarjetaBancaria" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso" (
  "NumeroReferencia" BIGINT,
  "IdIngresoEgresoInformado" BIGINT,
  "TipoComprobante" VARCHAR(500),
  "IdIngresoEgreso" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "IdConcepto" VARCHAR(500),
  "IdCaja" BIGINT,
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "FechaLiquidacion" DATE,
  "FechaIngresoRegistro" TIMESTAMP,
  "FechaMovimiento" TIMESTAMP,
  "IdTipoPersona" VARCHAR(500),
  "IdPersona" BIGINT,
  "IdTipoDocumentoIdentidad" VARCHAR(500),
  "IdTipoPersonaTitularPago" VARCHAR(500),
  "IdPersonaTitularPago" BIGINT,
  "IdPortafolioTitularPago" BIGINT,
  "SecuenciaAgrupacion" VARCHAR(500),
  "SecuenciaRelacionada" BIGINT,
  "Glosa" VARCHAR(500),
  "IdEmpleadoDigitador" BIGINT,
  "IdSucursalEmpleadoDigitador" BIGINT,
  "IdTipoComprobanteOrigen" VARCHAR(500),
  "IdComprobanteOrigen" BIGINT,
  "FechaComprobanteOrigen" TIMESTAMP,
  "FechaLiquidacionComprobanteOrigen" TIMESTAMP,
  "IdTipoProducto" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(500),
  "IdBolsa" VARCHAR(500),
  "IdMonedaOperacion" VARCHAR(500),
  "MontoOperacion" DECIMAL(28,16),
  "FechaParidad" TIMESTAMP,
  "IdTipoReajuste" VARCHAR(500),
  "ValorParidad" DECIMAL(28,16),
  "FolioCartera" BIGINT,
  "IdInstrumento" BIGINT,
  "IdTipoInstrumento" VARCHAR(500),
  "IdClasificacionTipoInstrumento" VARCHAR(500),
  "EsBilateral" VARCHAR(500),
  "TitularPago" VARCHAR(500),
  "EsIngresoAutomatico" VARCHAR(500),
  "NeteaComprobante" VARCHAR(500),
  "AgrupaComprobante" VARCHAR(500),
  "MueveCuentaInversion" VARCHAR(500),
  "IdEstado" VARCHAR(500),
  "IdEmpresa" BIGINT,
  "IdCajaComprobante" BIGINT,
  "CantidadSecuenciaMovimiento" INT,
  "CantidadSecuenciaComprobante" INT,
  "NombrePersona" VARCHAR(500),
  "DocumentoIdentidad" VARCHAR(500),
  "NombrePersonaTitularPago" VARCHAR(500),
  "IdCondicionDeLiquidacion" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_ComprobantesALiquidar" (
  "IdComprobante" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "AfectaCuentaInversion" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_FormasDePagoALiquidar" (
  "IdComprobante" BIGINT,
  "IdComprobanteFormaDePago" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobante" (
  "IdInterno" BIGINT,
  "IdComprobanteInformado" BIGINT,
  "IdTipoMovimientoInformado" VARCHAR(500),
  "IdComprobante" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "MontoComprobante" DECIMAL(28,16),
  "CantidadFormaDePago" BIGINT,
  "MontoTotalFormaDePago" DECIMAL(28,16),
  "IdEstadoComprobante" VARCHAR(500),
  "AfectaCuentaInversion" VARCHAR(500),
  "IdPortafolioTitularPago" BIGINT,
  "IdConcepto" VARCHAR(500),
  "FechaMovimiento" TIMESTAMP,
  "IdPersona" BIGINT,
  "NombrePersona" VARCHAR(500),
  "TitularPago" VARCHAR(500),
  "IdTipoPersonaTitularPago" VARCHAR(500),
  "IdPersonaTitularPago" BIGINT,
  "FechaLiquidacion" DATE,
  "IdCaja" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteDetalle" (
  "IdInterno" BIGINT,
  "IdComprobanteInformado" BIGINT,
  "IdComprobanteDetalle" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "IdTipoComprobanteOrigen" VARCHAR(500),
  "SecuenciaRelacionada" BIGINT,
  "PendienteDeLiquidar" INT,
  "IdConcepto" VARCHAR(500),
  "Monto" DECIMAL(28,16)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteFormaDePago" (
  "IdInterno" BIGINT,
  "IdComprobanteInformado" BIGINT,
  "IdComprobanteFormaDePagoInformado" BIGINT,
  "IdComprobanteFormaDePago" BIGINT,
  "IdCuentaBancariaCorredora" BIGINT,
  "IdEstadoFormaDePago" VARCHAR(500),
  "IdEstadoComprobante" VARCHAR(500),
  "TipoMedioDePago" VARCHAR(500),
  "LiquidacionParcialEnviaACTA" VARCHAR(500),
  "EstadosPosibleTipoMovimientoMedioDePago" VARCHAR(500),
  "AfectaSaldoEnEstado" VARCHAR(500),
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "IdDeposito" BIGINT,
  "IdEstadoDepositoDetalle" VARCHAR(500),
  "FechaLiquidacion" DATE,
  "DiasRetencion" BIGINT,
  "IdMedioDePago" VARCHAR(500),
  "IdCaja" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_InformacionDetalle" (
  "Codigo" VARCHAR(100),
  "Descripcion" VARCHAR(2000)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Servicio_ComprobantesMuevenCuentaInversion" (
  "IdComprobante" BIGINT,
  "IdComprobanteDetalle" BIGINT,
  "IdComprobanteFormaDePago" BIGINT,
  "AfectaCuentaInversion" VARCHAR(10),
  "FechaMovimiento" DATE,
  "IdConcepto" VARCHAR(10),
  "IdPersona" BIGINT,
  "NombrePersona" VARCHAR(200),
  "TitularPago" VARCHAR(1),
  "IdTipoPersonaTitularPago" VARCHAR(10),
  "IdPersonaTitularPago" BIGINT,
  "IdMoneda" VARCHAR(3),
  "Monto" DECIMAL(28,16),
  "IdPortafolio" BIGINT,
  "IdTipoMovimientoCtaInvCliente" VARCHAR(10),
  "IdTipoMovimientoCtaInvCorredora" VARCHAR(10),
  "IdTipoComprobante" VARCHAR(10),
  "IdTipoMovimientoAsociado" VARCHAR(10),
  "IdMovimientoAsociado" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.MovimientoDetalleGenerarComprobante" (
  "Grupo" VARCHAR(500),
  "IdComprobante" BIGINT,
  "NroLinea" INT,
  "IdIngresoEgreso" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "IdTipoMovimientoDetalle" VARCHAR(500),
  "IdConcepto" VARCHAR(500),
  "IdCaja" BIGINT,
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "FechaLiquidacion" DATE,
  "FechaIngresoRegistro" TIMESTAMP,
  "FechaMovimiento" TIMESTAMP,
  "IdTipoPersona" VARCHAR(500),
  "IdPersona" BIGINT,
  "NombrePersona" VARCHAR(500),
  "IdTipoDocumentoIdentidad" VARCHAR(500),
  "DocumentoIdentidad" VARCHAR(500),
  "IdTipoPersonaTitularPago" VARCHAR(500),
  "IdPersonaTitularPago" BIGINT,
  "NombrePersonaTitularPago" VARCHAR(500),
  "IdPortafolioTitularPago" BIGINT,
  "SecuenciaAgrupacion" VARCHAR(500),
  "SecuenciaRelacionada" BIGINT,
  "MueveCuentaInversion" VARCHAR(500),
  "IdEmpleadoDigitador" BIGINT,
  "IdSucursalEmpleadoDigitador" BIGINT,
  "EsBilateral" VARCHAR(500),
  "TitularPago" VARCHAR(500),
  "IdTipoComprobanteOrigen" VARCHAR(500),
  "IdComprobanteOrigen" BIGINT,
  "FechaComprobanteOrigen" TIMESTAMP,
  "FechaLiquidacionComprobanteOrigen" TIMESTAMP,
  "IdTipoProducto" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(500),
  "IdBolsa" VARCHAR(500),
  "IdMonedaOperacion" VARCHAR(500),
  "MontoOperacion" DECIMAL(28,16),
  "FechaParidad" TIMESTAMP,
  "IdTipoReajuste" VARCHAR(500),
  "ValorParidad" DECIMAL(28,16),
  "FolioCartera" BIGINT,
  "IdInstrumento" BIGINT,
  "IdTipoInstrumento" VARCHAR(500),
  "IdClasificacionTipoInstrumento" VARCHAR(500),
  "ComprobanteAgrupado" VARCHAR(500),
  "ComprobanteNeteado" VARCHAR(500),
  "NumeroReferencia" BIGINT,
  "TipoComprobante" VARCHAR(500),
  "IdCondicionDeLiquidacion" VARCHAR(500),
  "Glosa" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::GBL.SaldoPendienteDeLiquidar_Proceso_Paso" (
  "IdPersona" BIGINT,
  "IdPortafolio" BIGINT,
  "IdMoneda" VARCHAR(500),
  "IdCliente" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.Caja" (
  "IdCaja" BIGINT,
  "Codigo" VARCHAR(10),
  "IdEstado" VARCHAR(3),
  "IdEmpresa" BIGINT,
  "Nombre" VARCHAR(100)
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.Comprobante" (
  "IdComprobante" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "IdEstado" VARCHAR(500),
  "IdPortafolioTitularPago" BIGINT,
  "IdConcepto" VARCHAR(500),
  "FechaMovimiento" TIMESTAMP,
  "IdPersona" BIGINT,
  "NombrePersona" VARCHAR(500),
  "TitularPago" VARCHAR(500),
  "IdTipoPersonaTitularPago" VARCHAR(500),
  "IdPersonaTitularPago" BIGINT,
  "FechaLiquidacion" DATE,
  "IdCaja" BIGINT,
  "TipoComprobante" VARCHAR(500),
  "IdMoneda" VARCHAR(500),
  "IdEmpresa" BIGINT,
  "IdUsuario" BIGINT,
  "FechaIngresoRegistro" TIMESTAMP,
  "IdTipoPersona" VARCHAR(500),
  "IdTipoDocumentoIdentidad" VARCHAR(500),
  "DocumentoIdentidad" VARCHAR(500),
  "NombrePersonaTitularPago" VARCHAR(500),
  "SecuenciaAgrupacion" VARCHAR(500),
  "EsBilateral" VARCHAR(500),
  "ComprobanteAgrupado" VARCHAR(500),
  "ComprobanteNeteado" VARCHAR(500),
  "SeparadoPorConcepto" VARCHAR(500),
  "GeneradoDesdeMovimientos" VARCHAR(500),
  "MueveCuentaInversion" VARCHAR(500),
  "IdCondicionDeLiquidacion" VARCHAR(500),
  "Glosa" VARCHAR(500),
  "SecuenciaMovimiento" VARCHAR(500),
  "IdUsuarioBloqueo" BIGINT,
  "IdCarga" BIGINT,
  "IdEvento" BIGINT,
  "IdUsuarioAnulacion" BIGINT,
  "FechaHoraAnulacion" TIMESTAMP
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" (
  "IdComprobanteDetalle" BIGINT,
  "IdComprobante" BIGINT,
  "IdMoneda" VARCHAR(500),
  "IdTipoMovimiento" VARCHAR(500),
  "IdConcepto" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "IdTipoComprobanteOrigen" VARCHAR(500),
  "SecuenciaRelacionada" BIGINT,
  "IdEstado" VARCHAR(500),
  "IdTipoProducto" VARCHAR(500),
  "IdComprobanteOrigen" BIGINT,
  "IdTipoOperacion" VARCHAR(500),
  "IdMonedaOperacion" VARCHAR(500),
  "NroLinea" INT,
  "SecuenciaAgrupacion" VARCHAR(500),
  "IdEmpleadoDigitador" BIGINT,
  "IdSucursalEmpleadoDigitador" BIGINT,
  "FechaComprobanteOrigen" TIMESTAMP,
  "FechaLiquidacionComprobanteOrigen" TIMESTAMP,
  "IdBolsa" VARCHAR(500),
  "MontoOperacion" DECIMAL(28,16),
  "FechaParidad" TIMESTAMP,
  "IdTipoReajuste" VARCHAR(500),
  "ValorParidad" DECIMAL(28,16),
  "FolioCartera" BIGINT,
  "IdInstrumento" BIGINT,
  "IdTipoInstrumento" VARCHAR(500),
  "IdClasificacionTipoInstrumento" VARCHAR(500),
  "IdCondicionDeLiquidacion" VARCHAR(500),
  "IdCarga" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" (
  "IdComprobanteDetalle" BIGINT,
  "IdIngresoEgreso" BIGINT,
  "IdComprobanteDetalleOrigen" BIGINT,
  "IdComprobante" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" (
  "IdComprobanteFormaDePago" BIGINT,
  "IdCuentaBancariaCorredora" BIGINT,
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "IdDeposito" BIGINT,
  "IdEstadoDepositoDetalle" VARCHAR(500),
  "DiasRetencion" BIGINT,
  "IdMedioDePago" VARCHAR(500),
  "IdComprobante" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "IdEstado" VARCHAR(500),
  "NumeroCuentaBancariaCorredora" VARCHAR(500),
  "IdModalidadDePago" VARCHAR(500),
  "IdCuentaBancariaSocio" BIGINT,
  "NumeroCuentaBancariaSocio" VARCHAR(500),
  "IdBancoSocio" BIGINT,
  "FechaAfectaSaldo" TIMESTAMP,
  "IdUsuarioLiquidacion" BIGINT,
  "FechaHoraLiquidacion" TIMESTAMP,
  "NombreMedioDePago" VARCHAR(500),
  "NombreModalidadDePago" VARCHAR(500),
  "IdTipoCuentaBancariaCorredora" VARCHAR(500),
  "NombreTipoCuentaBancariaCorredora" VARCHAR(500),
  "NombreCuentaBancariaCorredora" VARCHAR(500),
  "IdTarjetaBancariaCorredora" BIGINT,
  "IdTipoTarjetaBancariaCorredora" VARCHAR(500),
  "NombreTipoTarjetaBancariaCorredora" VARCHAR(500),
  "NumeroTarjetaBancariaCorredora" VARCHAR(500),
  "IdChequera" BIGINT,
  "NumeroDocumentoCorredora" BIGINT,
  "FechaDocumentoPagoCorredora" TIMESTAMP,
  "NombreCuentaBancariaSocio" VARCHAR(500),
  "IdTipoCuentaBancariaSocio" VARCHAR(500),
  "NombreTipoCuentaBancariaSocio" VARCHAR(500),
  "IdTarjetaBancariaSocio" BIGINT,
  "IdTipoTarjetaBancariaSocio" VARCHAR(500),
  "NombreTipoTarjetaBancariaSocio" VARCHAR(500),
  "NumeroTarjetaBancariaSocio" VARCHAR(500),
  "NumeroSerieSocio" VARCHAR(500),
  "NumeroDocumentoSocio" BIGINT,
  "FechaDocumentoPagoSocio" TIMESTAMP,
  "IdPlaza" BIGINT,
  "FechaIngresoRegistro" TIMESTAMP,
  "SaldoDisponible" DECIMAL(28,16),
  "SaldoContable" DECIMAL(28,16),
  "IdUsuarioGiro" BIGINT,
  "FechaGiro" DATE,
  "IdComprobantePartida" BIGINT,
  "IdNominaTransferencia" BIGINT,
  "Depositado" VARCHAR(500),
  "IdUsuarioCertificacion" BIGINT,
  "FechaHoraCertificacion" DATE,
  "ClaveDCV" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" (
  "IdConcepto" VARCHAR(500),
  "GeneraComprobanteAutomaticamente" VARCHAR(500),
  "NoValidaSaldoCaja" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" (
  "Valor" VARCHAR(100),
  "CodigoParametro" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.CuadraturaDiariaOperaciones" (
  "IdCuadraturaDiariaOperaciones" VARCHAR(500),
  "IdEmpresa" BIGINT,
  "OperacionCuadraturaDiariaOperaciones" VARCHAR(500),
  "FechaEjecucion" DATE,
  "TotalOperaciones" BIGINT,
  "OperacionesConProblemas" BIGINT,
  "TipoOperacion" VARCHAR(500),
  "Resultado" VARCHAR(500),
  "Orden" INT,
  "DetalleCuadraturaJSON" VARCHAR(100000),
  "IdUsuario" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.CuentaBanco" (
  "IdCuentaBanco" BIGINT,
  "IdTipoCuentaBancaria" VARCHAR(500),
  "NombreCuentaBancaria" VARCHAR(500),
  "NumeroCuentaBancaria" VARCHAR(500),
  "IdBanco" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.DepositoDetalle" (
  "IdDeposito" BIGINT,
  "IdEstado" VARCHAR(500),
  "IdComprobanteFormaDePago" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.IngresoEgreso" (
  "IdIngresoEgreso" BIGINT,
  "IdEmpresa" BIGINT,
  "IdModulo" VARCHAR(500),
  "IdUsuario" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "IdConcepto" VARCHAR(500),
  "IdCaja" BIGINT,
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "FechaLiquidacion" DATE,
  "FechaIngresoRegistro" TIMESTAMP,
  "FechaMovimiento" TIMESTAMP,
  "IdTipoPersona" VARCHAR(500),
  "IdPersona" BIGINT,
  "IdPortafolio" BIGINT,
  "IdUsuarioEmision" BIGINT,
  "NombrePersona" VARCHAR(500),
  "IdTipoDocumentoIdentidad" VARCHAR(500),
  "DocumentoIdentidad" VARCHAR(500),
  "IdTipoPersonaTitularPago" VARCHAR(500),
  "IdPersonaTitularPago" BIGINT,
  "NombrePersonaTitularPago" VARCHAR(500),
  "IdPortafolioTitularPago" BIGINT,
  "IdTipoDocumentoIdentidadTitularPago" VARCHAR(500),
  "DocumentoIdentidadTitularPago" VARCHAR(500),
  "SecuenciaAgrupacion" VARCHAR(500),
  "SecuenciaRelacionada" BIGINT,
  "Glosa" VARCHAR(500),
  "IdEmpleadoDigitador" BIGINT,
  "IdSucursalEmpleadoDigitador" BIGINT,
  "IdTipoComprobanteOrigen" VARCHAR(500),
  "IdComprobanteOrigen" BIGINT,
  "FechaComprobanteOrigen" TIMESTAMP,
  "FechaLiquidacionComprobanteOrigen" TIMESTAMP,
  "IdTipoProducto" VARCHAR(500),
  "IdTipoOperacion" VARCHAR(500),
  "IdOperacion" BIGINT,
  "FechaOperacion" TIMESTAMP,
  "IdCondicionLiquidacion" VARCHAR(500),
  "IdBolsa" VARCHAR(500),
  "IdMonedaOperacion" VARCHAR(500),
  "MontoOperacion" DECIMAL(28,16),
  "FechaParidad" TIMESTAMP,
  "IdTipoReajuste" VARCHAR(500),
  "ValorParidad" DECIMAL(28,16),
  "FolioCartera" BIGINT,
  "IdInstrumento" BIGINT,
  "IdTipoInstrumento" VARCHAR(500),
  "IdClasificacionTipoInstrumento" VARCHAR(500),
  "EsBilateral" VARCHAR(500),
  "TitularPago" VARCHAR(500),
  "EsIngresoAutomatico" VARCHAR(500),
  "AgrupaComprobante" VARCHAR(500),
  "NeteaComprobante" VARCHAR(500),
  "MueveCuentaInversion" VARCHAR(500),
  "EsTraspaso" VARCHAR(500),
  "IdEstado" VARCHAR(500),
  "IdTipoCanalIngreso" VARCHAR(500),
  "FechaHoraEmision" TIMESTAMP,
  "IdUsuarioBloqueo" BIGINT,
  "IdCarga" BIGINT,
  "IdEvento" BIGINT,
  "IdUsuarioAnulacion" BIGINT,
  "FechaHoraAnulacion" TIMESTAMP
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" (
  "IdIngresoEgresoFormaDePago" BIGINT,
  "IdIngresoEgreso" BIGINT,
  "IdTipoMovimiento" VARCHAR(500),
  "IdMedioDePago" VARCHAR(500),
  "NombreMedioDePago" VARCHAR(500),
  "IdModalidadDePago" VARCHAR(500),
  "NombreModalidadDePago" VARCHAR(500),
  "IdCuentaBancariaCorredora" BIGINT,
  "IdTipoCuentaBancariaCorredora" VARCHAR(500),
  "NombreTipoCuentaBancariaCorredora" VARCHAR(500),
  "NombreCuentaBancariaCorredora" VARCHAR(500),
  "NumeroCuentaBancariaCorredora" VARCHAR(500),
  "IdTarjetaBancariaCorredora" BIGINT,
  "IdTipoTarjetaBancariaCorredora" VARCHAR(500),
  "NombreTipoTarjetaBancariaCorredora" VARCHAR(500),
  "NumeroTarjetaBancariaCorredora" VARCHAR(500),
  "IdChequera" BIGINT,
  "NumeroDocumentoCorredora" BIGINT,
  "FechaDocumentoPagoCorredora" TIMESTAMP,
  "IdCuentaBancariaSocio" BIGINT,
  "IdTipoCuentaBancariaSocio" VARCHAR(500),
  "IdBancoSocio" BIGINT,
  "NombreTipoCuentaBancariaSocio" VARCHAR(500),
  "NombreCuentaBancariaSocio" VARCHAR(500),
  "NumeroCuentaBancariaSocio" VARCHAR(500),
  "IdTarjetaBancariaSocio" BIGINT,
  "IdTipoTarjetaBancariaSocio" VARCHAR(500),
  "NombreTipoTarjetaBancariaSocio" VARCHAR(500),
  "NumeroTarjetaBancariaSocio" VARCHAR(500),
  "FechaVctoTarjetaBancariaSocio" TIMESTAMP,
  "NumeroSerieSocio" VARCHAR(500),
  "NumeroDocumentoSocio" BIGINT,
  "FechaDocumentoPagoSocio" TIMESTAMP,
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "FechaIngresoRegistro" TIMESTAMP,
  "IdEstado" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.MonedaTesoreria" (
  "IdMoneda" VARCHAR(500),
  "IdEstado" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.NominaTransferencia" (
  "IdNominaTransferencia" BIGINT,
  "IdEmpresa" BIGINT,
  "IdComprobantePartidaEgreso" BIGINT,
  "FechaMovimiento" TIMESTAMP,
  "FechaIngresoRegistro" TIMESTAMP,
  "FechaLiquidacion" DATE,
  "FechaRecepcion" TIMESTAMP,
  "IdMedioDePago" VARCHAR(500),
  "IdModalidadDePago" VARCHAR(500),
  "IdCuentaBanco" BIGINT,
  "IdTipoCuentaBanco" VARCHAR(500),
  "IdMoneda" VARCHAR(500),
  "Monto" DECIMAL(28,16),
  "NombreArchivo" VARCHAR(500),
  "IdFormatoArchivo" BIGINT,
  "IdFormatoNomina" VARCHAR(500),
  "EsNomina" VARCHAR(500),
  "EsTransferencia" VARCHAR(500),
  "IdUsuario" BIGINT,
  "IdEstado" VARCHAR(500),
  "UsaTalonarioCheque" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" (
  "IdNominaTransferenciaDetalle" VARCHAR(500),
  "IdNominaTransferencia" BIGINT,
  "IdComprobanteFormaDePago" BIGINT,
  "IdUsuario" BIGINT,
  "IdEstado" VARCHAR(500)
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.SaldoPendienteDeLiquidar_Persona_Proceso" (
  "IdPersona" BIGINT
);

CREATE TABLE "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" (
  "IdTipoMovimiento" VARCHAR(500),
  "IdMedioDePago" VARCHAR(500),
  "EstadosPosibleTipoMovimientoMedioDePago" VARCHAR(500),
  "AfectaSaldoEnEstado" VARCHAR(500),
  "AfectoADiasRetencion" VARCHAR(500)
);

CREATE TABLE "BSCL.Plataforma.BD::ACC.Usuario" (
  "IdUsuario" BIGINT,
  "IdEmpresa" BIGINT,
  "IdEntidad" BIGINT,
  "TipoEntidad" VARCHAR(3),
  "IdEstado" VARCHAR(3)
);
