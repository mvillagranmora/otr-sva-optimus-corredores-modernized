SELECT PORT."IdCliente",PORT."IdPortafolio" 
FROM "BSCL.Portafolio.BD::POR.Portafolio" PORT
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON PORT."IdPortafolio" = P."IdPortafolio";