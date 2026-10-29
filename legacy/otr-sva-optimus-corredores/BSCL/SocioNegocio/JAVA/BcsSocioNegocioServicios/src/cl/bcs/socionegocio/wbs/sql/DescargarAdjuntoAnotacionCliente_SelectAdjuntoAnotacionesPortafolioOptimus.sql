SELECT 
	ANOT."IdAnotacion",
	COALESCE(ANOT."NombreArchivo",'') AS "NombreArchivo",
	ANOT."Archivo"
FROM "BSCL.Portafolio.BD::POR.Anotacion" ANOT
WHERE ANOT."Borrado" = 'N' and ANOT."IdAnotacion" IN (:p_IdsAnotaciones);