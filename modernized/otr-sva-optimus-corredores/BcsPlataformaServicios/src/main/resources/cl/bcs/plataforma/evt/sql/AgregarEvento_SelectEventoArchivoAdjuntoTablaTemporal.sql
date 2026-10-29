SELECT
	 "BSCL.Plataforma.BD.Secuencias::EVT.EventoArchivoAdjunto".nextval AS "IdEventoArchivoAdjunto",
	 EVE."IdEvento" as "IdEvento",
	 TEMP."ArchivoAdjunto" AS "Archivo",
	 TEMP."Nombre" AS "NombreArchivo",
	 TEMP."Contrasenia"
FROM "BSCL.Plataforma.BD::GBL.EventoArchivoAdjunto_Temp" AS TEMP
LEFT JOIN "BSCL.Plataforma.BD::GBL.AgregarEvento_Evento" AS EVE ON TEMP."IdSecuencia" = EVE."IdSecuencia"
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" AS CONF ON (CONF."CodigoTipoEvento" = EVE."CodigoTipoEvento" AND CONF."IdEstado" = 'ACT');