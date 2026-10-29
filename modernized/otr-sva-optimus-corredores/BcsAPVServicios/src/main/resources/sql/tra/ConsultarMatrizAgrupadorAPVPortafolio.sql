SELECT POR."IdPortafolio", POR."IdEstado"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
WHERE POR."IdMatrizAgrupadorAPV" = :IdMatrizAgrupadorAPV 
AND POR."IdCliente" = :IdCliente 
AND POR."Proposito" = 'PROPOSITOAPV'; 