Update COMPROBANTE
	Set "IdComprobanteReferencia" = :IdComprobante,
	    "IdTipoComprobanteReferencia" = :IdTipoComprobante
	From "BSCL.Facturacion.BD::FAC.Comprobante" As COMPROBANTE
	WHERE COMPROBANTE."IdComprobante" = :IdComprobanteReferencia AND :IdComprobanteReferencia > 0