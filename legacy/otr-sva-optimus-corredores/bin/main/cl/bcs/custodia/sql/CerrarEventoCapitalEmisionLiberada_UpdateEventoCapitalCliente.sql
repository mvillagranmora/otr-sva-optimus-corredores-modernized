UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
SET 
	"IdEstado" = 'CER',
	"IdComprobanteFac" = COALESCE(:IdComprobanteFac, 0),
	"IdMovimientoComprobanteFac" = COALESCE(:IdMovimientoComprobanteFac, 0)
WHERE "IdEventoCapital" = :IdEventoCapital
 AND "IdEventoCapitalCliente" = :IdEventoCapitalCliente