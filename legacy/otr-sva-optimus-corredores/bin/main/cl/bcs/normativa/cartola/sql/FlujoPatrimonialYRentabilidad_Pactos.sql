-- Pactos

SELECT 
 CART."IdCliente"
,"INGFechaOperacion" AS "FechaOperacion"
,IFNULL(SUM("INGMontoATasaMercadoActual"),TO_DECIMAL(0,26,8)) AS "Monto"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CART
INNER JOIN #tmpTblClientesPortafolios AS TEMP ON (CART."IdCliente" = TEMP."IdCliente" AND CART."IdPortafolio" = TEMP."IdPortafolio")
WHERE "IdTipoInstrumento" = 'PAC' 
GROUP BY 
	 CART."IdCliente"
	,"INGFechaOperacion"