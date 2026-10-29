UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
SET "IdEstado" = 'CER'
WHERE "IdEventoCapital" = :IdEventoCapital
  AND "IdTipoEventoCapital" = 'CA'