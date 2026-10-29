UPDATE "BSCL.Plataforma.BD::DOC.FirmaAnexo"
SET "EsAutorizado" = 'N'
WHERE "IdAnexo" = :IdAnexo
AND "EsAutorizado" = 'P'
AND "IdPersonaAgente" IS NOT NULL;