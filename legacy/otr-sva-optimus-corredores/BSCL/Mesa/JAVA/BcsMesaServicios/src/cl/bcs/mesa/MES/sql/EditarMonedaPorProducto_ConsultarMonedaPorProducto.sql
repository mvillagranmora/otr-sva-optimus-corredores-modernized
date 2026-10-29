SELECT 
	INMO."IdInstrumentoMoneda",
	INMO."IdInstrumento",
	INMO."Nemotecnico",
	INMO."IdTipoProducto",
	INMO."IdTipoInstrumento",
	INMO."IdMonedaPrincipal",
	INMO."IdMonedaSecundaria",
	INMO."IdEstado",
	INMO."FeriadoMonedaPrincipal"
FROM "BSCL.Mesa.BD::MES.InstrumentoMoneda" AS INMO
WHERE INMO."IdInstrumentoMoneda" IN (:IdsInstrumentoMoneda)
AND INMO."IdEmpresa" = :IdEmpresa;