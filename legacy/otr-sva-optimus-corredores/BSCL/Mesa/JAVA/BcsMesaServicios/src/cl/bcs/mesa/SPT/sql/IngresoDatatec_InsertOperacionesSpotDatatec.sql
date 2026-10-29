INSERT INTO "BSCL.Mesa.BD::SPT.OperacionSpotDatatec"(
	 "IdUnicoDatatec","NumeroRegistro","NumeroIntentos","IdEstadoRegistro","GlosaProceso","IdEmpresa","IdOperacionSpot","FechaOperacion"
	,"IdCliente","IdPortafolio","NombreCliente","IdInstrumento","IdTipoOperacion","IdMonedaPrincipal","IdMonedaSecundaria","IdMonedaLocal"
	,"MontoMonedaPrincipal","TipoCambioCierre","MontoMonedaSecundaria","TipoCambioCosto"
	,"IdFormaPagoRecibo","FechaValutaRecibo","IdFormaPagoEntrego","FechaValutaEntrego","Origen","IdUsuarioCreador","FechaCreacion"
	,"PuntaCompra","PuntaVenta","IdDatatec","InstitucionValidacion","UsuarioValidacion","ClaseMensaje","FechaReproceso"
)
VALUES(
	 :IdUnicoDatatec,:NumeroRegistro,:NumeroIntentos,:IdEstadoRegistro,:GlosaProceso,:IdEmpresa,:IdOperacionSpot,:FechaOperacion
	,:IdCliente,:IdPortafolio,:NombreCliente,:IdInstrumento,:IdTipoOperacion,:IdMonedaPrincipal,:IdMonedaSecundaria,:IdMonedaLocal
	,:MontoMonedaPrincipal,:TipoCambioCierre,:MontoMonedaSecundaria,:TipoCambioCosto
	,:IdFormaPagoRecibo,:FechaValutaRecibo,:IdFormaPagoEntrego,:FechaValutaEntrego,:Origen,:IdUsuarioCreador,:FechaCreacion
	,:PuntaCompra,:PuntaVenta,:IdDatatec,:InstitucionValidacion,:UsuarioValidacion,:ClaseMensaje,:FechaReproceso
);
