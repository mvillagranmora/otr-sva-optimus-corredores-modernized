SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo"
	SET 
		"Editable" = 'S'
WHERE "Esquema" IN ('DJ1949', 'DJ1922') AND  "Version" = '2022' AND "Nombre" = 'AcogidoISFUT';