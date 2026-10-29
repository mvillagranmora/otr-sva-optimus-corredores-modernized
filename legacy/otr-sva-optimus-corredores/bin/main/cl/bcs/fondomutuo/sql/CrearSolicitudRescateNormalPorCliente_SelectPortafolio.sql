SELECT
"IdPortafolio"
,"CodigoPortafolio"
FROM "BSCL.Portafolio.BD::POR.Portafolio"
WHERE "IdPortafolio" IN (:IdPortafolio);