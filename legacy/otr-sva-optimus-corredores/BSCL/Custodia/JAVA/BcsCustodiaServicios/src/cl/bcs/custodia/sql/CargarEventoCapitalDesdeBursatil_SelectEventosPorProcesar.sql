SELECT "IdEventoCapital", TO_VARCHAR("FechaLimite") AS "FechaLimite" FROM "BSCL.Custodia.BD::CUS.EventoCapital"
WHERE "IdEventoCapital" IN (:IdEventoCapital);