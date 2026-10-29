SET SCHEMA "BSCL";

CREATE SEQUENCE "BSCL.Custodia.BD.Secuencias::CircularCarteraCMF" START WITH 1 INCREMENT BY 1;
ALTER TABLE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" ADD(
"IdCircularCartera" INTEGER,
"IdEstado" VARCHAR(4) DEFAULT 'ACT',
"IdOrigen" VARCHAR(15) DEFAULT 'PAUTA',
"CodigoValorizacion" VARCHAR(4) DEFAULT '',
"IdTipoInstrumentoCMF" VARCHAR(15) DEFAULT ''
);

UPDATE "BSCL.Custodia.BD::CUS.CircularCarteraCMF" SET "IdCircularCartera" = "BSCL.Custodia.BD.Secuencias::CircularCarteraCMF".NEXTVAL;

DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion";
DO BEGIN
	DECLARE maxSeqIdHomologacion INT;
	SELECT MAX("IdHomologacion")+ 2 INTO maxSeqIdHomologacion FROM "BSCL.Normativa.BD::DOC.Homologacion";	
	EXEC CONCAT('CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion" START WITH ',maxSeqIdHomologacion);
END;

COMMIT;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'BASETASA', '0', '0', '0', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'BASETASA' AND "Version" = 'V1' AND "ValorOrigen" = '0');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'BASETASA', '30', '30', '30', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'BASETASA' AND "Version" = 'V1' AND "ValorOrigen" = '30');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'BASETASA', '360', '360', '360', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'BASETASA' AND "Version" = 'V1' AND "ValorOrigen" = '360');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'BASETASA', '365', '365', '365', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'BASETASA' AND "Version" = 'V1' AND "ValorOrigen" = '365');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CODVALORIZ', 'TIR', 'TR', 'TR', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CODVALORIZ' AND "Version" = 'V1' AND "ValorOrigen" = 'TR');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CODVALORIZ', 'Porcentaje del Valor Par', 'VP', 'VP', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CODVALORIZ' AND "Version" = 'V1' AND "ValorOrigen" = 'VP');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'CMF', 'V1', 'CODVALORIZ', 'Precio', 'PR', 'PR', 'ACT' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'CMF' AND "Tipo" = 'CODVALORIZ' AND "Version" = 'V1' AND "ValorOrigen" = 'PR');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_NCG499_OK', 'CL', 'Registro actualizado correctamente', '' FROM DUMMY 
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_NCG499_OK');
