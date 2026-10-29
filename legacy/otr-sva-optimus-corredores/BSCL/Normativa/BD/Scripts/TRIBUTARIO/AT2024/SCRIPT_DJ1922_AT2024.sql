SET SCHEMA "BSCL";



ALTER TABLE "BSCL.Normativa.BD::TRI.DatosDJ1922" ALTER ("NumeroSerieCuotas" BIGINT CS_FIXED NULL DEFAULT 900);


ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1922" ADD ("DetBenNetoUtilPer_C133" DECIMAL(26,8) CS_FIXED DEFAULT 0);
ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1922" ADD ("DetBenNetoIntPer_C134" DECIMAL(26,8) CS_FIXED DEFAULT 0);
ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1922" ADD ("DetBenNetoDivPer_C135" DECIMAL(26,8) CS_FIXED DEFAULT 0);
ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1922" ADD ("DetBenNetoGanCapPer_C136" DECIMAL(26,8) CS_FIXED DEFAULT 0);
ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1922" ADD ("DetBenNetoPerGastDev_C137" DECIMAL(26,8) CS_FIXED DEFAULT 0);
ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1922" ADD ("NumSerieCuotasParticipe_C138" NVARCHAR(30) DEFAULT '');


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1922', '2023', 'DetBenNetoUtilPer_C133', '', 77, 'UTILIDADES PERCIBIDAS', 'AMOUNT', 0, '65', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 76, '', 'C133', 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1922', '2023', 'DetBenNetoIntPer_C134', '', 78, 'INTERESES PERCIBIDOS', 'AMOUNT', 0, '65', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 77, '', 'C134', 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1922', '2023', 'DetBenNetoDivPer_C135', '', 79, 'DIVIDENDOS PERCIBIDOS', 'AMOUNT', 0, '65', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 78, '', 'C135', 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1922', '2023', 'DetBenNetoGanCapPer_C136', '', 80, 'GANANCIAS DE CAPITAL PERCIBIDAS', 'AMOUNT', 0, '65', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 79, '', 'C136', 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1922', '2023', 'DetBenNetoPerGastDev_C137', '', 81, 'PERDIDAS Y GASTOS DEVENGADOS', 'AMOUNT', 0, '65', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 80, '', 'C137', 0);

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1922', '2023', 'NumSerieCuotasParticipe_C138', '', 17, 'NUMERO DE SERIE DE CUOTAS DE PROPIEDAD DEL PARTICIPE O APORTANTE', 'ID', 0, '15', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '2. INFORMACION DE LOS APORTES AL FONDO', 16, '', 'C138', 0);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1922', '2023', 'H3063', 'NORMA', 'S', 'Si el Fondo de Inversión es de tipo "Público o Mutuo", la columna C4 RUT del Fondo debe ser distinto a la columna C80 RUN del Fondo', 'E', 'Si [C5] = "I" o "F", entonces; [C4] <> [C80]');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1922', '2023', 'H3064', 'NORMA', 'S', 'La columna C3 RUT Administradora debe ser distinto a la columna C4 RUT del Fondo', 'E', '[C3] <> [C4]');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1922', '2023', 'H3065', 'NORMA', 'S', 'La columna C4 RUT del Fondo debe ser distinto a la columna C7 RUT Participe o Aportante', 'E', '[C4] <> [C7]');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1922', '2023', 'H3066', 'NORMA', 'S', 'Si campo "Tipo Operación" es <> 7, los campos "Moneda o Unidad de reajuste", "Monto nominal aportes al fondo en $", "Serie cuotas de propiedad del partícipe o aportante", "N° cuotas serie de propiedad del participe o aportante", “Partícipe o aportante cumple requisitos art.82 Ley N° 20.712”, “Cuotas Fondos que cumplen requisitos art.82 Ley N°20.712”,"Valor de la cuota", "Valor mercado al cierre del ejercicio" y “% de Participación en patrimonio Fondo al cierre del Ejercicio” = cero o blanco', 'E', 'si [C10] = 1 .o. 2 .o. 3 .o. 4 .o. 5 .o. 6, entonces [C13] .y. [C138] .y. [C15] .y.[C16] .y. [C17] .y. [C18] .y. [C64] .y. [C19] = 0 o blanco');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1922', '2023', 'H3067', 'NORMA', 'S', 'El Monto Beneficio Neto Positivo debe ser igual a la suma de las columnas "Utilidades Percibidas", "Intereses Percibidos", "Dividendos Percibidos", "Ganancias de Capital Percibidos" descontado la columna "Pérdidas o Gastos Devengados", en caso que dicha operación entregue un número positivo, de lo contrario cero o vacío', 'E', 'Si [C133] + [C134] + [C135] + [C136] - [C137] > 0 entonces; [C113] = [C133] + [C134] + [C135] + [C136] - [C137]; sino cero o blanco');


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1922', '2023', 'H3068', 'NORMA', 'S', 'El Monto Beneficio Neto Negativo debe ser igual al valor absoluto de la suma de las columnas "Utilidades Percibidas", "Intereses Percibidos", "Dividendos Percibidos", "Ganancias de Capital Percibidos" descontadas la columna "Pérdidas o Gastos Devengados", en caso que dicha operación sea menor a cero, de lo contario cero o vacío', 'E', 'Si [C133] + [C134] + [C135] + [C136] - [C137] < 0  entonces; [C114] = (-1) * { [C133] + [C134] + [C135] + [C136] - [C137] }; sino cero o blanco');


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1922', '2023', 'H3069', 'NORMA', 'S', 'Si el campo "Tipo de Operación" es igual a 7 el campo "Fecha Operación" debe corresponder al 31.12.del año comercial que se informa (AT-1), ejemplo, en AT 2024 debe indicar 31.12.2023', 'E', '[C10] = 7 entonces el [C11] = 31/12/20XX, en que 20XX corresponde al año comercial que se informa AT-1.');


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1922', '2023', 'H3070', 'NORMA', 'S', 'Si el declarante de la DJ 1922 es sociedad administradora de fondos de inversión, se debe cumplir que todo RUT de Fondo distinto que se informa en la columna [C4] debe estar registrado en la columna "RUT FONDO INVERSIÓN" de la columna [C107] de la sección C', 'E', 'Si RUT declarante= [C3] correspondiente a RUT Sociedad Administradora de Fondos de Inversión, se debe cumplir que para todo [C4] único debe estar registrado en [C107]');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1922', '2023', 'H3071', 'NORMA', 'S', 'Para cada RUT Fondo de Inversión registrado en la columna [C107] de la sección C, se debe indicar obligatoriamente un monto distinto de blanco en las columnas del beneficio neto percibido, ya sea positivo o negativo, el cual podría ser cero pero no blanco.', 'E', 'Si [C107] > 0; entonces: [C113] <> blanco o vacío .o. [C114] <> blanco o vacío');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1922', '2023', 'H3072', 'NORMA', 'S', 'Si el Fondo de Inversión es de tipo "Público o Mutuo", el RUN del Fondo debe ser obligatorio y se debe registrar un valor de máximo 6 dígitos', 'E', 'Si [C5] = "I" o "F", entonces; [C80] Obligatorio y Largo (Cuerpo de [C80]) <=6 dígitos (antes de guion y sin considerar dígito verificador) ');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1922', '2023', 'H3073', 'NORMA', 'S', 'Las columnas "Beneficio Neto Percibido Positivo", "Beneficio Neto Percibido Negativo", "Utilidades Percibidas", "Intereses Percibidos", "Dividendos Percibidos", "Ganancias de Capital Percibidos" y "Pérdidas o Gastos Devengados" no pueden ser negativos ', 'E', '[C113] , [C114] , [C133] , [C134] , [C135] , [C136] , [C137] > = 0 o blanco');

UPDATE BSCL."BSCL.Normativa.BD::DOC.Validacion"
SET  "Descripcion"='Debe existir valor en  las columnas:“Rut de Soc. Administradora”, "RUT del Fondo , RUN del Fondo", “Tipo de Fondo”, “Nombre o Razón Social del Participe o Aportante”, “RUT del Participe o Aportante” .o. "TAX ID", “Código País de Residencia del Participe o Aportante”, "Tipo de Operación" y de la Sección B1 las columnas: "Fecha de la operacion", "Moneda o Unidad de Reajuste","Tributa como S.A"', "Formula" = '[C3], [C4], [C80], [C5], [C6], { [C7].o. [C8] }, [C9], [C10], [C11], [C12], [C65]  < > blanco .o. vacío .o. 0, dependiendo del tipo de dato'
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Codigo"='H1822';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Validacion"
SET  "Descripcion"='Los campos: "Rut de sociedad administradora", "Rut del fondo", "RUN del fondo", "rut del participe o aportante" y "RUT Fondo Inversión" de la sección C,  deben cumplir con modulo 11 (verificacion digito verificador) ', "Formula"='(C3), (C4), (C7)  (C80) y [C107]= verificación digito verificador'
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Codigo"='H1859';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Validacion"
SET  "Descripcion"='El campo "Si el campo C10 "Tipo de Operación"es igual a 7, el campo "Cuotas de fondos que cumplen requisitos art. 82  N°1 letra B) iii), Ley Adm. Fondos Contenida Art. 1° Ley 20712)" debe tomar valor 1, 2, ó 3', "Formula"='Si [C10] = 7, entonces: (C17) = { 1 .o. 2 .o. 3}'
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Codigo"='H1866';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Validacion"
SET  "Descripcion"='El Campo Tributa como S.A debe corresponde a: 1 = Fondo debe tributar como S.A. (conforme a lo dispuesto en el art. 92 de la LUF). 2 = Fondo No debe tributar como S.A.',  "Formula"='C65 = 1 .o. 2'
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Codigo"='H2668';


DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Campo"
WHERE
    "Esquema" = 'DJ1922' AND
    "Version" = '2023' AND
    (
        "Nombre" = 'DetBenNetoPerPOS_C113' OR
        "Nombre" = 'DetBenNetoPerNEG_C114'
    );


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1922', '2023', 'DetBenNetoPerPOS_C113', '', 75, 'BENEFICIO NETO PERCIBIDO (POSITIVO)', 'AMOUNT', 0, '65', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 74, '', 'C113', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1922', '2023', 'DetBenNetoPerNEG_C114', '', 76, 'BENEFICIO NETO PERCIBIDO (NEGATIVO)', 'AMOUNT', 0, '66', 'N', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', 75, '', 'C114', 0);


UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=1, "Titulo"='Secuencia', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='', "OrdenCargaExterna"=0, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='ID';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=2, "Titulo"='Numero', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='1', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='S', "Grupo"='1. DATOS DE LOS INFORMADOS', "OrdenCargaExterna"=1, "Ayuda"='', "CodigoSII"='C2', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='Numero';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=4, "Titulo"='RUT DEL FONDO MUTUO ', "TipoDato"='RUT', "Largo"=0, "ValorDefecto"='3', "Obligatorio"='S', "Editable"='N', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. DATOS DE LOS INFORMADOS', "OrdenCargaExterna"=3, "Ayuda"='', "CodigoSII"='C4', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='RutFondo';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=5, "Titulo"='RUN DEL FONDO MUTUO ', "TipoDato"='RUT', "Largo"=0, "ValorDefecto"='4', "Obligatorio"='S', "Editable"='N', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. DATOS DE LOS INFORMADOS', "OrdenCargaExterna"=4, "Ayuda"='', "CodigoSII"='C80', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='RunFondo';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=8, "Titulo"='NOMBRE O RAZON SOCIAL DEL PARTICIPE O APORTANTE', "TipoDato"='TEXT', "Largo"=250, "ValorDefecto"='7', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. DATOS DE LOS INFORMADOS', "OrdenCargaExterna"=7, "Ayuda"='', "CodigoSII"='C6', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='NombreParticipe';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=9, "Titulo"='RUT DEL PARTICIPE', "TipoDato"='RUT_CLIENTE', "Largo"=0, "ValorDefecto"='8', "Obligatorio"='S', "Editable"='N', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='S', "Grupo"='1. DATOS DE LOS INFORMADOS', "OrdenCargaExterna"=8, "Ayuda"='', "CodigoSII"='C7', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='RutCliente';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='TipoOperacionFondos1922', "Orden"=12, "Titulo"='TIPO DE OPERACION', "TipoDato"='NEMO', "Largo"=100, "ValorDefecto"='11', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. DATOS DE LOS INFORMADOS', "OrdenCargaExterna"=11, "Ayuda"='', "CodigoSII"='C10', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='TipoOperacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='TipoReajuste1922', "Orden"=14, "Titulo"='MONEDA O UNIDAD DE REAJUSTE', "TipoDato"='NEMO', "Largo"=100, "ValorDefecto"='13', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO', "OrdenCargaExterna"=13, "Ayuda"='', "CodigoSII"='C12', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='MonedaReajuste';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=15, "Titulo"='MONTO NOMINAL DE  APORTES AL FONDO EN   $', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO', "OrdenCargaExterna"=14, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='MontoNominalAportes';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=16, "Titulo"='MONTO NOMINAL DE  APORTES AL FONDO EN  $ Actualizado', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='14', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO', "OrdenCargaExterna"=15, "Ayuda"='', "CodigoSII"='C13', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='MontoNominalAportesActualizado';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=22, "Titulo"='VALOR DE MERCADO AL CIERRE DEL EJERCICIO', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='20', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO', "OrdenCargaExterna"=21, "Ayuda"='', "CodigoSII"='C64', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='NumCuotasSerieParticipe';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=3, "Titulo"='RUT DE SOC. ADMINISTRADORA', "TipoDato"='RUT', "Largo"=0, "ValorDefecto"='2', "Obligatorio"='S', "Editable"='N', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. DATOS DE LOS INFORMADOS', "OrdenCargaExterna"=2, "Ayuda"='', "CodigoSII"='C3', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='RutAdministradora';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=6, "Titulo"='Fondo', "TipoDato"='TEXT', "Largo"=100, "ValorDefecto"='5', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. DATOS DE LOS INFORMADOS', "OrdenCargaExterna"=5, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='Fondo';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='TipoFondo1922', "Orden"=7, "Titulo"='TIPO DE FONDO', "TipoDato"='NEMO', "Largo"=100, "ValorDefecto"='6', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. DATOS DE LOS INFORMADOS', "OrdenCargaExterna"=6, "Ayuda"='', "CodigoSII"='C5', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='TipoFondo';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=10, "Titulo"='TAX-ID', "TipoDato"='TEXT', "Largo"=250, "ValorDefecto"='9', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. DATOS DE LOS INFORMADOS', "OrdenCargaExterna"=9, "Ayuda"='', "CodigoSII"='C8', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='TaxId';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='CodigoPais1922', "Orden"=11, "Titulo"='CODIGO PAIS DE RESIDENCIA DEL PARTICIPE O APORTANTE', "TipoDato"='NEMO', "Largo"=100, "ValorDefecto"='10', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. DATOS DE LOS INFORMADOS', "OrdenCargaExterna"=10, "Ayuda"='', "CodigoSII"='C9', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CodigoPais';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=13, "Titulo"='FECHA DE LA OPERACION', "TipoDato"='DATE_FILTER', "Largo"=0, "ValorDefecto"='12', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO', "OrdenCargaExterna"=12, "Ayuda"='', "CodigoSII"='C11', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='FechaInversion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=18, "Titulo"='NUMERO DE CUOTAS DE LA SERIE DE PROPIEDAD DEL PARTICIPE O APORTANTE', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='16', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO', "OrdenCargaExterna"=17, "Ayuda"='', "CodigoSII"='C15', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CantidadCuotasParticipe';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='ParticipeCumpleRequisitos1922', "Orden"=19, "Titulo"='PARTICIPE CUMPLE REQUISITOS (Art.82 N1  letra B) iii), Ley Adm.Fondos contenida Art. 1Ã‚ Ley 20.712)', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='17', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO', "OrdenCargaExterna"=18, "Ayuda"='', "CodigoSII"='C16', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='ParticipeCumpleRequisitos';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='CuotasCumplenRequisitos1922', "Orden"=20, "Titulo"='CUOTAS DE FONDOS QUE CUMPLEN REQUISITOS Art.82', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='18', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO', "OrdenCargaExterna"=19, "Ayuda"='', "CodigoSII"='C17', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CuotasCumplenRequisitos';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=21, "Titulo"='VALOR DE LA CUOTA ($)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='19', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO', "OrdenCargaExterna"=20, "Ayuda"='', "CodigoSII"='C18', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='ValorCuotaPesos';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=23, "Titulo"='% DE PARTICIPACION EN EL PATRIMONIO DEL FONDO AL 31.12', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='21', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO', "OrdenCargaExterna"=22, "Ayuda"='', "CodigoSII"='C19', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='PorcentajeParticipacion31Dic';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=38, "Titulo"='DIVIDENDOS, REMESAS O DISTRIBUCIONES SIN DERECHO A CRÃ‰DITO', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='33', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=37, "Ayuda"='', "CodigoSII"='C69', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DivRemDistAfectGCSINDC';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=39, "Titulo"='RENTAS TRIB. CUMPLIDA RENTAS PROVENIENTES DEL REGISTRO RAP Y DIFERENCIA INICIAL DE SOCIEDAD', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='34', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=38, "Ayuda"='', "CodigoSII"='C96', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DisRemRentasTribCProvRap_C96';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=73, "Titulo"='NUMERO CERT.', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='63', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=72, "Ayuda"='', "CodigoSII"='C43', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='NumeroCertificado';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=24, "Titulo"='VALOR NOMINAL CUOTAS RESCATADAS O ENAJENADAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='22', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS', "OrdenCargaExterna"=23, "Ayuda"='', "CodigoSII"='C20', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='ValNominalCuotasRescatadas';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=25, "Titulo"='CANTIDAD DE CUOTAS RESCATADAS O ENAJENADAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='23', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS', "OrdenCargaExterna"=24, "Ayuda"='', "CodigoSII"='C21', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CantidadCuotasRescatadas';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=26, "Titulo"='DIFERENCIA OBTENIDA EN EL RESCATE O ENAJENACION DE CUOTAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS', "OrdenCargaExterna"=25, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DiferenciaPorRescateOEnajenacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=27, "Titulo"='DIFERENCIA OBTENIDA EN EL RESCATE O ENAJENACION DE CUOTAS (ACTUALIZADA)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='24', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS', "OrdenCargaExterna"=26, "Ayuda"='', "CodigoSII"='C22', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DiferenciaPorRescateOEnajenacionActualizado';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=28, "Titulo"='DIFERENCIA EN EL RESCATE DE CUOTAS DE FONDOS DE INVERSION ART.107', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS', "OrdenCargaExterna"=27, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DifPorRescateOEnajenacionCFIAcogido107';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=29, "Titulo"='DIFERENCIA EN EL RESCATE DE CUOTAS DE FONDOS DE INVERSION ART.107 (ACTUALIZADA)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='25', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS', "OrdenCargaExterna"=28, "Ayuda"='', "CodigoSII"='C23', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DifPorRescateOEnajenacionCFIAcogido107Actualizado';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=30, "Titulo"='VALOR NOMINAL INVERSION A LA FECHA DE LIQUIDACION O DISMINUCION DE CAPITAL', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='26', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS', "OrdenCargaExterna"=29, "Ayuda"='', "CodigoSII"='C24', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='ValNominalInversionaFechaLiquidacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=31, "Titulo"='CUOTAS ENAJENADAS LIQUIDACION DEL FONDO O RESCATADAS POR DISMINUCION DE CAPITAL', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='27', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS', "OrdenCargaExterna"=30, "Ayuda"='', "CodigoSII"='C25', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CantCuotasCedidasPorLiquidacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=32, "Titulo"='DIFERENCIA VALOR DE CUOTAS A FECHA LIQUIDACION V/S VALOR A FECHA DE ADQUISICION', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS', "OrdenCargaExterna"=31, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DiferenciaValorCuotasFechaLiquidacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=33, "Titulo"='DIFERENCIA VALOR DE CUOTAS A FECHA LIQUIDACION V/S VALOR A FECHA DE ADQUISICION (ACTUALIZADO)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='28', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS', "OrdenCargaExterna"=32, "Ayuda"='', "CodigoSII"='C26', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DiferenciaValorCuotasFechaLiquidacionActualizado';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=35, "Titulo"='DIVIDENDOS, REMESAS O DISTRIBUCIONES CON CRÃ‰DITO POR IDPC GENERADOS A CONTAR DEL 01.01.2017', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='30', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=34, "Ayuda"='', "CodigoSII"='C66', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DivRemDistAfectGCConIDPC';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=36, "Titulo"='CON CDIVIDENDOS, REMESAS O DISTRIBUCIONES RÃ‰DITO POR IDPC ACUMULADOS HASTA EL 31.12.2016', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='31', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=35, "Ayuda"='', "CodigoSII"='C67', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DivRemDistAfectGCConIDPCal31122016';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=37, "Titulo"='DIVIDENDOS, REMESAS O DISTRIBUCIONES CON CRÃ‰DITO POR PAGO DE IDPC VOLUNTARIO', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='32', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=36, "Ayuda"='', "CodigoSII"='C68', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DivRemDistAfectGCConIDPCVOL';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=40, "Titulo"='RENTAS TRIB. CUMPLIDA OTRAS RENTAS PERCIBIDAS SIN PRIORIDAD EN SU ORDEN DE IMPUTACIÃ“N', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='35', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=39, "Ayuda"='', "CodigoSII"='C97', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DisRemRentasTribCORP_C97';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=41, "Titulo"='RENTAS TRIB. CUMPLIDA EXCESO DISTRIBUCIONES DESPROPORCIONADAS (NÂ°9 ART.14 LETRA A)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='36', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=40, "Ayuda"='', "CodigoSII"='C98', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DisRemRentasTribCExDD_C98';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=42, "Titulo"='RENTAS TRIB. CUMPLIDA UTILIDADES AFECTADAS CON IMPUESTO SUSTITUTIVO AL FUT', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='37', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=41, "Ayuda"='', "CodigoSII"='C99', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DisRemRentasTribCUtAFFut_C99';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=43, "Titulo"='RENTAS TRIB. CUMPLIDA RENTAS GENE. HASTA EL 31.12.1983 Y/O UTILIDADES AFECTADAS ', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='38', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=42, "Ayuda"='', "CodigoSII"='C70', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DivRemDistNoRentaFUT';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=44, "Titulo"='RENTAS EXENTAS DE IMPUESTO GLOBAL COMPLEMENTARIO (IGC) , AFECTAS A IMPUESTO ADICIONAL', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='39', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=43, "Ayuda"='', "CodigoSII"='C100', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DisRemRentasExIGCAfecImptoAdi_C100';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=45, "Titulo"='RENTAS EXENTAS DE IMPUESTO GLOBAL COMPLEMENTARIO (IGC) Y/O IMPUESTO ADICIONAL (IA)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=44, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DividendosExentosIGCoAdicional';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=46, "Titulo"='RENTAS EXENTAS DE IMPUESTO GLOBAL COMPLEMENTARIO (IGC) Y/O IMPUESTO ADICIONAL (IA) (ACTUALIZADO)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='40', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=45, "Ayuda"='', "CodigoSII"='C28', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DividendosExentosIGCoAdicionalActualizado';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=47, "Titulo"='DIVIDENDOS REMESAS O DISTRIBUCIONES NO CONSTITUTIVOS DE RENTA', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=46, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DividendosNoConstitutivosDeRenta';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=48, "Titulo"='DIVIDENDOS REMESAS O DISTRIBUCIONES NO CONSTITUTIVOS DE RENTA (ACTUALIZADO)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='41', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=47, "Ayuda"='', "CodigoSII"='C29', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DividendosNoConstitutivosDeRentaActualizado';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=49, "Titulo"='RENTAS O CANTIDADES DE FUENTE EXTRANJERA (', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='42', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=48, "Ayuda"='', "CodigoSII"='C31', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DivRemDistNoRentaFE';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=50, "Titulo"='DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 10%', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=49, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DividendosAfectosImptoUnicoTasa10';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=51, "Titulo"='DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 10% (ACTUALIZADO)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='43', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=50, "Ayuda"='', "CodigoSII"='C30', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DividendosAfectosImptoUnicoTasa10Actualizado';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=52, "Titulo"='DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 4%', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=51, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DividendosAfectosImptoUnicoTasa4';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=53, "Titulo"='DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO UNICO TASA 4% (ACTUALIZADO)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='44', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=52, "Ayuda"='', "CodigoSII"='C32', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DividendosAfectosImptoUnicoTasa4Actualizado';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=54, "Titulo"='DISTRIBUCIONES EFECTUADAS MEDIANTE LA DISMINUCIIN DEL VALOR DE CUOTA DEL FONDO NO IMPUTADA A CAPITAL', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='45', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=53, "Ayuda"='', "CodigoSII"='C33', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DistribucionXDiminucionNoImputadaCapital';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=55, "Titulo"='DEVOLUCIONES DE CAPITAL', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=54, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DevolucionesDeCapital';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=56, "Titulo"='DEVOLUCIONES DE CAPITAL (ACTUALIZADO)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='46', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. MONTO DE DISTRIBUCIONES O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)', "OrdenCargaExterna"=55, "Ayuda"='', "CodigoSII"='C34', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DevolucionesDeCapitalActualizado';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=57, "Titulo"='NO SUJ. A REST. GEN. HASTA EL 31.12.2019 SIN DER. A DEV. NO SUJETOS A REST. GEN. HASTA EL 31.12.2019', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='47', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=56, "Ayuda"='', "CodigoSII"='C71', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredImpGloCompNSRSD';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=58, "Titulo"='NO SUJ. A REST. GEN. HASTA EL 31.12.2019 CON DER. A DEV. NO SUJETOS A REST. GEN. HASTA EL 31.12.2019', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='48', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=57, "Ayuda"='', "CodigoSII"='C72', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredImpGloCompNSRCD';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=59, "Titulo"='NO SUJ. A REST. GEN. DESDE 01.01 .2020 SIN DER. A DEV. NO SUJ. A REST. GEN.  DESDE 01.01.2020', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='49', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=58, "Ayuda"='', "CodigoSII"='C101', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredIGCAcumD01012017RANSRD01012020SDD_C101';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=60, "Titulo"='NO SUJ. A REST. GEN. DEL 01.01 .2020 CON DER. A DEV. NO SU. A RES. GEN. DESDE DEL 01.01.2020', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='50', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=59, "Ayuda"='', "CodigoSII"='C102', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredIGCAcumD01012017RANSRD01012020CDD_C102';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=61, "Titulo"='SUJETOS A RESTITUCIÃ“N SIN DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS AFECTAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='51', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=60, "Ayuda"='', "CodigoSII"='C73', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredImpGloCompSRSD';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=62, "Titulo"='SUJETOS A RESTITUCIÃ“N CON DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS AFECTAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='52', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=61, "Ayuda"='', "CodigoSII"='C74', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredImpGloCompSRCD';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=63, "Titulo"='ASOCIADOS A RENTAS EXENTAS SIN DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS EXENTAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='53', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=62, "Ayuda"='', "CodigoSII"='C103', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredIGCAcumD01012017RXSRSDD_C103';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=64, "Titulo"='ASOCIADOS A RENTAS EXENTAS CON DERECHO A DEVOLUCIÃ“N SUJETOS A RESTITUCIÃ“N RENTAS EXENTAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='54', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=63, "Ayuda"='', "CodigoSII"='C104', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredIGCAcumD01012017RXSRCDD_C104';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=65, "Titulo"='CRÃ‰DITO POR IPE', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='55', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=64, "Ayuda"='', "CodigoSII"='C75', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredImpGloCompCTDCIF';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=66, "Titulo"='ACUMULADOS HASTA EL 31.12.2016 SIN DERECHO A DEVOLUCIÃ“N RENTAS AFECTAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='56', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=65, "Ayuda"='', "CodigoSII"='C77', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredImpGloCompSDD31122016';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=67, "Titulo"='ACUMULADOS HASTA EL 31.12.2016 CON DERECHO A DEVOLUCIÃ“N RENTAS AFECTAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='57', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=66, "Ayuda"='', "CodigoSII"='C76', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredImpGloCompCDD31122016';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=68, "Titulo"='ACUMULADOS HASTA EL 31.12.2016 SIN DERECHO A DEVOLUCIÃ“N RENTAS EXENTAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='58', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=67, "Ayuda"='', "CodigoSII"='C105', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredIGCAcumH31122016RXSDD_C105';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=69, "Titulo"='ACUMULADOS HASTA EL 31.12.2016 CON DERECHO A DEVOLUCIÃ“N RENTAS EXENTAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='59', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=68, "Ayuda"='', "CodigoSII"='C106', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredIGCAcumH31122016RXCDD_C106';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=70, "Titulo"='ACUMULADOS HASTA EL 31.12.2016 CRÃ‰DITO POR IPE', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='60', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=69, "Ayuda"='', "CodigoSII"='C78', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredImpGloCompCTDCIF31122016';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=71, "Titulo"='CRÃ‰DITO POR IMPUESTO TASA ADICIONAL EX. ART. 21 LIR.', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='61', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=70, "Ayuda"='', "CodigoSII"='C79', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='CredImpGloCompCITAExArt21';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=72, "Titulo"='MONTO INVERSION EN CUOTAS DE FONDOS DE INVERSION LEY NÂ° 18.815', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='62', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. CRÃ‰DITOS PARA IMPUESTO GLOBAL COMPLEMENTARIO O ADICIONAL', "OrdenCargaExterna"=71, "Ayuda"='', "CodigoSII"='C42', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='MontoInversionenCuotasCFIAnterior93';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=74, "Titulo"='RUT FONDO DE INVERSIÃ“N', "TipoDato"='RUT', "Largo"=0, "ValorDefecto"='64', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', "OrdenCargaExterna"=73, "Ayuda"='', "CodigoSII"='C107', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DetBenNetoPerRutFdo_C107';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=75, "Titulo"='BENEFICIO NETO PERCIBIDO (POSITIVO)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='65', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', "OrdenCargaExterna"=74, "Ayuda"='', "CodigoSII"='C113', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DetBenNetoPerPOS_C113';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='TributaSa1922', "Orden"=34, "Titulo"='TRIBUTA S.A.', "TipoDato"='TEXT', "Largo"=0, "ValorDefecto"='29', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. DETALLE DE OPERACIONES Y MOVIMIENTOS', "OrdenCargaExterna"=33, "Ayuda"='', "CodigoSII"='C65', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='TributaSa';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=17, "Titulo"='NUMERO DE SERIE DE CUOTAS DE PROPIEDAD DEL PARTICIPE O APORTANTE', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='15', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO', "OrdenCargaExterna"=16, "Ayuda"='', "CodigoSII"='C138', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='NumSerieCuotasParticipe_C138';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=76, "Titulo"='BENEFICIO NETO PERCIBIDO (NEGATIVO)', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='66', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', "OrdenCargaExterna"=75, "Ayuda"='', "CodigoSII"='C114', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DetBenNetoPerNEG_C114';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=82, "Titulo"='Secuencia Docto', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='S', "OcultoEnMantenedor"='S', "Comun"='S', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=0, "Ayuda"='Ayuda de Secuencia Docto', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='IdDocumento';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=83, "Titulo"='VoBo', "TipoDato"='TEXT', "Largo"=1, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=0, "Ayuda"='Ayuda de VoBo', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='VboUsuario';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=84, "Titulo"='Observaciones', "TipoDato"='TEXT', "Largo"=250, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=0, "Ayuda"='Ayuda de Observaciones', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='Observaciones';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=85, "Titulo"='Fuente', "TipoDato"='TEXT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=0, "Ayuda"='Ayuda de Fuente', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='FuenteDatos';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=86, "Titulo"='Secuencia Cliente', "TipoDato"='ID_CLIENTE', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=0, "Ayuda"='Ayuda de Secuencia Cliente', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='IdCliente';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=87, "Titulo"='Secuencia Portafolio', "TipoDato"='ID_PORTAFOLIO', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=0, "Ayuda"='Ayuda de Secuencia Portafolio', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='IdPortafolio';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=88, "Titulo"='Id Operacion', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='67', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='S', "Exportable"='N', "OcultoEnGrilla"='S', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=0, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='IdOperacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=89, "Titulo"='Id Emisor', "TipoDato"='NEMO', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='S', "Exportable"='N', "OcultoEnGrilla"='S', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=0, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='IdEmisor';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=93, "Titulo"='AcogidoISFUT', "TipoDato"='TEXT', "Largo"=1, "ValorDefecto"='N', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=0, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='AcogidoISFUT';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=95, "Titulo"='NUMERO DE SERIE DE CUOTAS DE PROPIEDAD DEL PARTICIPE O APORTANTE', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='900', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='S', "OcultoEnMantenedor"='S', "Comun"='N', "Clave"='N', "Grupo"='2. INFORMACION DE LOS APORTES AL FONDO', "OrdenCargaExterna"=0, "Ayuda"='', "CodigoSII"='C14', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='NumeroSerieCuotas';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=90, "Titulo"='Factor Actualizacion', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='S', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=81, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='FactorActualizacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=91, "Titulo"='Fecha Origen Inversion', "TipoDato"='DATE', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='S', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=82, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='FechaOrigenInversion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=92, "Titulo"='SecEvento', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='68', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=83, "Ayuda"='', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='SecEvento';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=94, "Titulo"='Origen Calificación', "TipoDato"='TEXT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='7. DATOS DE CONTROL', "OrdenCargaExterna"=0, "Ayuda"='Origen de Calificación', "CodigoSII"='', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='OrigenCalificacion';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=79, "Titulo"=' DIVIDENDOS PERCIBIDOS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='65', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', "OrdenCargaExterna"=78, "Ayuda"='', "CodigoSII"='C135', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DetBenNetoDivPer_C135';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=80, "Titulo"=' GANANCIAS DE CAPITAL PERCIBIDAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='65', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', "OrdenCargaExterna"=79, "Ayuda"='', "CodigoSII"='C136', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DetBenNetoGanCapPer_C136';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=81, "Titulo"='PERDIDAS Y GASTOS DEVENGADOS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='65', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', "OrdenCargaExterna"=80, "Ayuda"='', "CodigoSII"='C137', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DetBenNetoPerGastDev_C137';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=78, "Titulo"=' INTERESES PERCIBIDOS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='65', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', "OrdenCargaExterna"=77, "Ayuda"='', "CodigoSII"='C134', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DetBenNetoIntPer_C134';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "TipoDiccionario"='', "Orden"=77, "Titulo"=' UTILIDADES PERCIBIDAS', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='65', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='6. DETERMINACIÃ“N DEL BENEFICIO NETO PERCIBIDO', "OrdenCargaExterna"=76, "Ayuda"='', "CodigoSII"='C133', "CantidadDecimales"=0
WHERE "Esquema"='DJ1922' AND "Version"='2023' AND "Nombre"='DetBenNetoUtilPer_C133';



UPDATE "BSCL.Normativa.BD::DOC.Campo"
	SET "Clave" = 'S'
WHERE "Esquema" IN ('DJ1922') AND "Version" IN ('2023')
AND "Nombre" IN (
	'Numero'
	,'RutAdministradora'
	,'RutFondo'
	,'RunFondo'
	,'Fondo'
	,'TipoFondo'
	,'RutCliente'
	,'TipoOperacion'
	,'FechaInversion'
	,'SecEvento');


COMMIT;