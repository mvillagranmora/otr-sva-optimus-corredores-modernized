-- Actualiza Evento de solicitud de autorización
UPDATE COM 
	SET COM."IdEvento" = :IdEvento
FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS COM
WHERE COM."IdComprobante"  = CAST(:NumeroOperacionConcepto AS BIGINT)