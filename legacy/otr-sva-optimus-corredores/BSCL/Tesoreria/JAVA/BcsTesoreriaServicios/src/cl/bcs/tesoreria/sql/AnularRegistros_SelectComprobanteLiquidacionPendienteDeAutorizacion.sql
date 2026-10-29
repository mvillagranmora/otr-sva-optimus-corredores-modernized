SELECT EVE."IdEvento", ETV."IdEtapaEvento" AS "IdEtapa"
FROM "BSCL.Tesoreria.BD::TES.Comprobante" COM
LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" EVE ON (EVE."NumeroOperacionConcepto"=COM."IdComprobante")
LEFT JOIN "BSCL.Plataforma.BD::AUT.EventoDetalle" EVD ON (EVE."IdEvento"=EVD."IdEvento")
LEFT JOIN "BSCL.Plataforma.BD::AUT.EtapaEvento" ETV ON (EVE."IdEvento"=ETV."IdEvento")
WHERE COM."IdComprobante" IN (:IdsComprobante)
	  AND COM."IdEstado"='PDA' AND EVE."IdEstado"='VIG' AND EVD."NombreCampo"='IdComprobante'
	  AND EVE."IdTipoEvento" IN ('ALFDDS','ALFSTN');