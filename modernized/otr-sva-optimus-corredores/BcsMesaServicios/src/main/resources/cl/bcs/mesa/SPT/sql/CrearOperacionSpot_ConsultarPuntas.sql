
WITH datos AS
(

SELECT 
OPE."NumeroOperacion",
OPE."IdTipoOperacion",
OPE."IdInstrumento",
OPE."IdTipoCanalIngreso",
OPE."MontoMonedaPrincipal",
OPE."TipoCambioCierre",
OPE."IdMonedaPrincipal",
OPE."IdMonedaSecundaria",
puntas."PuntaCompra",
puntas."PuntaVenta",
puntas."IdPuntas",
MAX(ppt."MontoPorTramo") AS "MaxMonto",
MIN(pptMin."MontoPorTramo") AS "MinMonto"
FROM "BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso" OPE
INNER JOIN "BSCL.Mesa.BD::SPT.Puntas" puntas
	ON "IdMonedaPrincipal" = puntas."IdMonedaPunta" 
LEFT JOIN "BSCL.Mesa.BD::SPT.PuntasPorTramo" ppt
	ON puntas."IdPuntas" = ppt."IdPuntas"
	AND ppt."Canal" = 'Agente'
	AND ppt."MontoPorTramo" <= "MontoMonedaPrincipal" 
	AND ppt."IdEmpresa" = :IdEmpresa	 
	AND	ppt."Estado" = 'VIG'
LEFT JOIN  "BSCL.Mesa.BD::SPT.PuntasPorTramo" pptMin
	ON puntas."IdPuntas" = pptMin."IdPuntas"
	AND pptMin."Canal" = 'Agente'
	AND pptMin."IdEmpresa" = :IdEmpresa	 
	AND	pptMin."Estado" = 'VIG'
WHERE OPE."NumeroOperacion" IN (:NumeroOperacion)
AND OPE."IdTipoCanalIngreso" = 'AGE'
GROUP BY 
	OPE."NumeroOperacion",
	OPE."IdTipoOperacion",
	OPE."IdInstrumento",
	OPE."IdTipoCanalIngreso",
	OPE."MontoMonedaPrincipal",
	OPE."TipoCambioCierre",
	OPE."IdMonedaPrincipal",
	OPE."IdMonedaSecundaria",
	puntas."PuntaCompra",
	puntas."PuntaVenta",
	puntas."IdPuntas"
)

SELECT 
datos."NumeroOperacion",
datos."IdTipoOperacion",
datos."IdInstrumento",
datos."IdTipoCanalIngreso",
datos."MontoMonedaPrincipal",
datos."TipoCambioCierre",
datos."IdMonedaPrincipal",
datos."IdMonedaSecundaria",
datos."PuntaCompra",
datos."PuntaVenta",
punta."ValorCompra",
punta."ValorVenta" 
FROM datos
INNER JOIN "BSCL.Mesa.BD::SPT.PuntasPorTramo" punta
	ON	COALESCE(datos."MaxMonto",datos."MinMonto") = punta."MontoPorTramo"
	AND	punta."Canal" = 'Agente' 
	AND punta."IdEmpresa" = :IdEmpresa	 
	AND	punta."Estado" = 'VIG'
	AND punta."IdPuntas" = datos."IdPuntas"

	