Update CCM
	Set "IdComprobanteComisionMensual" = :IdComprobanteComisionMensual	
	From "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensual" As CCM
	WHERE CCM."IdMovimientoComprobanteComisionMensual" = :IdMovimientoComprobanteComisionMensual
	