SELECT
	CAR."IdCliente",
	CAR."IdPortafolio",
	CAR."FolioComprobante",
	CAR."IdTipoComprobante",
	CAR."IdModulo",
	CAR."FolioCartera",
	CAR."IdEstado",
	CAR."IdTipoOperacion",
	CAR."IdCartera",
	CAR."IdConcepto",
	SUB."IdSubyacente",
	SUB."FolioSubyacente",
	SUB."FolioCarteraRelacionada",
	SUB."INGCantidad",
	SUB."INGValorRescate",
	SUB."IdEstado" AS "IdEstadoSubyacente",
	SUB."IdInstrumento",
	SUB."Nemotecnico",
	SUB."IdEmisor",
	'SUBYACENTE' as "TipoCartera",
	TOPE."IdTipoOperacionAnulacion",
	SUB."EGRFolioComprobante",
	SUB."IdCarteraRetirada",
	CARRET."FolioCartera" AS "FolioCarteraRetirada"
FROM 
(
	SELECT "IdCliente","IdPortafolio","FolioComprobante","IdTipoComprobante","IdModulo","FolioCartera","IdEstado","IdTipoOperacion","IdCartera","IdConcepto"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "IdModulo" = 'FAC' 
		AND ("FolioComprobante" IN (:idsComprobante))
) CAR
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB
	ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CARRET
	ON CARRET."IdCartera" = SUB."IdCarteraRetirada"  --Cartera Retirada
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE
	ON TOPE."IdTipoOperacion" = CAR."IdTipoOperacion"
WHERE SUB."FolioCarteraRelacionada" IS NOT NULL

UNION ALL 

SELECT
	CAR."IdCliente",
	CAR."IdPortafolio",
	CAR."EGRFolioComprobante" AS "FolioComprobante",
	CAR."IdTipoComprobante",
	CAR."IdModulo",
	CAR."FolioCartera",
	CAR."IdEstado",
	CAR."IdTipoOperacion",
	CAR."IdCartera",
	CAR."IdConcepto",
	SUB."IdSubyacente",
	SUB."FolioSubyacente",
	SUB."FolioCarteraRelacionada",
	SUB."INGCantidad",
	SUB."INGValorRescate",
	SUB."IdEstado" AS "IdEstadoSubyacente",
	SUB."IdInstrumento",
	SUB."Nemotecnico",
	SUB."IdEmisor",
	'SUBYACENTE' as "TipoCartera",
	TOPE."IdTipoOperacionAnulacion",
	SUB."EGRFolioComprobante",
	SUB."IdCarteraRetirada",
	CARRET."FolioCartera" AS "FolioCarteraRetirada"
FROM 
(
	SELECT "IdCliente","IdPortafolio","EGRFolioComprobante","IdTipoComprobante","IdModulo","FolioCartera","IdEstado","IdTipoOperacion","IdCartera","IdConcepto"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE "EGRIdModulo" = 'FAC' 
		AND ("EGRFolioComprobante" IN (:idsComprobante))
) CAR
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB
	ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CARRET
	ON CARRET."IdCartera" = SUB."IdCarteraRetirada"  --Cartera Retirada
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE
	ON TOPE."IdTipoOperacion" = CAR."IdTipoOperacion"
WHERE SUB."FolioCarteraRelacionada" IS NOT NULL
AND SUB."IdEstado" IN ('VIG','ANT');