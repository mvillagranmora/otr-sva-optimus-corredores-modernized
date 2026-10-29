SET SCHEMA "BSCL";

ALTER TABLE "BSCL.Orden.BD::ORD.CobroAsignacion" ADD ("CodigoCobroClasificacion" NVARCHAR(40) NOT NULL DEFAULT '');


COMMIT;




