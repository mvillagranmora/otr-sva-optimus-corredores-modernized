SELECT SUM("EGRCantidad") AS "EGRCantidad" 
	, "FolioCartera"
FROM "BSCL.Custodia.BD::CUS.Cartera" 
WHERE "IdEmpresa" = :p_IdEmpresa
	AND "FolioCartera" IN ( :p_FolioPrestamo )
GROUP BY "FolioCartera"