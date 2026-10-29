UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
	SET "IdComprobanteTesoreria" = :IdComprobanteTesoreria
WHERE "IdEventoCapitalCliente" = :IdEventoCapitalCliente AND "IdComprobanteTesoreria" = 0;