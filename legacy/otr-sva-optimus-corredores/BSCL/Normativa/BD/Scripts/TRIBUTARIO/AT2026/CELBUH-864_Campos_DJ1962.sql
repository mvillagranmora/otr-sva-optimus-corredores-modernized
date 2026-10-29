SET SCHEMA BSCL;

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
SELECT 'DJ1962','2025','I001','NORMA','S'
,'En sistema Optimus Corredoras solo se puede declarar como tipo contribuyente 2 Corredor de Bolsa, si hay registros con tipo contribuyente distinto corregir','E','C27=2' 
FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema" IN ('DJ1962') AND "Version"='2025' AND "Codigo"='I001' );

INSERT INTO "BSCL.Normativa.BD::DOC.Validacion" ("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
SELECT 'DJ1962','2026','I001','NORMA','S'
,'En sistema Optimus Corredoras solo se puede declarar como tipo contribuyente 2 Corredor de Bolsa, si hay registros con tipo contribuyente distinto corregir','E','C27=2' 
FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema" IN ('DJ1962') AND "Version"='2026' AND "Codigo"='I001' ) 
AND  EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema" IN ('DJ1962') AND "Version"='2026');

DELETE FROM "BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo" ='TipoContribuyente1962' AND "Codigo" !='2';


INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT 'DJ1962', '2025', 'IdCartera', '', 49, 'IdCartera', 'ID', 250, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'N', 'S', '5. DATOS DE CONTROL', 0, 'Ayuda de Cartera', '', 0
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1962' AND "Version" = '2025' AND "Nombre"='IdCartera') ;

INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
SELECT 'DJ1962', '2026', 'IdCartera', '', 49, 'IdCartera', 'ID', 250, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'N', 'S', '5. DATOS DE CONTROL', 0, 'Ayuda de Cartera', '', 0
FROM DUMMY
WHERE EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1962' AND "Version" = '2026') 
AND NOT EXISTS  (SELECT 1 FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1962' AND "Version" = '2026' AND "Nombre"='IdCartera') ;

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Orden"=50 WHERE "Esquema" = 'DJ1962'  AND "Nombre"='FechaOperacion';

UPDATE "BSCL.Normativa.BD::DOC.Campo" SET "Orden"=51 WHERE "Esquema" = 'DJ1962'  AND "Nombre"='Observaciones';

UPDATE  "BSCL.Normativa.BD::DOC.Campo" SET "Clave"='N'  WHERE "Esquema" = 'DJ1962' AND "Nombre" IN ('CantidadCuotasParticipe','MontoAporteNominal');

ALTER TABLE "BSCL.Normativa.BD::TRI.DatosDJ1962" 
ADD ("IdCartera" BIGINT);


COMMIT;




