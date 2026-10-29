SELECT FA."IdAnexo"
FROM "BSCL.Plataforma.BD::DOC.FirmaAnexo" FA
WHERE FA."IdAnexo" IN (:IdsAnexos)
  AND FA."IdEstado" = 'ACT'
  AND (FA."EsAutorizado" IS NULL OR FA."EsAutorizado" = 'S');