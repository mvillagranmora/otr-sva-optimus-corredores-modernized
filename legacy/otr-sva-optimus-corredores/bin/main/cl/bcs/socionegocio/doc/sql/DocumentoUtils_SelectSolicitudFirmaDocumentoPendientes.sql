SELECT SFA."IdSolicitudFirmaAnexo" AS "IdSolicitudFirmaAnexo"
     , SFA."IdPersonaSolicitante"  AS "IdPersonaSolicitante"
     , SFA."IdPersonaFirmante"     AS "IdPersonaFirmante"
     , SFA."IdTipoCanalIngreso"    AS "IdTipoCanalIngresoSolicitudFirmaAnexo"
     , FA."IdFirmaAnexo"           AS "IdFirmaAnexo"
     , FA."IdAnexo"                AS "IdAnexo"
     , D."IdDocumento"             AS "IdDocumento"
     , D."IdCliente"               AS "IdCliente"
     , FA."IdEstado"               AS "IdEstadoFirma"
     , FA."FechaFirma"             AS "FechaFirma"
     , FA."IdPersonaAgente"        AS "IdPersonaAgente"
     , FA."IdTipoCanalIngreso"     AS "IdTipoCanalIngresoFirmaAnexo"
     , TFA."FechaExpiracion"       AS "FechaExpiracionToken"
     , TFA."Token"                 AS "Token"
FROM "BSCL.Plataforma.BD::DOC.Documento" D
     LEFT JOIN "BSCL.Plataforma.BD::DOC.FirmaAnexo" FA ON (FA."IdAnexo" = D."IdUltimoAnexo")
     LEFT JOIN "BSCL.Plataforma.BD::DOC.SolicitudFirmaAnexo" SFA ON (SFA."IdSolicitudFirmaAnexo" = FA."IdSolicitudFirmaAnexo")
     LEFT JOIN "BSCL.Plataforma.BD::DOC.TokenFirmaAnexo" TFA ON (TFA."IdSolicitudFirmaAnexo" = SFA."IdSolicitudFirmaAnexo")
WHERE D."IdUltimoAnexo" = :IdAnexo
  AND SFA."IdPersonaFirmante" IN (:IdsPersonasFirmantes)
  AND SFA."IdSolicitudFirmaAnexo" IS NOT NULL
  AND (FA."IdEstado" = 'PEN' OR FA."EsAutorizado" = 'P');