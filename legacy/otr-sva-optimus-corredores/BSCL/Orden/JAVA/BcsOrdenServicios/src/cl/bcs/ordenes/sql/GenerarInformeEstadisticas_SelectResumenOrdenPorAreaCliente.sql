SELECT 
	OPE."TipoOperacion" AS "NombreTipoOperacion"
	,OPE."AreaCliente"
	,COUNT(OPE."TipoOperacion") AS "CantidadOperaciones"  
	,SUM(CASE OPE."AccionOperacion" WHEN 'COMPRA' THEN "MontoCompra" ELSE "MontoVenta" END ) AS "MontoOperacion" 
FROM
	#Salida_Libro_Operaciones AS OPE
	WHERE OPE."IdTipoOperacion" NOT IN('ANVACN','ANCACN','ANVACX','ANCACX','ANVRF','ANCRF','ANVIIF','ANCIIF')
GROUP BY OPE."TipoOperacion"
		,OPE."AreaCliente"
ORDER BY OPE."TipoOperacion", OPE."AreaCliente";