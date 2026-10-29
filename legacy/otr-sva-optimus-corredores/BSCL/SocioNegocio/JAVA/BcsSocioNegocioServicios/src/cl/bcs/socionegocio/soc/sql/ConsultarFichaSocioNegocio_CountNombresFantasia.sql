SELECT NF."IdPersona"               AS "IdEntidad"
     , COUNT(NF."IdNombreFantasia") AS "Cantidad"
FROM "BSCL.SocioNegocio.BD::PER.NombreFantasia" AS NF
WHERE NF."Borrado" = 'N'
  AND NF."IdEstado" = 'ACT'
  AND NF."IdPersona" IN (:IdsEntidades)
GROUP BY NF."IdPersona";