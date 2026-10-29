SELECT COM."IdComprobante"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" COM
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" COD ON (COD."IdComprobante"=COM."IdComprobante")
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" FAC ON (FAC."IdComprobante"=COD."IdComprobanteOrigen")
LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" EVE ON (EVE."NumeroOperacionConcepto"=COM."IdComprobante")
LEFT JOIN "BSCL.Plataforma.BD::AUT.EventoDetalle" EVD ON (EVE."IdEvento"=EVD."IdEvento")
LEFT JOIN "BSCL.Plataforma.BD::AUT.EtapaEvento" ETV ON (EVE."IdEvento"=ETV."IdEvento")
WHERE COM."IdComprobanteOrigen" IN (:IdsComprobante)
	  AND COM."IdEstado"='PDA' AND EVE."IdEstado"='VIG' AND EVD."NombreCampo"='IdComprobante'
	  AND EVE."IdTipoEvento" IN ('ALFDDS');		-- eventos