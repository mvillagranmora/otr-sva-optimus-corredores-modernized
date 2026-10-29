SELECT "IdPortafolio"
FROM "BSCL.Portafolio.BD::POR.Portafolio" 
WHERE "IdCliente" = :IdCliente AND "CodigoPortafolio" = :CodigoPortafolio;