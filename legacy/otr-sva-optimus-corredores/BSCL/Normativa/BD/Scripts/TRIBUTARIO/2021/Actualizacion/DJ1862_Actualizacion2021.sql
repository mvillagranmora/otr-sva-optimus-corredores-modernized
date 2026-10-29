/*** Se inscriben aca actualizaciones a la version BASE 2020 + 2021 ***/
/*
 *   consideracion :
 *    
 *   - insert o update es para ambas versiones (obligatorio actualizar script en :
 *   - /Normativa/BD/Scripts/TRIBUTARIO/2021/DJ1862.sql  
 * 
 */
SET SCHEMA "BSCL";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1862';
--2020
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'ID', '', '', 1, 'Secuencia', 'Ayuda  de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'Numero', '', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '4. DATOS DE CONTROL', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'RutCliente', 'C1', '', 3, 'Rut Cliente', 'Ayuda de Rut Cliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'TaxID', 'C2', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'RUT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'PaisOtorganteIdentificador', 'C3', 'CodigoPais1862', 5, 'Pais Otorgante del Identificador', 'Ayuda de Pais Otorgante del Identificador', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'Nombre', 'C4', '', 6, 'Nombre o Razon Social', 'Ayuda de Nombre o Razon Social', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'CodigoMoneda', 'C5', 'CodigoMoneda1862', 7, 'Codigo Moneda', 'Ayuda de Codigo Moneda', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'MontoEnPesos', 'C6', '', 8, 'Monto En Pesos ($)', 'Ayuda de Monto En Pesos', 'DECIMAL', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 7, 2);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'NumeroTransaccion', 'C7', '', 9, 'Numero de Transaccion', 'Ayuda de Numero de Transaccion', 'QTY', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'CodigoModalidadOperacion', 'C8', 'CodigoOperacionDJ1862', 10, 'Codigo Modalidad Operacion', 'Ayuda de CodigoModalidadOperacion', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'CodigoConcepto', 'C9', 'CodigoConceptoDJ1862', 11, 'Codigo Concepto', 'Ayuda de CodigoConcepto', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'FechaOperacion', 'C10', '', 12, 'Fecha de la Operacion', 'Ayuda de Fecha de la Operacion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'NombreContraparte', 'C11', '', 13, 'Nombre o Razon Social Contraparte', 'Ayuda de Nombre o Razon Social', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'TaxIDContraparte', 'C12', '', 14, 'Tax ID Contraparte', 'Ayuda de Tax ID', 'TEXT', 15, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'CodigoPaisContraparte', 'C13', '', 15, 'Codigo Pais', 'Ayuda de Codigo Pais', 'NEMO', 15, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'BancoContraparte', 'C14', '', 16, 'Banco o entidad intermediarios o corresponsales en el extranjero', 'Ayuda de Banco o entidad intermediarios o corresponsales en el extranjero', 'TEXT', 15, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 15, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'IdDocumento', '', '', 17, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'VboUsuario', '', '', 18, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'Observaciones', '', '', 19, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'FuenteDatos', '', '', 20, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'IdCliente', '', '', 21, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'IdPortafolio', '', '', 22, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'IdComprobante', '', '', 23, 'Fecha Comprobante', '', 'ID', 0, '0', 'S', 'S', 'N', 'N', 'S', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);
-- Campos 2021
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'ID', '', '', 1, 'Secuencia', 'Ayuda  de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'Numero', '', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '4. DATOS DE CONTROL', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'RutCliente', 'C1', '', 3, 'Rut Cliente', 'Ayuda de Rut Cliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'TaxID', 'C2', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'RUT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'PaisOtorganteIdentificador', 'C3', 'CodigoPais1862', 5, 'Pais Otorgante del Identificador', 'Ayuda de Pais Otorgante del Identificador', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'Nombre', 'C4', '', 6, 'Nombre o Razon Social', 'Ayuda de Nombre o Razon Social', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'CodigoMoneda', 'C5', 'CodigoMoneda1862', 7, 'Codigo Moneda', 'Ayuda de Codigo Moneda', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'MontoEnPesos', 'C6', '', 8, 'Monto En Pesos ($)', 'Ayuda de Monto En Pesos', 'DECIMAL', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 7, 2);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'NumeroTransaccion', 'C7', '', 9, 'Numero de Transaccion', 'Ayuda de Numero de Transaccion', 'QTY', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'CodigoModalidadOperacion', 'C8', 'CodigoOperacionDJ1862', 10, 'Codigo Modalidad Operacion', 'Ayuda de CodigoModalidadOperacion', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'CodigoConcepto', 'C9', 'CodigoConceptoDJ1862', 11, 'Codigo Concepto', 'Ayuda de CodigoConcepto', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'FechaOperacion', 'C10', '', 12, 'Fecha de la Operacion', 'Ayuda de Fecha de la Operacion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'NombreContraparte', 'C11', '', 13, 'Nombre o Razon Social Contraparte', 'Ayuda de Nombre o Razon Social', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'TaxIDContraparte', 'C12', '', 14, 'Tax ID Contraparte', 'Ayuda de Tax ID', 'TEXT', 15, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'CodigoPaisContraparte', 'C13', '', 15, 'Codigo Pais', 'Ayuda de Codigo Pais', 'NEMO', 15, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'BancoContraparte', 'C14', '', 16, 'Banco o entidad intermediarios o corresponsales en el extranjero', 'Ayuda de Banco o entidad intermediarios o corresponsales en el extranjero', 'TEXT', 15, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 15, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'IdDocumento', '', '', 17, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'VboUsuario', '', '', 18, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'Observaciones', '', '', 19, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'FuenteDatos', '', '', 20, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'IdCliente', '', '', 21, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'IdPortafolio', '', '', 22, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'IdComprobante', '', '', 23, 'Fecha Comprobante', '', 'ID', 0, '0', 'S', 'S', 'N', 'N', 'S', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);


-- DICCIONARIO
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Diccionario" WHERE  "Tipo" = 'CodigoPais1862';

DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Diccionario" WHERE  "Tipo" = 'CodigoMoneda1862';

DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Diccionario" WHERE  "Tipo" = 'CodigoConceptoDJ1862';

DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Diccionario" WHERE  "Tipo" = 'CodigoOperacionDJ1862';

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoConceptoDJ1862', '11260', 1, 'Ingreso', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoConceptoDJ1862', '21260', 2, 'Egreso', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoOperacionDJ1862', 'SW', 1, 'SWIFT', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoOperacionDJ1862', 'TX', 2, 'TELEX', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoOperacionDJ1862', 'CH', 3, 'Transferencia directa con cheque bancario', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoOperacionDJ1862', 'OT', 4, 'Otra modalidad', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '999', 1, 'PESO CHILENO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '13', 2, 'DOLAR DE ESTADOS UNIDOS', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '143', 3, 'BAHT TAILANDES', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '134', 4, 'BOLIVAR FUERTE', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '4', 5, 'BOLIVIANO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '153', 6, 'COLON COSTARRICENSE', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '51', 7, 'CORONA DANESA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '96', 8, 'CORONA NORUEGA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '113', 9, 'CORONA SUECA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '135', 10, 'DINAR', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '139', 11, 'DIRHAM', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '36', 12, 'DOLAR AUSTRALIANO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '6', 13, 'DOLAR CANADIENSE', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '97', 14, 'DOLAR DE NUEVA ZELANDA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '136', 15, 'DOLAR DE SINGAPUR', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '154', 16, 'D?LAR FIYIANO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '127', 17, 'DOLAR HONG KONG', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '142', 18, 'EURO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '902', 19, 'FRANCO PACIFIC ISLAND', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '156', 20, 'FRANCO POLINESICO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '82', 21, 'FRANCO SUIZO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '23', 22, 'GUARANI', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '102', 23, 'LIBRA ESTERLINA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '149', 24, 'NUEVA LIRA TURCA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '138', 25, 'NUEVO DOLAR TAIWANES', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '24', 26, 'NUEVO SOL', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '981', 27, 'ONZA TROY ORO 981', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '900', 28, 'OTRAS NO ESPECIFICADAS', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '1', 29, 'PESO ARGENTINO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '129', 30, 'PESO COLOMBIANO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '150', 31, 'PESO FILIPINO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '132', 32, 'PESO MEXICANO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '980', 33, 'PESO ORO SELLADO CHILENO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '26', 34, 'PESO URUGUAYO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '128', 35, 'RAND', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '5', 36, 'REAL', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '152', 37, 'RINGGIT MALASIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '155', 38, 'RUBLO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '137', 39, 'RUPIA INDIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '151', 40, 'RUPIA INDONESIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '144', 41, 'WON DE LA REPUBLICA DE COREA DEL SUR', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '72', 42, 'YEN', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '48', 43, 'YUAN', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '998', 44, 'UNIDAD DE FOMENTO (UF) ', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '2', 45, 'UNIDAD CUENTA (BID) ', 'S', '', '');


--PAIS
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '997', 1, 'CHILE', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '224', 2, 'ARGENTINA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '317', 3, 'INDIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '306', 4, 'ISRAEL', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '504', 5, 'ITALIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '331', 6, 'JAPON', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '406', 7, 'AUSTRALIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '216', 9, 'MEXICO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '222', 10, 'PARAGUAY', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '333', 12, 'COREA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '517', 13, 'ESPAÑA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '514', 14, 'BELGICA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '581', 14, 'REINO UNIDO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '225', 15, 'ESTADOS UNIDOS', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '223', 16, 'URUGUAY', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '201', 17, 'VENEZUELA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '221', 18, 'BOLIVIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '219', 19, 'PERU', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '220', 20, 'BRASIL', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '226', 21, 'CANADA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '130', 22, 'CHAD', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '336', 23, 'CHINA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '202', 24, 'COLOMBIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '211', 25, 'COSTA RICA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '507', 26, 'DINAMARCA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '218', 27, 'ECUADOR', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '213', 28, 'EL SALVADOR', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '563', 29, 'ALEMANIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '505', 30, 'FRANCIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoPais1862', '215', 31, 'GUATEMALA', 'S', '', '');



--HOMOLOGACION
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1862';


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'CHILE', '44', '997', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'ARGENTINA', '10', '224', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'INDIA', '101', '317', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'ISRAEL', '106', '306', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'ITALIA', '107', '504', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'JAPON', '109', '331', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'AUSTRALIA', '13', '406', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'MEXICO', '137', '216', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'PARAGUAY', '165', '222', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'COREA', '173', '333', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'ESPAÑA', '194', '517', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'BELGICA', '20', '514', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'REINO UNIDO', '221', '581', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'ESTADOS UNIDOS', '223', '225', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'URUGUAY', '226', '223', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'VENEZUELA', '230', '201', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'BOLVIA', '25', '221', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'PERU', '166', '219', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'BRASIL', '29', '220', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'CANADA', '39', '226', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'CHAD', '43', '130', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'CHINA', '45', '336', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'COLOMBIA', '48', '202', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'COSTA RICA', '52', '211', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'DINAMARCA', '59', '507', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'ECUADOR', '64', '218', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'EL SALVADOR', '66', '213', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'ALEMANIA', '73', '563', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'FRANCIA', '76', '505', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2020', 'CodigoPais1862', 'GUATEMALA', '91', '215', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'CHILE', '44', '997', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'ARGENTINA', '10', '224', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'INDIA', '101', '317', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'ISRAEL', '106', '306', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'ITALIA', '107', '504', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'JAPON', '109', '331', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'AUSTRALIA', '13', '406', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'MEXICO', '137', '216', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'PARAGUAY', '165', '222', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'COREA', '173', '333', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'ESPAÑA', '194', '517', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'BELGICA', '20', '514', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'REINO UNIDO', '221', '581', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'ESTADOS UNIDOS', '223', '225', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'URUGUAY', '226', '223', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'VENEZUELA', '230', '201', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'BOLVIA', '25', '221', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'PERU', '166', '219', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'BRASIL', '29', '220', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'CANADA', '39', '226', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'CHAD', '43', '130', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'CHINA', '45', '336', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'COLOMBIA', '48', '202', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'COSTA RICA', '52', '211', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'DINAMARCA', '59', '507', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'ECUADOR', '64', '218', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'EL SALVADOR', '66', '213', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'ALEMANIA', '73', '563', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'FRANCIA', '76', '505', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL, 'DJ1862', '2021', 'CodigoPais1862', 'GUATEMALA', '91', '215', 'VIG');

