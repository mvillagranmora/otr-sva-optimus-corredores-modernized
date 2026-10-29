SELECT EP."IdPreComprobanteAsiento"
FROM "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" EP
WHERE EXISTS (SELECT 1
					FROM "BSCL.Contabilidad.BD::ITZ.Evento" EVT
				   WHERE "IdCentralizacion" = :IdCentralizacion AND EVT."IdEvento" = EP."IdEvento"
					)
AND EP."Estado" IN ('C','V');