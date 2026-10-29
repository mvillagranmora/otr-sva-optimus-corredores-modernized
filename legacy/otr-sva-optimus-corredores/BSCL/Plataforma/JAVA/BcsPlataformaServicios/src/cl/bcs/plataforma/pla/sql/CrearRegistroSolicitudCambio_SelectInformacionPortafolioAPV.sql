SELECT
Portafolio."IdPortafolio"
,Portafolio."IdMatrizAgrupadorAPV"
,Portafolio."IdCliente"
,MatrizAgrupador."IdProductoAgrupador"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador ON Portafolio."IdMatrizAgrupadorAPV" = MatrizAgrupador."IdMatrizAgrupadorAPV"
WHERE Portafolio."Proposito" = 'PROPOSITOAPV' AND Portafolio."IdPortafolio" = :IdPortafolio AND MatrizAgrupador."IdMatrizAgrupadorAPV" IS NOT NULL