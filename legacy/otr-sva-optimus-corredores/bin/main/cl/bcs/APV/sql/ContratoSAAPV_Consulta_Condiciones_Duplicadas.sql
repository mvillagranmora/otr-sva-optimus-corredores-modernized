SELECT 
	DISTINCT(CON."IdContratoSAAPV") AS "IdContratoSAAPV"
	, PORCON."Pago"
	, CON."FormaDePago"
	, PORCON."PeriodoInicio"
	, PORCON."PeriodoTermino"
	, CON."IdEntidadRecaudadora"
	, CON."IdEmpleador"
	, CON."IdMedioRecaudacionFormaPago"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS CON
INNER JOIN "BSCL.APV.BD::SCS.PortafolioContratoSAAPV" PORCON
ON PORCON."IdContratoSAAPV" = CON."IdContratoSAAPV"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
ON PORCON."IdPortafolio" = POR."IdPortafolio"
INNER JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" MA
ON MA."IdMatrizAgrupadorAPV" = POR."IdMatrizAgrupadorAPV"
INNER JOIN "BSCL.APV.BD::APV.ProductoAgrupador" PA
ON MA."IdProductoAgrupador" = PA."IdProductoAgrupador"
INNER JOIN #PortafoliosIngresados PORING
ON PORING."Producto" = PA."NombreProductoAgrupador"
AND PORING."IdPortafolio" = PORCON."IdPortafolio"
WHERE CON."FormaDePago" = :FormaDePago
AND CON."IdMedioRecaudacionFormaPago" = :IdMedioRecaudacionFormaPago
AND CON."IdEstado" = 'VIG'
AND POR."IdEstado" <> 'INA';