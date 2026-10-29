SELECT Portafolio."IdPortafolio"
     , Portafolio."IdCliente"
     , Portafolio."FechaIngreso"
     , Portafolio."IdPerfilLimitesExtra"
     , MatrizAgrupador."IdProductoAgrupador"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
     LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
WHERE Portafolio."IdCliente" = :IdCliente
  AND MatrizAgrupador."IdProductoAgrupador" = :IdProductoAgrupador
  AND Portafolio."IdPortafolio" != :IdPortafolio
  AND Portafolio."IdEstado" <> 'INA';