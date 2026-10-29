-- Drop table
SET SCHEMA "BSCL";

DROP TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1944";

CREATE TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1944" (
	ID 							BIGINT NOT NULL,
	"IdDocumento" 				BIGINT NOT NULL,
	"VboUsuario" 				CHAR(1) NOT NULL,
	"Observaciones" 			NVARCHAR(250) NOT NULL,
	"FuenteDatos" 				NVARCHAR(40) NOT NULL,
	"EjecutadoComplementoIPC"	NVARCHAR(1),
	"Numero" 					BIGINT NOT NULL,
	"TipoCartera" 				NVARCHAR(10),
	"RutCliente" 				NVARCHAR(40) NOT NULL,
	"IdCliente" 				BIGINT,
	"FechaOperacion" 			DATE,
	"RutInstitucion" 			NVARCHAR(40) NOT NULL,
	"SalAhorroNetoEjerNEGT1"	DECIMAL(26,8) NOT NULL,
	"SalArrastreEjerSIGT1"   	DECIMAL(26,8) NOT NULL,
	"SalAhorroNetoEjerT2"    	DECIMAL(26,8) NOT NULL,
	"SalArrastreEjerSigNegT2"	DECIMAL(26,8) NOT NULL,
	"MtoRetiro57BisT2"			DECIMAL(26,8) NOT NULL,
	"SalAhorroNetoEjeNegT3"		DECIMAL(26,8) NOT NULL,
	"SalArrastreEjeSigNegT3" 	DECIMAL(26,8) NOT NULL,
	"MtoRetiroAnoT3"			DECIMAL(26,8) NOT NULL,
	"MtoRenRetFFMMPOS"			DECIMAL(26,8) NOT NULL,
	"MtoRenRetFFMMNEG"			DECIMAL(26,8) NOT NULL,
	"MtoRenRetOTROSPOS"			DECIMAL(26,8) NOT NULL,
	"MtoRenRetOTROSNEG"			DECIMAL(26,8) NOT NULL,
--	"MTO_RESC"					DECIMAL(26,8) NOT NULL,
--	"RentabilidadReal"			DECIMAL(26,8) NOT NULL,
--	"SalAhorroNetoEjePosT3"		DECIMAL(26,8) NOT NULL,
	"NumeroCertificado"			BIGINT,
	PRIMARY KEY (ID)
	);

DROP TABLE BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N8";	
	
CREATE TABLE BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N8" (
	"IdDetalleCertificadoClienteDJ1944N8" 	BIGINT NOT NULL,
	"IdCertificadoCliente" 					BIGINT,
	"IdCliente" 							BIGINT,
	"IdTipoDocumento" 						NVARCHAR(10),
	"SalAhorroNetoEjerNEGT1"                DECIMAL(26,8),
    "SalAhorroNetoEjerT2"                   DECIMAL(26,8),
    "SalAhorroNetoEjeNegT3"                 DECIMAL(26,8),
    "MtoRenRetFFMMPOS"                      DECIMAL(26,8),
    "MtoRenRetFFMMNEG"                      DECIMAL(26,8),
	"IdEmpresa" 							BIGINT,
	"Version" 								NVARCHAR(10),
	"RutCliente"							NVARCHAR(40)	NOT NULL,
	PRIMARY KEY ("IdDetalleCertificadoClienteDJ1944N8")
);

--
ALTER TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N8"
    ADD CONSTRAINT "FK_CertificadoCliente_DetalleCertificadoClienteDJ1944N8" FOREIGN KEY ("IdCertificadoCliente")
        REFERENCES "BSCL.Normativa.BD::TRI.CertificadoCliente" ("IdCertificadoCliente")
        ON DELETE RESTRICT ON UPDATE RESTRICT;

DROP TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N17";    
        
CREATE TABLE BSCL."BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N17" (
	"IdDetalleCertificadoClienteDJ1944N17" 	BIGINT NOT NULL,
	"IdCertificadoCliente" 					BIGINT,
	"IdCliente" 							BIGINT,
	"IdTipoDocumento" 						NVARCHAR(10),
	"SalAhorroNetoEjerNEGT1"                DECIMAL(26,8),
    "SalAhorroNetoEjerT2"                   DECIMAL(26,8),
    "SalAhorroNetoEjeNegT3"                 DECIMAL(26,8),
    "MtoRenRetFFMMPOS"                      DECIMAL(26,8),
    "MtoRenRetFFMMNEG"                      DECIMAL(26,8),
	"IdEmpresa" 							BIGINT,
	"Version" 								NVARCHAR(10),
	"RutCliente"							NVARCHAR(40)	NOT NULL,
	PRIMARY KEY ("IdDetalleCertificadoClienteDJ1944N17")
);

--
ALTER TABLE "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1944N17"
    ADD CONSTRAINT "FK_CertificadoCliente_DetalleCertificadoClienteDJ1944N17" FOREIGN KEY ("IdCertificadoCliente")
        REFERENCES "BSCL.Normativa.BD::TRI.CertificadoCliente" ("IdCertificadoCliente")
        ON DELETE RESTRICT ON UPDATE RESTRICT;

	
--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1944";

DELETE FROM BSCL."BSCL.Normativa.BD::TRI.Complemento"  WHERE "Esquema"='DJ1944';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1944';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1944';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1944';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1944';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1944';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1944';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1944';

DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1944';



INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Esquema"
("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII", "HabilitadoMantenedor", "TieneCertificado")
SELECT 'DJ1944', '2020', 'TRI', 'A', 'Declaración Jurada Anual sobre saldo de cuentas de inversión art. 57 bis', '2020-01-01', 'SII', '2021-01-01', '2020-01-01', '2020-12-31', 1, 'S', 'S'
FROM DUMMY;

--CAMPOS 2020
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'IdDocumento', '', '', 2, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'VboUsuario', '', 'VistoBueno', 3, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'Observaciones', '', '', 4, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'FuenteDatos', '', '', 5, 'Fuente', 'Ayuda de Fuente', 'TEXT', 40, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'Numero', 'C0', '', 6, 'Numero', '', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '6. DATOS DE CONTROL', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'RutCliente', 'C1', '', 7, 'Rut Trabajador', 'Ayuda de RUT Trabajador', 'RUT_CLIENTE', 40, '', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'IdCliente', '', '', 8, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'N', 'N', 'N', 'N', 'S', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'RutInstitucion', 'C2', '', 9, 'Rut Institucion', 'Ayuda de RUT Institucion', 'RUT_CLIENTE', 40, '', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'SalAhorroNetoEjerNEGT1', 'C3', '', 10, 'Saldo de Ahorro Neto del Ejercicio Negativo 1', 'Ayuda Saldo de Ahorro Neto del Ejercicio Negativo', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. INVERSIONES EFECTUADAS CON ANTERIORIDAD AL 01.08.98', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'SalArrastreEjerSIGT1', 'C4', '', 11, 'Saldo de Arrastre Ejercicio Siguiente Negativo 1', 'Ayuda Saldo de Arrastre Ejercicio Siguiente Negativo', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. INVERSIONES EFECTUADAS CON ANTERIORIDAD AL 01.08.98', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'SalAhorroNetoEjerT2', 'C5', '', 12, 'Saldo Ahorro Neto del Ejercicio Negativo 2', 'Ayuda Saldo Ahorro Neto del Ejercicio Negativo', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. INVERSIONES EFECTUADAS A CONTAR DEL 01.08.98 HASTA EL 31.12.2014', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'SalArrastreEjerSigNegT2', 'C6', '', 13, 'Saldo de Arrastre Ejercicio Siguiente Negativo 2', 'Ayuda Saldo de Arrastre Ejercicio Siguiente Negativo', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. INVERSIONES EFECTUADAS A CONTAR DEL 01.08.98 HASTA EL 31.12.2014', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'MtoRetiro57BisT2', 'C7', '', 14, 'Monto Retiro INV. 57 BIS Hasta 31.12.2014', 'Ayuda Monto Retiro INV. 57 BIS Hasta 31.12.2014', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. MONTO RETIRO INV. 7 BIS HASTA 31.12.2014', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'SalAhorroNetoEjeNegT3', 'C9', '', 15, 'Saldo Ahorro Neto del Ejercicio Negativo 3', 'Ayuda Saldo Ahorro Neto del Ejercicio Negativo', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'SalArrastreEjeSigNegT3', 'C10', '', 16, 'Saldo de Arrastre Ejercicio Siguiente Negativo 3', 'Ayuda Saldo de Arrastre Ejercicio Siguiente Negativo', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'MtoRetiroAnoT3', 'C11', '', 17, 'Monto Retiro del Año', 'Ayuda Monto Retiro del Año', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'MtoRenRetFFMMPOS', 'C12', '', 18, 'Monto Rentabilidad o Ganancia Retirada en FFMM Positiva 4', 'Ayuda Monto Rentabilidad o Ganancia retirada en FFMM Positiva', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'MtoRenRetFFMMNEG', 'C13', '', 19, 'Monto Rentabilidad o Ganancia Retirada en FFMM Negativa 4', 'Ayuda Monto Rentabilidad o Ganancia Retirada en FFMM Negativa', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'MtoRenRetOTROSPOS', 'C14', '', 20, 'Monto Rentabilidad o Ganancia Retirada en Otros Instrumentos Positiva 5', 'Ayuda Monto Rentabilidad o Ganancia Retirada en Otros Instrumentos Positiva', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'MtoRenRetOTROSNEG', 'C15', '', 21, 'Monto Rentabilidad o Ganancia Retirada en Otros Instrumentos Negativa 5', 'Ayuda Monto Rentabilidad o Ganancia Retirada en Otros Instrumentos Negativa', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 15, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'NumeroCertificado', 'C16', '', 22, 'Número Certificado', 'Ayuda Número Certificado', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 16, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'FechaOperacion', '', '', 23, 'Fecha Operacion', '', 'DATE_FILTER', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 17, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'TipoCartera', '', '', 24, 'TipoCartera', '', 'TEXT', 1, 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2020', 'EjecutadoComplementoIPC', '', '', 25, 'Ejecucion complemento IPC', '', 'TEXT', 1, 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '6. DATOS DE CONTROL', 0, 0);
--CAMPOS 2021
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'ID', '', '', 1, 'Secuencia', 'Ayuda de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'IdDocumento', '', '', 2, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'VboUsuario', '', 'VistoBueno', 3, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'Observaciones', '', '', 4, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'FuenteDatos', '', '', 5, 'Fuente', 'Ayuda de Fuente', 'TEXT', 40, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'Numero', 'C0', '', 6, 'Numero', '', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '6. DATOS DE CONTROL', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'RutCliente', 'C1', '', 7, 'Rut Trabajador', 'Ayuda de RUT Trabajador', 'RUT_CLIENTE', 40, '', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'IdCliente', '', '', 8, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'N', 'N', 'N', 'N', 'S', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'RutInstitucion', 'C2', '', 9, 'Rut Institucion', 'Ayuda de RUT Institucion', 'RUT_CLIENTE', 40, '', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'SalAhorroNetoEjerNEGT1', 'C3', '', 10, 'Saldo de Ahorro Neto del Ejercicio Negativo 1', 'Ayuda Saldo de Ahorro Neto del Ejercicio Negativo', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. INVERSIONES EFECTUADAS CON ANTERIORIDAD AL 01.08.98', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'SalArrastreEjerSIGT1', 'C4', '', 11, 'Saldo de Arrastre Ejercicio Siguiente Negativo 1', 'Ayuda Saldo de Arrastre Ejercicio Siguiente Negativo', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. INVERSIONES EFECTUADAS CON ANTERIORIDAD AL 01.08.98', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'SalAhorroNetoEjerT2', 'C5', '', 12, 'Saldo Ahorro Neto del Ejercicio Negativo 2', 'Ayuda Saldo Ahorro Neto del Ejercicio Negativo', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. INVERSIONES EFECTUADAS A CONTAR DEL 01.08.98 HASTA EL 31.12.2014', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'SalArrastreEjerSigNegT2', 'C6', '', 13, 'Saldo de Arrastre Ejercicio Siguiente Negativo 2', 'Ayuda Saldo de Arrastre Ejercicio Siguiente Negativo', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. INVERSIONES EFECTUADAS A CONTAR DEL 01.08.98 HASTA EL 31.12.2014', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'MtoRetiro57BisT2', 'C7', '', 14, 'Monto Retiro INV. 57 BIS Hasta 31.12.2014', 'Ayuda Monto Retiro INV. 57 BIS Hasta 31.12.2014', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. MONTO RETIRO INV. 7 BIS HASTA 31.12.2014', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'SalAhorroNetoEjeNegT3', 'C9', '', 15, 'Saldo Ahorro Neto del Ejercicio Negativo 3', 'Ayuda Saldo Ahorro Neto del Ejercicio Negativo', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'SalArrastreEjeSigNegT3', 'C10', '', 16, 'Saldo de Arrastre Ejercicio Siguiente Negativo 3', 'Ayuda Saldo de Arrastre Ejercicio Siguiente Negativo', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'MtoRetiroAnoT3', 'C11', '', 17, 'Monto Retiro del Año', 'Ayuda Monto Retiro del Año', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'MtoRenRetFFMMPOS', 'C12', '', 18, 'Monto Rentabilidad o Ganancia Retirada en FFMM Positiva 4', 'Ayuda Monto Rentabilidad o Ganancia retirada en FFMM Positiva', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'MtoRenRetFFMMNEG', 'C13', '', 19, 'Monto Rentabilidad o Ganancia Retirada en FFMM Negativa 4', 'Ayuda Monto Rentabilidad o Ganancia Retirada en FFMM Negativa', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'MtoRenRetOTROSPOS', 'C14', '', 20, 'Monto Rentabilidad o Ganancia Retirada en Otros Instrumentos Positiva 5', 'Ayuda Monto Rentabilidad o Ganancia Retirada en Otros Instrumentos Positiva', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'MtoRenRetOTROSNEG', 'C15', '', 21, 'Monto Rentabilidad o Ganancia Retirada en Otros Instrumentos Negativa 5', 'Ayuda Monto Rentabilidad o Ganancia Retirada en Otros Instrumentos Negativa', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 15, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'NumeroCertificado', 'C16', '', 22, 'Número Certificado', 'Ayuda Número Certificado', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. INVERSIONES EFECTUADAS A CONTAR DEL 01.01.15 HASTA 31.12.2016', 16, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'FechaOperacion', '', '', 23, 'Fecha Operacion', '', 'DATE_FILTER', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 17, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'TipoCartera', '', '', 24, 'TipoCartera', '', 'TEXT', 1, 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1944', '2021', 'EjecutadoComplementoIPC', '', '', 25, 'Ejecucion complemento IPC', '', 'TEXT', 1, 'N', 'N', 'N', 'N', 'N', 'S', 'S', 'N', 'N', '6. DATOS DE CONTROL', 0, 0);



insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1944' AS "Esquema"
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
VALUES('DJ1944', '2020', 'H2449', 'NORMA', 'S', 'Debe existir al menos un valor en las columnas Inversiones efectuadas con anterioridad al 01.08.98, Inversiones efectuadas a contar del  01.08.98 o Inversiones efectuadas a partir del 01.01.2015 ', 'E', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1944', '2020', 'H2464', 'NORMA', 'S', 'Si hay valor en el campo "Monto Retiro (de inversiones efectuadas a contar del 01.01.2015 al 31.12.2016)", debe haber necesariamente algún valor en las columnas Monto de Rentabilidad o Ganancia Retirada en FFMM u Otros Instrumentos (positiva o negativa).', 'E', '');


--SERVICIOS DJ1944

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600090, 'BSCL.Normativa.BD.Vistas::TRI.DJ1944.Datos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600090);


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600091, 'BSCL.Normativa.BD.Servicios::TRI.DJ1944.EliminarDetalles', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600091);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600092, 'BSCL.Normativa.BD.Servicios::TRI.DJ1944.ObtenerTotales', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600092);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600094, 'BSCL.Normativa.BD.Servicios::TRI.DJ1944.GrabarDetalle', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600094);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 600093, 'BSCL.Normativa.BD.Vistas::TRI.DJ1944.Invalidos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=600093);

INSERT INTO BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma"(
"IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta","TipoSalida","FormatoSalida","TieneLog")
SELECT 'GenerarCertificado8' ,'Generar Certificado 8' ,1,'Tributario','cl.bcs.normativa.complementos.BcsJobGenerarCertificado8' ,'JAVA','','N','P','','N' 
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma" = 'GenerarCertificado8');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3611, 'BSCL.Normativa.BD.Servicios::TRI.DJ1944.GenerarCertificado8', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3611);

INSERT INTO BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma"(
"IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta","TipoSalida","FormatoSalida","TieneLog")
SELECT 'GenerarCertificado17' ,'Generar Certificado 17' ,1,'Tributario','cl.bcs.normativa.complementos.BcsJobGenerarCertificado17' ,'JAVA','','N','P','','N' 
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma" = 'GenerarCertificado17');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3612, 'BSCL.Normativa.BD.Servicios::TRI.DJ1944.GenerarCertificado17', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3612);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290714, 'BSCL.Normativa.BD.Servicios::TRI.DJ1944.GrabarMasivo', 113, 'P', '', '', '', 'S'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290714);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4300003, 'BSCL.Normativa.BD.Vistas::TRI.DJ1944.PreDatos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4300003);

--CONTROL PROCESO

insert into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1944' AND "Version" IN ('2020');


--CERTIFICADO
DELETE FROM BSCL."BSCL.Normativa.BD::TRI.CertificadoCliente" WHERE "Esquema"='DJ1944';

DELETE FROM BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente" WHERE "Esquema"='DJ1944'; 

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1944', '2020', 0, 'CERTIFICADO_8', 'Certificado 8', 'GenerarCertificado8');

INSERT INTO BSCL."BSCL.Normativa.BD::TRI.FolioCertificadoCliente"
("Esquema", "Version", "UltimoFolio", "CodigoCertificado", "NombreCertificado", "CodigoPrograma")
VALUES('DJ1944', '2020', 0, 'CERTIFICADO_17', 'Certificado 17', 'GenerarCertificado17');

--Archivo Normativa
DELETE FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"  WHERE "Esquema"='DJ1944';

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT8-PDF', '2020', 'DJ1944', '3', 'CERT8', 'CERTIFICADO 8 TRIBUTARIOS', 'CERTIFICADO8', '', 'pdf', '3611', 1, 'ACT');

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa"
("IdArchivoNormativa", "VersionArchivo", "Esquema", "Version", "IdTipoDocumento", "Descripcion", "NombreArchivo", "IdArchivo", "TipoArchivo", "ServicioArchivo", "IdEmpresa", "IdEstado")
VALUES('CERT17-PDF', '2020', 'DJ1944', '3', 'CERT17', 'CERTIFICADO 17 TRIBUTARIOS', 'CERTIFICADO17', '', 'pdf', '3612', 1, 'ACT');

-- PARAMETRO:  Nombre de archivo CERTIFICADOS
DELETE  FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" WHERE "IdNormativa"='DJ1944' AND "CodigoParametro" LIKE 'TRI_NOM_CERTIFICADO%';
  
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
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1944' AND "VersionArchivo"=2020
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
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1944' AND "VersionArchivo"=2020
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
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1944' AND "VersionArchivo"=2020
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
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1944' AND "VersionArchivo"=2020
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
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1944' AND "VersionArchivo"=2020
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_NOM_' || "NombreArchivo" ||  '_05' AND N."IdNormativa"="Esquema");  

