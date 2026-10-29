-- Seleccionar clientes y matrices de portafolios APV pendientes
SELECT "IdCliente" AS "IdCliente", 
       "IdMatrizAgrupadorAPV" AS "IdMatrizAgrupadorAPV"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
WHERE Portafolio."IdPortafolio" IN (:IdPortafolios)
  AND Portafolio."Proposito" = 'PROPOSITOAPV'
  AND Portafolio."IdEstadoAutorizacion" = 'PEN'
