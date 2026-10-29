SET SCHEMA "BSCL";

ALTER TABLE "BSCL.Normativa.BD::TRI.DatosDJ1949" ADD ("AcogidoISFUT" NVARCHAR(1) DEFAULT 'N');

DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1949' AND "Version" = 2022 and "Nombre" = 'AcogidoISFUT';

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "Editable", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1949', '2022', 'AcogidoISFUT', '', '', 46, 'AcogidoISFUT', '', 'N', 'TEXT', 1, 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);