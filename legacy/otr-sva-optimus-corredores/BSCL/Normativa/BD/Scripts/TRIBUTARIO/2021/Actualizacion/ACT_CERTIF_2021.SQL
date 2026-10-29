

----CERT43

DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1922' AND "Version"='2020'; 

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1922', '2020', 0, 'CERTIFICADO_43', 'Certificado 43', 'GenerarCertificado43');


DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1922' AND "VersionArchivo"='2020';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT43-PDF', '2020', 'DJ1922', '3', 'CERT43', 'CERTIFICADO 43 TRIBUTARIOS', 'CERTIFICADO43', '', 'pdf', '3586', 1, 'ACT');




DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1922' AND "Version"='2021'; 

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1922', '2021', 0, 'CERTIFICADO_43', 'Certificado 43', 'GenerarCertificado43');


DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1922' AND "VersionArchivo"='2021';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT43-PDF', '2021', 'DJ1922', '3', 'CERT43', 'CERTIFICADO 43 TRIBUTARIOS', 'CERTIFICADO43', '', 'pdf', '3586', 1, 'ACT');



----CERT9

DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1889' AND "Version"='2021'; 

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1889', '2021', 0, 'CERTIFICADO_9', 'Certificado 9', 'GenerarCertificado9');


DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1889' AND "VersionArchivo"='2021';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT9-PDF', '2021', 'DJ1889', '3', 'CERT9', 'CERTIFICADO 9 TRIBUTARIOS', 'CERTIFICADO9', 'X', 'pdf', '600033', 1, 'ACT');



DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1899' AND "Version"='2021'; 

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1899', '2021', 0, 'CERTIFICADO_24', 'Certificado 24', 'GenerarCertificado24');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1899', '2021', 0, 'CERTIFICADO_36', 'Certificado 36', 'GenerarCertificado36');

DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1899' AND "VersionArchivo"='2021';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT24-PDF', '2021', 'DJ1899', '3', 'CERT24', 'CERTIFICADO 24 TRIBUTARIOS', 'CERTIFICADO24', '', 'pdf', '600042', 1, 'ACT');
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT36-PDF', '2021', 'DJ1899', '3', 'CERT36', 'CERTIFICADO 36 TRIBUTARIOS', 'CERTIFICADO36', '', 'pdf', '600044', 1, 'ACT');



DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1944' AND "Version"='2021'; 


INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1944', '2021', 0, 'CERTIFICADO_8', 'Certificado 8', 'GenerarCertificado8');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1944', '2021', 0, 'CERTIFICADO_17', 'Certificado 17', 'GenerarCertificado17');

DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1944' AND "VersionArchivo"='2021';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT8-PDF', '2021', 'DJ1944', '3', 'CERT8', 'CERTIFICADO 8 TRIBUTARIOS', 'CERTIFICADO8', '', 'pdf', '3611', 1, 'ACT');

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT17-PDF', '2021', 'DJ1944', '3', 'CERT17', 'CERTIFICADO 17 TRIBUTARIOS', 'CERTIFICADO17', '', 'pdf', '3612', 1, 'ACT');






DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1949' AND "Version"='2021'; 

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1949', '2021', 0, 'CERTIFICADO_71', 'Certificado 71', 'GenerarCertificado71');

DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1949' AND "VersionArchivo"='2021';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"("IdArchivoNormativa","VersionArchivo","Esquema","Version",
"IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('CERT71-PDF','2021','DJ1949','3','CERT71','CERTIFICADO 71 TRIBUTARIOS','CERTIFICADO71','','pdf','3613',1,'ACT');


DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1894' AND "Version"='2021'; 

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1894', '2021', 0, 'CERTIFICADO_21', 'Certificado 21', 'GenerarCertificado21');

DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1894' AND "VersionArchivo"='2021';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT21-PDF', '2021', 'DJ1894', '3', 'CERT21', 'CERTIFICADO 21 TRIBUTARIOS', 'CERTIFICADO21', '', 'pdf', '3543', 1, 'ACT');



------Fecha : 07-02-2022


SELECT * FROM BSCL."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1922';

/*DJ1922 : 43*/

update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si el campo "Tipo de Operacion" es igual a "7", entonces: C3,C4,C6,C7,C11,C12,C13,C15,C18,C64 <> blanco, vacÃƒÂ­o o cero .y. [C24], [C25], [C26],[C66], [C67],[C68], [C69], [C96], [C97],[C98], [C99], [C100], [C28], [C29], [C70], [C31], [C30], [C32], [C33],[C34] = blanco .o. vacio .o. 0',
"Formula"='Si (C10) = 7 , entonces: C3,C4,C6,C7,C11,C12,C13,C15,C18,C64 <> blanco, vacio o cero .y. [C24], [C25], [C26],[C66], [C67],[C68], [C69], [C96], [C97],[C98], [C99], [C100], [C28], [C29], [C70], [C31], [C30], [C32], [C33],[C34] = blanco .o. vacio .o. 0'
WHERE "Esquema"='DJ1922' AND "Codigo"='H1842';

update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='La suma de los valores declarados en las columnas Creditos para G. Complem. o Adic. Impto. 1ra Categoria (Afectos y Exentos)  menos un monto de tolerancia determinado por el SII, debe ser menor o igual que la suma de los montos Dividendos Remesas o Distribuciones Reajustadas afectas y exentas , mas el incremento por impuesto de primera categoria por la tasa del impuesto de Primera Categoria del AT'
WHERE "Esquema"='DJ1922' AND "Codigo"='H1847';

update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si tipo de fondo por Informado es F .o. P, entonces Monto Inversion en cuotas de Fondos de Inversion Ley N°18.815, adquiridas antes del 04.06.93 (Ex. Art. 32, Ley N° 18.815) debe ser igual a cero ',
"Formula"='(C5) = F .o. P, entonces (C42) = 0'
WHERE "Esquema"='DJ1922' AND "Codigo"='H1856';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si los campos "Participe cumple requisitos Art. 82 N°1 letra B) iii) Ley adm fondos contenida Art. 82 Ley 20712" y "Cuotas de fondos que cumplen requisitos art. 82 letra B) iii), Ley Adm. Fondos Contenida Art. 82 Ley 20712)" son iguales a 1, entonces el campo Distribuciones o Remesas no afectas a Impuesto unico Tasa 10% tiene que ser mayor que cero',
"Formula"='(C16) y (C17) = 1, entonces (C31) > 0'
WHERE "Esquema"='DJ1922' AND "Codigo"='H1858';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Los campos: "Rut de sociedad administradora", "Rut del fondo","RUN del fondo"  y "rut del participe o aportante",  deben cumplir con modulo 11 (verificacion digito verificador)',
"Formula"='(C3), (C4), (C7)  (C80)= verificacion digito verificador'
WHERE "Esquema"='DJ1922' AND "Codigo"='H1859';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='El campo "Participe cumple requisitos (art. 82 letra B)  iii) ley Adm. Fondos contenidas Art. 82 ley 20712" debe tomar valor 1, 2, o 3',
"Formula"='(C16) = { 1 .o. 2 .o. 3}'
WHERE "Esquema"='DJ1922' AND "Codigo"='H1865';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='El campo "Cuotas de fondos que cumple requisitos (art. 82 letra B) iii)  ley Adm. Fondos contenida art. 82 ley 20712)" debe tomar valor 1, 2, o 3',
"Formula"='(C17) = { 1 .o. 2 .o. 3} '
WHERE "Esquema"='DJ1922' AND "Codigo"='H1866';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si el campo "Tipo de Operacion" es igual a "7", entonces: C3,C4,C6,C7,C11,C12,C13,C15,C18,C64 <> blanco, vacio o cero .y. [C24], [C25], [C26],[C66], [C67],[C68], [C69], [C96], [C97],[C98], [C99], [C100], [C28], [C29], [C70], [C31], [C30], [C32], [C33],[C34] = blanco .o. vacio .o. 0',
"Formula"='Si (C10) = 7 , entonces: C3,C4,C6,C7,C11,C12,C13,C15,C18,C64 <> blanco, vacio o cero .y. [C24], [C25], [C26],[C66], [C67],[C68], [C69], [C96], [C97],[C98], [C99], [C100], [C28], [C29], [C70], [C31], [C30], [C32], [C33],[C34] = blanco .o. vacio .o. 0'
WHERE "Esquema"='DJ1922' AND "Codigo"='H1842';





/*
SELECT N.*
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" A
INNER JOIN BSCL."BSCL.Normativa.BD::NOR.Configuracion" N ON N."CodigoParametro"='TRI_NOM_' || A."NombreArchivo" ||  '_02' AND N."IdNormativa"=A."Esquema"
WHERE A."Esquema"='DJ1922'
AND A."VersionArchivo" IN (2020,2021)
*/

UPDATE  N SET  N."Valor"='AV'
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" A
INNER JOIN BSCL."BSCL.Normativa.BD::NOR.Configuracion" N ON N."CodigoParametro"='TRI_NOM_' || A."NombreArchivo" ||  '_02' AND N."IdNormativa"=A."Esquema"
WHERE A."Esquema"='DJ1922'
AND A."VersionArchivo" IN (2020,2021);



/*DJ1944 : 17 y 8 */


/*
SELECT N.*
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" A
INNER JOIN BSCL."BSCL.Normativa.BD::NOR.Configuracion" N ON N."CodigoParametro"='TRI_NOM_' || A."NombreArchivo" ||  '_02' AND N."IdNormativa"=A."Esquema"
WHERE A."Esquema"='DJ1944'
AND A."VersionArchivo" IN (2020,2021)
*/

UPDATE  N SET  N."Valor"='AV'
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" A
INNER JOIN BSCL."BSCL.Normativa.BD::NOR.Configuracion" N ON N."CodigoParametro"='TRI_NOM_' || A."NombreArchivo" ||  '_02' AND N."IdNormativa"=A."Esquema"
WHERE A."Esquema"='DJ1944'
AND A."VersionArchivo" IN (2020,2021);




/*DJ1894 : 21 */


SELECT *  FROM BSCL."BSCL.Normativa.BD::DOC.Diccionario" where "Tipo"='TipoOperacionDJ1894';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Diccionario"  SET "Descripcion"='Inversion Recibida'
where "Tipo"='TipoOperacionDJ1894' AND "Codigo"='1';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Diccionario"  SET "Descripcion"='Reinversion Recibida, cuotas adquiridas hasta el 19.04.2001'
where "Tipo"='TipoOperacionDJ1894' AND "Codigo"='2';


UPDATE BSCL."BSCL.Normativa.BD::DOC.Diccionario"  SET "Descripcion"='Reinversion Recibida, cuotas adquiridas con posterioridad al 19.04.2001.'
where "Tipo"='TipoOperacionDJ1894' AND "Codigo"='3';


UPDATE BSCL."BSCL.Normativa.BD::DOC.Diccionario"  SET "Descripcion"='Liquidacion de Cuotas para Reinversion que No Provienen de Reinversiones Recibidas.'
where "Tipo"='TipoOperacionDJ1894' AND "Codigo"='8';


UPDATE BSCL."BSCL.Normativa.BD::DOC.Diccionario"  SET "Descripcion"='Liquidacion de Cuotas para Reinversion que Provienen de Reinversiones Recibidas.'
where "Tipo"='TipoOperacionDJ1894' AND "Codigo"='9';


SELECT * FROM BSCL."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1894';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='RUN Fondo Mutuo[C4] no debe estar en blanco y debe cumplir modulo 11',
"Formula"='RUN Fondo[C4] <> B .y. M11{(RUN Fondo[C4])} = 1'
WHERE "Esquema"='DJ1894' AND "Codigo"='H063';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Los valores de Tipo de Operacion [C5], deben ser: 1 o 2 o 3 o 4 o 5 o 6 o 7 o 8 o 9',
"Formula"='[C5] = 1, 2, 3, 4, 5, 6, 7, 8 o 9. '
WHERE "Esquema"='DJ1894' AND "Codigo"='H064';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='La suma del valor en las columnas Monto Nominal Inversion, Monto Nominal Rescate, Mayor Valor Actualizado, Menor Valor Actualizado  y Monto Reinvertido a valor de Liquidacion debe ser mayor que 0.',
"Formula"='[C7]+[C9]+[C10]+[C12]+[C14] > 0'
WHERE "Esquema"='DJ1894' AND "Codigo"='H259';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si Hay fecha Inversion o reinversion[C6], debe Haber Monto Nominal Inversion, y los campos: Monto Nominal Rescate , Mayor Valor Actualizado , Menor Valor Actualizado y Monto Reinvertido a valor de Liquidacion  deben estar vacios.',
"Formula"='Si [C6] <> B, entonces: [C7] >0 .y. E {[C9] + [C10] + [C12] + [C14] }=0'
WHERE "Esquema"='DJ1894' AND "Codigo"='H842';



update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si Tipo de Operacion es igual a 1, 2 o 3, la fecha de Inversion debe ser entre el 01/01/AT-1 y el 31/12/AT-1, incluidos.',
"Formula"='Si [C5] = { 1 .o. 2 .o. 3 }, entonces: 01/01/AT-1> [C6] < 31/12/AT-1'
WHERE "Esquema"='DJ1894' AND "Codigo"='H843';



update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Verificar, que se Haya indicado el tipo de declarante 1: Administradora de Fondos Mutuos 2: Institucion Intermediaria',
"Formula"='Tipo de Declarante = { 1.o.2} '
WHERE "Esquema"='DJ1894' AND "Codigo"='H845';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si Tipo de Operacion [C5] es igual a 1, 2 o 3, entonces debe Haber fecha inversion o reinversion[FI], Monto Nominal inversion o reinversion[MNI] y Porcentaje accionario[ACC]',
"Formula"='Si [C5] = 1 .o. 2 .o. 3, entonces: [C6] <> B .y. [C7] <> B .y. [ACC] <> B'
WHERE "Esquema"='DJ1894' AND "Codigo"='H962';

update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si Tipo de Operacion[C5] es igual a 8 o 9, donde: 8: Liquidacion de Cuotas para Reinversion que No Provienen de Reinversiones Recibidas. 9: Liquidacion de Cuotas para Reinversion que Provienen de Reinversiones Recibidas. entonces debe Haber Monto Reinvertido a valor de liquidacion[C14]',
"Formula"='Si [C5] = 8 .o. 9, entonces: [C14] <> B'
WHERE "Esquema"='DJ1894' AND "Codigo"='H964';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si Hay RutEmisor Instrumento, entonces este debe ser mayor o igual a 50.000.000.',
"Formula"='Si [C1] <> B, entonces: [C1] >= 50.000.000'
WHERE "Esquema"='DJ1894' AND "Codigo"='H965';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si hay RutSociedad Adm. fondo receptor de la reinversion, entonces debe ser mayor a 50.000.000',
"Formula"='Si RutSociedad Adm. fondo receptor de la inversion [C13]<> B, entonces: RutSoc. Adm, fondo receptor de la inversion [C13]>= 50.000.000'
WHERE "Esquema"='DJ1894' AND "Codigo"='H966';

update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si Tipo de Operacion [C5] es igual a 4 o 5 o6 o 7.Entonces,la fecha de Rescate [C8] debe ser entre el 01/01/AT-1 y el 31/12/AT-1, incluidos.',
"Formula"='Si [C5] = { 4 .o. 5 .o. 6 .o. 7 }, entonces: 01/01/AT-1>= [C8] <= 31/12/AT-1'
WHERE "Esquema"='DJ1894' AND "Codigo"='H1291';


/*
SELECT N.*
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" A
INNER JOIN BSCL."BSCL.Normativa.BD::NOR.Configuracion" N ON N."CodigoParametro"='TRI_NOM_' || A."NombreArchivo" ||  '_02' AND N."IdNormativa"=A."Esquema"
WHERE A."Esquema"='DJ1894'
AND A."VersionArchivo" IN (2020,2021)
*/

UPDATE  N SET  N."Valor"='AV'
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" A
INNER JOIN BSCL."BSCL.Normativa.BD::NOR.Configuracion" N ON N."CodigoParametro"='TRI_NOM_' || A."NombreArchivo" ||  '_02' AND N."IdNormativa"=A."Esquema"
WHERE A."Esquema"='DJ1894'
AND A."VersionArchivo" IN (2020,2021);



/*DJ1949 : 71 */


SELECT * FROM BSCL."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1949';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si solo declara retiros, remesas y dividendos reajustados ($), en el campo "Rentas con tributacion cumplida (C10), (C11), (C12), (C13), (C14)" y "Rentas Exentas de Impuesto Global Complementario (IGC) y/o Impuesto Adicional (C16)", y no se informaron retiros, remesas o dividendos afectos a los Impuestos Global Complementario y/o Adicional y Exentos, con derecho a credito por IDPC en las columnas (C6), (C7), (C8) o (C15)',
"Formula"=''
WHERE "Esquema"='DJ1949' AND "Codigo"='H2858';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Si solo declara retiros, remesas y dividendos reajustados ($), en el campo "Rentas Exentas e Ingresos No Constitutivos de Renta (REX), Rentas Exentas, Rentas Exentas de Impuesto Global Complementario (IGC) (Articulo 11, Ley 18.401), Afectas a Impuesto Adicional (C15)", y no se informaron retiros, remesas o dividendos afectos a los Impuestos Global Complementario y/o Adicional, con o sin derecho a credito por IDPC en las columnas (C6), (C7) o (C8)',
"Formula"=''
WHERE "Esquema"='DJ1949' AND "Codigo"='H2859';

update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Dado que existen retiros, remesas o dividendos reajustados ($),informados  en el campo  "Ingresos No constitutivos de renta (C17)" y no se informaron retiros, remesas o dividendos afectos a los Impuestos Global Complementario y/o Adicional, con derecho a credito por IDPC en las columnas  (C6), (C7) o (C8) y ademas podria o no estar declarando valor en el campo de retiros.',
"Formula"=''
WHERE "Esquema"='DJ1949' AND "Codigo"='H2860';

update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Columna C2 "RUT de la sociedad pagadora del dividendo " es un campo obligatorio y debe ingresar un RUT valido',
"Formula"=''
WHERE "Esquema"='DJ1949' AND "Codigo"='H2861';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='Columna C4 "RUT del beneficiario de los dividendos distribuidos " es un campo obligatorio y debe ingresar un RUT valido',
"Formula"=''
WHERE "Esquema"='DJ1949' AND "Codigo"='H2862';


UPDATE BSCL."BSCL.Plataforma.BD::PLA.Diccionario" SET "Descripcion"='Procesar los montos segun Calificaciones Tributarias ingresadas'
WHERE  "CodDiccionario"='UI_PROCESAR_MONTOS_SEGUN_CALIFICACIONES_TRIBUTARIAS_INGRESADAS';


UPDATE BSCL."BSCL.Plataforma.BD::PLA.Diccionario" SET "Descripcion"='100 % afecto cuando no hay Calificacion Tributaria'
WHERE  "CodDiccionario"='UI_100_POR_AFECTO_CUANDO_NO_HAY_CALIFICACION_TRIBUTARIA';


/*
SELECT N.*
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" A
INNER JOIN BSCL."BSCL.Normativa.BD::NOR.Configuracion" N ON N."CodigoParametro"='TRI_NOM_' || A."NombreArchivo" ||  '_02' AND N."IdNormativa"=A."Esquema"
WHERE A."Esquema"='DJ1949'
AND A."VersionArchivo" IN (2020,2021)
*/

UPDATE  N SET  N."Valor"='AV'
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" A
INNER JOIN BSCL."BSCL.Normativa.BD::NOR.Configuracion" N ON N."CodigoParametro"='TRI_NOM_' || A."NombreArchivo" ||  '_02' AND N."IdNormativa"=A."Esquema"
WHERE A."Esquema"='DJ1949'
AND A."VersionArchivo" IN (2020,2021);






/*DJ1949 : 71 */


SELECT * FROM BSCL."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1889';


update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='La columna "Tipo ahorro" [TA] es obligatoria y debe tomar valor 1 o 2 ',
"Formula"=''
WHERE "Esquema"='DJ1889' AND "Codigo"='H1286';

update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='La Rentabilidad Anual Actualizada Positiva debe ser menor o igual a la Rentabilidad Nominal determinada sobre los Retiros Positiva por el factor de Actualizacion del anio mas Holgura determinada por el SII.',
"Formula"=''
WHERE "Esquema"='DJ1889' AND "Codigo"='H111';

update BSCL."BSCL.Normativa.BD::DOC.Validacion" SET 
"Descripcion"='La Rentabilidad Anual Actualizada Negativa debe ser menor o igual a la Rentabilidad Nominal determinada sobre los Retiros Negativa por el factor de Actualizacion mas Holgura determinada por el SII.',
"Formula"=''
WHERE "Esquema"='DJ1889' AND "Codigo"='H112';


/*
SELECT N.*
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" A
INNER JOIN BSCL."BSCL.Normativa.BD::NOR.Configuracion" N ON N."CodigoParametro"='TRI_NOM_' || A."NombreArchivo" ||  '_02' AND N."IdNormativa"=A."Esquema"
WHERE A."Esquema"='DJ1889'
AND A."VersionArchivo" IN (2020,2021)
*/

UPDATE  N SET  N."Valor"='AV'
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" A
INNER JOIN BSCL."BSCL.Normativa.BD::NOR.Configuracion" N ON N."CodigoParametro"='TRI_NOM_' || A."NombreArchivo" ||  '_02' AND N."IdNormativa"=A."Esquema"
WHERE A."Esquema"='DJ1889'
AND A."VersionArchivo" IN (2020,2021);


