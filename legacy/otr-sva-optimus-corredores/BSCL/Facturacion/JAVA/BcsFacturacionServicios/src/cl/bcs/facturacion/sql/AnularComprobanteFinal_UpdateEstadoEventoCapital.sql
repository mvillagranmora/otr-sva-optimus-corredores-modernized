UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
SET "IdEstado" = 'ANU'
WHERE "IdComprobanteFac" = :IdComprobanteFac
  AND "IdTipoEventoCapital" = 'SU'