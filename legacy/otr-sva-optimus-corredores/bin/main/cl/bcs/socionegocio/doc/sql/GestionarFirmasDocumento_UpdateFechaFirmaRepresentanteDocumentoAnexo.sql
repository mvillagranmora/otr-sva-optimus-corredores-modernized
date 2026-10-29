/*UPDATE "BSCL.Plataforma.BD::DOC.FirmaAnexo"
SET "FechaFirma"='2023-06-27 04:00:00.000'
WHERE "IdAnexo" = 3735105 AND "IdEstado" = 'ACT';*/
/**/
UPDATE "BSCL.Plataforma.BD::DOC.FirmaAnexo" FA
SET FA."FechaFirma"= :FechaFirma
WHERE "IdAnexo" = :IdAnexo AND "IdEstado" = 'ACT';