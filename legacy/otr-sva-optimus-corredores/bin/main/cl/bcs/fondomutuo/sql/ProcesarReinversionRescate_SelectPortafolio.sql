SELECT "IdPortafolio"
FROM "BSCL.Portafolio.BD::POR.Portafolio"
WHERE "IdCliente" = :p_IdCliente AND (:p_IdPortafolio=0 OR "IdPortafolio" = :p_IdPortafolio)
ORDER BY "IdPortafolio"
