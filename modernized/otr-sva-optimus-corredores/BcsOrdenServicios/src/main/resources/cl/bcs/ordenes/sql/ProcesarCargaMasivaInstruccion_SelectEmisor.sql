SELECT
EMISOR."IdEmisor"
,CASE WHEN IFNULL(EMISOR."EmiteFungible",'') = '' THEN 'N' ELSE EMISOR."EmiteFungible" END AS "EmiteFungible"
FROM "BSCL.Plataforma.BD::MER.Emisor" EMISOR
WHERE EMISOR."IdEmisor" IN (:IdEmisor)