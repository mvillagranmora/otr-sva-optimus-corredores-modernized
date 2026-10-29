SELECT TFA."IdTokenFirmaAnexo"     AS "IdTokenFirmaAnexo"
     , TFA."IdSolicitudFirmaAnexo" AS "IdSolicitudFirmaAnexo"
     , TFA."FechaGeneracion"       AS "FechaGeneracion"
     , TFA."FechaExpiracion"       AS "FechaExpiracion"
FROM "BSCL.Plataforma.BD::DOC.TokenFirmaAnexo" TFA
     LEFT JOIN "BSCL.Plataforma.BD::DOC.FirmaAnexo" FA ON (FA."IdSolicitudFirmaAnexo" = TFA."IdSolicitudFirmaAnexo")
     LEFT JOIN "BSCL.Plataforma.BD::DOC.SolicitudFirmaAnexo" SFA ON (SFA."IdSolicitudFirmaAnexo" = FA."IdSolicitudFirmaAnexo")
WHERE FA."IdSolicitudFirmaAnexo" IS NOT NULL
  AND FA."IdAnexo" = :IdAnexo
  AND SFA."IdPersonaFirmante" = :IdPersonaFirmante;