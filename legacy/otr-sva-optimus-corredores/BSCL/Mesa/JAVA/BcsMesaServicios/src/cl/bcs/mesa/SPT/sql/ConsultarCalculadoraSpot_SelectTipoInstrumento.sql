SELECT
	 INSMON."IdInstrumento"
	,INSMON."IdTipoProducto"
	,INSMON."IdMonedaPrincipal"
	,INSMON."IdMonedaSecundaria"
	,INSMON."IdEstado"
	,INSMON."IdEmpresa"
	,INS."IdTipoInstrumento"
	,INS."RazonSocial"		AS "Instrumento"
	,INS."Nemotecnico"
FROM "BSCL.Mesa.BD::MES.InstrumentoMoneda" AS INSMON
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS 	ON (INS."Nemotecnico" = INSMON."Nemotecnico" AND INS."IdTipoInstrumento" = INSMON."IdTipoInstrumento")
WHERE  INS."IdTipoInstrumento" = 'SPT'
AND INSMON."IdMonedaPrincipal" ='USD'
AND INSMON."IdMonedaSecundaria" =:IdMonedaSecundaria;
