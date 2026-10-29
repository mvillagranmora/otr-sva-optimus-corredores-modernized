SET SCHEMA "BSCL";

DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion";
DO BEGIN
	DECLARE maxSeqIdHomologacion INT;
	SELECT MAX("IdHomologacion") + 1 INTO maxSeqIdHomologacion FROM "BSCL.Normativa.BD::DOC.Homologacion";	
	EXEC CONCAT('CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion" START WITH ', maxSeqIdHomologacion);
END;

COMMIT;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'MONEDAPRINCIPAL', 'UNIDAD DE FOMENTO', 'UF', 'CLF', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'MONEDAPRINCIPAL' AND "Version" = 'V1' AND "ValorOrigen" = 'UF');

COMMIT;