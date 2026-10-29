/*** Se inscriben aca actualizaciones a la version BASE 2020 + 2021 ***/
/*
 *   consideracion :
 *    
 *   - insert o update es para ambas versiones (obligatorio actualizar script en :
 *   - /Normativa/BD/Scripts/TRIBUTARIO/2021/DJ1949.sql  
 * 
 */


--CAMPO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" 			WHERE "Esquema" = 'DJ1949';

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', '', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'Cuenta', '', '', 37, 'Cuenta', '', 'TEXT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '4. DATOS DE CONTROL', 36, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'IdEventoCapitalCliente', '', '', 45, 'IdEventoCapitalCliente', '', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'IdDocumento', '', '', 41, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'VboUsuario', '', 'VistoBueno', 42, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'Observaciones', '', '', 43, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'FuenteDatos', '', '', 44, 'Fuente', 'Ayuda de Fuente', 'TEXT', 40, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'MtoDivReaAfecGCCCIDPC_C6', 'C6', '', 7, 'Afecto impuestos, IDPC a contar 01.01.2017', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 6, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'MtoDivReaAfecGCCCIDPCAL311216_C7', 'C7', '', 8, 'Afecto impuestos, IDPC hasta 31.12.2016', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 7, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'MtoDivReaAfecGCCDIDPCVol_C8', 'C8', '', 9, 'Afecto impuestos, IDPC Voluntario', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 8, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'MtoDivReaAfecGCSinDer_C9', 'C9', '', 10, 'Afecto impuestos, sin derecho a credito', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 9, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'MtoDivReaREXTribCumpRentasRap_C10', 'C10', '', 11, 'Renta Trib. cumplida, Registro RAP y DIf. a ex 14ter', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 10, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'MtoDivReaREXTribCumpOtrasRentas_C11', 'C11', '', 12, 'Renta Trib. otras rent. sin prioridad', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 11, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'MtoDivReaREXTribCumpExcDist_C12', 'C12', '', 13, 'Renta Trib. Exceso distribuciones', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 12, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'MtoDivReaREXTribCumpUtiISFUT_C13', 'C13', '', 14, 'Renta Trib. Utilidad afectada', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 13, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'MtoDivReaREXTribCumpRentasISFUT_C14', 'C14', '', 15, 'Renta Trib. Rentas gen. hasta 31.12.1983', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 14, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'MtoDivReaREXRentExIGCAfecIAart11_C15', 'C15', '', 16, 'Renta Exentas. Imp. Global, Art.11', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 15, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'MtoDivReaREXRentExIGCIA_C16', 'C16', '', 17, 'Renta Exentas. Imp. Global, y/o imp. adic.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 16, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'MtoDivReaREXNoConstRenta_C17', 'C17', '', 18, 'Ing. no constit. Renta', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 17, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCDesde01012017RentAfecNoRestSDD_C18', 'C18', '', 19, 'no sujeto a restitucion hasta 31.12.2019,Ing. no constit. Renta', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 18, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCDesde01012017RentAfecNoRestCDD_C19', 'C19', '', 20, 'no sujeto a restitucion hasta 31.12.2019,Ing. no constit. Renta', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 19, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCDesde01012017RentAfecNoRestSDD_C20', 'C20', '', 21, 'no sujeto a restitucion gen. a contar 01.01.2020,sin derecho a dev.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 20, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCDesde01012017RentAfecNoRestCDD_C21', 'C21', '', 22, 'no sujeto a restitucion gen. a contar 01.01.2020,con derecho a dev.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 21, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCDesde01012017RentAfecRestSDD_C22', 'C22', '', 23, 'Rent.Afecta Sujeto a restitucion, Sin derechoa a Dev.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 22, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCDesde01012017RentAfecRestCDD_C23', 'C23', '', 24, 'Rent.Afecta Sujeto a restitucion, Con derechoa a Dev.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 23, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCDesde01012017RentExenRestSDD_C24', 'C24', '', 25, 'Rent.Excenta Sujeto a restitucion, Sin derechoa a Dev.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 24, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCDesde01012017RentExenRestCDD_C25', 'C25', '', 26, 'Rent.Excenta Sujeto a restitucion, Con derechoa a Dev.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 25, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCDesde01012017CredIPE_C26', 'C26', '', 27, 'Credito por IPE', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 26, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCHasta31122016RentAfecSDD_C27', 'C27', '', 28, 'Acum hasta 31.12.2016, Rent. Afecta, sin derecho a dev ', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 27, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCHasta31122016RentAfecCDD_C28', 'C28', '', 29, 'Acum hasta 31.12.2016, Rent. Afecta, con derecho a dev ', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 28, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCHasta31122016RentExenSDD_C29', 'C29', '', 30, 'Acum hasta 31.12.2016, Rent. Excenta, sin derecho a dev ', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 29, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCHasta31122016RentExenCDD_C30', 'C30', '', 31, 'Acum hasta 31.12.2016, Rent. Excenta, con derecho a dev ', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 30, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCHasta31122016CredIPE_C31', 'C31', '', 32, 'Acum hasta 31.12.2016, Credito por IPE', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 31, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CIGCImptoTasaAdicional_C32', 'C32', '', 33, 'Cred. Impuesto tasa adic. ex art 21', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 32, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'DevCapArt17N7LIR_C33', 'C33', '', 34, 'Devolucion capital Art.7 ', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 33, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'NumCertificado_C34', 'C34', '', 35, 'Devolucion capital Art.7 ', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 34, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'FecMovto', 'C1', '', 2, 'Fecha Dividendo', '', 'DATE_FILTER', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 1, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'RutSociedad', 'C2', '', 3, 'Rut Sociedad', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 2, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'RutCliente', 'C4', '', 5, 'Rut Beneficiario', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 4, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'SecEvento', '', '', 38, 'SecEvento', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '4. DATOS DE CONTROL', 37, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'TipoSociedad', 'C3', '', 4, 'Tipo Sociedad', 'Ayuda de Tipo Sociedad', 'TEXT', 2, 'INICIAL', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '1. DATOS DE LOS INFORMADOS', 3, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'CantCuotas3112', 'C5', '', 6, 'cantidad al 31/12', '', 'QTY', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 5, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'Monto', '', '', 36, 'Monto', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 35, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'Mercado', '', '', 39, 'Mercado', '', 'TEXT', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 38, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2020', 'Nemo', '', '', 40, 'Nemo', '', 'NEMO', 100, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '4. DATOS DE CONTROL', 39, 0);



----2021









INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', '', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'Cuenta', '', '', 37, 'Cuenta', '', 'TEXT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '4. DATOS DE CONTROL', 36, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'IdEventoCapitalCliente', '', '', 45, 'IdEventoCapitalCliente', '', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'IdDocumento', '', '', 41, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'VboUsuario', '', 'VistoBueno', 42, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'Observaciones', '', '', 43, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'FuenteDatos', '', '', 44, 'Fuente', 'Ayuda de Fuente', 'TEXT', 40, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'MtoDivReaAfecGCCCIDPC_C6', 'C6', '', 7, 'Afecto impuestos, IDPC a contar 01.01.2017', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 6, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'MtoDivReaAfecGCCCIDPCAL311216_C7', 'C7', '', 8, 'Afecto impuestos, IDPC hasta 31.12.2016', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 7, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'MtoDivReaAfecGCCDIDPCVol_C8', 'C8', '', 9, 'Afecto impuestos, IDPC Voluntario', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 8, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'MtoDivReaAfecGCSinDer_C9', 'C9', '', 10, 'Afecto impuestos, sin derecho a credito', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 9, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'MtoDivReaREXTribCumpRentasRap_C10', 'C10', '', 11, 'Renta Trib. cumplida, Registro RAP y DIf. a ex 14ter', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 10, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'MtoDivReaREXTribCumpOtrasRentas_C11', 'C11', '', 12, 'Renta Trib. otras rent. sin prioridad', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 11, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'MtoDivReaREXTribCumpExcDist_C12', 'C12', '', 13, 'Renta Trib. Exceso distribuciones', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 12, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'MtoDivReaREXTribCumpUtiISFUT_C13', 'C13', '', 14, 'Renta Trib. Utilidad afectada', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 13, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'MtoDivReaREXTribCumpRentasISFUT_C14', 'C14', '', 15, 'Renta Trib. Rentas gen. hasta 31.12.1983', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 14, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'MtoDivReaREXRentExIGCAfecIAart11_C15', 'C15', '', 16, 'Renta Exentas. Imp. Global, Art.11', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 15, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'MtoDivReaREXRentExIGCIA_C16', 'C16', '', 17, 'Renta Exentas. Imp. Global, y/o imp. adic.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 16, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'MtoDivReaREXNoConstRenta_C17', 'C17', '', 18, 'Ing. no constit. Renta', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 17, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCDesde01012017RentAfecNoRestSDD_C18', 'C18', '', 19, 'no sujeto a restitucion hasta 31.12.2019,Ing. no constit. Renta', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 18, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCDesde01012017RentAfecNoRestCDD_C19', 'C19', '', 20, 'no sujeto a restitucion hasta 31.12.2019,Ing. no constit. Renta', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 19, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCDesde01012017RentAfecNoRestSDD_C20', 'C20', '', 21, 'no sujeto a restitucion gen. a contar 01.01.2020,sin derecho a dev.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 20, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCDesde01012017RentAfecNoRestCDD_C21', 'C21', '', 22, 'no sujeto a restitucion gen. a contar 01.01.2020,con derecho a dev.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 21, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCDesde01012017RentAfecRestSDD_C22', 'C22', '', 23, 'Rent.Afecta Sujeto a restitucion, Sin derechoa a Dev.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 22, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCDesde01012017RentAfecRestCDD_C23', 'C23', '', 24, 'Rent.Afecta Sujeto a restitucion, Con derechoa a Dev.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 23, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCDesde01012017RentExenRestSDD_C24', 'C24', '', 25, 'Rent.Excenta Sujeto a restitucion, Sin derechoa a Dev.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 24, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCDesde01012017RentExenRestCDD_C25', 'C25', '', 26, 'Rent.Excenta Sujeto a restitucion, Con derechoa a Dev.', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 25, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCDesde01012017CredIPE_C26', 'C26', '', 27, 'Credito por IPE', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 26, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCHasta31122016RentAfecSDD_C27', 'C27', '', 28, 'Acum hasta 31.12.2016, Rent. Afecta, sin derecho a dev ', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 27, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCHasta31122016RentAfecCDD_C28', 'C28', '', 29, 'Acum hasta 31.12.2016, Rent. Afecta, con derecho a dev ', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 28, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCHasta31122016RentExenSDD_C29', 'C29', '', 30, 'Acum hasta 31.12.2016, Rent. Excenta, sin derecho a dev ', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 29, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCHasta31122016RentExenCDD_C30', 'C30', '', 31, 'Acum hasta 31.12.2016, Rent. Excenta, con derecho a dev ', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 30, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCHasta31122016CredIPE_C31', 'C31', '', 32, 'Acum hasta 31.12.2016, Credito por IPE', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 31, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CIGCImptoTasaAdicional_C32', 'C32', '', 33, 'Cred. Impuesto tasa adic. ex art 21', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 32, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'DevCapArt17N7LIR_C33', 'C33', '', 34, 'Devolucion capital Art.7 ', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 33, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'NumCertificado_C34', 'C34', '', 35, 'Devolucion capital Art.7 ', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 34, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'FecMovto', 'C1', '', 2, 'Fecha Dividendo', '', 'DATE_FILTER', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 1, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'RutSociedad', 'C2', '', 3, 'Rut Sociedad', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 2, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'RutCliente', 'C4', '', 5, 'Rut Beneficiario', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 4, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'SecEvento', '', '', 38, 'SecEvento', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '4. DATOS DE CONTROL', 37, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'TipoSociedad', 'C3', '', 4, 'Tipo Sociedad', 'Ayuda de Tipo Sociedad', 'TEXT', 2, 'INICIAL', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '1. DATOS DE LOS INFORMADOS', 3, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'CantCuotas3112', 'C5', '', 6, 'cantidad al 31/12', '', 'QTY', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 5, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'Monto', '', '', 36, 'Monto', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 35, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'Mercado', '', '', 39, 'Mercado', '', 'TEXT', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 38, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1949', '2021', 'Nemo', '', '', 40, 'Nemo', '', 'NEMO', 100, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '4. DATOS DE CONTROL', 39, 0);






