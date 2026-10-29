UPDATE "BSCL.Plataforma.BD::BAT.Programa" 
SET "RutaSalida" = :RutaArchivo,
	"RutaLog" = :RutaLog,
	"Comentario" = :Comentario,
	"Resultado" = 'Finalizado correctamente.',
	"idEstado" = 'TER',
	"fechaFin" = CURRENT_UTCTIMESTAMP
WHERE "IdPrograma" = :IdPrograma;