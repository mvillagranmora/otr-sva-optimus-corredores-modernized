SELECT 
	"NombreTipoOperacion",
	"AccionTipoOperacion",
	"CantidadOperaciones" ,
	"MontoOperacion" ,
	"MaximaFecha",
	"MinimaFecha"
FROM
	(
	select ROW_NUMBER() OVER(PARTITION BY  OPE."TipoOperacion"
	       ORDER BY OPE."TipoOperacion" DESC) AS "RowNumber", 
	   OPE."TipoOperacion" AS "NombreTipoOperacion",
	   OPE."AccionOperacion" AS "AccionTipoOperacion",
	    COUNT(OPE."TipoOperacion") OVER (PARTITION BY OPE."TipoOperacion") AS "CantidadOperaciones"  
	   ,MAX(OPE."FechaMovimiento") OVER (PARTITION BY OPE."TipoOperacion") AS "MaximaFecha"
	   ,MIN(OPE."FechaMovimiento") OVER (PARTITION BY OPE."TipoOperacion") AS "MinimaFecha"
	   ,SUM(CASE OPE."AccionOperacion" WHEN 'COMPRA' THEN "MontoCompra" ELSE "MontoVenta" END ) OVER (PARTITION BY OPE."TipoOperacion") AS "MontoOperacion" 
from #Salida_Libro_Operaciones OPE
	WHERE OPE."IdTipoOperacion" NOT IN('ANVACN','ANCACN','ANVACX','ANCACX','ANVRF','ANCRF','ANVIIF','ANCIIF')) t
WHERE t."RowNumber" = 1;