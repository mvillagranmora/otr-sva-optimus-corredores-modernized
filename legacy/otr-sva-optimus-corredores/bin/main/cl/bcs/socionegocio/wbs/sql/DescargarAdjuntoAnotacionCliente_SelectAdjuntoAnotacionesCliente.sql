SELECT 
	ANOT."IdAnotacion",
	COALESCE(ANOT."NombreArchivo",'') AS "NombreArchivo",
	ANOT."Archivo"
FROM "BSCL.SocioNegocio.BD::CLI.Anotacion" ANOT
WHERE ANOT."Borrado" = 'N' and ANOT."IdAnotacion" IN (:p_IdsAnotaciones);