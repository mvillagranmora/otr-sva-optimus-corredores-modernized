UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
SET "IdEstado" = 'ACT'
WHERE "IdEventoCapital" = :IdEventoCapital
  AND "IdTipoEventoCapital" = 'EP';