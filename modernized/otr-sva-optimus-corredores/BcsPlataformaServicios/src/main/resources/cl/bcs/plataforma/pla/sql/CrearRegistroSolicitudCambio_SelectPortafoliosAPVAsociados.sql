SELECT
Portafolio."IdPortafolio"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
WHERE Portafolio."IdCliente" = :IdCliente AND MatrizAgrupador."IdProductoAgrupador" = :IdProductoAgrupador AND Portafolio."IdPortafolio" != :IdPortafolio;