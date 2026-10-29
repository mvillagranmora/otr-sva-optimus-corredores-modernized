INSERT INTO "BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso"(
	 "NumeroOperacion","IdInstrumento","IdTipoOperacion","IdTipoProducto","IdCliente","IdPortafolio","FechaOperacion"
	,"IdMonedaPrincipal","IdMonedaSecundaria","MontoMonedaPrincipal","MontoMonedaSecundaria"
	,"TipoCambioCierre","TipoCambioCierreForward","ParidadCierre","ParidadCierreForward","TipoCambioCosto","ParidadCosto"
	,"MontoEquivalente","MontoRentabilidad","MontoRentabilidadCanal","Margen","Utilidad","PuntaCompra","PuntaVenta"
	,"SolicitudTipoCambio","IdMesa","IdModulo","IdTipoDolar","TipoComprobanteFacturacion"
	,"IdAgente","BenOur","EquivalenteConGasto","TipoCambioCierreConGasto","Observacion","CodigoComercio"
	,"FolioPlanilla","FolioInterfaz","OperacionFicticia","IdTipoCanalIngreso","IdDatatec","Estado","IdSolicitudAporteRescate"
	,"TipoDocumento","IdContacto","AfectoADerivado","DolarObservado","IdEmpleadoDigitador"
	,"FacturacionAutomatica", "EstadoCaja"
)
VALUES(
	 :NumeroOperacion,:IdInstrumento,:IdTipoOperacion,:IdTipoProducto,:IdCliente,:IdPortafolio,:FechaOperacion
	,:IdMonedaPrincipal,:IdMonedaSecundaria,:MontoMonedaPrincipal,:MontoMonedaSecundaria
	,:TipoCambioCierre,:TipoCambioCierreForward,:ParidadCierre,:ParidadCierreForward,:TipoCambioCosto,:ParidadCosto
	,:MontoEquivalente,:MontoRentabilidad,:MontoRentabilidadCanal,:Margen,:Utilidad,:PuntaCompra,:PuntaVenta
	,:SolicitudTipoCambio,:IdMesa,:IdModulo,:IdTipoDolar,:TipoComprobanteFacturacion
	,:IdAgente,:BenOur,:EquivalenteConGasto,:TipoCambioCierreConGasto,:Observacion,:CodigoComercio
	,:FolioPlanilla,:FolioInterfaz,:OperacionFicticia,:IdTipoCanalIngreso,:IdDatatec,:Estado,:IdSolicitudAporteRescate
	,:TipoDocumento,:IdContacto,:AfectoADerivado,:DolarObservado,:IdEmpleadoDigitador
	,COALESCE(:FacturacionAutomatica,'N'), COALESCE(:EstadoCaja, 'PEN')
);