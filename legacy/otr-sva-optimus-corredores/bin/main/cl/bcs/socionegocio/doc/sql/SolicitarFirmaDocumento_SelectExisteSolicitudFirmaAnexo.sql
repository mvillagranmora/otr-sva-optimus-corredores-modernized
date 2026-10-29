SELECT SFA."IdSolicitudFirmaAnexo"
FROM "BSCL.Plataforma.BD::DOC.FirmaAnexo" FA
     LEFT JOIN "BSCL.Plataforma.BD::DOC.SolicitudFirmaAnexo" SFA ON (SFA."IdSolicitudFirmaAnexo" = FA."IdSolicitudFirmaAnexo")
WHERE FA."IdAnexo" = :IdAnexo
  AND FA."IdSolicitudFirmaAnexo" IS NOT NULL;