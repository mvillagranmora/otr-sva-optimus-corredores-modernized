UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
SET "IdEstado" = 'NOT'
WHERE "IdEventoCapital" IN (:IdEventoCapital)