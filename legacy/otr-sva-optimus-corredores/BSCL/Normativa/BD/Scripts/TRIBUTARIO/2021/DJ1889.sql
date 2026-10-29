SET SCHEMA BSCL;

DROP TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1889";
DROP TABLE BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1889N9";

CREATE TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1889" (
	"ID" 								BIGINT 			NOT NULL,
	"IdDocumento" 						BIGINT 			NOT NULL,
	"VboUsuario" 						CHAR(1) 		NOT NULL,
	"Observaciones" 					NVARCHAR(250) 	NOT NULL,
	"FuenteDatos" 						NVARCHAR(40) 	NOT NULL,
	"EjecutadoComplementoIPC" 			NVARCHAR(1),
	"Numero" 							BIGINT 			NOT NULL,
	"RutCliente" 						NVARCHAR(40) 	NOT NULL,
	"IdCliente" 						BIGINT,
	"FechaOperacion"					DATE,
	"TipoAhorro" 						BIGINT 			NOT NULL,
	"MontoNominal" 						DECIMAL(26,8) 	NOT NULL,
	"RentabilidadPositiva" 				DECIMAL(26,8) 	NOT NULL,
	"RentabilidadNegativa" 				DECIMAL(26,8) 	NOT NULL,
	"Factor" 							DECIMAL(26,8),
	"RentabilidadActualizadaPositiva" 	DECIMAL(26,8) 	NOT NULL,
	"RentabilidadActualizadaNegativa" 	DECIMAL(26,8) 	NOT NULL,
	"NumeroCertificado" 				BIGINT 			NOT NULL,
	PRIMARY KEY ("ID")
);

--
ALTER TABLE "BSCL.Normativa.BD::TRI.DatosDJ1889"
    ADD CONSTRAINT "FK_Documento_DatosDJ1889" FOREIGN KEY ("IdDocumento")
        REFERENCES "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        ON DELETE RESTRICT ON UPDATE RESTRICT;

--
CREATE TABLE BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1889N9" (
	"IdDetalleCertificadoClienteDJ1889N9" 	BIGINT NOT NULL,
	"IdCertificadoCliente" 					BIGINT,
	"IdCliente" 							BIGINT,
	"Mes" 									INTEGER,
	"IdTipoDocumento" 						NVARCHAR(10),
	"TipoAhorro" 							BIGINT 			NOT NULL,
	"MontoRetiro" 							DECIMAL(26,8),
	"RentaPositiva" 						DECIMAL(26,8),
	"RentaNegativa" 						DECIMAL(26,8),
	"Factor" 								DECIMAL(26,8),
	"RentaActualizadaPositiva" 				DECIMAL(26,8),
	"RentaActualizadaNegativa" 				DECIMAL(26,8),
	"IdEmpresa" 							BIGINT,
	"Version" 								NVARCHAR(10),
	"RutCliente"							NVARCHAR(40)	NOT NULL,

	PRIMARY KEY ("IdDetalleCertificadoClienteDJ1889N9")
);

--
ALTER TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1889N9"
    ADD CONSTRAINT "FK_CertificadoCliente_DetalleCertificadoClienteDJ1889N9" FOREIGN KEY ("IdCertificadoCliente")
        REFERENCES "BSCL.Normativa.BD::TRI.CertificadoCliente" ("IdCertificadoCliente")
        ON DELETE RESTRICT ON UPDATE RESTRICT;


--Limpiar todas las versiones  
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1889";

/* TRI.Complemento */
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.Complemento"  WHERE "Esquema"='DJ1889';-- AND "Version"='2016/12';

DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1889';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1889';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1889';-- and "Version" = '2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1889';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1889';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1889';-- and "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1889';-- AND "Version"='2016/12';
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1889';
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema" = 'DJ1889';

--ESQUEMA
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Esquema"(
"Esquema","Version","Modulo","Periodicidad","Objetivo",
"PeriodoInicio","IdEntidadReguladora","FechaPresentacionLimite",
"FechaDatosDesde","FechaDatosHasta","CodigoVersionSII","HabilitadoMantenedor", "TieneCertificado")
SELECT
'DJ1889',2020,'TRI','A','Declaración Jurada Anual sobre Cuentas de Ahorro Voluntario',
'2020-01-01','SII','2022-01-01',
'2020-01-01','2020-12-31',1,'S','S'
FROM DUMMY;


--CAMPO
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'ID', '', 1, 'Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'S', 'S', 'S', '', 0, 'Ayuda de Secuencia', '', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'IdDocumento', '', 2, 'Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'S', 'S', 'S', 'N', '5. DATOS DE CONTROL', 0, 'Ayuda de Secuencia Docto', '', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'VboUsuario', 'VistoBueno', 3, 'VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'S', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 'Ayuda de VoBo', '', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'Observaciones', '', 4, 'Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'S', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 'Ayuda de Observaciones', '', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'FuenteDatos', '', 5, 'Fuente', 'TEXT', 40, 'INICIAL', 'N', 'N', 'N', 'S', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 'Ayuda de Fuente', '', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'Numero', '', 6, 'Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 1, '', 'C0', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'RutCliente', '', 7, 'Rut del Afiliado', 'RUT_CLIENTE', 40, '', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '', 2, 'Ayuda de Rut Afiliado', 'C2', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'FechaOperacion', '', 9, 'Fecha Operacion', 'DATE_FILTER', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 3, '', '', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'TipoAhorro', '', 10, 'Tipo Ahorro', 'ID', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '', 4, '', 'C3', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'MontoNominal', '', 11, 'Monto Anual Nominal De Los Retiros', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '', 5, 'Ayuda Monto Anual Nominal De Llos Retiros', 'C4', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'RentabilidadPositiva', '', 12, 'Positiva', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. RENTABILIDAD NOMINAL ANUAL DETERMINADA SOBRE LOS RETIROS', 6, 'Ayuda Rentabilidad Anual Determinada Sobre Los Retiros Positiva', 'C5', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'RentabilidadNegativa', '', 13, 'Negativa', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. RENTABILIDAD NOMINAL ANUAL DETERMINADA SOBRE LOS RETIROS', 7, 'Ayuda Rentabilidad Anual Determinada Sobre Los Retiros Negativa', 'C6', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'Factor', '', 14, 'Factor', 'TEXT', 0, '0', 'N', 'S', 'N', 'N', 'S', 'S', 'N', 'N', '3. RENTABILIDAD ANUAL ACTUALIZADA ($)', 8, 'Ayuda Factor', '', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'RentabilidadActualizadaPositiva', '', 15, 'Positiva ($)', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. RENTABILIDAD ANUAL ACTUALIZADA ($)', 9, 'Ayuda Rentabilidad Anual Actualizada ($)', 'C7', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'RentabilidadActualizadaNegativa', '', 16, 'Negativa ($)', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. RENTABILIDAD ANUAL ACTUALIZADA ($)', 10, 'Ayuda Rentabilidad Anual Actualizada ($)', 'C8', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'NumeroCertificado', '', 17, 'Número De Certificado', 'ID', 7, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. NUMERO DE CERTIFICADO', 11, 'Ayuda Número De Certificado', 'C9', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "TipoDiccionario", "Orden", "Titulo", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "Ayuda", "CodigoSII", "CantidadDecimales")
VALUES('DJ1889', '2020', 'EjecutadoComplementoIPC', '', 18, 'Ejecucion complemento IPC', 'TEXT', 1, 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '5. DATOS DE CONTROL', 0, '', 'C99', 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1889', '2020', 'IdCliente', '', '', 19, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '', 0, 0);


/*
INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Campo"(
"Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable",
"ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales")
SELECT 'DJ1889',2020,'ID','','',1,'Secuencia','Ayuda de Secuencia','ID',0,'0','N','N','N','S','N','S','S','N','',0,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'IdDocumento','','',2,'Secuencia Docto','Ayuda de Secuencia Docto','ID',0,'0','N','N','N','S','S','S','S','N','5. DATOS DE CONTROL',0,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'VboUsuario','','VistoBueno',3,'VoBo','Ayuda de VoBo','TEXT',1,'N','N','S','S','S','N','N','S','N','5. DATOS DE CONTROL',0,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'Observaciones','','',4,'Observaciones','Ayuda de Observaciones','TEXT',250,'','N','S','S','S','N','N','S','N','5. DATOS DE CONTROL',0,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'FuenteDatos','','',5,'Fuente','Ayuda de Fuente','TEXT',40,'INICIAL','N','N','N','S','N','N','S','N','5. DATOS DE CONTROL',0,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'Numero','C0','',6,'Numero','','ID',0,'0','N','N','N','S','N','N','N','N','',1,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'RutCliente','C2','',7,'Rut del Afiliado','Ayuda de Rut Afiliado','RUT_CLIENTE',40,'','S','N','N','S','N','N','N','S','',2,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'IdCliente','','',8,'Secuencia Cliente','|Ayuda de Secuencia Cliente','ID_CLIENTE',0,'0','S','N','N','N','S','N','N','N','',3,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'FechaOperacion','','',9,'Fecha Operacion','','DATE_FILTER',0,'0','S','S','N','S','N','N','N','N','',4,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'TipoAhorro','C3','',10,'Tipo Ahorro','','AMOUNT',0,'0','S','S','N','S','N','N','N','S','',5,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'MontoNominal','C4','',11,'Monto Anual Nominal De Los Retiros','Ayuda Monto Anual Nominal De Llos Retiros','AMOUNT',0,'0','N','S','N','S','N','N','N','N','',6,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'RentabilidadPositiva','C5','',12,'Positiva','Ayuda Rentabilidad Anual Determinada Sobre Los Retiros Positiva','AMOUNT',0,'0','N','S','N','S','N','N','N','N','2. RENTABILIDAD NOMINAL ANUAL DETERMINADA SOBRE LOS RETIROS',7,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'RentabilidadNegativa','C6','',13,'Negativa','Ayuda Rentabilidad Anual Determinada Sobre Los Retiros Negativa','AMOUNT',0,'0','N','S','N','S','N','N','N','N','2. RENTABILIDAD NOMINAL ANUAL DETERMINADA SOBRE LOS RETIROS',8,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'Factor','','',14,'Factor','Ayuda Factor','AMOUNT',0,'0','N','S','N','S','N','N','N','N','3. RENTABILIDAD ANUAL ACTUALIZADA ($)',9,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'RentabilidadActualizadaPositiva','C7','',15,'Positiva ($)','Ayuda Rentabilidad Anual Actualizada ($)','AMOUNT',0,'0','N','S','N','S','N','N','N','N','3. RENTABILIDAD ANUAL ACTUALIZADA ($)',10,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'RentabilidadActualizadaNegativa','C8','',16,'Negativa ($)','Ayuda Rentabilidad Anual Actualizada ($)','AMOUNT',0,'0','N','S','N','S','N','N','N','N','3. RENTABILIDAD ANUAL ACTUALIZADA ($)',11,0 FROM DUMMY UNION ALL
SELECT 'DJ1889',2020,'NumeroCertificado','C9','',17,'Número De Certificado','Ayuda Número De Certificado','TEXT',7,'0','S','S','N','S','N','N','N','N','4. NUMERO DE CERTIFICADO',12,0 FROM DUMMY UNION ALL
--Ejecucion complemento actualizacion IPC
SELECT 'DJ1889',2020,'EjecutadoComplementoIPC','C99','',18,'Ejecucion complemento IPC','','TEXT','1','N','N','N','N','N','S','S','N','N','5. DATOS DE CONTROL',0,0 FROM DUMMY;
*/

--DOCUMENTO
INSERT INTO "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1889' AS "Esquema"
,'2020' AS "Version"
,1 AS "IdEmpresa"
,'2020-12-31' AS "FechaLimite"
,'ACT' AS "IdEstadoDocumento"
,'2020-01-01 0:00:00.0' AS "FechaCreacion"
,'2020-12-31 0:00:00.0' AS "FechaPresentacion"
,"DocumentoIdentidad" AS "DocumentoIdentidadDeclarante"
,"DocumentoIdentidadRepresentante" AS "DocumentoIdentidadRepresentante"
,"DireccionElectronicaPrincipal" AS "DireccionElectronicaDeclaracion"
, rtrim("DireccionFisicaPrincipalCalle")||' '|| "DireccionFisicaPrincipalNumero" AS "DireccionFisicaDeclaracion"
, "DireccionFisicaPrincipalNombreDivisionPais" AS "ComunaDeclaracion"
,"TelefonoPrincipal" AS "TelefonoDeclaracion"
,'I9153103' AS "FolioDeclaracion"
,'N' AS "EnProceso"
,'2020-12-02 21:35:09.444' AS "FechaInicioUltimoProceso"
,''AS "AnoMes" FROM (
		SELECT top 1 * FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ORDER BY "IdEmpresa"
) AS "EMP";






--COMPLEMENTO

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento" ("Esquema","CodigoComplemento","Descripcion")
SELECT 'DJ1889','CARGAIPC','Carga IPC' FROM DUMMY; 

	/*
	INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"(
		"Esquema","Version","UltimoFolio","CodigoCertificado","NombreCertificado","CodigoPrograma")
	SELECT 'DJ1889','2020',0,'CERTIFICADO_9','Certificado 9','GenerarCertificado9' FROM DUMMY;

	INSERT INTO BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma"(
		"IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta","TipoSalida","FormatoSalida","TieneLog")
	SELECT 'GenerarCertificado9' ,'Generar Certificado 9' ,1,'Tributario','cl.bcs.normativa.complementos.BcsJobGenerarCertificado9' ,'JAVA','','N','P','','N' FROM DUMMY;
	*/

	--no posee validaciones, sgun archivo set_validacines_ddjj_at2017.xls bajado desde internet, sii
	/*INSERT INTO "BSCL"."BSCL.Normativa.BD::DOC.Validacion" (
		"Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
	SELECT 'DJ1889','2020','H0','NORMA','S','Debe ejecutar el complemento carga IPC','E','' FROM DUMMY;*/

--VALIDACION
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1889', '2020', 'H1286', 'NORMA', 'S', 'La columna "Tipo ahorro" [TA] es obligatoria y debe tomar valor 1 ó 2 ', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1889', '2020', 'H111', 'NORMA', 'S', 'La Rentabilidad Anual Actualizada Positiva debe ser menor o igual a la Rentabilidad Nominal determinada sobre los Retiros Positiva por el factor de Actualización del año mas Holgura determinada por el SII.', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1889', '2020', 'H112', 'NORMA', 'S', 'La Rentabilidad Anual Actualizada Negativa debe ser menor o igual a la Rentabilidad Nominal determinada sobre los Retiros Negativa por el factor de Actualización mas Holgura determinada por el SII.', 'E', '');

--CONTROL PROCESO
INSERT INTO "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1889' AND "Version" IN ('2020');

--CONFIGURACION
DELETE FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" WHERE "CodigoParametro"  IN ('P23','P214');

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
VALUES(1, 'P23', 'Factor maximo actualizado de Enero a Diciembre AT-1', '1.029', 'DJ1889', 'N', 'Validacion DJ1889', '1', 'Numerico, (10000)', 'N', '', 'SISTEMA', '2020-05-03 12:00:00.000', 'PROJO', '2021-07-07 23:05:04.000', ' ');

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
VALUES(1, 'P214', 'Holgura determinada por el SII', '0', 'DJ1889', 'N', 'Validacion DJ1889', '1', 'Numerico, (10000)', 'N', '', 'SISTEMA', '2020-05-03 12:00:00.000', 'PROJO', '2021-07-07 23:05:04.000', ' ');

--SERVICIOS
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600029, 'BSCL.Normativa.BD.Vistas::TRI.DJ1889.Invalidos', 2, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600029);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600034, 'PLUGIN_NOR.jar@cl.bcs.normativa.declaraciones.DJ1889.ReajustarValorIPC', 5, 'J', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600034);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600028, 'BSCL.Normativa.BD.Vistas::TRI.DJ1889.PreDatos', 51, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600028);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600031, 'BSCL.Normativa.BD.Servicios::TRI.DJ1889.GrabarDetalle', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600031);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600033, 'BSCL.Normativa.BD.Servicios::TRI.DJ1889.GenerarCertificado9', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600033);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600032, 'BSCL.Normativa.BD.Servicios::TRI.DJ1889.EliminarDetalles', 1, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600032);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600030, 'BSCL.Normativa.BD.Servicios::TRI.DJ1889.ObtenerTotales', 52, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600030);


--CERTIFICADO
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "Esquema"='DJ1889';
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1889'; 

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1889', '2020', 0, 'CERTIFICADO_9', 'Certificado 9', 'GenerarCertificado9');


DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1889';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT9-PDF', '2020', 'DJ1889', '3', 'CERT9', 'CERTIFICADO 9 TRIBUTARIOS', 'CERTIFICADO9', 'X', 'pdf', '600033', 1, 'ACT');

--PARAMETROS: Nombre de archivo de los certificados EJ:
DELETE  FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" WHERE "IdNormativa"='DJ1889' AND "CodigoParametro" LIKE 'TRI_NOM_CERTIFICADO%';
  
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion" ("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 
	1 AS "IdEmpresa"
	,'TRI_NOM_' || "NombreArchivo" ||  '_01'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
	,'[RUT]' AS "Valor"
	,"Esquema" AS  "IdNormativa"
	,'S' AS "EsModificableUsuario"
	,'Certificado masivo' AS "Uso"
	,'[RUT]'  AS "ValorDefecto"
	,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles"
	,'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion"
	,'' AS  "CreadorParametro"
	,CURRENT_UTCTIMESTAMP AS "FechaCreacion"
	,'' AS "ModificadorParametro"
	,CURRENT_UTCTIMESTAMP AS "FechaModificacion"
	,'' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1889' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_01' AND N."IdNormativa"="Esquema");  

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_02'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'CB' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'CB'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1889' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_02' AND N."IdNormativa"="Esquema");  

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_03'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[PERIODO]' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'[PERIODO]'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1889' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_03' AND N."IdNormativa"="Esquema");  

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_04'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[DJ]' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'[DJ]'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1889' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_04' AND N."IdNormativa"="Esquema");  

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_05'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[CERT]' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'[CERT]'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1889' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_05' AND N."IdNormativa"="Esquema");  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290704,'BSCL.Normativa.BD.Servicios::TRI.DJ1889.GrabarMasivo',0,'P','','','' 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290704);

