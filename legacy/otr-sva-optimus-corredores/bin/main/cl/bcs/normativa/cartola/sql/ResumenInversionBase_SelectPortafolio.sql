SELECT "IdCliente","IdPortafolio" 
FROM "BSCL.Portafolio.BD::POR.Portafolio"
WHERE "IdCliente" IN (:IdCliente);