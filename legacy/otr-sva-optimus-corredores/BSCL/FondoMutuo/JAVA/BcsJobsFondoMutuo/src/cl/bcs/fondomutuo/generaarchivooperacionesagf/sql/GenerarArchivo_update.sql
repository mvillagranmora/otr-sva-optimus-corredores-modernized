UPDATE "BSCL.Plataforma.BD::BAT.Programa" 
SET "RutaSalida" = :RutaArchivo,
	"RutaLog" = :RutaLog
WHERE "IdPrograma" = :IdPrograma;