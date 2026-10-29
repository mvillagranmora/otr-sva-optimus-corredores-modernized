UPDATE MOV
	SET "IdEstado" = 'ING'
	FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV
	WHERE MOV."IdComprobanteOrigen" = :IdComprobanteOrigen;
	