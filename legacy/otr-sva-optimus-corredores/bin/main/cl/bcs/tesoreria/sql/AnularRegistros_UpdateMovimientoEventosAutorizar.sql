-- Actualiza Evento de solicitud de autorización
UPDATE IE 
	SET IE."IdEvento" = :IdEvento
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE
WHERE IE."IdIngresoEgreso"  = CAST(:NumeroOperacionConcepto AS BIGINT);