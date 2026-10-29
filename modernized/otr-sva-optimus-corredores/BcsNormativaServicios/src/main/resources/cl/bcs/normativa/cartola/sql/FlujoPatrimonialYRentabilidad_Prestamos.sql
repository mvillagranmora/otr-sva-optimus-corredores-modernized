-- Prestamos

SELECT
	PREST."IdCliente" 
	,"INGFechaOperacion" AS "FechaOperacion"
	,SUM("INGMontoInteresCorridoCompraActual") AS "Monto"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" AS PREST
INNER JOIN #tmpTblClientesPortafolios AS TEMP ON (PREST."IdCliente" = TEMP."IdCliente" AND PREST."IdPortafolio" = TEMP."IdPortafolio")
WHERE "IdEstado" ='VIG'
GROUP BY 
    PREST."IdCliente",
	"INGFechaOperacion",
	"PlazoEnDiasOperacion",
	"INGMontoInteresCompraProximo"