SELECT "IdEnvio", "Archivo", "Nombre"
FROM
(
	SELECT 
		E."IdEvento" as "IdEnvio",
		N."IdNotificacion",
		EAA."Archivo",
		EAA."NombreArchivo" as "Nombre" 
	FROM "BSCL.Plataforma.BD::EVT.Notificacion" N
	INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" ND ON ND."IdNotificacion" = N."IdNotificacion"
	INNER JOIN  "BSCL.Plataforma.BD::EVT.Evento" E ON E."IdEvento" = ND."IdEvento"
	INNER JOIN "BSCL.Plataforma.BD::EVT.EventoArchivoAdjunto" EAA ON EAA."IdEvento" = E."IdEvento"
	INNER JOIN  "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoDetalles" CCED ON CCED."IdConfiguracionEvento" = N."IdConfiguracionEvento"
) WHERE "IdNotificacion" IN (:Notificaciones);