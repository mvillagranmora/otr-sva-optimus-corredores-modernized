SELECT 
	INMO."IdInstrumentoMoneda",
	INMO."IdEmpresa",
	INMO."IdInstrumento",
	INMO."Nemotecnico",
	INMO."IdTipoProducto",
	INMO."IdTipoInstrumento",
	INMO."IdMonedaPrincipal",
	INMO."IdMonedaSecundaria",
	INMO."IdEstado",
	INMO."FeriadoMonedaPrincipal"
FROM "BSCL.Mesa.BD::MES.InstrumentoMoneda" AS INMO
WHERE "IdInstrumentoMoneda" <> :IdInstrumentoMoneda
AND INMO."IdEmpresa" = :IdEmpresa
AND INMO."IdInstrumento" = :IdInstrumento
AND INMO."Nemotecnico" = :Nemotecnico
AND INMO."IdTipoProducto" = :IdTipoProducto
AND INMO."IdTipoInstrumento" = :IdTipoInstrumento
AND INMO."IdMonedaPrincipal" = :IdMonedaPrincipal
AND INMO."IdMonedaSecundaria" = :IdMonedaSecundaria;