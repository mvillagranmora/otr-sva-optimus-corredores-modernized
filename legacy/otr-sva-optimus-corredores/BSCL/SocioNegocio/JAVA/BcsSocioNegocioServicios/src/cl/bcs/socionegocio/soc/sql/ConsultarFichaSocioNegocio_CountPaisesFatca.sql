SELECT FPR."IdPersona"               AS "IdEntidad"
     , COUNT(FPR."IdPaisResidencia") AS "Cantidad"
FROM "BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia" AS FPR
WHERE FPR."IdPersona" IN (:IdsEntidades)
GROUP BY FPR."IdPersona";