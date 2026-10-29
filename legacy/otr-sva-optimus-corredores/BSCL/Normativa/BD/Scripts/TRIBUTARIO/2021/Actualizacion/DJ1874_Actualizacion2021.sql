
SET SCHEMA BSCL;


-- ::::: CARGA MASIVA
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1874' AND "Version" = 2020;
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1874' AND "Version" = 2021;
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1874' AND "Version" = 2022;

-- :::: 2020
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'ID', '', '', 1, 'Secuencia', '', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'RutEmisor', 'C3', '', 2, 'RUT Emisor', '', 'RUT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 1, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'RutInversionista', 'C4', '', 3, 'RUT Inversionista', '', 'RUT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 2, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'TipoContribuyente', 'C5', '', 4, 'Tipo Contribuyente', '', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 3, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'NemotecnicoSVS', 'C6', '', 5, 'Nemotecnico SVS', '', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 4, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'FechaOperacion', 'C7', '', 6, 'Fecha Operacion', '', 'DATE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 5, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'MonedaUnidadReajuste', 'C9', '', 7, 'Moneda o unidad de reajuste', '', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 6, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'NumeroCuentaDCV', 'C10', '', 8, 'N Cuenta DCV', '', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 7, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'MovimientoSaldoInicial', 'C14', '', 9, 'Saldo Inicial', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 8, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'MovimientoAdquision', 'C15', '', 10, 'Adquisicion', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 9, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'MovimientoEnajenacion', 'C16', '', 11, 'Enajenacion', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 10, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'MovimientoRescateAnticipado', 'C17', '', 12, 'Rescate Anticipado', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 11, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'MovimientoSaldoFinal', 'C18', '', 13, 'Saldo Final', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 12, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'SaldoFinalNominal', 'C19', '', 14, 'Saldo Final Nominal', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 13, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'InteresDevengadoMonedaUnidadReajuste', 'C20', '', 15, 'Interes devengado (en moneda o unidad de reajuste)', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 14, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'InteresDevengadoPesos', 'C21', '', 16, 'Interes devengado en pesos', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 15, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'ResultadoEnajenacionMonedaUnidadReajuste', 'C22', '', 17, 'Resultado enajenacion (en moneda o unidad de reajuste)', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 16, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'ResultadoEnajenacionPesos', 'C23', '', 18, 'Resultado enajenacion pesos', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 17, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'RetencionMonedaUnidadReajuste', 'C24', '', 19, 'Retencion Art.74 N7 (en moneda o unidad de reajuste)', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 18, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'RetencionPesos', 'C25', '', 20, 'Retencion Art.74 en pesos', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 19, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'Numero', 'C0', '', 21, 'Numero', '', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '2. DATOS DE CONTROL', 20, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'FolioCartera', '', '', 22, 'FolioCartera', '', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '2. DATOS DE CONTROL', 21, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'FechaAdquisicion', '', '', 23, 'FechaAdquisicion', 'Ayuda de FechaAdquisicion', 'DATE_FILTER', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '2. DATOS DE CONTROL', 22, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'IdDocumento', '', '', 24, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '2. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'VboUsuario', '', '', 25, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '2. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'Observaciones', '', '', 26, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '2. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'FuenteDatos', '', '', 27, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '2. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'IdCliente', '', '', 28, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '2. DATOS DE CONTROL', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'IdPortafolio', '', '', 29, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '2. DATOS DE CONTROL', 0, 0);

--3. DATOS VALORIZACION
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'TipoDocumento', '', '', 30, 'TipoDocumento', 'Ayuda de TipoDocumento', 'TEXT', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 23, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'Nemotecnico', '', '', 31, 'Nemotecnico', '', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. DATOS VALORIZACION', 24, 0);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'IdTipoOperacion', '', '', 32, 'TipoOperacion', 'Ayuda de TipoOperacion', 'TEXT', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 25, 0);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'IdTipoProducto', '', '', 33, 'TipoProducto', 'Ayuda de TipoProducto', 'TEXT', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 26, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'IdClasificacionTipoInstrumento', '', '', 34, 'ClasificacionTipoInstrumento', 'Ayuda de ClasificacionTipoInstrumento', 'TEXT', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 27, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'IdTipoInstrumento', '', '', 35, 'TipoInstrumento', 'Ayuda de TipoInstrumento', 'TEXT', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 28, 0);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'IdInstrumento', '', '', 36, 'IdInstrumento', '', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '3. DATOS VALORIZACION', 0, 0);
----DESDE AQUI
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'InsFechaVencimiento', '', '', 37, 'FechaVencimiento', 'Ayuda de FechaVencimiento', 'DATE', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 29, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'InsFechaEmision', '', '', 38, 'FechaEmision', 'Ayuda de FechaEmision', 'DATE', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 30, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'IdMonedaInstrumento', '', '', 39, 'MonedaInstrumento', 'Ayuda de MonedaInstrumento', 'TEXT', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 31, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'IdMonedaEmisionInstrumento', '', '', 40, 'MonedaEmisionInstrumento', 'Ayuda de MonedaEmisionInstrumento', 'TEXT', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 32, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'IdEmisor', '', '', 41, 'Emisor', 'Ayuda de Emisor', 'TEXT', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 33, 0);
/*
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'IdCondicionDeLiquidacion', '', '', 42, 'IdCondicionDeLiquidacion', 'Ayuda de IdCondicionDeLiquidacion', 'TEXT', 0, '', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 0, 0);
*/
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'FechaLiquidacion', '', '', 43, 'FechaLiquidacion', 'Ayuda de FechaLiquidacion', 'DATE', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 34, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'Cantidad', '', '', 44, 'Cantidad', 'Ayuda de Cantidad', 'QTY', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 35, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'Tasa', '', '', 46, 'Tasa', 'Ayuda de Tasa', 'DECIMAL', 0, '', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 0, 4);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'Precio', '', '', 47, 'Precio', 'Ayuda de Precio', 'DECIMAL', 0, '', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '3. DATOS VALORIZACION', 36, 4);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'ValIndicadorValorizado', '', '', 48, 'Valorizado', 'Ayuda de Valorizado', 'TEXT', 0, '', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. RESULTADO VALORIZACION', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'ValResultado', '', '', 49, 'Resultado', 'Ayuda de Resultado', 'TEXT', 0, '', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. RESULTADO VALORIZACION', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'ValMontoMonedaInstrumento', '', '', 50, 'MontoMonedaInstrumento', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '4. RESULTADO VALORIZACION', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'ValMontoMonedaSistema', '', '', 51, 'MontoMonedaSistema', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '4. RESULTADO VALORIZACION', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'ValMontoMonedaInstrumentoFechaVenta', '', '', 52, 'MontoMonedaInstrFechaVenta', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '4. RESULTADO VALORIZACION', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'ValMontoMonedaSistemaFechaVenta', '', '', 53, 'MontoMonedaSistemaFechaVenta', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '4. RESULTADO VALORIZACION', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'ValMontoDevengadoAMonedaInstrumento', '', '', 54, 'MontoDevengadoAMonedaInstrumento', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '4. RESULTADO VALORIZACION', 0, 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1874', '2020', 'ValMontoDevengadoAMonedaSistema', '', '', 55, 'MontoDevengadoAMonedaSistema', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '4. RESULTADO VALORIZACION', 0, 0);


--2021
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
SELECT "Esquema", '2021' AS "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales"
FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1874' AND "Version" = 2020; 


--2022
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
SELECT "Esquema", '2022' AS "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales"
FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1874' AND "Version" = 2021; 






/* DELETE A HOMOLOGACION*/
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'DJ1874' AND "Version" = 2020 AND "Tipo"='MONEDAREAJUSTE';

/* INSERT A HOMOLOGACION*/
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2020','MONEDAREAJUSTE','PESO CHILENO','CLP','1','ACT'
FROM DUMMY;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2020','MONEDAREAJUSTE','DOLAR','USD','2','ACT'
FROM DUMMY;
 
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2020','MONEDAREAJUSTE','EURO','EUR','3','ACT'
FROM DUMMY;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2020','MONEDAREAJUSTE','UNIDAD DE FOMENTO','UF','4','ACT'
FROM DUMMY;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2020','MONEDAREAJUSTE','INDICE VALOR PROMEDIO','IVP','5','ACT'
FROM DUMMY;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2020','MONEDAREAJUSTE','OTRA','','6','ACT'
FROM DUMMY;

DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'DJ1874' AND "Version" = 2021 AND "Tipo"='MONEDAREAJUSTE';

/* INSERT A HOMOLOGACION*/
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2021','MONEDAREAJUSTE','PESO CHILENO','CLP','1','ACT'
FROM DUMMY;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2021','MONEDAREAJUSTE','DOLAR','USD','2','ACT'
FROM DUMMY;
 
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2021','MONEDAREAJUSTE','EURO','EUR','3','ACT'
FROM DUMMY;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2021','MONEDAREAJUSTE','UNIDAD DE FOMENTO','UF','4','ACT'
FROM DUMMY;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2021','MONEDAREAJUSTE','INDICE VALOR PROMEDIO','IVP','5','ACT'
FROM DUMMY;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2021','MONEDAREAJUSTE','OTRA','','6','ACT'
FROM DUMMY;



DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'DJ1874' AND "Version" = 2022 AND "Tipo"='MONEDAREAJUSTE';

/* INSERT A HOMOLOGACION*/
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2022','MONEDAREAJUSTE','PESO CHILENO','CLP','1','ACT'
FROM DUMMY;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2022','MONEDAREAJUSTE','DOLAR','USD','2','ACT'
FROM DUMMY;
 
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2022','MONEDAREAJUSTE','EURO','EUR','3','ACT'
FROM DUMMY;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2022','MONEDAREAJUSTE','UNIDAD DE FOMENTO','UF','4','ACT'
FROM DUMMY;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2022','MONEDAREAJUSTE','INDICE VALOR PROMEDIO','IVP','5','ACT'
FROM DUMMY;

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
SELECT  "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1874','2022','MONEDAREAJUSTE','OTRA','','6','ACT'
FROM DUMMY;

delete FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3288;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(3288, 'BSCL.Normativa.BD.Vistas::TRI.DJ1874.PreDatos', 47, 'Q', '', '', '', 'S');



---complementos
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.Complemento" WHERE "Esquema"='DJ1874' AND  "CodigoComplemento"='VALORIZARDECLARACION';

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento"
("Esquema", "CodigoComplemento", "Descripcion")
VALUES('DJ1874', 'VALORIZARDECLARACION', 'Valorizar Declaracion');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4306007, 'PLUGIN_NOR.jar@cl.bcs.normativa.declaraciones.DJ1874.ValorizarDeclaracion', 1, 'J', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4306007);


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_REALIZAR_VALORIZACION_A_REGISTROS_EN_LA_DECLARACION_JURADA',
'CL',
'Realizar valorizacion a registros en la declaracion jurada',
'RELVAL'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_REALIZAR_VALORIZACION_A_REGISTROS_EN_LA_DECLARACION_JURADA');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_CARGA_SISTEMA',
'CL',
'Carga Sistema',
'CARSIS'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_CARGA_SISTEMA');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_CARGA_EXTERNA',
'CL',
'Carga Externa',
'CAREXT'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_CARGA_EXTERNA');


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_VALORIZAR_DECLARACION',
'CL',
'Valorizar Declaracion',
'VALDEC'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_VALORIZAR_DECLARACION');

 

/* PARAMETRO DJ1874 INCLUIR  PACTOS EN CARGA INICIAL */
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'DJ1874_INCLUIR_PACTOS'AS "CodigoParametro",'Incluye los pactos desde la carga inicial en la DJ 1874' AS "Descripcion"
, 'N' AS "Valor"
, 'DJ1874' AS  "IdNormativa"
, 'S' AS "EsModificableUsuario"
, '' AS "Uso"
, 'N' AS "ValorDefecto"
, 'S,N' AS "ValoresPosibles"
, 'S' AS "ModificablePorUsuario"
, '' AS  "ModificablePostProduccion"
, '' AS  "CreadorParametro"
, CURRENT_UTCTIMESTAMP AS "FechaCreacion"
, '' AS "ModificadorParametro"
, CURRENT_UTCTIMESTAMP AS "FechaModificacion"
, '' AS "Observaciones"
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='DJ1874_INCLUIR_PACTOS' );


INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma", "Descripcion", "MaximoIntentos", "Modulo", "NombreClasePrograma","TipoClasePrograma", "EsPauta")
SELECT
'VALORIZAR_DECLARACION' 					AS "IdTipoPrograma"
, 'Valorizar Declaracion jurada'				AS "Descripcion"
, 1 											AS "MaximoIntentos"
, 'Normativa'						 			AS "Modulo"
, 'cl.bcs.normativa.jobs.BcsJobValorizarDeclaracion'  AS "NombreClasePrograma"
, 'JAVA' 										AS "TipoClasePrograma"
, 'N' 											AS "EsPauta"
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='VALORIZAR_DECLARACION');


/* COMPLEMENTO CALCULO 4% */
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.Complemento" WHERE "Esquema"='DJ1874' AND "CodigoComplemento"='CALCULO4PORCIENTO';

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento"
("Esquema", "CodigoComplemento", "Descripcion")
VALUES('DJ1874', 'CALCULO4PORCIENTO', 'Retencion 4% Art. 104');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3616, 'BSCL.Normativa.BD.Vistas::TRI.DJ1874.ConsultarNemos', 2, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3616);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3617, 'BSCL.Plataforma.BD.Vistas::TRI.ConsultarClientesComplemento4Porciento', 2, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3617);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3618, 'BSCL.Plataforma.BD.Vistas::TRI.ConsultarNemoComplemento4Porciento', 2, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3618);




