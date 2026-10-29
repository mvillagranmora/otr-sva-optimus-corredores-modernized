-- Count Portafolio APV
SELECT "IdPortafolio"
FROM "BSCL.Portafolio.BD::POR.Portafolio"
WHERE "IdPortafolio" IN (:IdPortafolios)
  AND "Proposito" = 'PROPOSITOAPV'
