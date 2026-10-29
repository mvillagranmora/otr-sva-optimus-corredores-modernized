SELECT T."IdPersona"         AS "IdEntidad"
     , COUNT(T."IdTelefono") AS "Cantidad"
FROM "BSCL.SocioNegocio.BD::PER.Telefono" AS T
WHERE T."Borrado" = 'N'
  AND T."IdEstado" = 'ACT'
  AND T."IdPersona" IN (:IdsEntidades)
GROUP BY T."IdPersona";