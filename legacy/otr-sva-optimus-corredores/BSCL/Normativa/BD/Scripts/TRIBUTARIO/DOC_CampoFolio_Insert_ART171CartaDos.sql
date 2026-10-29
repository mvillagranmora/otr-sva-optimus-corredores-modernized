SET SCHEMA "BSCL";

DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='ART171Car2' AND "Nombre" = 'Folio';
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='ART171Car2' AND "Nombre" = 'Firma';
DELETE FROM "BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='ART171Car2' AND "Version" = 'V1';

INSERT INTO "BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
				("Esquema","Version","UltimoFolio","CodigoCertificado","NombreCertificado","CodigoPrograma")
				VALUES('ART171Car2','V1',1,'ART171Car2','Carta Dos Art. 171','ConsultarCartaDos');


INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('ART171Car2', 'V1', 'Folio', '', 1, 'Folio', 'TEXT', 1, '', 'S', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '', 0, 'Ayuda de Folio', '', 0);
INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('ART171Car2', 'V1', 'Firma', '', 1, 'Firma', 'TEXT', 1, '', 'S', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '', 0, 'Ayuda de Firma', '', 0);


commit;