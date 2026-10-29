SELECT 
"IdFirmaAnexo",
"IdTipoCanalIngreso"
FROM "BSCL.Plataforma.BD::DOC.FirmaAnexo"
WHERE "IdAnexo" = :IdAnexo
AND "IdEstado" = 'ACT'
AND "EsAutorizado" = 'S';