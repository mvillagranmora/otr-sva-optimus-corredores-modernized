SELECT CAR."IdCliente",CAR."IdPortafolio",CAR."FolioComprobante",CAR."IdTipoComprobante",CAR."IdModulo",CAR."FolioCartera",CAR."IdEstado",CAR."IdTipoOperacion",CAR."IdCartera",

SUB."IdSubyacente",SUB."FolioSubyacente",SUB."FolioCarteraRelacionada",SUB."INGCantidad",SUB."INGValorRescate",SUB."IdEstado" AS "IdEstadoSubyacente",SUB."IdInstrumento",SUB."Nemotecnico",'SUBYACENTE' as "TipoCartera",
TOPE."IdTipoOperacionAnulacion"
FROM 
(
	SELECT "IdCliente","IdPortafolio","FolioComprobante","IdTipoComprobante","IdModulo","FolioCartera","IdEstado","IdTipoOperacion","IdCartera"
	FROM "BSCL.Custodia.BD::CUS.Cartera"
	WHERE ("IdCarga" > 0 AND "IdModulo" = 'CUS' AND "FolioComprobante" IN (:idsComprobante)) OR ("IdModulo" = 'FAC' AND ("FolioComprobante" IN (:idsComprobante)))	

) CAR
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE ON TOPE."IdTipoOperacion" = CAR."IdTipoOperacion"
WHERE SUB."FolioCarteraRelacionada" IS NOT NULL;

