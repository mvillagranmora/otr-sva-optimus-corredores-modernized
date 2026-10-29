SELECT
	COM."IdComprobante",
	COM."IdTipoMovimiento",
	EVD."Valor" AS "AfectaCuentaInversion"
FROM "BSCL.Tesoreria.BD::TES.Comprobante" COM
JOIN "BSCL.Plataforma.BD::AUT.Evento" EVE ON EVE."NumeroOperacionConcepto"=TO_NVARCHAR(COM."IdComprobante")
LEFT JOIN "BSCL.Plataforma.BD::AUT.EventoDetalle" EVD ON EVE."IdEvento"=EVD."IdEvento" 
WHERE EVD."NombreCampo"='AfectaCuentaInversion' AND "IdComprobante" = :IdComprobante;