SELECT "IdPortafolio" AS "IdPortafolio"
FROM "BSCL.Portafolio.BD::POR.Portafolio"
WHERE "IdPortafolio" IN (:IdPortafolios)
  AND "Proposito" = 'PROPOSITOAPV'
