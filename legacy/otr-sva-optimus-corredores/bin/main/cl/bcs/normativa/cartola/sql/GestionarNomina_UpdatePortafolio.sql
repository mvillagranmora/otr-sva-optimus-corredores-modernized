UPDATE P SET 
    P."FechaUltimaCartola" = :FechaCertificacion, 
    P."IdUltimaCartola" = C."IdCartola"
FROM "BSCL.Portafolio.BD::POR.Portafolio" p, (SELECT "IdPortafolio", "IdCartola" FROM "BSCL.Normativa.BD::CAR.Cartola" WHERE "IdNomina" = :IdNomina) c
WHERE p."IdPortafolio" = c."IdPortafolio";