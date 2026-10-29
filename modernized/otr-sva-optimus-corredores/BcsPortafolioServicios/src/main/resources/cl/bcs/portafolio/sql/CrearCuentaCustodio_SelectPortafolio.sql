SELECT
    P."IdPortafolio" AS "IdPortafolio",
    P."Proposito" AS "Proposito"
FROM "BSCL.Portafolio.BD::POR.Portafolio" P
WHERE P."IdPortafolio" = :IdPortafolio
