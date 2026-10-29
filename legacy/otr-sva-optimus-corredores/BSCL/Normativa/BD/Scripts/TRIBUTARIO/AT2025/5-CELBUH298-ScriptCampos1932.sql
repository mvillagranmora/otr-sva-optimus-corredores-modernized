SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Obligatorio"='N' WHERE "Esquema"='DJ1932' AND "Version"='2024';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Obligatorio"='S' WHERE "Nombre" IN ('RutDepositanteInversionistaPrestamista','Pais') AND "Esquema"='DJ1932' AND "Version"='2024' ;

UPDATE  "BSCL.Normativa.BD::DOC.Campo" SET "OcultoEnMantenedor"='S' WHERE "Esquema"='DJ1932' AND "Version"='2024' AND "Grupo" = '6. DATOS DE CONTROL';

COMMIT;