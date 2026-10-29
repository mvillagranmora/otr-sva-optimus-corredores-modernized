/*** Se inscriben aca actualizaciones a la version BASE 2020 + 2020 ***/
/*
 *   consideracion :
 *    
 *   - insert o update es para ambas versiones (obligatorio actualizar script en :
 *   - /Normativa/BD/Scripts/TRIBUTARIO/2021/DJ1946.sql  
 * 
 */

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1946';

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'ID', '', '', 1, 'Secuencia', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'S', 'N', '', 0, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'IdDocumento', '', '', 40, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'S', 'S', 'S', 'N', '2. DATOS DE CONTROL', 0, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1946', '2020', 'VboUsuario', '', '', 41, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'S', 'N', 'N', 'S', 'N', '2. DATOS DE CONTROL', 0, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'Observaciones', '', '', 42, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'S', 'N', 'N', 'S', 'N', '2. DATOS DE CONTROL', 0, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'FuenteDatos', '', '', 43, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'S', 'N', 'S', 'N', 'N', 'S', 'N', '2. DATOS DE CONTROL', 0, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'IdCliente', '', '', 44, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'S', 'S', 'N', 'N', '2. DATOS DE CONTROL', 0, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'Numero', 'C1', '', 2, 'Numero', '', 'ID', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Transacciones informadas', 1, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'Operacion', 'C2', '', 3, 'Operacion', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 2, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'TipoRenta', 'C3', '', 4, 'TipoRenta', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 3, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'RentaBruta', 'C4', '', 5, 'RentaBruta', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 4, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'TasaDeRetencion', 'C5', '', 6, 'TasaDeRetencion', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 5, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'Impuesto', 'C6', '', 7, 'Impuesto', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 6, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'RentaLiquida', 'C7', '', 8, 'RentaLiquida', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 7, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'CreditoIDPC', 'C8', '', 9, 'CreditoIDPC', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 8, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'OtrosCreditos', 'C9', '', 10, 'OtrosCreditos', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 9, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'F50Codigo', 'C10', '', 11, 'F50Codigo', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 10, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'F50Folio', 'C11', '', 12, 'F50Folio', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 11, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'FechaPago', 'C12', '', 13, 'FechaPago', '', 'DATE_FILTER', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 12, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'MonedaPago', 'C13', '', 14, 'MonedaPago', '', 'TEXT', 40, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 13, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'TipoCambioPago', 'C14', '', 15, 'TipoCambioPago', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 14, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PagadorTipoJuridico', 'C15', '', 16, 'PagadorTipoJuridico', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 15, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PagadorRazonSocial', 'C16', '', 17, 'PagadorRazonSocial', '', 'TEXT', 150, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 16, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PagadorNombre', 'C17', '', 18, 'PagadorNombre', '', 'TEXT', 40, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 17, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PagadorRut', 'C18', '', 19, 'PagadorRut', '', 'RUT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 18, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1946', '2020', 'Pagadordv', 'C19', '', 20, 'PagadorRut', '', 'RUT', 0, '0', 'N', 'S', 'N', 'S', 'S', 'S', 'N', 'S', '1. Transacciones informadas', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PagadorDomicilio', 'C20', '', 20, 'PagadorDomicilio', '', 'TEXT', 180, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 19, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PagadorCiudad', 'C21', '', 21, 'PagadorCiudad', '', 'TEXT', 30, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 20, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PagadorRelacion', 'C22', '', 22, 'PagadorRelacion', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 21, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PagadorHolding', 'C23', '', 23, 'PagadorHolding', '', 'TEXT', 60, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 22, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PerceptorTipoJuridico', 'C24', '', 24, 'PerceptorTipoJuridico', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 23, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PerceptorPaisResidencia', 'C25', '', 25, 'PerceptorPaisResidencia', '', 'TEXT', 30, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 24, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PerceptorRazonSocial', 'C26', '', 26, 'PerceptorRazonSocial', '', 'TEXT', 150, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 25, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PerceptorNombre', 'C27', '', 27, 'PerceptorNombre', '', 'TEXT', 40, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 26, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PerceptorIdentificacionTributaria', 'C28', '', 28, 'PerceptorIdentificacionTributaria', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 27, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PerceptorRut', 'C29', '', 29, 'PerceptorRut', '', 'RUT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 28, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PerceptorFechaNacimiento', 'C31', '', 30, 'PerceptorFechaNacimiento', '', 'DATE', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 29, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PerceptorTipoDomicilio', 'C32', '', 31, 'PerceptorTipoDomicilio', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 30, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PerceptorDomicilio', 'C33', '', 32, 'PerceptorDomicilio', '', 'TEXT', 180, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 31, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PerceptorCiudad', 'C34', '', 33, 'PerceptorCiudad', '', 'TEXT', 30, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 32, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'PerceptorPais', 'C35', '', 34, 'PerceptorPais', '', 'TEXT', 2, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 33, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'IntermediarioTipoJuridico', 'C36', '', 35, 'IntermediarioTipoJuridico', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 34, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'IntermediarioPaisResidencia', 'C37', '', 36, 'IntermediarioPaisResidencia', '', 'TEXT', 30, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 35, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'IntermediarioRazonSocial', 'C38', '', 37, 'IntermediarioRazonSocial', '', 'TEXT', 150, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 36, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'IntermediarioNombre', 'C39', '', 38, 'IntermediarioNombre', '', 'TEXT', 40, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 37, 0); 

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" 
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales") 
VALUES('DJ1946', '2020', 'IntermediarioIdentificacionTributaria', 'C40', '', 39, 'IntermediarioIdentificacionTributaria', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Transacciones informadas', 38, 0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo"
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales")
SELECT "Esquema", '2021' as "Version","Nombre","CodigoSII","TipoDiccionario", "Orden","Titulo","Ayuda","TipoDato","Largo", "ValorDefecto"
,"Obligatorio","Editable","ModoMasivo","Exportable", "OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo", "OrdenCargaExterna","CantidadDecimales"
FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1946' AND "Version" = '2020';




/* MEJORA CARGA EXTERNA CON EXCEL DE EXPORTACION CELBUHOPRO-416 */
/*  Felipe Sepúlveda */

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=1, "Titulo"='Secuencia', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='ID';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C1', "TipoDiccionario"='', "Orden"=2, "Titulo"='Numero', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='S', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=1, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='Numero';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C2', "TipoDiccionario"='', "Orden"=3, "Titulo"='Operacion', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='S', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=2, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='Operacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C3', "TipoDiccionario"='', "Orden"=4, "Titulo"='TipoRenta', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=3, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='TipoRenta';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C4', "TipoDiccionario"='', "Orden"=5, "Titulo"='RentaBruta', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=4, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='RentaBruta';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C5', "TipoDiccionario"='', "Orden"=6, "Titulo"='TasaDeRetencion', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=5, "CantidadDecimales"=2 WHERE "Esquema"='DJ1946' AND "Nombre"='TasaDeRetencion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C6', "TipoDiccionario"='', "Orden"=7, "Titulo"='Impuesto', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=6, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='Impuesto';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C7', "TipoDiccionario"='', "Orden"=8, "Titulo"='RentaLiquida', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=7, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='RentaLiquida';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C8', "TipoDiccionario"='', "Orden"=9, "Titulo"='CreditoIDPC', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=8, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='CreditoIDPC';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C9', "TipoDiccionario"='', "Orden"=10, "Titulo"='OtrosCreditos', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=9, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='OtrosCreditos';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C10', "TipoDiccionario"='', "Orden"=11, "Titulo"='F50Codigo', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=10, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='F50Codigo';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C11', "TipoDiccionario"='', "Orden"=12, "Titulo"='F50Folio', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=11, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='F50Folio';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C12', "TipoDiccionario"='', "Orden"=13, "Titulo"='FechaPago', "Ayuda"='', "TipoDato"='DATE_FILTER', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=12, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='FechaPago';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C13', "TipoDiccionario"='', "Orden"=14, "Titulo"='MonedaPago', "Ayuda"='', "TipoDato"='TEXT', "Largo"=40, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=13, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='MonedaPago';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C14', "TipoDiccionario"='', "Orden"=15, "Titulo"='TipoCambioPago', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=14, "CantidadDecimales"=2 WHERE "Esquema"='DJ1946' AND "Nombre"='TipoCambioPago';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C15', "TipoDiccionario"='', "Orden"=16, "Titulo"='PagadorTipoJuridico', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=15, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PagadorTipoJuridico';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C16', "TipoDiccionario"='', "Orden"=17, "Titulo"='PagadorRazonSocial', "Ayuda"='', "TipoDato"='TEXT', "Largo"=150, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=16, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PagadorRazonSocial';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C17', "TipoDiccionario"='', "Orden"=18, "Titulo"='PagadorNombre', "Ayuda"='', "TipoDato"='TEXT', "Largo"=40, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=17, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PagadorNombre';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C18', "TipoDiccionario"='', "Orden"=19, "Titulo"='PagadorRut', "Ayuda"='', "TipoDato"='RUT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=18, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PagadorRut';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C20', "TipoDiccionario"='', "Orden"=20, "Titulo"='PagadorDomicilio', "Ayuda"='', "TipoDato"='TEXT', "Largo"=180, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=19, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PagadorDomicilio';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C21', "TipoDiccionario"='', "Orden"=21, "Titulo"='PagadorCiudad', "Ayuda"='', "TipoDato"='TEXT', "Largo"=30, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=20, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PagadorCiudad';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C22', "TipoDiccionario"='', "Orden"=22, "Titulo"='PagadorRelacion', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=21, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PagadorRelacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C23', "TipoDiccionario"='', "Orden"=23, "Titulo"='PagadorHolding', "Ayuda"='', "TipoDato"='TEXT', "Largo"=60, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=22, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PagadorHolding';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C24', "TipoDiccionario"='', "Orden"=24, "Titulo"='PerceptorTipoJuridico', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=23, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PerceptorTipoJuridico';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C25', "TipoDiccionario"='', "Orden"=25, "Titulo"='PerceptorPaisResidencia', "Ayuda"='', "TipoDato"='TEXT', "Largo"=30, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=24, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PerceptorPaisResidencia';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C26', "TipoDiccionario"='', "Orden"=26, "Titulo"='PerceptorRazonSocial', "Ayuda"='', "TipoDato"='TEXT', "Largo"=150, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=25, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PerceptorRazonSocial';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C27', "TipoDiccionario"='', "Orden"=27, "Titulo"='PerceptorNombre', "Ayuda"='', "TipoDato"='TEXT', "Largo"=40, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=26, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PerceptorNombre';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C28', "TipoDiccionario"='', "Orden"=28, "Titulo"='PerceptorIdentificacionTributaria', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=27, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PerceptorIdentificacionTributaria';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C29', "TipoDiccionario"='', "Orden"=29, "Titulo"='PerceptorRut', "Ayuda"='', "TipoDato"='RUT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=28, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PerceptorRut';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C31', "TipoDiccionario"='', "Orden"=30, "Titulo"='PerceptorFechaNacimiento', "Ayuda"='', "TipoDato"='DATE', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=29, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PerceptorFechaNacimiento';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C32', "TipoDiccionario"='', "Orden"=31, "Titulo"='PerceptorTipoDomicilio', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=30, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PerceptorTipoDomicilio';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C33', "TipoDiccionario"='', "Orden"=32, "Titulo"='PerceptorDomicilio', "Ayuda"='', "TipoDato"='TEXT', "Largo"=180, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=31, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PerceptorDomicilio';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C34', "TipoDiccionario"='', "Orden"=33, "Titulo"='PerceptorCiudad', "Ayuda"='', "TipoDato"='TEXT', "Largo"=30, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=32, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PerceptorCiudad';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C35', "TipoDiccionario"='', "Orden"=34, "Titulo"='PerceptorPais', "Ayuda"='', "TipoDato"='TEXT', "Largo"=2, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=33, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='PerceptorPais';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C36', "TipoDiccionario"='', "Orden"=35, "Titulo"='IntermediarioTipoJuridico', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=34, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='IntermediarioTipoJuridico';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C37', "TipoDiccionario"='', "Orden"=36, "Titulo"='IntermediarioPaisResidencia', "Ayuda"='', "TipoDato"='TEXT', "Largo"=30, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=35, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='IntermediarioPaisResidencia';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C38', "TipoDiccionario"='', "Orden"=37, "Titulo"='IntermediarioRazonSocial', "Ayuda"='', "TipoDato"='TEXT', "Largo"=150, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=36, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='IntermediarioRazonSocial';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C39', "TipoDiccionario"='', "Orden"=38, "Titulo"='IntermediarioNombre', "Ayuda"='', "TipoDato"='TEXT', "Largo"=40, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=37, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='IntermediarioNombre';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='C40', "TipoDiccionario"='', "Orden"=39, "Titulo"='IntermediarioIdentificacionTributaria', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=38, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='IntermediarioIdentificacionTributaria';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=40, "Titulo"='Secuencia Docto', "Ayuda"='Ayuda de Secuencia Docto', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='S', "OcultoEnMantenedor"='S', "Comun"='S', "Clave"='N', "Grupo"='2. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='IdDocumento';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=41, "Titulo"='VoBo', "Ayuda"='Ayuda de VoBo', "TipoDato"='TEXT', "Largo"=1, "ValorDefecto"='N', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='2. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='VboUsuario';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=42, "Titulo"='Observaciones', "Ayuda"='Ayuda de Observaciones', "TipoDato"='TEXT', "Largo"=250, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='2. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='Observaciones';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=43, "Titulo"='Fuente', "Ayuda"='Ayuda de Fuente', "TipoDato"='TEXT', "Largo"=0, "ValorDefecto"='INICIAL', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='2. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='FuenteDatos';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo" SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=44, "Titulo"='Secuencia Cliente', "Ayuda"='Ayuda de Secuencia Cliente', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='S', "OcultoEnMantenedor"='S', "Comun"='N', "Clave"='N', "Grupo"='2. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0 WHERE "Esquema"='DJ1946' AND "Nombre"='IdCliente';

/* MEJORA CARGA EXTERNA CON EXCEL DE EXPORTACION CELBUHOPRO-416 */