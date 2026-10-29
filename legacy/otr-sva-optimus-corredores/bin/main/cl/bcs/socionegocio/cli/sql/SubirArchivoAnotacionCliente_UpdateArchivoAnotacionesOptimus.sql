UPDATE "BSCL.SocioNegocio.BD::CLI.Anotacion" SET 
	"Archivo" = :ArchivoBlob, "NombreArchivo" = :NombreArchivo
WHERE "IdAnotacion" = :IdAnotacion;