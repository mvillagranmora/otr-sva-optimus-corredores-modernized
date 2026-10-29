SELECT TB."IdPersona"                AS "IdEntidad"
     , COUNT(TB."IdTarjetaBancaria") AS "Cantidad"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" AS TB
WHERE TB."Borrado" = 'N'
  AND TB."IdEstado" = 'ACT'
  AND TB."IdPersona" IN (:IdsEntidades)
GROUP BY TB."IdPersona";