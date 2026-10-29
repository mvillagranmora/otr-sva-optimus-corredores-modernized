SELECT "IdPortafolio", det."IdMoneda",det."MontoTotal"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por
INNER JOIN "BSCL.Plataforma.BD.Vistas::LIM.ConsultarDetallePlantillaLimites" det ON por."IdPerfilLimitesExtra" = det."IdPlantillaLimite"
WHERE det."IdTipoLimite" = 'LIMPORTSOBREGIRO' AND det."IdEstado" = 'ACT' AND por."IdPortafolio" || det."IdMoneda" IN (:keysPortafolioMoneda)
