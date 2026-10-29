INSERT INTO "BSCL.Mesa.BD::SPT.OperacionSpot"(
	 "IdOperacionSpot","IdEmpresa","IdCliente","IdPortafolio","IdInstrumento","IdTipoOperacion","FechaOperacion","FechaLiquidacion"
	,"IdMonedaPrincipal","IdMonedaSecundaria","IdMonedaLocal","IdMonedaEquivalente","MontoMonedaPrincipal","MontoMonedaPrincipalEquivalente","MontoMonedaSecundaria"
	,"TipoCambioCierre","TipoCambioCierreForward","ParidadCierre","ParidadCierreForward","TipoCambioCosto","ParidadCosto"
	,"MontoEquivalente","MontoRentabilidad","MontoRentabilidadCanal","ResultadoMonedaLocalPuntas","ResultadoMonedaLocalBidAsk","ResultadoMonedaLocalDO"
	,"ResultadoMonedaLocalTCSpot","ResultadoMonedaLocalTCCierreMesa","ResultadoMonedaLocalConsolidado","Margen","MargenBidAsk","Utilidad"
	,"PuntaCompra","PuntaVenta","PuntaBid","PuntaAsk","ParidadBid","ParidadAsk","ValorDolarObservado","ValorTCSpot","SolicitudTipoCambio","IdEvento"
	,"IdMesa","IdModulo","IdTipoDolar","TipoComprobanteFacturacion","IdComprobanteFacturacion","IdComprobanteTesoreriaIngreso","IdComprobanteTesoreriaEgreso"
	,"IdAgente","BenOur","EquivalenteConGasto","TipoCambioCierreConGasto","Observacion","CodigoComercio","FolioPlanilla","FolioInterfaz","OperacionFicticia"
	,"IdTipoCanalIngreso","IdDatatec","Estado","IdUsuario","IdUsuarioModifica"
	,"FechaModifica","FechaReproceso","FechaIngresoRegistro","EstadoConfirmacion","IdOperacionSpotOrigen","IdOperacionSpotVigente"
	,"IdSolicitudAporteRescate","TipoDocumento","IdContacto","AfectoADerivado","DolarObservado", "IdTipoSpot", "IdPlataformaNegociacion", "IdEmpleadoDigitador"
	,"FacturacionAutomatica", "EstadoCaja"
)
VALUES(
	 :IdOperacionSpot,:IdEmpresa,:IdCliente,:IdPortafolio,:IdInstrumento,:IdTipoOperacion,:FechaOperacion,:FechaLiquidacion
	,:IdMonedaPrincipal,:IdMonedaSecundaria,:IdMonedaLocal,:IdMonedaEquivalente,:MontoMonedaPrincipal,:MontoMonedaPrincipalEquivalente,:MontoMonedaSecundaria
	,:TipoCambioCierre,:TipoCambioCierreForward,:ParidadCierre,:ParidadCierreForward,:TipoCambioCosto,:ParidadCosto
	,:MontoEquivalente,:MontoRentabilidad,:MontoRentabilidadCanal,:ResultadoMonedaLocalPuntas,:ResultadoMonedaLocalBidAsk,:ResultadoMonedaLocalDO
	,:ResultadoMonedaLocalTCSpot,:ResultadoMonedaLocalTCCierreMesa,:ResultadoMonedaLocalConsolidado,:Margen,:MargenBidAsk,:Utilidad
	,:PuntaCompra,:PuntaVenta,:PuntaBid,:PuntaAsk,:ParidadBid,:ParidadAsk,:ValorDolarObservado,:ValorTCSpot,:SolicitudTipoCambio,:IdEvento
	,:IdMesa,:IdModulo,:IdTipoDolar,:TipoComprobanteFacturacion,:IdComprobanteFacturacion,:IdComprobanteTesoreriaIngreso,:IdComprobanteTesoreriaEgreso
	,:IdAgente,:BenOur,:EquivalenteConGasto,:TipoCambioCierreConGasto,:Observacion,:CodigoComercio,:FolioPlanilla,:FolioInterfaz,:OperacionFicticia
	,:IdTipoCanalIngreso,:IdDatatec,:Estado,:IdUsuario,:IdUsuarioModifica
	,:FechaModifica,:FechaReproceso,:FechaIngresoRegistro,:EstadoConfirmacion,:IdOperacionSpotOrigen,:IdOperacionSpotVigente
	,:IdSolicitudAporteRescate,:TipoDocumento,:IdContacto,:AfectoADerivado,:DolarObservado, :IdTipoSpot, :IdPlataformaNegociacion, :IdEmpleadoDigitador
	,COALESCE(:FacturacionAutomatica,'N'), COALESCE(:EstadoCaja, 'PEN')
);
