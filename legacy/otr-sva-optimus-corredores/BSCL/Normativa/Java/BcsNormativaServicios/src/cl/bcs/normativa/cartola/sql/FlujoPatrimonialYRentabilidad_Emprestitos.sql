-- Emprestitos

SELECT 
	 EMP."IdCliente"
	,"INGFechaOperacion" AS "FechaOperacion"
	,SUM("INGMontoInteresCorridoCompraActual") AS "Monto"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" AS EMP
INNER JOIN #tmpTblClientesPortafolios AS TEMP ON (EMP."IdCliente" = TEMP."IdCliente" AND EMP."IdPortafolio" = TEMP."IdPortafolio")
WHERE "IdEstado" ='VIG'
GROUP BY 
	"INGFechaOperacion"
	,EMP."IdCliente"