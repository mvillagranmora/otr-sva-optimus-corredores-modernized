SELECT DE."IdPersona"                     AS "IdEntidad"
     , COUNT(DE."IdDireccionElectronica") AS "Cantidad"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DE
WHERE DE."Borrado" = 'N'
  AND DE."IdEstado" = 'ACT'
  AND DE."IdPersona" IN (:IdsEntidades)
GROUP BY DE."IdPersona";