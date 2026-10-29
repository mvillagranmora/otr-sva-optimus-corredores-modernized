SELECT "IdEventoCapital"
,"FechaPago"
,"FechaLimite"
,"Nemo"
,"IdTipoInstrumento"
,"IdEstado"
,"CantidadCuotas"
FROM "BSCL.Custodia.BD::CUS.EventoCapital"
WHERE "IdEventoCapital" IN (:IdEventoCapital)