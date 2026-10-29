SET SCHEMA "BSCL";

/*==============================================================*/
/* Se incluyen nuevo archivo de carga masiva de Instrucciones   */
/*==============================================================*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Archivo"
("IdArchivo","IdEmpresa","CodigoArchivo","TieneSeparador","SeparadorColumnas","TieneCabecera","IdModulo","CodigoArchivoDetalle","CodigoArchivoPie")
VALUES((SELECT MAX("IdArchivo")+1 FROM "BSCL.Plataforma.BD::PLA.Archivo"),1,'CARGA_INGRESO_INSTRUCCIONES_MASIVA','S',';','N','ORD','','');

------------------------
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_TIPO_INSTRUCCION', 'STRING', 1, 2, 'S', '^(RV|RF|IF)$', 'Formato del Tipo de Instrumento no es correcto. Opciones RV|RF|IF', 'N', 0, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_OPERACION', 'STRING', 2, 2, 'S', '^(CO|VE|EN|RE)$', 'Formato del Código Operación no es correcto. Opciones CO|VE|EN|RE', 'N', 0, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_NEMOTECNICO', 'STRING', 3, 10, 'S', '', '', 'N', 0, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_EMISOR', 'STRING', 4, 10, 'N', '', '', 'N', 0, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_FECHA_VENCIMIENTO', 'STRING', 5, 10, 'N', '(\d{4})\-(\d{2})\-(\d{2})', 'Formato Fecha no cumple con el formato permitido aaaa-mm-dd', 'N', 0, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_FECHA_TRADE', 'STRING', 6, 10, 'S', '(\d{4})\-(\d{2})\-(\d{2})', 'Formato Fecha no cumple con el formato permitido aaaa-mm-dd', 'N', 0, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_CONDICION_LIQUIDACION', 'STRING', 7, 2, 'S', '^(PH|PM|CN|T3|T4|T5)$', 'Formato de la Condición de Liquidación no es correcto. Opciones PH|PM|CN|T3|T4|T5', 'N', 0, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_INTERMEDIARIO', 'STRING', 8, 10, 'S', '', '', 'N', 0, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_FOLIO_FACTURA', 'STRING', 9, 10, 'N', '', '', 'N', 0, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_CANTIDAD', 'NUMBER', 10, 10, 'S', '', '', 'N', 0, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_PRECIO', 'DOUBLE', 11, 26, 'N', '^([0-9]*[0-9]\.*[0-9]*)$', '', 'S', 8, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_TASA', 'DOUBLE', 12, 26, 'N', '^([0-9]*[0-9]\.*[0-9]*)$', '', 'S', 8, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_VALOR_RESCATE', 'NUMBER', 13, 26, 'N', '^([0-9]*[0-9]\.*[0-9]*)$', '', 'N', 8, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_TIPO_REAJUSTE', 'NUMBER', 14, 3, 'N', '', '', 'N', 0, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_TOTAL_COBROS', 'DOUBLE', 15, 26, 'S', '^([0-9]*[0-9]\.*[0-9]*)$', '', 'S', 8, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_TOTAL_MONTO', 'DOUBLE', 16, 26, 'S', '^([0-9]*[0-9]\.*[0-9]*)$', '', 'S', 8, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_DOCUMENTO_IDENTIDAD', 'STRING', 17, 40, 'N', '^(\d{7,9}\-([\dkK]))$', 'Formato del Documento Identidad no es correcto. Debe contar con guión y dígito verificador', 'N', 0, 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Formato"
("IdFormato","CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula")
VALUES((SELECT MAX("IdFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Formato" ),'CINS_PORTAFOLIO', 'STRING', 18, 10, 'N', '', '', 'N', 0, 'S');


------------------------
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ) AS "IdArchivoFormato",
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA') AS "IdArchivo",
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_TIPO_INSTRUCCION') AS "IdFormato"
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_OPERACION')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_NEMOTECNICO')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_EMISOR')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_FECHA_VENCIMIENTO')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_FECHA_TRADE')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_CONDICION_LIQUIDACION')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_INTERMEDIARIO')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_FOLIO_FACTURA')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_CANTIDAD')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_PRECIO')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_TASA')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_VALOR_RESCATE')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_TIPO_REAJUSTE')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_TOTAL_COBROS')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_TOTAL_MONTO')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_DOCUMENTO_IDENTIDAD')
FROM dummy;

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato"
SELECT (SELECT MAX("IdArchivoFormato")+1 FROM  BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" ),
	   (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'),
	   (SELECT "IdFormato" FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'CINS_PORTAFOLIO')
FROM dummy;

/*==============================================================*/
/* ROLLBACK */
/*==============================================================*/
/*
DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.ArchivoFormato" 
WHERE "IdArchivo" = (SELECT "IdArchivo" FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA');

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" like 'CINS_%';

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Archivo" WHERE "CodigoArchivo" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA';
*/
commit;
