INSERT INTO "BSCL.Mesa.BD::FWD.CrearOperacionForward_Proceso"(
	 "FolioInterno","IdTipoOperacion","IdTipoInstrumento","IdTipoProducto","IdInstrumento","Nemotecnico","IdCliente","IdPortafolio","IdPersonaEjecutivo","IdTipoCanalIngreso"
	,"IdMonedaPrincipal","IdMonedaSecundaria","MontoOperacion","MontoInicial","MontoFinal","FechaOperacion","TCCierre","ParidadCierre","Plazo"
	,"FechaVencimiento","FechaPago","IdModalidadLiquidacion","IdReferenciaMercado","IdPlaza"
	,"TCCosto","PuntosTasasForward","PuntosForward","TCForward","TasasMonedaPrincipal","TasasMonedaSecundaria","MontoDiferido","MontoMargen"
	,"IdFixing","FechaFixing","HoraFixing","IdMesa","IdSpotAsociado","IdClasificacionCartera","Observaciones","IdEstado","IdMonedaCompensacion"
)
VALUES(
	 :FolioInterno,:IdTipoOperacion,:IdTipoInstrumento,:IdTipoProducto,:IdInstrumento,:Nemotecnico,:IdCliente,:IdPortafolio,:IdPersonaEjecutivo,:IdTipoCanalIngreso
	,:IdMonedaPrincipal,:IdMonedaSecundaria,:MontoOperacion,:MontoInicial,:MontoFinal,:FechaOperacion,:TCCierre,:ParidadCierre,:Plazo
	,:FechaVencimiento,:FechaPago,:IdModalidadLiquidacion,:IdReferenciaMercado,:IdPlaza
	,:TCCosto,:PuntosTasasForward,:PuntosForward,:TCForward,:TasasMonedaPrincipal,:TasasMonedaSecundaria,:MontoDiferido,:MontoMargen
	,:IdFixing,:FechaFixing,:HoraFixing,:IdMesa,:IdSpotAsociado,:IdClasificacionCartera,:Observaciones,:IdEstado, :IdMonedaCompensacion
);