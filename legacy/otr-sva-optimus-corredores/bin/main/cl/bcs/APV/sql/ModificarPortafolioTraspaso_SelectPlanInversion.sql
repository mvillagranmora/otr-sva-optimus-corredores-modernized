SELECT 
PORT."IdPortafolio",
PORT."CodigoPortafolio", 
PORT."Proposito", 
PORT."IdCliente", 
PORT."IdEstado",
MATR."IdMatrizAgrupadorAPV",
MATR."IdPlanTipoInversion"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS PORT
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MATR ON MATR."IdMatrizAgrupadorAPV"=PORT."IdMatrizAgrupadorAPV"
WHERE PORT."Proposito" = 'PROPOSITOAPV'
AND PORT."IdCliente" = :IdCliente ;