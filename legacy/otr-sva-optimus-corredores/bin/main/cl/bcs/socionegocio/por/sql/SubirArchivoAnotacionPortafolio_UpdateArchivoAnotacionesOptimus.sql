UPDATE "BSCL.Portafolio.BD::POR.Anotacion" SET 
	"Archivo" = :ArchivoBlob, "NombreArchivo" = :NombreArchivo
WHERE "IdAnotacion" = :IdAnotacion;