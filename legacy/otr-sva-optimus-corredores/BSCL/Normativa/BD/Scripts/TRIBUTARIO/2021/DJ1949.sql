SET SCHEMA BSCL;
DROP TABLE "BSCL.Normativa.BD::TRI.DatosDJ1949";
CREATE COLUMN TABLE  "BSCL.Normativa.BD::TRI.DatosDJ1949" (
    	"ID" 													BIGINT 			NOT NULL,
    	"IdDocumento" 											BIGINT 			NOT NULL,
   		"VboUsuario" 											CHAR(1) 		NOT NULL,
    	"Observaciones" 										NVARCHAR(250) 	NULL,
    	"FuenteDatos" 											NVARCHAR(40) 	NOT NULL,
		"IdEventoCapitalCliente" 								BIGINT			        ,
		"Mercado"												NVARCHAR(3) 	NOT NULL,
		"Nemo"													NVARCHAR(100)	NOT NULL,
		"TipoRegistro"											NVARCHAR(1)		NULL,
		"FecMovto"												DATE			NOT NULL,
		"SecEvento"												BIGINT			NOT NULL,
		"RutSociedad"											NVARCHAR(40)	NOT NULL,
		"TipoSociedad"											NVARCHAR(1)		NOT NULL,
		"RutCliente"											NVARCHAR(40)	NOT NULL,
		"Cuenta"												NVARCHAR(40)	        ,
		"CantCuotas3112"										DECIMAL(26,8)	DEFAULT 0,
		"Monto"													DECIMAL(26,8)	DEFAULT 0,
		"MontoActualizado"										DECIMAL(26,8)	DEFAULT 0,
		"MtoDivReaAfecGCCCIDPC_C6" 								DECIMAL(26,8)	DEFAULT 0,
		"MtoDivReaAfecGCCCIDPCAL311216_C7"						DECIMAL(26,8)	DEFAULT 0,
		"MtoDivReaAfecGCCDIDPCVol_C8"							DECIMAL(26,8)	DEFAULT 0,
		"MtoDivReaAfecGCSinDer_C9"								DECIMAL(26,8)	DEFAULT 0,
		"MtoDivReaREXTribCumpRentasRap_C10"						DECIMAL(26,8)	DEFAULT 0,
		"MtoDivReaREXTribCumpOtrasRentas_C11"					DECIMAL(26,8)	DEFAULT 0,
		"MtoDivReaREXTribCumpExcDist_C12" 						DECIMAL(26,8)	DEFAULT 0,
		"MtoDivReaREXTribCumpUtiISFUT_C13"						DECIMAL(26,8)	DEFAULT 0,
		"MtoDivReaREXTribCumpRentasISFUT_C14"					DECIMAL(26,8)	DEFAULT 0,
		"MtoDivReaREXRentExIGCAfecIAart11_C15"					DECIMAL(26,8)	DEFAULT 0,
		"MtoDivReaREXRentExIGCIA_C16"							DECIMAL(26,8)	DEFAULT 0,
		"MtoDivReaREXNoConstRenta_C17"							DECIMAL(26,8)	DEFAULT 0,
		"CIGCDesde01012017RentAfecNoRestSDD_C18"				DECIMAL(26,8)	DEFAULT 0,
		"CIGCDesde01012017RentAfecNoRestCDD_C19"				DECIMAL(26,8)	DEFAULT 0,
		"CIGCDesde01012017RentAfecNoRestSDD_C20"				DECIMAL(26,8)	DEFAULT 0,
		"CIGCDesde01012017RentAfecNoRestCDD_C21"				DECIMAL(26,8)	DEFAULT 0,
		"CIGCDesde01012017RentAfecRestSDD_C22"					DECIMAL(26,8)	DEFAULT 0,
		"CIGCDesde01012017RentAfecRestCDD_C23"					DECIMAL(26,8)	DEFAULT 0,
		"CIGCDesde01012017RentExenRestSDD_C24"					DECIMAL(26,8)	DEFAULT 0,
		"CIGCDesde01012017RentExenRestCDD_C25"					DECIMAL(26,8)	DEFAULT 0,
		"CIGCDesde01012017CredIPE_C26"							DECIMAL(26,8)	DEFAULT 0,
		"CIGCHasta31122016RentAfecSDD_C27"						DECIMAL(26,8)	DEFAULT 0,
		"CIGCHasta31122016RentAfecCDD_C28"						DECIMAL(26,8)	DEFAULT 0,
		"CIGCHasta31122016RentExenSDD_C29"						DECIMAL(26,8)	DEFAULT 0,
		"CIGCHasta31122016RentExenCDD_C30"						DECIMAL(26,8)	DEFAULT 0,
		"CIGCHasta31122016CredIPE_C31"							DECIMAL(26,8)	DEFAULT 0,
		"CIGCImptoTasaAdicional_C32"							DECIMAL(26,8)	DEFAULT 0,
		"TasaEfectCredFUT"										DECIMAL(26,8)	DEFAULT 0,
		"TasaEfectCredFUNT"										DECIMAL(26,8)	DEFAULT 0,
		"DevCapArt17N7LIR_C33"									DECIMAL(26,8)	DEFAULT 0,
		"NumCertificado_C34"									BIGINT	                 ,
   PRIMARY KEY ("ID")
);


DROP TABLE BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71";
CREATE TABLE BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71" (
--Datos de Control
	"IdDetalleCertificadoClienteDJ1949N71" 	BIGINT NOT NULL,
	"IdCertificadoCliente" 					BIGINT,
	"IdCliente"								BIGINT,
    "IdDocumento" 							BIGINT NOT NULL,
    "IdTipoDocumento"						NVARCHAR(10),
	"Version" 								NVARCHAR(10),
	"IdEmpresa"								BIGINT,
	--Datos de certificado
	"FechaDividendo" 						DATE,
	"RutSocPagadora"						NVARCHAR(40) NOT NULL,
	"TipoSociedad"							NVARCHAR(1)	 NOT NULL,
	"RutReceptor"							NVARCHAR(40) NOT NULL,
	"MontoF6"								DECIMAL(26,8)	DEFAULT 0,
	"MontoActualizadoF8"					DECIMAL(26,8)	DEFAULT 0,
	--Datos de dividendo
	"NroCertificadoF5"						DECIMAL(26,8)	DEFAULT 0,
	"FactorF7"								DECIMAL(26,8)	DEFAULT 1,
	"NroDividendoF2"						DECIMAL(26,8) 	DEFAULT 0,
	--Montos Retiros reajustados
	"ConCreditoIDPCDesed2017C6F9"			DECIMAL(26,8)	DEFAULT 0,
	"ConCreditoIDPCHasta2017C7F10"			DECIMAL(26,8)	DEFAULT 0,
	"ConDerechoIDPCVoluntarioC8F11"			DECIMAL(26,8)	DEFAULT 0,
	"SinDerechoCreditoC9F12"				DECIMAL(26,8)	DEFAULT 0,
	--Rentas con tributación cumplida
	"MtoDivReaREXTribCumpRentasRap_C10F13"	 DECIMAL(26,8) DEFAULT 0,
	"MtoDivReaREXTribCumpOtrasRentas_C11F14" DECIMAL(26,8) DEFAULT 0,
	"MtoDivReaREXTribCumpExcDist_C12F15"	DECIMAL(26,8) DEFAULT 0,
	"MtoDivReaREXTribCumpUtiISFUT_C13F16"		DECIMAL(26,8) DEFAULT 0,
	"MtoDivReaREXTribCumpRentasISFUT_C14F17"	DECIMAL(26,8) DEFAULT 0,
	--Rentas Excentas
	"MtoDivReaREXRentExIGCAfecIAart11_C15F18"	DECIMAL(26,8)	DEFAULT 0,
	"RentasExcentasIGCC16F19" 				DECIMAL(26,8) DEFAULT 0,
	"MtoDivReaREXNoConstRenta_C17F20"			DECIMAL(26,8)	DEFAULT 0,
	--Resto
	"CIGCDesde01012017RentAfecNoRestSDD_C18F21"				DECIMAL(26,8)	DEFAULT 0,
	"CIGCDesde01012017RentAfecNoRestCDD_C19F22"				DECIMAL(26,8)	DEFAULT 0,
	"CIGCDesde01012017RentAfecNoRestSDD_C20F23"				DECIMAL(26,8)	DEFAULT 0,
	"CIGCDesde01012017RentAfecNoRestCDD_C21F24"				DECIMAL(26,8)	DEFAULT 0,
	"CIGCDesde01012017RentAfecRestSDD_C22F25"				DECIMAL(26,8)	DEFAULT 0,
	"CIGCDesde01012017RentAfecRestCDD_C23F26"				DECIMAL(26,8)	DEFAULT 0,
	"CIGCDesde01012017RentExenRestSDD_C24F27"				DECIMAL(26,8)	DEFAULT 0,
	"CIGCDesde01012017RentExenRestCDD_C25F28"				DECIMAL(26,8)	DEFAULT 0,
	"CIGCDesde01012017CredIPE_C26F29"						DECIMAL(26,8)	DEFAULT 0,
	"CIGCHasta31122016RentAfecSDD_C27F30"					DECIMAL(26,8)	DEFAULT 0,
	"CIGCHasta31122016RentAfecCDD_C28F31"					DECIMAL(26,8)	DEFAULT 0,
	"CIGCHasta31122016RentExenSDD_C29F32"					DECIMAL(26,8)	DEFAULT 0,
	"CIGCHasta31122016RentExenCDD_C30F33"					DECIMAL(26,8)	DEFAULT 0,
	"CIGCHasta31122016CredIPE_C31F34"						DECIMAL(26,8)	DEFAULT 0,
	"CIGCImptoTasaAdicional_C32F35"							DECIMAL(26,8)	DEFAULT 0,
	"DevCapArt17N7LIR_C33F36"								DECIMAL(26,8)	DEFAULT 0,
	"TasaEfectCredFUT"										DECIMAL(26,8)	DEFAULT 0,
	"TasaEfectCredFUNT"										DECIMAL(26,8)	DEFAULT 0,
	"RutCliente"											NVARCHAR(40)	NOT NULL,
	PRIMARY KEY ("IdDetalleCertificadoClienteDJ1949N71")
);
ALTER TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1949N71"
    ADD CONSTRAINT "FK_CertificadoCliente_DetalleCertificadoClienteDJ1949N71" FOREIGN KEY ("IdCertificadoCliente")
        REFERENCES "BSCL.Normativa.BD::TRI.CertificadoCliente" ("IdCertificadoCliente")
        ON DELETE RESTRICT ON UPDATE RESTRICT;
--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1949";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" WHERE "Esquema"	= 'DJ1949';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  	WHERE "Esquema"	= 'DJ1949';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" 			WHERE "Esquema" = 'DJ1949';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" 	WHERE "Esquema"	= 'DJ1949';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" 		WHERE "Esquema"	= 'DJ1949';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" 		WHERE "Esquema" = 'DJ1949';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" 		WHERE "Esquema"	= 'DJ1949';
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" 	WHERE "Esquema"	= 'DJ1949';

--Se crea secuencia de certificado
DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1949N71";
CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::TRI.IdDetalleCertificadoClienteDJ1949N71";

--Certificado
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "Esquema"='DJ1949';

DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"	= 'DJ1949';

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1949', '2020', 0, 'CERTIFICADO_71', 'Certificado 71', 'GenerarCertificado71');

--Archivo Normativa
DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1949';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"("IdArchivoNormativa","VersionArchivo","Esquema","Version",
"IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('CERT71-PDF','2020','DJ1949','3','CERT71','CERTIFICADO 71 TRIBUTARIOS','CERTIFICADO71','','pdf','3613',1,'ACT');


--Esquema
INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor", "TieneCertificado")
VALUES
('DJ1949','2020','TRI','A','Declaracion Jurada anual sobre rentas obtenidas por terceros.','2020-01-01','SII','2021-01-01','2020-01-01','2020-12-31',0,'S', 'S');


insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1949' AS "Esquema"
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



--VALIDACIONES

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1949', '2020', 'H2827', 'NORMA', 'S', 'En la columna "FECHA DEL RETIRO, REMESA Y/O DIVIDENDO DISTRIBUIDO" (C1) es de llenado obligatorio, debe poseer formato fecha (DD/MM/AAAA) y debe corresponder a alguna fecha.', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1949', '2020', 'H2857', 'NORMA', 'S', 'Dado que existen solo retiros, remesas o dividendos reajustados ($), afectos a los Impuestos Global Complementario y/o Adicional,  que no tiene  derecho a credito por IDPC, declarados  en el campo  "Sin derecho a crdito (C9)", y no se informaron retiros, remesas o dividendos afectos a los Impuestos Global Complementario.', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1949', '2020', 'H2858', 'NORMA', 'S', 'Si sólo declara retiros, remesas y dividendos reajustados ($), en el campo "Rentas con tributación cumplida (C10), (C11), (C12), (C13), (C14)" y "Rentas Exentas de Impuesto Global Complementario (IGC) y/o Impuesto Adicional (C16)", y no se informaron retiros, remesas o dividendos afectos a los Impuestos Global Complementario y/o Adicional y Exentos, con derecho a crédito por IDPC en las columnas (C6), (C7), (C8) o (C15)".', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1949', '2020', 'H2859', 'NORMA', 'S', 'Si sólo declara retiros, remesas y dividendos reajustados ($), en el campo "Rentas Exentas e Ingresos No Constitutivos de Renta (REX), Rentas Exentas, Rentas Exentas de Impuesto Global Complementario (IGC) (Artículo 11, Ley 18.401), Afectas a Impuesto Adicional (C15)", y no se informaron retiros, remesas o dividendos afectos a los Impuestos Global Complementario y/o Adicional, con o sin derecho a crédito por IDPC en las columnas (C6), (C7) o (C8)', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1949', '2020', 'H2860', 'NORMA', 'S', 'Dado que existen retiros, remesas o dividendos reajustados ($),informados  en el campo  "Ingresos No constitutivos de renta (C17)" y no se informaron retiros, remesas o dividendos afectos a los Impuestos Global Complementario y/o Adicional, con derecho a crédito por IDPC en las columnas  (C6), (C7) o (C8) y además podría o no estar declarando valor en el campo de retiros.', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1949', '2020', 'H2861', 'NORMA', 'S', 'Columna C2 "RUT de la sociedad pagadora del dividendo " es un campo obligatorio y debe ingresar un RUT válido', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1949', '2020', 'H2862', 'NORMA', 'S', 'Columna C4 "RUT del beneficiario de los dividendos distribuidos " es un campo obligatorio y debe ingresar un RUT válido', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1949', '2020', 'H2863', 'NORMA', 'S', 'Si informa Monto debe informar RUT del beneficiario de los dividendos distribuidos', 'E', '');


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


--CONTROL PROCESO

INSERT into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1949' AND "Version" IN ('2020');

--SERVICIOS CAMBIAR LOS NUMEROS DESPUES DE CREARLOS
/*
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600023, 'BSCL.Normativa.BD.Vistas::TRI.DJ1949.Invalidos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600023);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600054, 'PLUGIN_NOR.jar@cl.bcs.normativa.declaraciones.DJ1949.AjustarRescatesDelPeriodo', 2, 'J', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600054);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600027, 'PLUGIN_NOR.jar@cl.bcs.normativa.declaraciones.DJ1949.ReajustarValorIPC', 3, 'J', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600027);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600022, 'BSCL.Normativa.BD.Vistas::TRI.DJ1949.PreDatos', 180, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600022);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600025, 'BSCL.Normativa.BD.Servicios::TRI.DJ1949.GrabarDetalle', 3, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600025);


*/

-- Servicios

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290665, 'BSCL.Normativa.BD.Servicios::TRI.DJ1949.ObtenerTotales', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290665);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290666, 'BSCL.Normativa.BD.Vistas::TRI.DJ1949.Datos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290666);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 44290668, 'PLUGIN_NOR.jar@cl.bcs.normativa.declaraciones.DJ1949.ActualizarDetalles', 1, 'J', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=44290668);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290669, 'BSCL.Normativa.BD.Servicios::TRI.DJ1949.EliminarDetalles', 4, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290669);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290673, 'BSCL.Normativa.BD.Servicios::TRI.DJ1949.AsociarEventoCapital', 4, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290673);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290675, 'BSCL.Normativa.BD.Vistas::TRI.DJ1949.CalificacionPendiente', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290675);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3610, 'BSCL.Normativa.BD.Servicios::TRI.DJ1949.GenerarCertificado71', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3610);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3613, 'BSCL.Normativa.BD.Servicios::TRI.DJ1949.GenerarDocumentoCertificado71', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3613);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290671, 'BSCL.Normativa.BD.Vistas::TRI.DJ1949.DatosComplemento', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290671);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600095, 'BSCL.Normativa.BD.Servicios::TRI.DJ1949.GrabarDetalle', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600095);


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290707, 'BSCL.Normativa.BD.Servicios::TRI.DJ1949.GrabarMasivo', 1, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290707);

--COMPLEMENTOS
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.Complemento" WHERE "Esquema"='DJ1949' AND "CodigoComplemento"='ACTUALIZARMONTOS';
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.Complemento" WHERE "Esquema"='DJ1949' AND "CodigoComplemento"='EVENTOCAPITAL';
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.Complemento" WHERE "Esquema"='DJ1949' AND "CodigoComplemento"='CALIFICACIONPENDIENT';

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento" ("Esquema", "CodigoComplemento", "Descripcion")
VALUES('DJ1949', 'ACTUALIZARMONTOS', 'Procesar Montos');
INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento" ("Esquema", "CodigoComplemento", "Descripcion")
VALUES('DJ1949', 'EVENTOCAPITAL', 'Evento Capital');
INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento" ("Esquema", "CodigoComplemento", "Descripcion")
VALUES('DJ1949', 'CALIFICACIONPENDIENT', 'Calificaciones Pendientes');


-- Diccionario
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_PROCESAR_MONTOS',
'CL',
'Procesar Montos',
'PROMON'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_PROCESAR_MONTOS');


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_PROCESAR_MONTOS_SEGUN_CALIFICACIONES_TRIBUTARIAS_INGRESADAS',
'CL',
'Procesar los montos según Calificaciones Tributarias ingresadas',
'PROCAL'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_PROCESAR_MONTOS_SEGUN_CALIFICACIONES_TRIBUTARIAS_INGRESADAS');


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_100_POR_AFECTO_CUANDO_NO_HAY_CALIFICACION_TRIBUTARIA',
'CL',
'100 % afecto cuando no hay Calificación Tributaria',
'100AFEC'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_100_POR_AFECTO_CUANDO_NO_HAY_CALIFICACION_TRIBUTARIA');



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_CONFIRMA',
'CL',
'Confirma',
'conf'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_CONFIRMA');

INSERT INTO BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma"(
	"IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta","TipoSalida","FormatoSalida","TieneLog")
SELECT 'GenerarCertificado71' ,'Generar Certificado 71' ,1,'Tributario','cl.bcs.normativa.complementos.BcsJobGenerarCertificado71' ,'JAVA','','N','P','','N' 
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma" = 'GenerarCertificado71');


INSERT INTO BSCL."BSCL.Normativa.BD::TRI.Complemento" ("Esquema", "CodigoComplemento", "Descripcion")
SELECT 'DJ1949', 'EVENTOCAPITAL', 'Evento Capital' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Normativa.BD::TRI.Complemento" WHERE  "Esquema" = 'DJ1949' AND "CodigoComplemento" = 'EVENTOCAPITAL');


DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" IN ('UI_ASOCIAR_EVENTO_CAPITAL', 'UI_DEBE_SELECCIONAR_UN_EVENTO_ASOCIAR');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_ASOCIAR_EVENTO_CAPITAL', 'CL', 'Evento Capital asociado correctamente.', '');


 INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
VALUES('UI_DEBE_SELECCIONAR_UN_EVENTO_ASOCIAR', 'CL', 'Debe seleccionar un registro válido y posteriormente asociar.', '');

-- PARAMETRO:  Nombre de archivo CERTIFICADOS
DELETE  FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" WHERE "IdNormativa"='DJ1949' AND "CodigoParametro" LIKE 'TRI_NOM_CERTIFICADO%';
  
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_01'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[RUT]' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'[RUT]'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1949' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_01' AND N."IdNormativa"="Esquema");  

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_NOM_' || "NombreArchivo" ||  '_02'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'AV' AS "Valor"
,"Esquema" AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Certificado masivo' AS "Uso"
,'AV'  AS "ValorDefecto"
,'[RUT],[PERIODO],[DJ],[CERT],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1949' AND "VersionArchivo"=2020
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
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1949' AND "VersionArchivo"=2020
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
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1949' AND "VersionArchivo"=2020
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
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1949' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_05' AND N."IdNormativa"="Esquema");  


