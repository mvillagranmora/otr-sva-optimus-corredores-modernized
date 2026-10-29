SELECT "FechaLimite","IdEventoCapital","FechaPago"
FROM "BSCL.Custodia.BD::CUS.EventoCapital"
WHERE "IdEventoCapital" = :IdEventoCapital
  and "IdEstado" IN ('ACT','NOT','PRO')