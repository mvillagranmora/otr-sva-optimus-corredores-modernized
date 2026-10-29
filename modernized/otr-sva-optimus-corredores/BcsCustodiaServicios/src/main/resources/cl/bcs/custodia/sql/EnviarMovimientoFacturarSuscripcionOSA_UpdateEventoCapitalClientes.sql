UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
SET "IdEstado" = 'ENV',
"IdComprobanteFac" = (:IdComprobanteFac),
"IdMovimientoComprobanteFac" = (:IdMovimientoComprobanteFac)
WHERE "IdEventoCapitalCliente" = :IdEventoCapitalCliente
  AND "IdTipoEventoCapital" = 'SU'