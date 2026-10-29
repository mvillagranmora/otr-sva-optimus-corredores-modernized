-- Simultaneas

SELECT 
	 SIM."IdCliente"
	,"INGFechaOperacion" AS "FechaOperacion"
	,SUM("INGMontoATasaMercadoActual") AS "Monto"
FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" AS SIM
INNER JOIN #tmpTblClientesPortafolios AS TEMP ON (SIM."IdCliente" = TEMP."IdCliente" AND SIM."IdPortafolio" = TEMP."IdPortafolio")
GROUP BY 
	 SIM."IdCliente"
	,"INGFechaOperacion"