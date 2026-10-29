SET SCHEMA BSCL;

ALTER TABLE "BSCL.Normativa.BD::TRI.DatosDJ1932"
ADD ("TipoOperacion" NVARCHAR(15));


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1932', '2025', 'TipoOperacion', '', 21, 'TipoOperacion', 'TEXT', 15, '', 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', '6. DATOS DE CONTROL', 0, 'Ayuda de Operacion', '', 0);

COMMIT;