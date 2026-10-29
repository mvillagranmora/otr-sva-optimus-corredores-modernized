SET SCHEMA "BSCL";

ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874" DROP ("IdEstadoInstrumento");
ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874" DROP ("RetencionPesos4porciento");
ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874" DROP ("RetencionMonedaUnidadReajuste4porciento");

ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874" ADD ("IdEstadoInstrumento" NVARCHAR(40) NULL);

ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874" ADD ("RetencionPesos4porciento" DECIMAL(26,8) CS_FIXED NULL);

ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874" ADD ("RetencionMonedaUnidadReajuste4porciento" DECIMAL(26,8) CS_FIXED NULL);

DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Nombre" = 'RetencionPesos4porciento' AND "Esquema" = 'DJ1874';

DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Nombre" = 'RetencionMonedaUnidadReajuste4porciento' AND "Esquema" = 'DJ1874';

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2022', 'RetencionMonedaUnidadReajuste4porciento', '', '', 54, 'Retencion Art.104 4% (en moneda o unidad de reajuste)', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. REAJUSTE 4% Art.104', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2022', 'RetencionPesos4porciento', '', '', 55, 'Retencion Art.104 4% en pesos', '', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '5. REAJUSTE 4% Art.104', 0, 0);

--2021
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2021', 'RetencionMonedaUnidadReajuste4porciento', '', '', 54, 'Retencion Art.104 4% (en moneda o unidad de reajuste)', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. REAJUSTE 4% Art.104', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2021', 'RetencionPesos4porciento', '', '', 55, 'Retencion Art.104 4% en pesos', '', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '5. REAJUSTE 4% Art.104', 0, 0);

--2020
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'RetencionMonedaUnidadReajuste4porciento', '', '', 54, 'Retencion Art.104 4% (en moneda o unidad de reajuste)', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. REAJUSTE 4% Art.104', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'RetencionPesos4porciento', '', '', 55, 'Retencion Art.104 4% en pesos', '', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '5. REAJUSTE 4% Art.104', 0, 0);
