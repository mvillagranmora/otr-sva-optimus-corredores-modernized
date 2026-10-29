INSERT INTO "BSCL.Orden.BD::ORD.CrearTransacciones_Proceso"
	( "IdInterno"
	, "IdInstrumento"
	, "IdInstrumentoSubyacente"
	, "IdLibroOrdenes"
	, "IdBolsa"
	, "IdTipoOperacion"
	, "IdCorredoraCorresponsal"
	, "IdTipoReajuste"
	, "IdCondicionDeLiquidacionContado"
	, "CodigoOperadorCorredor"
	, "FlujoOrigen")
VALUES
	( :IdInterno
	, :IdInstrumento
	, :IdInstrumentoSubyacente
	, :IdLibroOrdenes
	, :IdBolsa
	, :IdTipoOperacion
	, :IdCorredoraCorresponsal
	, :IdTipoReajuste
	, :IdCondicionDeLiquidacionContado
	, :CodigoOperadorCorredor
	, :FlujoOrigen);

