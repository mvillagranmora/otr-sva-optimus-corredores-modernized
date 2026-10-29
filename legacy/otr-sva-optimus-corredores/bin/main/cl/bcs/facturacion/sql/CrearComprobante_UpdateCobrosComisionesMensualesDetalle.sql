Update CCMD
	Set "IdComprobanteComisionMensual" = :IdComprobanteComisionMensual	
	From "BSCL.Facturacion.BD::FAC.ComprobanteComisionMensualDetalle" As CCMD
	WHERE CCMD."IdMovimientoComprobanteComisionMensual" = :IdMovimientoComprobanteComisionMensual