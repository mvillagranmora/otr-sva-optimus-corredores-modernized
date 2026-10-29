SET SCHEMA "BSCL";

UPDATE "BSCL"."BSCL.Normativa.BD::DOC.Documento"
SET "FechaCertificado"='2020-01-01', "DefineFechaCertificado"='N'
WHERE "Esquema" IN ('DJ1889','DJ1922','DJ1894','DJ1949','DJ1899','DJ1944');

UPDATE "BSCL"."BSCL.Normativa.BD::DOC.Documento"
SET "FechaCertificado"='2020-01-01', "DefineFechaCertificado"='N'
WHERE "IdTipoDocumento" = 'DECLACORR');

UPDATE "BSCL"."BSCL.Normativa.BD::DOC.Documento"
SET "FechaCertificado"='2023-01-01', "DefineFechaCertificado"='S'
WHERE "Esquema" IN ('DJ1922');

UPDATE "BSCL"."BSCL.Normativa.BD::DOC.Documento"
SET "FechaCertificado"='2023-03-10', "DefineFechaCertificado"='S'
WHERE "Esquema" IN ('DJ1894');

UPDATE "BSCL"."BSCL.Normativa.BD::DOC.Documento"
SET "FechaCertificado"='2023-03-14', "DefineFechaCertificado"='S'
WHERE "Esquema" IN ('DJ1899');

UPDATE "BSCL"."BSCL.Normativa.BD::DOC.Documento"
SET "FechaCertificado"='2023-01-30', "DefineFechaCertificado"='S'
WHERE "Esquema" IN ('DJ1889');

UPDATE "BSCL"."BSCL.Normativa.BD::DOC.Documento"
SET "FechaCertificado"='2023-03-24', "DefineFechaCertificado"='S'
WHERE "Esquema" IN ('DJ1949');

UPDATE "BSCL"."BSCL.Normativa.BD::DOC.Documento"
SET "FechaCertificado"='2023-03-24', "DefineFechaCertificado"='S'
WHERE "Esquema" IN ('DJ1944');