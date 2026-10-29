SET SCHEMA BSCL;

DROP TABLE "BSCL.Normativa.BD::TRI.DatosDJ1914";

CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1914" (
"ID"                                            BIGINT  NOT NULL ,
"IdDocumento"                                   BIGINT  NOT NULL ,
"VboUsuario"                                    CHAR(1)  NOT NULL ,
"Observaciones"                                 NVARCHAR(250) NOT NULL ,
"FuenteDatos"                                   NVARCHAR(40) NOT NULL ,
"IdCliente" 									BIGINT,
"Numero"                                        BIGINT  NOT NULL ,											
"RutCliente"                                    NVARCHAR(40),                       
"TipoInstrumento"                               NVARCHAR(40),                       
"NumeroIdentificadorInversion"                  NVARCHAR(40),                       
"FechaInversionOriginal"                        DATE,                               
"RetiroInversion54BISMontoNominal"              DECIMAL(26, 8),                     
"RetiroInversion54BISInteresPositivo"           DECIMAL(26, 8),                     
"RetiroInversion54BISInteresNegativo"           DECIMAL(26, 8),                     
"NumeroCertificado"                             BIGINT,

						
PRIMARY KEY ("ID")) ;

ALTER TABLE "BSCL.Normativa.BD::TRI.DatosDJ1914"
    ADD CONSTRAINT "FK_Documento_DatosDJ1914" FOREIGN KEY ("IdDocumento")
        REFERENCES "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        ON DELETE RESTRICT ON UPDATE RESTRICT;

DROP TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1914N45";

CREATE TABLE BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1914N45" (
	"IdDetalleCertificadoClienteDJ1914N45" 	BIGINT NOT NULL,
	"IdCertificadoCliente" 					BIGINT,
	"IdCliente" 							BIGINT,
	"IdTipoDocumento" 						NVARCHAR(10),
	"TipoInstrumento"                		BIGINT,
    "NumeroIdentificadorInversion"          BIGINT,
    "FechaInversionOriginal"                DATE,
    "RetiroInversion54BISMontoNominal"      DECIMAL(26,8),
    "RetiroInversion54BISInteresPositivo"   DECIMAL(26,8),
    "RetiroInversion54BISInteresNegativo"   DECIMAL(26,8),
	"IdEmpresa" 							BIGINT,
	"Version" 								NVARCHAR(10),
	"RutCliente"							NVARCHAR(40)	NOT NULL,
	PRIMARY KEY ("IdDetalleCertificadoClienteDJ1914N45")
);

--
ALTER TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1914N45"
    ADD CONSTRAINT "FK_CertificadoCliente_DetalleCertificadoClienteDJ1914N45" FOREIGN KEY ("IdCertificadoCliente")
        REFERENCES "BSCL.Normativa.BD::TRI.CertificadoCliente" ("IdCertificadoCliente")
        ON DELETE RESTRICT ON UPDATE RESTRICT;


--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1914";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" WHERE "Esquema"	= 'DJ1914';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  	WHERE "Esquema"	= 'DJ1914';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" 			WHERE "Esquema" = 'DJ1914';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" 	WHERE "Esquema"	= 'DJ1914';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" 		WHERE "Esquema"	= 'DJ1914';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" 		WHERE "Esquema" = 'DJ1914';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" 		WHERE "Esquema"	= 'DJ1914';
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" 	WHERE "Esquema"	= 'DJ1914';

-- Esquema
insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
	 "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
	 "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1914','2020','TRI','A','','2020-01-01','SII','2021-01-01','2020-01-01','2020-12-31',0,'S');

insert into "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
	 "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
	 "CodigoVersionSII","HabilitadoMantenedor")
values
('DJ1914','2021','TRI','A','','2021-01-01','SII','2022-01-01','2021-01-01','2021-12-31',0,'S');


-- Documento
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")

SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1914' AS "Esquema"
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


insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")

SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1914' AS "Esquema"
,'2021' AS "Version"
,1 AS "IdEmpresa"
,'2021-12-31' AS "FechaLimite"
,'ACT' AS "IdEstadoDocumento"
,'2021-01-01 0:00:00.0' AS "FechaCreacion"
,'2021-12-31 0:00:00.0' AS "FechaPresentacion"
,"DocumentoIdentidad" AS "DocumentoIdentidadDeclarante"
,"DocumentoIdentidadRepresentante" AS "DocumentoIdentidadRepresentante"
,"DireccionElectronicaPrincipal" AS "DireccionElectronicaDeclaracion"
, rtrim("DireccionFisicaPrincipalCalle")||' '|| "DireccionFisicaPrincipalNumero" AS "DireccionFisicaDeclaracion"
, "DireccionFisicaPrincipalNombreDivisionPais" AS "ComunaDeclaracion"
,"TelefonoPrincipal" AS "TelefonoDeclaracion"
,'I9153103' AS "FolioDeclaracion"
,'N' AS "EnProceso"
,'2021-12-02 21:35:09.444' AS "FechaInicioUltimoProceso"
,''AS "AnoMes" FROM (
        SELECT top 1 * FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ORDER BY "IdEmpresa"
) AS "EMP";

-- INSERT TABLA CAMPO 2020
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'ID', '', '', 1, 'Secuencia', 'Ayuda  de Secuencia', 'ID', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'Numero', 'C0', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos de los informados', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'RutCliente', 'C1', '', 3, 'Rut Cliente', 'Ayuda de Rut Cliente', 'RUT_CLIENTE', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos de los informados', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'TipoInstrumento', 'C2', '', 4, 'Tipo de Instrumento', 'Ayuda de Tipo de Instrumento', 'NEMO', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. Datos de los informados', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'NumeroIdentificadorInversion', 'C3', '', 5, 'Numero Identificador Inversion', 'Ayuda de Numero Identificador de la Inversion o Instrumento', 'NEMO', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. Datos de los informados', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'FechaInversionOriginal', 'C4', '', 6, 'Fecha Inversion Original', 'Ayuda de Fecha Inversion Original', 'DATE_FILTER', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. Datos de los informados', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'RetiroInversion54BISMontoNominal', 'C8', '', 20, 'Monto Nominal', 'Ayuda de Monto Nominal', 'AMOUNT', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. Retiro de la inversión acogida al 54 BIS de la LIR', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'RetiroInversion54BISInteresPositivo', 'C9', '', 25, 'Intereses y Otros Rendimientos - Positivo', 'Ayuda de Intereses y Otros Rendimientos - Positivo', 'AMOUNT', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. Retiro de la inversión acogida al 54 BIS de la LIR', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'RetiroInversion54BISInteresNegativo', 'C10', '', 26, 'Intereses y Otros Rendimientos - Negativo', 'Ayuda de Intereses y Otros Rendimientos - Negativo', 'AMOUNT', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. Retiro de la inversión acogida al 54 BIS de la LIR', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'NumeroCertificado', 'C11', '', 27, 'Numero Certificado', 'Ayuda de Numero Certificado', 'ID', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. Datos Certificado', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'IdDocumento', '', '', 28, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'VboUsuario', '', '', 29, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'Observaciones', '', '', 30, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'FuenteDatos', '', '', 31, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2020', 'IdCliente', '', '', 32, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'S', 'S', 'N', 'N', '5. DATOS DE CONTROL', 0, 0);
-- INSERT TABLA CAMPO 2021
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'ID', '', '', 1, 'Secuencia', 'Ayuda  de Secuencia', 'ID', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'Numero', 'C0', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos de los informados', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'RutCliente', 'C1', '', 3, 'Rut Cliente', 'Ayuda de Rut Cliente', 'RUT_CLIENTE', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos de los informados', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'TipoInstrumento', 'C2', '', 4, 'Tipo de Instrumento', 'Ayuda de Tipo de Instrumento', 'NEMO', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. Datos de los informados', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'NumeroIdentificadorInversion', 'C3', '', 5, 'Numero Identificador Inversion', 'Ayuda de Numero Identificador de la Inversion o Instrumento', 'NEMO', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. Datos de los informados', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'FechaInversionOriginal', 'C4', '', 6, 'Fecha Inversion Original', 'Ayuda de Fecha Inversion Original', 'DATE_FILTER', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '1. Datos de los informados', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'RetiroInversion54BISMontoNominal', 'C8', '', 20, 'Monto Nominal', 'Ayuda de Monto Nominal', 'AMOUNT', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. Retiro de la inversión acogida al 54 BIS de la LIR', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'RetiroInversion54BISInteresPositivo', 'C9', '', 25, 'Intereses y Otros Rendimientos - Positivo', 'Ayuda de Intereses y Otros Rendimientos - Positivo', 'AMOUNT', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. Retiro de la inversión acogida al 54 BIS de la LIR', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'RetiroInversion54BISInteresNegativo', 'C10', '', 26, 'Intereses y Otros Rendimientos - Negativo', 'Ayuda de Intereses y Otros Rendimientos - Negativo', 'AMOUNT', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '3. Retiro de la inversión acogida al 54 BIS de la LIR', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'NumeroCertificado', 'C11', '', 27, 'Numero Certificado', 'Ayuda de Numero Certificado', 'ID', 0, '', 'S', 'S', 'S', 'S', 'N', 'N', 'N', 'N', '4. Datos Certificado', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'IdDocumento', '', '', 28, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'VboUsuario', '', '', 29, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'Observaciones', '', '', 30, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'FuenteDatos', '', '', 31, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1914', '2021', 'IdCliente', '', '', 32, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'S', 'S', 'N', 'N', '5. DATOS DE CONTROL', 0, 0);


--SERVICIOS--
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290711, 'BSCL.Normativa.BD.Servicios::TRI.DJ1914.GrabarMasivo', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290711);


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3319, 'BSCL.Normativa.BD.Servicios::TRI.DJ1914.GrabarDetalle', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3319);


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3318, 'BSCL.Normativa.BD.Servicios::TRI.DJ1914.EliminarDetalles', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3318);


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3320, 'BSCL.Normativa.BD.Servicios::TRI.DJ1914.ObtenerTotales', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3320);


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290712, 'BSCL.Normativa.BD.Vistas::TRI.DJ1914.Invalidos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290712);


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 6124, 'BSCL.Normativa.BD.Servicios::TRI.DJ1914.GenerarCertificado45', 0, 'P', '', '', '', 'S'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=6124);
--SERVICIOS--



--VALIDACIONES 2020
insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1914' as "Esquema",
    '2020' as "Version",
    'H1810' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'RUT inversionista no debe estar en blanco y debe cumplir módulo 11 y debe ser menor o igual a 50.000.000.' as "Descripcion",
    'E' as "Severidad",
    'C1 <> Blanco o Vacío .y M11{(C1)} .y. C1> 50.000.000'  as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1914' as "Esquema",
    '2020' as "Version",
    'H1811' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Verificar, que se haya indicado el tipo de instrumento [TI]: 1,2,3,4,5' as "Descripcion",
    'E' as "Severidad",
    'C2 = 1.o.2.o.3.o.4.o.5'  as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1914' as "Esquema",
    '2020' as "Version",
    'H1812' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Verificar que  N° de identificador de la inversión o instrumento no debe estar en blanco .' as "Descripcion",
    'E' as "Severidad",
    'C3 <> blaco o vacío'  as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1914' as "Esquema",
    '2020' as "Version",
    'H1815' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si Tipo de Instrumento [C2] es igual a 1 .o. 2 .o.3 .o. 4.o.5.  Entonces, el Monto Nominal de la Inversión o Reinversión  debe ser mayor que 0.' as "Descripcion",
    'E' as "Severidad",
    'si C2 =  1 .o. 2 .o.3 .o. 4.o.5 => C5 > 0'  as "Formula"
from dummy;

--VALIDACIONES 2021
insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1914' as "Esquema",
    '2021' as "Version",
    'H1810' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'RUT inversionista no debe estar en blanco y debe cumplir módulo 11 y debe ser menor o igual a 50.000.000.' as "Descripcion",
    'E' as "Severidad",
    'C1 <> Blanco o Vacío .y M11{(C1)} .y. C1> 50.000.000'  as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1914' as "Esquema",
    '2021' as "Version",
    'H1811' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Verificar, que se haya indicado el tipo de instrumento [TI]: 1,2,3,4,5' as "Descripcion",
    'E' as "Severidad",
    'C2 = 1.o.2.o.3.o.4.o.5'  as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1914' as "Esquema",
    '2021' as "Version",
    'H1812' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Verificar que  N° de identificador de la inversión o instrumento no debe estar en blanco .' as "Descripcion",
    'E' as "Severidad",
    'C3 <> blaco o vacío'  as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1914' as "Esquema",
    '2021' as "Version",
    'H1815' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si Tipo de Instrumento [C2] es igual a 1 .o. 2 .o.3 .o. 4.o.5.  Entonces, el Monto Nominal de la Inversión o Reinversión  debe ser mayor que 0.' as "Descripcion",
    'E' as "Severidad",
    'si C2 =  1 .o. 2 .o.3 .o. 4.o.5 => C5 > 0'  as "Formula"
from dummy;



-- Control Proceso
insert into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1914' AND "Version" IN ('2020');

--CERTIFICADO
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "Esquema"='DJ1914';

DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1914';

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1914', '2020', 0, 'CERTIFICADO_45', 'Certificado 45', 'GenerarCertificado45');