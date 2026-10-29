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
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'INSTRUMENTOCMF', 'CUOTAS DE FONDOS MUTUOS', 'FFM', 'CFM', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'INSTRUMENTOCMF' AND "Version" = 'V1' AND "ValorOrigen" = 'FFM');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', '', '', '', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = '');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'N-1', 'N1', 'N-1', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'N1');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'N-2', 'N2', 'N-2', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'N3');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'N-3', 'N3', 'N-3', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'N3');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'N-4', 'N4', 'N-4', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'N4');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'N-5', 'N5', 'N-5', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'N5');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'AAA', 'AAA+', 'AAA', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'AAA+');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'AAA', 'AAA-', 'AAA', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'AAA-');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'AAA', 'AAA', 'AAA', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'AAA');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'AA', 'AA+', 'AA', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'AA+');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'AA', 'AA-', 'AA', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'AA-');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'AA', 'AA', 'AA', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'AA');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'A', 'A+', 'A', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'A+');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'A', 'A', 'A', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'A');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'A', 'A-', 'A', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'A-');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'BBB', 'BBB+', 'BBB', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'BBB+');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'BBB', 'BBB-', 'BBB', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'BBB-');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'BB', 'BB+', 'BB', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'BB+');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'BB', 'BB-', 'BB', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'BB-');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'B', 'B+', 'B', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'B+');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'B', 'B', 'B', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'B');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'B', 'B-', 'B', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'B-');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'C', 'CCC+', 'C', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'CCC+');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'C', 'CCC-', 'C', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'CCC-');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'C', 'CC+', 'C', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'CC+');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'C', 'CC-', 'C', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'CC-');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'C', 'C+', 'C', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'CC+');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'C', 'C', 'C', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'C');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'C', 'C-', 'C', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'C-');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'D', 'D', 'D', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'D');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CLASIFICACIONRIESGO', 'E', 'E', 'E', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CLASIFICACIONRIESGO' AND "Version" = 'V1' AND "ValorOrigen" = 'E');

COMMIT;