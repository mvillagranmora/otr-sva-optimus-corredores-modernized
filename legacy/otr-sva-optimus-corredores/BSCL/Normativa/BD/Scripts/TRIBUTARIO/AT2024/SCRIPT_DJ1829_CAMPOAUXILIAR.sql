SET SCHEMA "BSCL";



UPSERT BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1829', '2023', 'FechaContrato', '', 57, 'Fecha Contrato', 'DATE_FILTER', 0, '', 'N', 'N', 'N', 'S', 'N', 'S', 'N', 'N', '1. DATOS DE LOS CONTRATOS INFORMADOS', 50, 'Ayuda FechaContrato', '', 0)
WHERE "Esquema"='DJ1829' AND "Version"='2023' AND "Nombre"='FechaContrato';


UPSERT BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1829', '2022', 'FechaContrato', '', 57, 'Fecha Contrato', 'DATE_FILTER', 0, '', 'N', 'N', 'N', 'S', 'N', 'S', 'N', 'N', '1. DATOS DE LOS CONTRATOS INFORMADOS', 50, 'Ayuda FechaContrato', '', 0)
WHERE "Esquema"='DJ1829' AND "Version"='2022' AND "Nombre"='FechaContrato';

UPSERT BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1829', '2021', 'FechaContrato', '', 57, 'Fecha Contrato', 'DATE_FILTER', 0, '', 'N', 'N', 'N', 'S', 'N', 'S', 'N', 'N', '1. DATOS DE LOS CONTRATOS INFORMADOS', 50, 'Ayuda FechaContrato', '', 0)
WHERE "Esquema"='DJ1829' AND "Version"='2021' AND "Nombre"='FechaContrato';

UPSERT BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1829', '2020', 'FechaContrato', '', 57, 'Fecha Contrato', 'DATE_FILTER', 0, '', 'N', 'N', 'N', 'S', 'N', 'S', 'N', 'N', '1. DATOS DE LOS CONTRATOS INFORMADOS', 50, 'Ayuda FechaContrato', '', 0)
WHERE "Esquema"='DJ1829' AND "Version"='2020' AND "Nombre"='FechaContrato';

UPSERT BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1829', '2024', 'FechaContrato', '', 57, 'Fecha Contrato', 'DATE_FILTER', 0, '', 'N', 'N', 'N', 'S', 'N', 'S', 'N', 'N', '1. DATOS DE LOS CONTRATOS INFORMADOS', 50, 'Ayuda FechaContrato', '', 0)
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='FechaContrato';





UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=34, "Titulo"='Fecha de Vencimiento', "TipoDato"='DATE', "Largo"=0, "ValorDefecto"='', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS ', "OrdenCargaExterna"=33, "Ayuda"='', "CodigoSII"='C40', "CantidadDecimales"=0
WHERE "Esquema"='DJ1829' AND "Nombre"='FechaVencimiento';




COMMIT;