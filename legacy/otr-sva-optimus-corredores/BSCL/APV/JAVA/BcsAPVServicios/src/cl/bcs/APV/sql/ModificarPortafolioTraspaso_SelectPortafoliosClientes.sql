SELECT 
PORT."IdPortafolio",
PORT."CodigoPortafolio", 
PORT."Proposito", 
PORT."IdCliente", 
PORT."IdEstado"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS PORT
WHERE PORT."Proposito" = 'PROPOSITOAPV'
AND PORT."IdCliente" = :IdCliente ;