--REPLICA_OPTIMUS_BCS.APV_APV------------------------------------------------------------------------------------------------------------
SELECT
	COUNT(CONCAT(CAST(IdMatrizAgrupadorAPV AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdPlanTipoInversion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombrePlan AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdPropositoPortafolio AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdProductoAgrupador AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Producto AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreAgrupador AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoAgrupador AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Codigo AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.APV_APV;
	

--REPLICA_OPTIMUS_BCS.APV_Contratos------------------------------------------------------------------------------------------------------------
SELECT
	COUNT(CONCAT(CAST(IdContratoSAAPV AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombrePlanAhorro AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FechaSuscripcion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FolioContrato AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(DocumentoIdentidadCliente AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(ApellidoPaternoCliente AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(ApellidoMaternoCliente AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdEmpleadoEjecutivo AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombrePersonaEjecutivo AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FechaNacimientoCliente AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Nacionalidad AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(EstadoCivil AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Genero AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CargoPersona AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CategoriaInversionista AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(PerfilComercial AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(EmailCliente AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(DocumentoIdentidadEmpleador AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreEncargado AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(EmailEncargado AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(TelefonoEncargado AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(MedioRecaudacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(EntidadRecaudadora AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoPrevision AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Pensionado AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Trabajador AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FormaDePago AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Recaudador AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(DiaDescuento AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Recaudacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Institucion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(ComisionTransferencia AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreEntidadOrigen AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(RutEntidadOrigen AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(TipoDeAhorro AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FondoDeOrigen AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(TipoTraspaso AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(MontoTraspaso AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(UFTraspaso AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(PorcentajeTraspaso AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Producto AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(MontoPesos AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(MontoUF AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Porcentaje AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Pago AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(PeriodoInicio AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(PeriodoTermino AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(RegimenTributario AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdMatrizAgrupadorAPV AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoAgrupador AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Proposito AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoTipoSolicitud AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(TipoMovimiento AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(DocumentoIdentidadEntidadDestino AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreEntidadDestino AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdSerieFondo AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdFondo AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FondoDestino AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(RegimenTributarioDestino AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.APV_Contratos;
	

--REPLICA_OPTIMUS_BCS.CuentaInversion_MovimientosCuentaInversion------------------------------------------------------------------------------------------------------------
SELECT
	COUNT(CONCAT(CAST(IdCargoAbonoMovimiento AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FechaMovimiento AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(EsIngresoManual AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdCuentaInversion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoCuentaInversion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreCuentaInversion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(DescripcionPortafolio AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Proposito AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoMoneda AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreMoneda AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(SimboloMoneda AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(DecimalesMoneda AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(TipoMovimiento AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Monto AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreConcepto AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Descripcion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdCondicionDeLiquidacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoCondicionDeLiquidacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreCondicionDeLiquidacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(PlazoCondicionDeLiquidacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FechaEfectivaMovimiento AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Cargo AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Abono AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(SaldoPorFechaLiquidacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(SaldoPorFechaMovimiento AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdTipoComprobante AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoTipoComprobante AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreTipoComprobante AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdComprobante AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FolioComprobante AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdComprobanteDetalle AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdComprobanteFormaDePago AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdTipoMovimientoAsociado AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdMovimientoAsociado AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoTipoOperacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreTipoOperacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdOperacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FechaLiquidacionOperacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FechaVencimientoOperacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdMonedaOperacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(MontoOperacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(TipoCambioOperacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoTipoProducto AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreTipoProducto AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreTipoInstrumento AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(EsCargoAbonoPorTraspaso AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdCargoAbonoRelacionadoTraspaso AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreClienteRelacionado AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoPortafolioRelacionado AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(EsCertificadoAutomatico AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdUsuarioCertificador AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoUsuarioCertificador AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(FechaCertificacion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdPersonaPagoCobro AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdContraparte AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(EsIndicadorBilateral AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdEmpleadoDigitador AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdEjecutivo AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdSucursal AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdModulo AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(CodigoUsuarioCreador AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreUsuario AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdCarga AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(SecuenciaMovimientoMigracion AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdTipoMovimientoFondoMutuo AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
	COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.CuentaInversion_MovimientosCuentaInversion;
	

--REPLICA_OPTIMUS_BCS.CuentaInversion_PatrimonioCajaFechaLiquidacion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CategoriaAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupadorAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlanAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Proposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoAhorro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Vigencia AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.CuentaInversion_PatrimonioCajaFechaLiquidacion;


--REPLICA_OPTIMUS_BCS.CuentaInversion_PatrimonioCajaFechaOperacion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CategoriaAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupadorAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlanAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Proposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoAhorro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Vigencia AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.CuentaInversion_PatrimonioCajaFechaOperacion;


--REPLICA_OPTIMUS_BCS.CuentaInversion_SaldoFechaEfectiva------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoCuentaInversion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaSaldo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCuentaInversion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEjecutivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEjecutivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Producto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PropositoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SaldoAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SaldoDisponible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SaldoEnGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SaldoRetiro AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.CuentaInversion_SaldoFechaEfectiva;


--REPLICA_OPTIMUS_BCS.CuentaInversion_SaldoFechaMovimiento------------------------------------------------------------------------------------------------------------
SELECT
--Respuesta: 6.116.880 - 6.117.105
COUNT(CONCAT(CAST(CodigoCuentaInversion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
--Respuesta: 6.116.880 - 6.117.105
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaSaldo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCuentaInversion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEjecutivo AS VARCHAR(200)),' ')) +
--Respuesta: 6.116.880 - 6.117.105
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEjecutivo AS VARCHAR(200)),' ')) +
--Respuesta: 6.116.880 - 6.117.105
COUNT(CONCAT(CAST(NombreMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Producto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PropositoPortafolio AS VARCHAR(200)),' ')) +
--Respuesta: 4.893.504
COUNT(CONCAT(CAST(SaldoAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SaldoDisponible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SaldoEnGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SaldoRetiro AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.CuentaInversion_SaldoFechaMovimiento;


--REPLICA_OPTIMUS_BCS.Custodia_CarteraFM------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(CantidadInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionRiesgoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CostoUF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmisionInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacionRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraRelacionada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoMovimientoFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProductoTIPOPE AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMercadoActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RentabilidadReal AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_CarteraFM;


--REPLICA_OPTIMUS_BCS.Custodia_CarteraFMHis------------------------------------------------------------------------------------------------------------
SELECT
--Resultado: 306.185.990 - 322.982.500
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(CantidadInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionRiesgoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CostoUF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmisionInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
--Resultado: 306.185.990 - 258.386.000
COUNT(CONCAT(CAST(FechaLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacionRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraRelacionada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
--Resultado: 306.185.990 - 322.982.500
COUNT(CONCAT(CAST(IdCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
--Resultado: 306.185.990 - 322.982.500
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoMovimientoFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
--Resultado: 306.185.990 - 322.982.500
COUNT(CONCAT(CAST(IdTipoProductoTIPOPE AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMercadoActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompra AS VARCHAR(200)),' ')) +
--Resultado: 122.474.396 - 129.193.000
COUNT(CONCAT(CAST(PrecioCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RentabilidadReal AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_CarteraFMHis;


--REPLICA_OPTIMUS_BCS.Custodia_CarteraOtrosProductos------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(CantidadInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionRiesgoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CobraPrimaAlInicio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoIFRS AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ConvexidadCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ConvexidadMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CorteMinimoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DurationCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DurationMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsFungible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsPuntos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEfectivaInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmisionInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaFixing AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacionRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPagoCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPrecioTasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaTasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraRelacionada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FuenteTasaPrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(HoraFixing AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFixing AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMesa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModalidadCumplimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaPagoCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPlaza AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdReferenciaMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProductoTIPOPE AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCorredoraMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCapitalCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCapitalCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDevengoTipoCambio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDevengoTipoCambioAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferido AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferidoAcumulado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferidoDiario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoInteresCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoInteresCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMargen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMatriz AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMercadoActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPerdidaDiferida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoReajusteCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoReajusteCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoUtilidadDiferida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoValorRazonableActivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoValorRazonableNeto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoValorRazonablePasivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCupon AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(PlazoEnDiasOperacionInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioAPlazoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TamanoContrato AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaPuntosMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaPuntosMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaValorRazonableActivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaValorRazonablePasivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(UsaGarantiaEspecifica AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorParidadCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorParidadCosto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescateInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCosto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCotizador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioSpot AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_CarteraOtrosProductos;


--REPLICA_OPTIMUS_BCS.Custodia_CarteraOtrosProductosHis------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(CantidadInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionRiesgoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CobraPrimaAlInicio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoIFRS AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ConvexidadCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ConvexidadMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CorteMinimoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DurationCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DurationMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsFungible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsPuntos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEfectivaInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmisionInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaFixing AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacionRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPagoCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPrecioTasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaTasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraRelacionada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FuenteTasaPrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(HoraFixing AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFixing AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMesa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModalidadCumplimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaPagoCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPlaza AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdReferenciaMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProductoTIPOPE AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCorredoraMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCapitalCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCapitalCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDevengoTipoCambio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDevengoTipoCambioAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferido AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferidoAcumulado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferidoDiario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoInteresCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoInteresCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMargen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMatriz AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMercadoActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPerdidaDiferida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoReajusteCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoReajusteCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoUtilidadDiferida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoValorRazonableActivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoValorRazonableNeto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoValorRazonablePasivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCupon AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(PlazoEnDiasOperacionInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioAPlazoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TamanoContrato AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaPuntosMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaPuntosMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaValorRazonableActivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaValorRazonablePasivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(UsaGarantiaEspecifica AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorParidadCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorParidadCosto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescateInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCosto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCotizador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioSpot AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_CarteraOtrosProductosHis;


--REPLICA_OPTIMUS_BCS.Custodia_CarteraRFIF------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(CantidadInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionRiesgoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoIFRS AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ConvexidadCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ConvexidadMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CorteMinimoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DurationCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DurationMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsFungible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEfectivaInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmisionInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacionRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPagoCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPrecioTasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaTasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraRelacionada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FuenteTasaPrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMesa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModalidadCumplimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaPagoCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPlaza AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdReferenciaMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProductoTIPOPE AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCorredoraMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCapitalCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCapitalCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferido AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferidoAcumulado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferidoDiario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoInteresCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoInteresCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMargen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMatriz AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMercadoActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPerdidaDiferida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoReajusteCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoReajusteCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoUtilidadDiferida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCupon AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(PlazoEnDiasOperacionInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioAPlazoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TamanoContrato AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaPuntosMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaPuntosMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(UsaGarantiaEspecifica AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorParidadCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorParidadCosto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescateInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCosto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCotizador AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_CarteraRFIF;


--REPLICA_OPTIMUS_BCS.Custodia_CarteraRFIFHis------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(CantidadInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionRiesgoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoIFRS AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ConvexidadCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ConvexidadMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CorteMinimoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DurationCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DurationMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsFungible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEfectivaInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmisionInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacionRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPagoCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPrecioTasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaTasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraRelacionada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FuenteTasaPrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMesa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModalidadCumplimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaPagoCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPlaza AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdReferenciaMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProductoTIPOPE AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCorredoraMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCapitalCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCapitalCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferido AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferidoAcumulado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferidoDiario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoInteresCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoInteresCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMargen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMatriz AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMercadoActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPerdidaDiferida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoReajusteCorteCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoReajusteCorteCuponMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoUtilidadDiferida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCupon AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(PlazoEnDiasOperacionInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioAPlazoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TamanoContrato AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaPuntosMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaPuntosMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(UsaGarantiaEspecifica AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorParidadCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorParidadCosto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescateInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCosto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCotizador AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_CarteraRFIFHis;


--REPLICA_OPTIMUS_BCS.Custodia_CarteraRV------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(CantidadInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionRiesgoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CobraPrimaAlInicio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CorteMinimoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CostoUF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEfectivaInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmisionInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacionRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaTasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraRelacionada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMesa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModalidadCumplimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPlaza AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdReferenciaMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoMovimientoFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProductoTIPOPE AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCorredoraMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferido AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMatriz AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMercadoActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPerdidaDiferida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoUtilidadDiferida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(PlazoEnDiasOperacionInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioAPlazoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RentabilidadReal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TamanoContrato AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(UsaGarantiaEspecifica AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescateInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCierre AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_CarteraRV;


--REPLICA_OPTIMUS_BCS.Custodia_CarteraRVHis------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(CantidadInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionRiesgoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CobraPrimaAlInicio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CorteMinimoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CostoUF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEfectivaInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmisionInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacionRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaTasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorizacionCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraRelacionada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMesa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModalidadCumplimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPlaza AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdReferenciaMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoMovimientoFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProductoTIPOPE AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAPlazoMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoATasaCorredoraMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraMonedaInstrumentoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferido AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMatriz AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMercadoActualMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPerdidaDiferida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoUtilidadDiferida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCondicionLiquidacionInicial AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(PlazoEnDiasOperacionInicial  AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioAPlazoInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioPromedioCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RentabilidadReal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TamanoContrato AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompraInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(UsaGarantiaEspecifica AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescateInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCierre AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_CarteraRVHis;


--REPLICA_OPTIMUS_BCS.Custodia_CustodiaFechaLiquidacion------------------------------------------------------------------------------------------------------------
SELECT
--1.326.560
COUNT(CONCAT(CAST(CantidadCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadSimulVen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadTransEgr AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadTransIng AS VARCHAR(200)),' ')) +
--1.326.560
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionRelacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoSimulVen AS VARCHAR(200)),' ')) +
--1.459.216
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsFungible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsPagaderoEn AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
--1.326.550
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ISIN AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPPPCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
--1.326.550
COUNT(CONCAT(CAST(NombreTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCuentaBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPSimulVen AS VARCHAR(200)),' ')) +
--1.326.550
COUNT(CONCAT(CAST(PrecioMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoSimulVen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Relacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Rentabilidad AS VARCHAR(200)),' ')) +
--1.193.895
COUNT(CONCAT(CAST(TipoFechaConsulta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalPosicion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalPosicionPresunta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoPrestamo AS VARCHAR(200)),' ')) +
--1.326.550
COUNT(CONCAT(CAST(ValorMercadoSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoSimulVen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPSimulVen AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_CustodiaFechaLiquidacion;


--REPLICA_OPTIMUS_BCS.Custodia_CustodiaFechaOperacion------------------------------------------------------------------------------------------------------------
SELECT
--1.325.920
COUNT(CONCAT(CAST(CantidadCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadSimulVen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadTransEgr AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadTransIng AS VARCHAR(200)),' ')) +
--1.325.920
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionRelacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoSimulVen AS VARCHAR(200)),' ')) +
--1.325.920
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsFungible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsPagaderoEn AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
--1.325.920
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ISIN AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPPPCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
--1.325.920
COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCuentaBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPSimulCom AS VARCHAR(200)),' ')) +
--1.325.920
COUNT(CONCAT(CAST(PPPSimulVen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoSimulVen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Relacion AS VARCHAR(200)),' ')) +
--1.325.920
COUNT(CONCAT(CAST(Rentabilidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoFechaConsulta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalPosicion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalPosicionPresunta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoPrestamo AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(ValorMercadoSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoSimulVen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPSimulVen AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_CustodiaFechaOperacion;


--REPLICA_OPTIMUS_BCS.Custodia_CustodiaHis------------------------------------------------------------------------------------------------------------
SELECT
--2.784.210
COUNT(CONCAT(CAST(CantidadCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadSimulVen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadTransEgr AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadTransIng AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionRelacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiferenciaMercadoSimulVen AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsFungible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsPagaderoEn AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaRespaldo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ISIN AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPPPCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCuentaBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PPPSimulCom AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(PPPSimulVen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoSimulVen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Relacion AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(Rentabilidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalPosicion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalPosicionPresunta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMercadoSimulCom AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(ValorMercadoSimulVen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPEmpresti AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPPactosC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPPactosV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPPrestamo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPSimulCom AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorPPPSimulVen AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_CustodiaHis;


--REPLICA_OPTIMUS_BCS.Custodia_EventoCapital------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(APagarPesosSorteoLetras AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(APagarSorteoLetras AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(BloqueoSorteoLetras AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CuentaDepositanteSorteoLetras AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Descripcion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EmisorPagadorSorteoLetras AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EmisorSorteoLetras AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsInscrito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Factor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FactorNuevo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaActualizacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaHoraIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaHoraUltimaActualizacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLimite AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(GlosaInstrumentoSorteoLetras AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(GlosaMonedaSorteoLetras AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisorNuevo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEventoCapital AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumentoNuevo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSorteoLetras AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDividendo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoEventoCapital AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(InstrumentoSorteoLetras AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NemoNuevo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEventoCapital AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroDividendo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Origen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Paridad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Secuencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SorteadoSorteoLetras AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Valor AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_EventoCapital;


--REPLICA_OPTIMUS_BCS.Custodia_EventoCapitalCliente------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CantidadDerecho AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadNueva AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoUsuarioAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Descripcion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Factor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaHoraIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaHoraUltimaActualizacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConceptoGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEventoCapital AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEventoCapitalCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumentoNuevo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSocioNegocio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoEventoCapital AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacionGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaEvento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCapital AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoInteres AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NemoNuevo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEventoCapital AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroDividendo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Origen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Paridad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Precio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioNuevo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Proposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Secuencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SecuenciaCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoSocioNegocio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Total AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Valor AS VARCHAR(200)),' ')) AS TOTALIZADO  FROM REPLICA_OPTIMUS_BCS.Custodia_EventoCapitalCliente;


--REPLICA_OPTIMUS_BCS.Custodia_Garantia------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionRiesgo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoIFRS AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CorteMinimo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionEspecie AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRCantidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRIdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsEspecifica AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsFungible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobanteOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmisionInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacionRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraGarantizada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioComprobanteOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioExternoReferencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsaOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBovedaOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumentoAval AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdContraparteGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEntidadClasificadoraRiesgo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEspecieGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumentoCarteraGarantizada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMesaOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModulo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOrigenGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoClasificacionRiesgo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobanteOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumentoAval AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumentoGarantizado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacionOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProductoOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGCantidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGDurationCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGDurationCompraProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGFechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGFechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGFechaValorizacionActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGFechaValorizacionAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGFechaValorizacionGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGFuenteTasaPrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMonto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoATasaCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoATasaCompraAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoATasaCompraProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoATasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoATasaMercadoAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoATasaMercadoProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoInteresCompraProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoInteresCorridoCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoInteresCorridoMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoReajusteCompraProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoReajusteCorridoCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoReajusteCorridoMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGPrecioGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGPrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGPrecioMercadoAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGPrecioMercadoProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGTasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGTasaMercadoProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGValorRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ISIN AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoGarantiaMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioCompraOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioMonedaInstrumentoOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoValorizado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NemotecnicoGarantizado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreClasificacionTipoInstrumentoAval AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreOrigenGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoInstrumentoAval AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlazoEnDiasOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompraOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioPromedioCompraOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RazonSocialContraparte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompraOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorClasificacionRiesgo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorNominal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCierre AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_Garantia;


--REPLICA_OPTIMUS_BCS.Custodia_GarantiaHis------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionRiesgo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoIFRS AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CorteMinimo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionEspecie AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRCantidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRFechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRIdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsEspecifica AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsFungible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobanteOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmisionInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacionRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaRespaldo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraGarantizada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioComprobanteOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioExternoReferencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsaOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBovedaOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumentoAval AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdContraparteGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEntidadClasificadoraRiesgo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEspecieGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumentoCarteraGarantizada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMesaOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModulo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOrigenGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoClasificacionRiesgo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobanteOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumentoAval AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumentoGarantizado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacionOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProductoOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGCantidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGDurationCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGDurationCompraProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGFechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGFechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGFechaValorizacionActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGFechaValorizacionAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGFechaValorizacionGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGFuenteTasaPrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMonto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoATasaCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoATasaCompraAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoATasaCompraProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoATasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoATasaMercadoAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoATasaMercadoProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoInteresCompraProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoInteresCorridoCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoInteresCorridoMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoReajusteCompraProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoReajusteCorridoCompraActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGMontoReajusteCorridoMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGPrecioGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGPrecioMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGPrecioMercadoAnterior AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGPrecioMercadoProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGTasaMercadoActual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGTasaMercadoProximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(INGValorRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ISIN AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoGarantiaMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioCompraOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPrecioPromedioMonedaInstrumentoOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoValorizado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NemotecnicoGarantizado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreClasificacionTipoInstrumentoAval AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreOrigenGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoInstrumentoAval AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlazoEnDiasOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompraOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioPromedioCompraOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RazonSocialContraparte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompraOperacionOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorClasificacionRiesgo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorNominal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTipoCambioCierre AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_GarantiaHis;


--REPLICA_OPTIMUS_BCS.Custodia_MovimientoCarteraEgresos------------------------------------------------------------------------------------------------------------
SELECT
--Resultado: 29.842.390 - 29.822.750
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CostoUF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRSpread AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
--Resultado: 29.842.390 - 29.822.750
COUNT(CONCAT(CAST(FolioComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
--Resultado: 26.858.151 - 26.840.475
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoMovimientoFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ISIN AS VARCHAR(200)),' ')) +
--Resultado: 23.873.912 - 23.858.200
COUNT(CONCAT(CAST(Monto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Precio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RentabilidadReal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SaldoCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Tasa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescate AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_MovimientoCarteraEgresos;


--REPLICA_OPTIMUS_BCS.Custodia_MovimientoCarteraIngresos------------------------------------------------------------------------------------------------------------
SELECT
--Resultado: 45.735.550 - 45.735.790
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CostoUF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EGRSpread AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
--Resultado: 45.735.550 - 45.735.800
COUNT(CONCAT(CAST(FolioComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBoveda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
--Resultado: 45.735.550 - 45.735.800
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoMovimientoFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ISIN AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Monto AS VARCHAR(200)),' ')) +
--Resultado: 32.014.885 - 32.015.060
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Precio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RentabilidadReal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SaldoCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Tasa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescate AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_MovimientoCarteraIngresos;


--REPLICA_OPTIMUS_BCS.Custodia_PatrimonioConsolidadoFechaLiquidacion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CantidadPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CategoriaAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Fondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupadorAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlanAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Proposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Serie AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoAhorro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCuota AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Vigencia AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_PatrimonioConsolidadoFechaLiquidacion;


--REPLICA_OPTIMUS_BCS.Custodia_PatrimonioConsolidadoFechaOperacion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CantidadPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CategoriaAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Fondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupadorAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlanAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Proposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Serie AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoAhorro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCuota AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Vigencia AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_PatrimonioConsolidadoFechaOperacion;


--REPLICA_OPTIMUS_BCS.Custodia_PatrimonioCustodiaFechaLiquidacion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CantidadPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CategoriaAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Fondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupadorAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlanAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Proposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Serie AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoAhorro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCuota AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Vigencia AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_PatrimonioCustodiaFechaLiquidacion;


--REPLICA_OPTIMUS_BCS.Custodia_PatrimonioCustodiaFechaOperacion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CantidadPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CategoriaAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Fondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupadorAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPatrimonio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlanAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Proposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Serie AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoAhorro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCuota AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Vigencia AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Custodia_PatrimonioCustodiaFechaOperacion;


--REPLICA_OPTIMUS_BCS.Facturacion_Comprobante------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(AccionTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DecimalesMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DireccionFacturacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsExento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobanteReferencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmision AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEnvioDTE AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioComprobanteReferencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Glosa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobanteReferencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpleadoEjecutivoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoDTE AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoSII AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursalEmpleadoEjecutivoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobanteReferencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoFinalMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEmpleadoEjecutivoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstadoDTE AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstadoSII AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreLargoCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreSucursalEmpleadoEjecutivoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Proposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SimboloMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoDocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalCobrosMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalCobrosMonedaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalCobrosMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalComisionMonedaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalDerechoMonedaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalGastoMonedaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalImpuestoMonedaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TotalRetencionMonedaOperacion AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Facturacion_Comprobante;


--REPLICA_OPTIMUS_BCS.Facturacion_ComprobanteCobro------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoCobroClasificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DecimalesMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FormaCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCobroClasificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobanteCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaCobroLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaCobroOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaCobroSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCobroMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCobroMonedaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCobroMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Porcentaje AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Facturacion_ComprobanteCobro;


--REPLICA_OPTIMUS_BCS.Facturacion_ComprobanteDetalle------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobanteDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoComprobanteDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroLinea AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Precio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Tasa AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Facturacion_ComprobanteDetalle;


--REPLICA_OPTIMUS_BCS.Facturacion_ComprobanteFacturacionFormaPago------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoBanco AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaDocumentoPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVctoTarjetaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBanco AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobanteFormaDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCuentaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMedioDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModalidadDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTarjetaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCuentaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoTarjetaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBanco AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCuentaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMedioDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreModalidadDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoCuentaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoTarjetaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCuentaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroDocumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroSerie AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroTarjetaBancaria AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Facturacion_ComprobanteFacturacionFormaPago;


--REPLICA_OPTIMUS_BCS.FondoMutuo_ContratoSuscripcionCuotas------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
--COUNT(CONCAT(CAST(DiasDescuento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Estado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Fecha AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Folio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioRelacionado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Hora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFolioCSC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPesos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoUF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBanco AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMedioPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoPago AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.FondoMutuo_ContratoSuscripcionCuotas;


--REPLICA_OPTIMUS_BCS.FondoMutuo_ContratoSuscripcionCuotasDetalle------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(IdCSCDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFolioCSC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSerieFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Monto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Porcentaje AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Regimen AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.FondoMutuo_ContratoSuscripcionCuotasDetalle;


--REPLICA_OPTIMUS_BCS.FondoMutuo_Fondo------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(BloqueoAporte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(BloqueoIndefinido AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(BloqueoRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CategoriaPerfilFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoSVS AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidadFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoAporteBackoffice AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoAporteComercial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoLiquidezBackoffice AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoLiquidezComercial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoReinversionBackoffice AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoReinversionComercial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoRescateBackoffice AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoRescateComercial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaBloqueoDesde AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaBloqueoHasta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaConstitucion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FondoExtranjero AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(HoraCierreAporteBackoffice AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(HoraCierreAporteComercial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(HoraCierreLiquidezBackoffice AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(HoraCierreLiquidezComercial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(HoraCierreReinversionBackoffice AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(HoraCierreReinversionComercial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(HoraCierreRescateBackoffice AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(HoraCierreRescateComercial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPaisEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPaisOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MoneyMarket AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Vigencia AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.FondoMutuo_Fondo;


--REPLICA_OPTIMUS_BCS.FondoMutuo_RegimenSerieFondo------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(DescripcionRegimen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(GrupoRegimen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoRegimenFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoRegimenSerieDeFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSerieFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PermiteAporte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PermiteCambioGrupo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PermiteOtrosGrupos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PermiteRescate AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.FondoMutuo_RegimenSerieFondo;


--REPLICA_OPTIMUS_BCS.FondoMutuo_Reinversion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Canal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CostoUF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Estado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCertificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCurse AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionTesoreria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioContrato AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioOmnibus AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FormaDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisorDestino AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoSpot AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoTesoreriaSpot AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdIngresoEgreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOperacionSpot AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSerieFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSerieFondoDestino AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSolicitudAporteRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoMovimientoFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacionSpot AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Monto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaSistema AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreSerieFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCuotas AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Producto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RentabilidadReal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Spread AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Switch AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoCambio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoSolicitud AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCuota AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.FondoMutuo_Reinversion;


--REPLICA_OPTIMUS_BCS.FondoMutuo_ReinversionSerieFondoDestino------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdReinversionSerieFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSerieFondoDestino AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSolicitudAporte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSolicitudRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoMovimientoAporte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoMovimientoRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAporte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreSerieFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Porcentaje AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.FondoMutuo_ReinversionSerieFondoDestino;


--REPLICA_OPTIMUS_BCS.FondoMutuo_SerieDeFondo------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Alias AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CambioFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CobraComision AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoSerie AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DecimalesCuota AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DecimalesValorCuota AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasRetencionesAporte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasRetencionesRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasValutaCertificacionAporte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasValutaCertificacionRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasValutaCurseAporte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasValutaCurseRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasValutaValorCuotaAporte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasValutaValorCuotaRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Diferido AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Estado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ExcluyeGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorCuota AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSerieFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSerieFondoOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(InstrumentoDCV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ISIN AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MedioPagoAbono AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MedioPagoCargo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMaximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMinimo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreFamilia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreSerieFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Omnibus AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PatrimonioMinimo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCuota AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.FondoMutuo_SerieDeFondo;


--REPLICA_OPTIMUS_BCS.FondoMutuo_SolicitudAporteRescateFondoMutuo------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CalculoPorMontoCuota AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CobraComision AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoSerie AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoSVS AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ComisionInformada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CostoMonedaLocal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CostoMonedaLocalInversion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CostoUF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DecimalesCuota AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DecimalesValorCuota AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(Descripcion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionTipoMovimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionTipoMovimientoRelacionado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasRetencionesRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Diferido AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Estado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ExcluyeGarantia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaAceptacion AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(FechaAnulacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCertificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaConstitucion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmision AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCategoriaPerfilFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClienteAGF AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(IdClienteDestino AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDividendo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisorDestino AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMedioDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMedioDePagoAbonoAGF AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(IdMedioDePagoCargoAGF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPaisOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersonaAGF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolioAGF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolioDestino AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRegimenTributario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSerieFondo AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(IdSerieFondoDestino AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSerieFondoOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSolicitudAporteRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSolicitudAporteRescateRelacionado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoMovimientoFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoMovimientoFondoMutuoRelacionado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +

COUNT(CONCAT(CAST(IndicadorMLT AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MoneyMarket AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Monto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoComision AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoComisionLocal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoInversion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMaximo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMinimo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPagoLocal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCorto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEmpleado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstadoLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreSerieFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCuotas AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCuotasInversion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Omnibus AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(OrigenOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Overnight AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ParidadFechaCertificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ParidadFechaInversion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RentabilidadReal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SolicitudBatch AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TextoPreferenciaPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoSolicitud AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCuota AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCuotaInversion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorUFFechaInversion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Vigencia AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.FondoMutuo_SolicitudAporteRescateFondoMutuo;
select count(1) from REPLICA_OPTIMUS_BCS.FondoMutuo_SolicitudAporteRescateFondoMutuo;

--REPLICA_OPTIMUS_BCS.FondoMutuo_TipoMovimientoFondoMutuo-------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(IdTipoMovimientoFondoMutuo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Descripcion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.FondoMutuo_TipoMovimientoFondoMutuo;

--REPLICA_OPTIMUS_BCS.FondoMutuo_ValorCuotaHistorico------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoSerie AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaValorCuota AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSerieFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdValorCuotaHistorico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreSerieFondo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCuota AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.FondoMutuo_ValorCuotaHistorico;


--REPLICA_OPTIMUS_BCS.Mesa_MovimientosFWD------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoUsuarioCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidadCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaAnticipo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEfectiva AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaFixing AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionEntregamos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionRecibimos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFixing AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMedioPagoEntregamos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMedioPagoRecibimos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMesa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPlaza AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Margen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Modalidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDiferido AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoEquivalente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPagoEntregamos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPagoRecibimos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreFixing AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMedioPagoEntregamos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMedioPagoRecibimos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMesa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreModalidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePlaza AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ParidadCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ParidadCosto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Plazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlazoResidual AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TCForward AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoCambioCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoCambioCosto AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Mesa_MovimientosFWD;


--REPLICA_OPTIMUS_BCS.Mesa_MovimientosSpot------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoComercio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidadCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionEntregamos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionRecibimos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobanteFacturacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMedioPagoEntregamos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMedioPagoRecibimos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMesa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSolicitudAporteRescate AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Margen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMonedaSecundaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPagoEntregamos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPagoRecibimos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCodigoComercio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMedioPagoEntregamos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMedioPagoRecibimos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMesa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(OperacionFicticia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(OrigenForward AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ParidadCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ParidadCosto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Producto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PuntaCompra AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PuntaVenta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoCambioCierre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoCambioCosto AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Mesa_MovimientosSpot;


--REPLICA_OPTIMUS_BCS.Mesa_Pactos------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(AfectaCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPropositoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCondicionLiquidacionAnticipo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Glosa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAgente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClienteCorredor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacionAnticipo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMesaDestino AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMesaOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaLiquidacionAnticipo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolioCorredor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSocioDeNegocio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Intermesa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoOperacionFinal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoOperacionFinalAnticipo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoOperacionFinalMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoOperacionFinalMonedaAnticipo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoOperacionInicial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoOperacionInicialAnticipo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoOperacionInicialMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoOperacionInicialMonedaAnticipo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCondicionLiquidacionAnticipo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEjecutivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMesaDestino AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMesaOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlazoEnDias AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Tasa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaAnticipo AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Mesa_Pactos;


--REPLICA_OPTIMUS_BCS.Mesa_PactosDetalle------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CantidadDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CarteraNominal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClasificacionTipoInstrumentoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoDescIFRSDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoIFRSDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsFungibleDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraCustodiaDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCarteraDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsaDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBovedaDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumentoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConceptoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisorDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumentoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaEmisionInstrumentoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaLiquidacionInstrumentoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOperacionDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdReservaDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumentoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajusteDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCompraDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoLiquidacionDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoValorizadoReserva AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NemotecnicoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBovedaDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioCompraDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercadoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaCompraDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaMercadoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescateDetalle AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Mesa_PactosDetalle;


--REPLICA_OPTIMUS_BCS.Orden_Asignacion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(ApellidoMaterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ApellidoPaterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadAsignada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CantidadTransaccion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoCorredorContraparte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsAPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsFungible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsVentaCorta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaAsignacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacionOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacionTransaccion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVigenciaOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioTransaccion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioTransaccionOD AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(GlosaOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAsignacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsaTransaccion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionDeLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCorredorContraparte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoAsignacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoTransaccion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumentoSubyacente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdLibroOrdenes AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaSubyacente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMovimientoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOperadorContraparte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOperadorCorredor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPorcentajeVisibilidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolioCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPrecioOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoVigenciaOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTransaccion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTransaccionOd AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTransaccionPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IndicadorDCV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IndicadorOrdenDirecta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAsignado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAsignadoLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoAsignadoLocal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoTransaccion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NemotecnicoSubyacente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreLibroOrdenes AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombres AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Plazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioTransaccion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PropositoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaTransaccion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoCambio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescateAsignado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescateOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescateTransaccion AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Orden_Asignacion;


--REPLICA_OPTIMUS_BCS.Orden_CobroAsignacion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoCobroClasificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FormaCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdAsignacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCarga AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCobroAsignacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCobroCalculado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCobroCalculadoLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCobroCalculadoLocal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCobroAplicado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCobroPerfil AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Orden_CobroAsignacion;


--REPLICA_OPTIMUS_BCS.Orden_CobroOrden------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoCobroClasificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FormaCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCarga AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCobroOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdGrupoCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCobroCalculado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCobroCalculadoLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoCobroCalculadoLocal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoMinimo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCobroAplicado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCobroPerfil AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Orden_CobroOrden;


--REPLICA_OPTIMUS_BCS.Orden_Orden------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(ApellidoMaterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ApellidoPaterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Bloqueado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Cantidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoClaseOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoProcedenciaOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ComentarioBloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ComentarioDesbloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsFungible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EspecificaBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsVentaCorta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaAceptacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaAnulacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaAutorizacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmision AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistroCorta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOferta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaRecepcion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaSolicitudAnulacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVigencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Folio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Glosa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsaDistribucion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCarga AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionDeLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDireccionEnvioCorrespondencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpleadoDigitador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpleadoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoAsignacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdFormaCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumentoSubyacente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdLibroOrdenes AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMedioDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaLiquidacionCobros AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaLiquidacionOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaLocal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilCobros AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilComercialBaseOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilLimites AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilProductos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPorcentajeVisibilidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolioCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTelefonoContacto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoBloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalDistribucion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCustodia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDesbloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPrecio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoTasa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoVigenciaOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioBloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioReserva AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IndicadorFacturacionInmediata AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ISIN AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ISINSubyacente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoLocal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MotivoBloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MotivoReserva AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NemotecnicoSubyacente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstadoOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombres AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreUsuarioBloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreUsuarioDigitador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreUsuarioPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreUsuarioReserva AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlazoIF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlazoSimultanea AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PlazoVentaCorta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioMercado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioOrden AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PropositoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Reservado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SecOrdenMigracion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaRFIF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaSimultanea AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaVentaCorta AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoCambio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorRescate AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Orden_Orden;


--REPLICA_OPTIMUS_BCS.Plataforma_Banco------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoBanco AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBanco AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPaisOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoBanco AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBanco AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_Banco;


--REPLICA_OPTIMUS_BCS.Plataforma_Ciudad------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCiudad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCiudad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreClasificacionDivisionPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreDivisionPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreDivisionPais_1 AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoClasificacionDivisionPais AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_Ciudad;


--REPLICA_OPTIMUS_BCS.Plataforma_ClasificacionDivisionPais------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(IdClasificacionDivisionPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreClasificacionDivisionPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoClasificacionDivisionPais AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_ClasificacionDivisionPais;


--REPLICA_OPTIMUS_BCS.Plataforma_ClasificacionTipoInstrumento------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Codigo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Descripcion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombre AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_ClasificacionTipoInstrumento;


--REPLICA_OPTIMUS_BCS.Plataforma_Concepto------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoConcepto AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_Concepto;


--REPLICA_OPTIMUS_BCS.Plataforma_DivisionPais------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(IdCiudad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacion1 AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacion2 AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDivisionPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreDivisionPais AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_DivisionPais;


--REPLICA_OPTIMUS_BCS.Plataforma_Empresa------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Codigo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSocioDeNegocio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombre AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_Empresa;


--REPLICA_OPTIMUS_BCS.Plataforma_Estado------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(ID AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Modulo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEntidad AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_Estado;


--REPLICA_OPTIMUS_BCS.Plataforma_Giro------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Codigo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdGiro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombre AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_Giro;


--REPLICA_OPTIMUS_BCS.Plataforma_Instrumento------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(ClasificacionRiesgoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoRubro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CorteMinimo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DecimalesPrecio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsDivisible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstaInscrito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmision AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaInicioVigencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaReferencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FrecuenciaCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumentoRelacionado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPaisEmisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IndArt104 AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IndicadorBolson AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IndicadorLiquidaDCV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ISIN AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCodigoRubro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCupones AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Precio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PrecioReferencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PresenciaBursatilAjustada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RazonSocial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Reajustable AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Serie AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Tamano AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Tasa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TasaFiscal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TieneCupon AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoValorizacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorizaPor AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_Instrumento;

--REPLICA_OPTIMUS_BCS.Plataforma_MatrizComercialCorredora------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoCobroClasificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionPerfilComercialBase AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FormaCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCategoriaInversionista AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCobroClasificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilComercialBase AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Prioridad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTopeMinimoOrden AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_MatrizComercialCorredora;


--REPLICA_OPTIMUS_BCS.Plataforma_MatrizComercialPortafolios------------------------------------------------------------------------------------------------------------
SELECT
--Respuesta: 15.021.410 - 15.089.760
COUNT(CONCAT(CAST(CanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoCobroClasificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsExtraCobros AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsExtraProductos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FormaCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCanalIngreso AS VARCHAR(200)),' ')) +
--Respuesta: 15.021.410 - 15.089.760
COUNT(CONCAT(CAST(IdCategoriaInversionista AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCobroClasificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdValorCobro AS VARCHAR(200)),' ')) +
--Respuesta: 10.514.987 - 10.562.832
COUNT(CONCAT(CAST(NombreCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Prioridad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorTopeMinimoOrden AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_MatrizComercialPortafolios;


--REPLICA_OPTIMUS_BCS.Plataforma_ModeloComercial------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(DescripcionPerfilComercialBase AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCategoriaInversionista AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModeloFacturacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilCobros AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilComercialBase AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilCondicionesOperativas AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilLimites AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilProductos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(PorDefecto AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_ModeloComercial;


--REPLICA_OPTIMUS_BCS.Plataforma_Moneda------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Codigo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoNumerico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Simbolo AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_Moneda;


--REPLICA_OPTIMUS_BCS.Plataforma_Pais------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePais AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_Pais;


--REPLICA_OPTIMUS_BCS.Plataforma_ParametroFinanciero------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Agno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(AgnoMes AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Fecha AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaActualizacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaParametro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaValor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdParametroFinanciero AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombre AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Periodo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Valor AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_ParametroFinanciero;


--REPLICA_OPTIMUS_BCS.Plataforma_PerfilCobro------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(DescripcionPerfilCobros AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasPlazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacionPerfilCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoPerfilCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacionPerfilCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDetallePerfilCobros AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoPerfilCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilCobros AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdValorCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Prioridad AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_PerfilCobro;


--REPLICA_OPTIMUS_BCS.Plataforma_PerfilProducto------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(DescripcionPerfilProductos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCanalIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCategoriaInversionista AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDetallePerfilProductos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilProductos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_PerfilProducto;


--REPLICA_OPTIMUS_BCS.Plataforma_Profesion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdProfesion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombre AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_Profesion;


--REPLICA_OPTIMUS_BCS.Plataforma_TipoIndicador------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CantidadDecimales AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoEntidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(LongitudMaxima AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(LongitudMinima AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorIndicadorDefecto AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_TipoIndicador;


--REPLICA_OPTIMUS_BCS.Plataforma_TipoInstrumento------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Codigo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombre AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_TipoInstrumento;


--REPLICA_OPTIMUS_BCS.Plataforma_TipoOperacion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Accion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Codigo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacionRelacionada AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombre AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_TipoOperacion;


--REPLICA_OPTIMUS_BCS.Plataforma_TipoProducto------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Codigo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombre AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_TipoProducto;

--REPLICA_OPTIMUS_BCS.Plataforma_ValorCobro--------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(IdValorCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionValorCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorCobro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorMinimo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Plataforma_ValorCobro;

--REPLICA_OPTIMUS_BCS.Portafolio_Portafolio------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(ALIASGRUPO AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ApellidoMaterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ApellidoPaterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ComentarioBloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ComentarioDesbloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaBloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCambioPerfil AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaDesbloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDireccionEnvioCorrespondencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEjecutivoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilCobros AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilComercialBaseOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilLimites AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPerfilProductos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTelefonoContacto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoBloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDesbloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioModificacionPerfil AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombres AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Proposito AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Portafolio_Portafolio;


--REPLICA_OPTIMUS_BCS.SocioNegocio_Cliente------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Alias AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ApellidoMaterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ApellidoPaterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Borrado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Cargo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoEjecutivoExterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoEmpleado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoCargoEmpleado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ComentarioBloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ComentarioDesbloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionSegmento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DetalleCargo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Empleador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsEnrolado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsExtranjero AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoCivil AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaBloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaDefuncion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaDesbloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaInicioActividad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaInicioConyuge AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaInicioRepresentante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaNacimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(GlobalLegalEntity AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCarga AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCategoriaInversionista AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDireccionElectronicaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDireccionFisicaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpleado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdGiroPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdNombreFantasiaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPaisNacimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPaisNacionalidadPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPaisResidencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersonaConyuge AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersonaEjecutivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersonaEmpleador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersonaRepresentante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolioDefecto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdProfesion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSegmentoCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTelefonoPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoBloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDesbloqueo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioCreador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombres AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RazonSocial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RegimenConyugal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Sexo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Tipo AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_Cliente;


--REPLICA_OPTIMUS_BCS.SocioNegocio_DetalleIndicador------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Codigo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDetalleIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorIndicador AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_DetalleIndicador;


--REPLICA_OPTIMUS_BCS.SocioNegocio_Ejecutivo------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoEmpleado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoEmpleadoReemplazante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoEmpleadoSupervisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaFinReemplazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaInicioReemplazo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpleadoEjecutivo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpleadoReemplaza AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpleadoSupervisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersonaEmpleado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompletoReemplazante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompletoSupevisor AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_Ejecutivo;


--REPLICA_OPTIMUS_BCS.SocioNegocio_Empleado------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoEmpleado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoCargoEmpleado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaInicioCargo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaModificacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaTerminoCargo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpleado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstadoEmpleado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoCargoEmpleado AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_Empleado;


--REPLICA_OPTIMUS_BCS.SocioNegocio_IndicadorCliente------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(ClaseDato AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoValorIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(GrupoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoEntidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(LongitudMaxima AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(LongitudMinima AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ModeloCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreValorIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorIndicador AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_IndicadorCliente;


--REPLICA_OPTIMUS_BCS.SocioNegocio_IndicadorPersona------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CantidadDecimales AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClaseDato AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoValorIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(GrupoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoEntidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(LongitudMaxima AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(LongitudMinima AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreValorIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorIndicador AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_IndicadorPersona;


--REPLICA_OPTIMUS_BCS.SocioNegocio_IndicadorPortafolio------------------------------------------------------------------------------------------------------------
SELECT
--Respuesta: 57.496.557
COUNT(CONCAT(CAST(CantidadDecimales AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ClaseDato AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoIndicador AS VARCHAR(200)),' ')) +
--Respuesta: 57.496.557
COUNT(CONCAT(CAST(CodigoValorIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(GrupoIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCliente AS VARCHAR(200)),' ')) +
--Respuesta: 57.496.557
COUNT(CONCAT(CAST(IdIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
--Respuesta: 57.496.557
COUNT(CONCAT(CAST(IdTipoCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoEntidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoIndicador AS VARCHAR(200)),' ')) +
--Respuesta: 57.496.557
COUNT(CONCAT(CAST(IdTipoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(LongitudMaxima AS VARCHAR(200)),' ')) +
--Respuesta: 57.496.557
COUNT(CONCAT(CAST(LongitudMinima AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ModeloCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoIndicador AS VARCHAR(200)),' ')) +
--Respuesta: 38.331.038
COUNT(CONCAT(CAST(NombreValorIndicador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorIndicador AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_IndicadorPortafolio;


--REPLICA_OPTIMUS_BCS.SocioNegocio_Persona------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(AliasPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ApellidoMaterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ApellidoPaterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Cargo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionTipoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Empleador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsContraparte AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsEmpleado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsExtranjero AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsProveedor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoCivil AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaDefuncion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaEmisionDocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaInicioActividad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaNacimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaVencimientoDocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDireccionElectronicaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDireccionFisicaPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoDocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdGiroPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPaisNacimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPaisResidencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdProfesion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTelefonoPrincipal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombres AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroSerie AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RazonSocial AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RegimenConyugal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Sexo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoPersona AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_Persona;


--REPLICA_OPTIMUS_BCS.SocioNegocio_PersonaCuentaBancaria------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoBanco AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBanco AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCuentaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCuentaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBanco AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoCuentaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCuentaBancaria AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_PersonaCuentaBancaria;


--REPLICA_OPTIMUS_BCS.SocioNegocio_PersonaDireccionElectronica------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoProposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Comentario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionProposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Direccion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDireccionElectronica AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioCreador AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_PersonaDireccionElectronica;


--REPLICA_OPTIMUS_BCS.SocioNegocio_PersonaDireccionFisica------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Calle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPostal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoProposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Comentario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Departamento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionProposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCiudad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDireccionFisica AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDivisionPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCiudad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Numero AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Sector AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_PersonaDireccionFisica;


--REPLICA_OPTIMUS_BCS.SocioNegocio_PersonaNombreFantasia------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(DescripcionNombreFantasia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdNombreFantasia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_PersonaNombreFantasia;


--REPLICA_OPTIMUS_BCS.SocioNegocio_PersonaRelacion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Administracion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Alias AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ApellidoMaterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ApellidoPaterno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoTipoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidadDefecto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaFinRelacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaInicioRelacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaNacimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPaisResidencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersonaDestino AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersonaOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdRelacionPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDocumentoIdentidadDefecto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoRelacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nombres AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoRelacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(RazonSocial AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_PersonaRelacion;


--REPLICA_OPTIMUS_BCS.SocioNegocio_PersonaTelefono------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Anexo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoProposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Comentario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DescripcionProposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPais AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTelefono AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoTelefono AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoTelefono AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Numero AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_PersonaTelefono;


--REPLICA_OPTIMUS_BCS.SocioNegocio_Supervisor------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoEmpleado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaCreacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpleadoSupervisor AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_Supervisor;


--REPLICA_OPTIMUS_BCS.SocioNegocio_TipoRelacion------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Administracion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsIndefinida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsPersonaNatural AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoEntidadDestino AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoEntidadOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoRelacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoRelacionDestino AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoRelacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TextoUsaPorcentajeParticipacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(UsaPorcentajeParticipacion AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.SocioNegocio_TipoRelacion;


--REPLICA_OPTIMUS_BCS.Tesoreria_Comprobante------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoCaja AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolioPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoUsuarioAnulacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ComprobanteNeteado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DecimalesMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidadPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidadTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsBilateral AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaHoraAnulacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCaja AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMedioDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModalidadDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolioTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDocumentoIdentidadPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDocumentoIdentidadTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioAnulacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Monto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoConSigno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoEgreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCaja AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMedioDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreModalidadDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePortafolioPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreUsuarioLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SecuenciaAgrupacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SeparadoPorConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoPersona AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Tesoreria_Comprobante;


--REPLICA_OPTIMUS_BCS.Tesoreria_ComprobanteDetalle------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(ComprobanteNeteado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsBilateral AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoComprobanteDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobanteOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionComprobanteOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaParidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCaja AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCarga AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobanteDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobanteOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConceptoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConceptoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpleadoDigitador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMovimientoRelacionado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersonaTitularPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolioTitularPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursalEmpleadoDigitador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobanteOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPersonaTitularPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuario AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConceptoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConceptoDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstadoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstadoComprobanteDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMonedaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMonedaDetalle AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePersonaTitularPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroLinea AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SecuenciaAgrupacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SeparadoPorConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoMovimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorParidad AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Tesoreria_ComprobanteDetalle;


--REPLICA_OPTIMUS_BCS.Tesoreria_ComprobanteFormaDePago------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(AfectoSaldo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoCaja AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolioPersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DecimalesMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Depositado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DiasRetencion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoEnviaADeposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidadPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaAfectaSaldo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaDocumentoPagoCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaDocumentoPagoTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaGiro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaHoraCertificacionMedioPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaHoraLiquidacionMedioPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBancoCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBancoTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCaja AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdChequeraCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobanteFormaDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobantePartida AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConceptoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCuentaBancariaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCuentaBancariaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdDeposito AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstadoMedioPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMedioDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModalidadDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdNominaTransferencia AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolioPersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTarjetaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTarjetaBancariaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCuentaBancariaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoCuentaBancariaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDocumentoIdentidadPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoSocioNegocioPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoSocioNegocioPersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoTarjetaBancariaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoTarjetaBancariaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioCertificacionMedioPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioGiro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioLiquidacionMedioPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Monto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoConSigno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoEgreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBancoCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBancoTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCaja AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConceptoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCuentaBancariaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCuentaBancariaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstadoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstadoMedioPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMedioDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreModalidadDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePortafolioPersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoCuentaBancariaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoCuentaBancariaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoPersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoSocioNegocioPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoSocioNegocioPersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoTarjetaBancariaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoTarjetaBancariaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreUsuarioCertificacionMedioPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreUsuarioGiro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreUsuarioLiquidacionMedioPago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCuentaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCuentaBancariaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroDocumentoCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroDocumentoTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroSerieCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroSerieTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroTarjetaBancariaCorredora AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroTarjetaBancariaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SaldoContable AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SaldoDisponible AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoMedioDePago AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Tesoreria_ComprobanteFormaDePago;


--REPLICA_OPTIMUS_BCS.Tesoreria_IngresoEgreso------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(CodigoPortafolioTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DecimalesMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidadPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidadTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsBilateral AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaComprobanteOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaHoraAnulacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaHoraEmisionComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaIngresoRegistro AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaLiquidacionComprobanteOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaMovimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FechaParidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCartera AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Glosa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdBolsa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCaja AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCarga AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClasificacionTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdClientePersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdComprobanteOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCondicionLiquidacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpleadoDigitador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdIngresoEgreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMatrizAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMedioDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModalidadDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdModuloEnviaMovimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMonedaOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMovimientoRelacionado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolioPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolioTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSocioDeNegocioPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdSucursalEmpleadoDigitador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoComprobanteOrigen AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDocumentoIdentidadPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoDocumentoIdentidadTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoInstrumento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoPersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdTipoReajuste AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioAnulacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioEmisionComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdUsuarioEnviaMovimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Monto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoConSigno AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoEgreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoIngreso AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Nemotecnico AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCaja AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreConcepto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEmpresa AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMedioDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreModalidadDePago AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePersonaTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePortafolioPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePortafolioTitular AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoOperacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoProducto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreUsuarioEmisionComprobante AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(SecuenciaAgrupacion AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoMovimiento AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(TipoPersona AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(ValorParidad AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Tesoreria_IngresoEgreso;


--REPLICA_OPTIMUS_BCS.Tesoreria_MandatoPAC------------------------------------------------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(Canal AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(DocumentoIdentidad AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Estado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoCargo AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EstadoCliente AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Fecha AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Firma AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioCSC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioPACMandatoAgrupador AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(FolioSAAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdCuentaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdMandatoPAC AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoPesos AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(MontoUF AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreBanco AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreCompleto AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreMoneda AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombrePlaza AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NombreTipoCtaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NroMandato AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(NumeroCtaBancaria AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(Receptor AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.Tesoreria_MandatoPAC;

--REPLICA_OPTIMUS_BCS.FondoMutuo_NOAPV--------------------------------------------------------------------
SELECT
COUNT(CONCAT(CAST(IdMatrizAgrupadorNOAPV AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(CodigoPortafolio AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(IdEstado AS VARCHAR(200)),' ')) +
COUNT(CONCAT(CAST(EsADC AS VARCHAR(200)),' ')) AS TOTALIZADO FROM REPLICA_OPTIMUS_BCS.FondoMutuo_NOAPV;