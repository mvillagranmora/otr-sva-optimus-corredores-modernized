UPDATE MOV
	SET "IdEstado" = 'ANU'
	FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS MOV
	WHERE MOV."IdComprobanteOrigen" = :IdComprobanteOrigen;
	