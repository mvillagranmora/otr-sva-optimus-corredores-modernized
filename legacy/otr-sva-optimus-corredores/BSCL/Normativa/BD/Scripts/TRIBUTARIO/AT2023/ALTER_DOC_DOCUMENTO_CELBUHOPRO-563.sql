SET SCHEMA "BSCL";


ALTER TABLE "BSCL"."BSCL.Normativa.BD::DOC.Documento" ADD ("FechaCertificado" DATE CS_DAYDATE NULL);

ALTER TABLE "BSCL"."BSCL.Normativa.BD::DOC.Documento" ADD ("DefineFechaCertificado" NVARCHAR CS_STRING DEFAULT 'N' NULL);
