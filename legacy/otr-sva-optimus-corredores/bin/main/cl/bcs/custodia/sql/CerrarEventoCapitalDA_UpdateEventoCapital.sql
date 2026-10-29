UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
SET "IdEstado" = :IdEstado
WHERE "IdEventoCapital" = :IdEventoCapital
AND "IdTipoEventoCapital" = 'DA';