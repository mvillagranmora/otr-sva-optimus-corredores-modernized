-- Actualizar estado de autorización de portafolios APV relacionados
UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdCliente" = :IdCliente
  AND "IdMatrizAgrupadorAPV" = :IdMatrizAgrupadorAPV
  AND "Proposito" = 'PROPOSITOAPV'
