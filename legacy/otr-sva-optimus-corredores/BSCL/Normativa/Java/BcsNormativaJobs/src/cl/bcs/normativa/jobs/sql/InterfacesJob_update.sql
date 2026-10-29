UPDATE "BSCL.Plataforma.BD::BAT.Programa" 
SET "RutaSalida" = :RutaArchivo,
	"RutaLog" = :RutaLog,
	"Comentario" = :Comentario
WHERE "IdPrograma" = :IdPrograma;