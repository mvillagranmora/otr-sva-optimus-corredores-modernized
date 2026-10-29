SELECT "IdCartera" 
	, "FolioCartera"
FROM "BSCL.Custodia.BD::CUS.Cartera" 
WHERE "FolioCartera" IN (:p_FolioCartera)
AND "IdEstado" = 'ANT'
AND "FolioSistemaExterno" NOT IN ('', '0')