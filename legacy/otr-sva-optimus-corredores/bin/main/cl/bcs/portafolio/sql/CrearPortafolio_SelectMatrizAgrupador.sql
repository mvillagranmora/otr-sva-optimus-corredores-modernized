
SELECT 
"IdEstado",  
"IdPortafolio"
FROM "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
INNER JOIN (SELECT "IdMatrizAgrupadorAPV" FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"
			WHERE "IdProductoAgrupador" = :IdProductoAgrupador) AS Matriz
		ON Matriz."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
WHERE Portafolio."IdCliente" = :IdCliente
AND Portafolio."Proposito" = 'PROPOSITOAPV'
ORDER BY "IdPortafolio" DESC  ;