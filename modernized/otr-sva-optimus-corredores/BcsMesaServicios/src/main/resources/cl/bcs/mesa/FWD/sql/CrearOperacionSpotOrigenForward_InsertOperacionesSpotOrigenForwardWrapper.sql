INSERT INTO "BSCL.Mesa.BD::FWD.CrearOperacionSpotOrigenForward_Proceso"(
	 "IdCartera","IdInstrumento","IdTipoOperacion","IdCliente","IdPortafolio","IdEstado"
	,"EGRIdTipoOperacion","EGRFechaOperacion","FechaVencimientoOperacion","IdMonedaPrincipal","IdMonedaSecundaria"
	,"INGMontoMonedaPrincipal","INGMontoMonedaSecundaria","EGRMontoMonedaPrincipal","EGRMontoMonedaSecundaria"
	,"ValorTipoCambioCierre","ValorParidadCierre","ValorTipoCambioCosto","ValorParidadCosto"
	,"IdMesa","IdAgente","INGGlosa","FolioCartera"
)
VALUES(
	 :IdCartera,:IdInstrumento,:IdTipoOperacion,:IdCliente,:IdPortafolio,:IdEstado
	,:EGRIdTipoOperacion,:EGRFechaOperacion,:FechaVencimientoOperacion,:IdMonedaPrincipal,:IdMonedaSecundaria
	,:INGMontoMonedaPrincipal,:INGMontoMonedaSecundaria,:EGRMontoMonedaPrincipal,:EGRMontoMonedaSecundaria
	,:ValorTipoCambioCierre,:ValorParidadCierre,:ValorTipoCambioCosto,:ValorParidadCosto
	,:IdMesa,:IdAgente,:INGGlosa,:FolioCartera
);
