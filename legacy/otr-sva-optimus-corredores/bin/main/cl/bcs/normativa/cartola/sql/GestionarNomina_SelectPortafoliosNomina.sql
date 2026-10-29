SELECT
"IdCliente"
,"IdPortafolio"
FROM "BSCL.Normativa.BD::CAR.Cartola"
WHERE "IdNomina" = :IdNomina AND "Reprocesar" = 'S';