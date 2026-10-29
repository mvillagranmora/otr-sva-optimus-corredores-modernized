UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
SET "IdEstado" = 'CRT'
WHERE "IdEventoCapitalCliente" = :IdEventoCapitalCliente
  AND "IdTipoEventoCapital" = 'SU'