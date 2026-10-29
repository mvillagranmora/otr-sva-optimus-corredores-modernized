SET SCHEMA BSCL;

DROP TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1946";

CREATE COLUMN TABLE "BSCL.Normativa.BD::TRI.DatosDJ1946" (
	"ID" 											BIGINT 			NOT NULL,
    "IdDocumento" 									BIGINT 			NOT NULL,
    "VboUsuario" 									CHAR(1) 		NOT NULL,
    "Observaciones" 								NVARCHAR(250) 	NOT NULL,
    "FuenteDatos" 									NVARCHAR(40) 	NOT NULL,
	"IdCliente" 									BIGINT,
	"IdEmpresa" 									BIGINT,
    
    "Numero"										BIGINT 			NOT NULL,
	"Operacion"										BIGINT			NOT NULL,
	"TipoRenta"										BIGINT			NOT NULL,
	"RentaBruta"									DECIMAL(26,8)	NOT NULL,
	"TasaDeRetencion"								DECIMAL(26,8)	NOT NULL,
	"Impuesto"										DECIMAL(26,8)	NOT NULL,
	"RentaLiquida"									DECIMAL(26,8)	NOT NULL,
	"CreditoIDPC"									DECIMAL(26,8)	NOT NULL,
	"OtrosCreditos"									DECIMAL(26,8)	NOT NULL,
	"F50Codigo"										BIGINT			NOT NULL,
	"F50Folio"										BIGINT			NOT NULL,
	"FechaPago"										DATE			NOT NULL,
	"MonedaPago"									NVARCHAR(40)	NOT NULL,
	"TipoCambioPago"								DECIMAL(26,8)	NOT NULL,
	
	"PagadorTipoJuridico"							BIGINT			NOT NULL,
	"PagadorRazonSocial"							NVARCHAR(150)	NOT NULL,
	"PagadorNombre"									NVARCHAR(40)	NOT NULL,
	"PagadorRut"									NVARCHAR(40)	NOT NULL,
	"PagadorDomicilio"								NVARCHAR(180)	NOT NULL,
	"PagadorCiudad"									NVARCHAR(30)	NOT NULL,
	"PagadorRelacion"								BIGINT			NOT NULL,
	"PagadorHolding"								NVARCHAR(60)	NOT NULL,
	
	"PerceptorTipoJuridico"							BIGINT			NOT NULL,
	"PerceptorPaisResidencia"						NVARCHAR(30)	NOT NULL,
	"PerceptorRazonSocial"							NVARCHAR(150)	NOT NULL,
	"PerceptorNombre"								NVARCHAR(40)	NOT NULL,
	"PerceptorIdentificacionTributaria"				BIGINT			NOT NULL,
	"PerceptorRut"									NVARCHAR(40)	NOT NULL,
	"PerceptorFechaNacimiento"						DATE			NOT NULL,
	"PerceptorTipoDomicilio"						BIGINT			NOT NULL,
	"PerceptorDomicilio"							NVARCHAR(180)	NOT NULL,
	"PerceptorCiudad"								NVARCHAR(30)	NOT NULL,
	"PerceptorPais"									NVARCHAR(2)		NOT NULL,
	
	"IntermediarioTipoJuridico"							BIGINT			NOT NULL,
	"IntermediarioPaisResidencia"						NVARCHAR(30)	NOT NULL,
	"IntermediarioRazonSocial"							NVARCHAR(150)	NOT NULL,
	"IntermediarioNombre"								NVARCHAR(40)	NOT NULL,
	"IntermediarioIdentificacionTributaria"				BIGINT			NOT NULL,
									
    PRIMARY KEY ("ID")
);

ALTER TABLE "BSCL.Normativa.BD::TRI.DatosDJ1946"
    ADD CONSTRAINT "FK_Documento_DatosDJ1946" FOREIGN KEY ("IdDocumento")
        REFERENCES "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        ON DELETE RESTRICT ON UPDATE RESTRICT;
        
--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1946";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1946';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1946';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1946';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1946';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1946';
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1946';


--ESQUEMA
INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
VALUES
('DJ1946','2020','TRI','A','Operaciones sobre instrumentos de deuda de oferta publica acogidos al articulo 104','2020-01-01','SII','2021-01-01','2020-01-01','2020-12-31',0,'S');

--DOCUMENTO
insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1946' AS "Esquema"
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


/*CAMPOS*/
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


--VALIDACIONES
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2490','NORMA','S',
'Los campos, "N°" (C1), "Operación" (C2), "Tipo de Renta" (C3), "Renta Bruta" (C4) y "Renta Líquida" (C7) de la SECCIÓN B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS son obligatorios de ingresar y no pueden quedar vacíos, en blanco o con valor cero.',
'E','C1.y. C2.y. C3.y.C4.y. C7 <> blanco, vacío o cero');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2491','NORMA','S',
'El campo "Operación" (C2) de la SECCIÓN B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS debe corresponder a alguno de los siguientes códigos: 6, 7, 8, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21',
'E','C2 = 6.o.7.o.8.o.10.o.11.o.12.o.13.o.14.o.15.o.16.o.17.o.18.o.19.o.20.o.21');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2492','NORMA','S',
'El campo "Tipo de Renta" (C3) de la SECCIÓN B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS debe corresponder a alguno de los siguientes códigos: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10',
'E','C3 = 1.o.2.o.3.o.4.o.5.o.6.o.7.o.8.o.9.o.10');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2493','NORMA','S',
'Si se ingresa un valor al campo "Tasa de Retención" (C5) de la SECCIÓN B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS, entonces debe estar entre 0 y 100.',
'E','Si C5 <> Blaco o vacío => 0> =C5>= 100');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2494','NORMA','S',
'Si se ingresa un valor al campo "Impuesto" (C6) de la SECCIÓN B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS, entonces debe ser mayor o igual a cero.',
'E','Si C6 >blanco o vacío => C6 >= cero');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2495','NORMA','S',
'Si se ingresa un valor al campo "Otros Créditos" (C9) de la SECCIÓN B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS, entonces debe ser mayor o igual a cero.',
'E','Si C9 >blanco o vacío => C9 >= cero');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2496','NORMA','S',
'Si se ingresa un valor al campo "Código" (C10) de la SECCIÓN B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS, entonces debe corresponder a alguno de los siguientes códigos del Formulario',
'E','C10=650,653,657,660,662,602,606,600,604,26,28,664,667,670,673,676,679,681,686,691,696,701,682,687,692,697,702,684,689,694,699,704,706,711,244,272,719,707,712,709,714,717,721,723,50,52,54,56,729,732,735,738,58,741,745,749,753,742,746,750,743,747');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2497','NORMA','S',
'Si se ingresa un valor al campo "Fecha" (C12) de la SECCIÓN B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS, entonces debe ser distinta a cero.',
'E','Si C12 <> blanco o vacío => C12 <> 0 .y. 01011900');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2498','NORMA','S',
'Si se ingresa un valor al campo "Moneda" (C13) de la SECCIÓN B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS, entonces debe corresponder a alguno de los códigos del listado de códigos de monedas vigente de acuerdo a suplemento de declaraciones juradas para el año tributario respectivo.',
'E','SI C13 <> blanco o vacío => C13 = listado de monedas vigentes');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2499','NORMA','S',
'El campo "Tipo Jurídico" (C15) de la SECCIÓN C: ANTECEDENTES DEL PAGADOR DE LAS RENTAS es obligatorio de ingresar y no puede quedar vacío, en blanco o con valor cero.y debe corresponder a alguno de los siguientes códigos: 1, 2, 3, 4, 5, 6, 7',
'E','C15 <> 0 .y. C15 = 1.o.2.o.3.o.4.o.5.o.6.o.7');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2500','NORMA','S',
'Si el campo "Tipo Jurídico" (C15) de la SECCIÓN C: ANTECEDENTES DEL PAGADOR DE LAS RENTAS es igual a 1: Persona Natural, entonces los campos "Razón Social o Apellidos" (C16), "Nombre" (C17), "RUT" (C18), "Dv" (C19) y "Relación" (C22) son obligatorios de ingresar y no pueden quedar vacíos, en blanco o con valor cero.',
'E','Si C15 =1 => C16.y.C17.y.C18.y.C19.y.C22 <> blanco, vació o cero ');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2501','NORMA','S',
'Si el campo "Tipo Jurídico" (C15) de la SECCIÓN C: ANTECEDENTES DEL PAGADOR DE LAS RENTAS es distinto a 1: Persona Natural, entonces los campos "Razón Social o Apellidos" (C16), "RUT" (C18), "Dv" (C19) y "Relación" (C22) son obligatorios de ingresar y no pueden quedar vacíos, en blanco o con valor cero.',
'E','Si C15 <> 1 => C16.y.C18.y.C19.y.C22 <> blanco, vació o cero ');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2502','NORMA','S',
'El campo "Relación" (C22) de la SECCIÓN C: ANTECEDENTES DEL PAGADOR DE LAS RENTAS debe corresponder a alguno de los siguientes códigos: 1, 2, 3, 4, 5, 6, 7, 8, 9',
'E','C22 = 1.o.2.o.3.o.4.o.5.o.6.o.7.o.8.o.9');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2503','NORMA','S',
'El campo "Tipo Jurídico" (C24) de la SECCIÓN D:ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA es obligatorio de ingresar y no puede quedar vacío, en blanco o con valor cero y debe corresponder a alguno de los siguientes códigos: 1, 2, 3, 4, 5, 6, 7',
'E','C24 = 1.o.2.o.3.o.4.o.5.o.6.o.7. campo obligatorio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2504','NORMA','S',
'Si el campo "Tipo Jurídico" (C24) de la SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA  es igual a 1: Persona Natural, entonces los campos "País de Residencia" (C25), "Razón Social o Apellidos" (C26) y "Nombre" (C27) son obligatorios de ingresar y no pueden quedar vacíos, en blanco o con valor cero.',
'E','Si C24 =1 => C25.y.C26.y.C27 <> blanco, vació o cero');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2505','NORMA','S',
'Si el campo "Tipo Jurídico" (C24) de la SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA es distinto a 1: Persona Natural, entonces los campos "País de Residencia" (C25) y "Razón Social o Apellidos" (C26) son obligatorios de ingresar y no pueden quedar vacíos, en blanco o con valor cero.',
'E','Si C24 <> 1 => C25.y.C26. <> blanco, vació o cero ');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2506','NORMA','S',
'El campo "País de Residencia " (C25) de la SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA, debe corresponder a alguno de los códigos del listado de países vigente de acuerdo a suplemento de declaraciones juradas para el año tributario respectivo. Excepto RFE',
'E','Si C25 <> blanco o vacío => C25 =  listado de paises vigente .y. C25 <> RFE');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2507','NORMA','S',
'El campo "Tipo de Domicilio" (C32) de la SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA debe corresponder a alguno de los siguientes códigos: 1, 2, 3',
'E','C32 = 1.o.2.o.3');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2508','NORMA','S',
'Si se ingresa un valor al campo "País" (C35) de la SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA, éste debe corresponder a alguno de los códigos del listado de países vigente de acuerdo a suplemento de declaraciones juradas para el año tributario respectivo.  Excepto RFE',
'E','C35 = listado de paises vigente .y. C35 <> RFE campo obligatorio');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2509','NORMA','S',
'Si se ingresa un valor en el campo "Tipo Jurídico" (C36) de la SECCIÓN E: ANTECEDENTES DEL AGENTE, MANDATARIO O INTERMEDIARIO  entonces debe corresponder a alguno de los siguientes códigos: 1, 2, 3, 4, 5, 6, 7',
'E','Si C36 <> blanco, vacío o cero => C36 = 1.o.2.o.3.o.4.o.5.o.6.o.7.');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2510','NORMA','S',
'Si se ingresa un valor en el campo "Tipo Jurídico" (C36) de la SECCIÓN E: ECCIÓN E: ANTECEDENTES DEL AGENTE, MANDATARIO O INTERMEDIARIO y éste es igual a 1: Persona Natural, entonces los campos "País de Residencia" (C37), "Razón Social o Apellidos" (C38) y "Nombre" (C39) son obligatorios de ingresar y no pueden quedar vacíos, en blanco o con valor cero.',
'E','Si C36 <> blanco o vacío => Si C36 =1 => C37.y.C38.y.C39 <> blanco, vacío o cero');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2511','NORMA','S',
'Si se ingresa un valor en el campo "Tipo Jurídico" (C36) de la SECCIÓN E: ECCIÓN E: ANTECEDENTES DEL AGENTE, MANDATARIO O INTERMEDIARIO y éste es distinto a 1: Persona Natural, entonces los campos "País de Residencia" (C37) y "Razón Social o Apellidos" (C38) son obligatorios de ingresar y no pueden quedar vacíos, en blanco o con valor cero.',
'E','Si C36 <> blanco o vacío => Si C36 <>1 => C37.y.C38  <> blanco, vacío o cero');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2512','NORMA','S',
'Si se ingresa un valor en el campo "País de Residencia " (C37) de la SECCIÓN E:ANTECEDENTES DEL AGENTE, MANDATARIO O INTERMEDIARIO, éste debe corresponder a alguno de los códigos del listado de países vigente de acuerdo a suplemento de declaraciones juradas para el año tributario respectivo.  Excepto RFE.',
'E','Si C37 <> blanco o vacío => C37 = listado de paises vigente .y. C37 <> RFE');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2740','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces los campos "TASA DE RETENCIÓN" (C5), IMPUESTO (C6) y "FOLIO" (C11) son obligatorios de ingresar y no pueden quedar vacíos o en blanco.',
'E','Si C3 = 1.o.2 ; => C5 <> vacío .o. blanco .y. C6 <> vacío .o. blanco .y. C11 <> vacío .o. blanco');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2741','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 3, 4, 5, 6, 7, 8 o 9, entonces los campos "TASA DE RETENCIÓN" (C5), "IMPUESTO" (C6), "CÓDIGO" (C10) y "FOLIO" (C11) deben quedar vacíos o en blanco o cero.',
'E','Si C3 = 3.o.4.o.5.o.6.o.7.o.8.o.9 ; => C5 = vacío .o. blanco .o. cero  .y. C6 = vacío .o. blanco.o. cero .y. C10 <> vacío .o. blanco .o. cero .y. C11 = vacío .o. blanco .o. cero');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2742','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces el campo "CÓDIGO" (C10) es obligatorios de ingresar, no pueden quedar vacíos o en blanco y debe corresponder a los siguientes codigos del formulario 50 (base imponible) segun el valor informado en el campo 
Operación (C2): 7 - Beneficios empresariales = Codigo formulario 50: 50; 52; 729; 732; 58 ; 719; 721; 706; 707; 709; 711; 712; 714; 721; 244; 717',
'E','C3 = 1.o.2 .y. C2 = 7; =>  C10 = 50.o.52.o.729.o.732.o.58.o.719.o.721.o.706.o.707.o.709.o.711.o.712.o.714.o. 721.o.244.o.717
');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2743','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces el campo "CÓDIGO" (C10) es obligatorios de ingresar, no pueden quedar vacíos o en blanco y debe corresponder a los siguientes codigos del formulario 50 (base imponible) segun el valor informado en el campo "Operación" (C2): 8   - Navegación marítima, por aguas interiores y aérea = Codigo formulario 50: 54; 56; 735; 738',
'E','C3 = 1.o.2  .y. C2 = 8 => C10 = 54.o.56.o.735.o.738.');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2744','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces el campo "CÓDIGO" (C10) es obligatorios de ingresar, no pueden quedar vacíos o en blanco y debe corresponder a los siguientes codigos del formulario 50 (base imponible) segun el valor informado en el campo "Operación" (C2): 10 - Dividendos = Codigo formulario 50: 650; 653; 657; 660; 759; 763',
'E','C3 = 1.o.2 .y. C2 = 10 => C10 = 650.o.653.o.657.o.660.o.759.o.763.');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2745','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces el campo "CÓDIGO" (C10) es obligatorios de ingresar, no pueden quedar vacíos o en blanco y debe corresponder a los siguientes codigos del formulario 50 (base imponible) segun el valor informado en el campo "Operación" (C2): 11 - Intereses = Codigo formulario 50: 681; 682; 684; 686; 687; 689; 691; 692; 694; 696; 697; 699; 701; 702; 704; 765; 766; 768',
'E','C3 = 1.o.2  .y. C2 = 11 => C10 = 681.o.682.o.684.o.686.o.687.o.689.o.691.o.692.o.694.o.696.o.697.o.699.o. 701.o.702.o.704.o.765.o.766.o.768');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2746','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces el campo "CÓDIGO" (C10) es obligatorios de ingresar, no pueden quedar vacíos o en blanco y debe corresponder a los siguientes codigos del formulario 50 (base imponible) segun el valor informado en el campo "Operación" (C2): 12 - Regalías = Codigo formulario 50: 662; 664; 602; 600; 667; 606; 604; 670; 26; 673; 28; 676',
'E','C3 = 1.o.2 .y. C2 = 12 => C10 = 662.o.664.o. 602.o. 600.o. 667.o. 606.o. 604 .o. 670.o. 26.o.673.o 28.o.676');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2747','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces el campo "CÓDIGO" (C10) es obligatorios de ingresar, no pueden quedar vacíos o en blanco y debe corresponder a los siguientes codigos del formulario 50 (base imponible) segun el valor informado en el campo "Operación" (C2): 13 - Ganancias de capital = Codigo formulario 50: 741; 742; 743; 745; 746; 747; 749; 750; 751; 753; 757; 770; 771; 772; 774; 775; 776; 778',
'E','C3 = 1.o.2 .y. C2 = 13 => C10 = 741.o.742.o.743.o.745.o.746.o.747.o.749.o. 750.o.751.o.753.o.757.o.770.o.771.o.772.o.774 .o.775.o.776;.o. 778');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2748','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces el campo "CÓDIGO" (C10) es obligatorios de ingresar, no pueden quedar vacíos o en blanco y debe corresponder a los siguientes codigos del formulario 50 (base imponible) segun el valor informado en el campo Operación (C2): 16 - Remuneraciones en calidad de consejero = Codigo formulario 50: 272',
'E','C3 = 1.o.2 .y. C2 = 16 => C10 = 272');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2749','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces el campo "CÓDIGO" (C10) es obligatorios de ingresar, no pueden quedar vacíos o en blanco y debe corresponder a los siguientes codigos del formulario 50 (base imponible) segun el valor informado en el campo Operación (C2): Operación (C2): 21 - Otras rentas = Codigo formulario 50: 780; 723',
'E','C3 = 1.o.2 .y. C2 = 21 => C10 = 780 .o. 723');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2750','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces el campo "CÓDIGO" (C10) es obligatorios de ingresar, no pueden quedar vacíos o en blanco y debe corresponder a los siguientes codigos del formulario 50 (base imponible) segun el valor informado en el campo Operación (C2): 6   - Rentas inmobiliarias o 18 - Pensiones o 19 - Funciones públicas o 20 - Estudiantes o 21 - Otras rentas = Codigo formulario 50: 723',
'E','C3 = 1.o.2 .y. C2 = 6.o.18.o.19.o.20.o.21; entonces C10 = 723');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2751','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces el campo "CÓDIGO" (C10) es obligatorios de ingresar, no pueden quedar vacíos o en blanco y debe corresponder a los siguientes codigos del formulario 50 (base imponible) segun el valor informado en el campo Operación (C2): 14 - Rentas del trabajo independiente Codigo formulario 50: 706; 707; 709; 711; 712; 714; 721; 244; 717',
'E','C3 = 1.o.2 .y. Si C2 = 14 entonces C10 = 706.o.707.o.709.o.711.o.712.o.714.o.721.o.244.o.717');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2752','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces el campo "CÓDIGO" (C10) es obligatorios de ingresar, no pueden quedar vacíos o en blanco y debe corresponder a los siguientes codigos del formulario 50 (base imponible) segun el valor informado en el campo Operación (C2): 15 - Rentas del trabajo dependiente = Codigo formulario 50: 706; 707; 709; 711; 712; 714; 244; 717',
'E','C3 = 1.o.2 .y. Si C2 = 15 entonces C10 = 706 .o. 707.o. 709 .o. 711 .o. 712 .o. 714 .o.244 .o.717');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2753','NORMA','S',
'Si el campo "TIPO DE RENTA" (C3) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS es igual a 1 o 2entonces el campo "CÓDIGO" (C10) es obligatorios de ingresar, no pueden quedar vacíos o en blanco y debe corresponder a los siguientes codigos del formulario 50 (base imponible) segun el valor informado en el campo Operación (C2):  17 - Artistas y deportistas = Codigo formulario 50: 719 o 721',
'E','C3 = 1.o.2 .y. Si C2 = 17 entonces C10 = 719.o.721');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2754','NORMA','S',
'Si  el campo "Fecha" (C12) de la SECCION B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS, debe comprender la fecha de la remesa de la renta entre el 01 de enero y 31 de diciembre del año comercial respectivo.',
'E','Si C12 <> blanco o vacío => 01.01. > C21 > 31.12.');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2755','NORMA','S',
'Si se ingresa un valor en el campo "RAZÓN SOCIAL O APELLIDOS" (C16), "NOMBRE" (C17) o "GRUPO O HOLDING EMPRESARIAL" (C23) de la SECCIÓN C: ANTECEDENTES DEL PAGADOR DE LAS RENTAS, debe contener al menos 3 caracteres alfanuméricos.',
'E','Si C16.o. C17.o. C23 <>  blanco, vacío .o. cero ; entonces C16 .o. C17.o. C23 >= 3 caracteres');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2756','NORMA','S',
'Si se ingresa un valor en el campo "RAZÓN SOCIAL O APELLIDOS" (C26) y "NOMBRE" (C27) de la SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA, debe contener al menos 3 caracteres alfanuméricos.',
'E','Si C26.o. C27 <>  blanco, vacío .o. cero ; entonces C26 .o. C27 >= 3 caracteres');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2757','NORMA','S',
'El campo "DOMICILIO" (C20) y CIUDAD (C21) de la SECCIÓN C: ANTECEDENTES DEL PAGADOR DE LAS RENTAS, son obligatorios de ingresar, no pueden quedar vacíos o en blanco o con valor cero y deben contener al menos 3 caracteres alfanuméricos.',
'E','Si C20 .y. C21 <> blanco, vacío .o. cero; entonces C20 .y. C21 >= 3 caracteres');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2758','NORMA','S',
'Si se ingresa un valor en el campo "DOMICILIO" (C33) y "CIUDAD" (C34) de la SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA, son obligatorios de ingresar, no pueden quedar vacíos o en blanco o con valor cero y deben contener al menos 3 caracteres alfanuméricos.',
'E','Si C33 .y. C34 <> blanco, vacío .o. cero; entonces C33 .y. C34 >= 3 caracteres');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2759','NORMA','S',
'Si se ingresa un valor en el campo "RAZÓN SOCIAL O APELLIDOS" (C38) y "NOMBRE" (C39) de la SECCIÓN E: ANTECEDENTES DEL AGENTE, MANDATARIO O INTERMEDIARIO, debe contener al menos 3 caracteres alfanuméricos.',
'E','Si C38 .y. C39 <> blanco, vacío .o. cero; entonces C33 .y. C34 >= 3 caracteres');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2760','NORMA','S',
'El campo "Crédito por IDPC" (C8) de la SECCIÓN B: ANTECEDENTES DE LAS OPERACIONES EN CHILE Y RENTAS REMESADAS, debe ser mayor o igual a cero.',
'E','C8 <> blanco o vacío .y. C8 >= cero');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2761','NORMA','S',
'Si se ingresa un valor al campo "FECHA DE NACIMIENTO" (C31) de la SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA, entonces debe ser distinta a cero y no mayor al año comercial a informar.',
'E','Si C31 <> blanco, vacío, cero; entonces C31 < AT +1 .y. C31 <> 01011900 ');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2762','NORMA','S',
'Si el campo "TIPO JURÍDICO" (C24) de la SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA es distinto de 1: "Persona Natural", entonces el campo "NOMBRE" (C27) debe quedar vacío o en blanco.',
'E','Si C24 <> 1 => C27 = blanco vacío o cero ');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2763','NORMA','S',
'Si se ingresa un valor al campo "TIPO JURÍDICO" (C36) de la SECCIÓN E: ANTECEDENTES DEL AGENTE, MANDATARIO O INTERMEDIARIO distinto de 1: "Persona Natural", entonces el campo "NOMBRE" (C39) debe quedar vacío o en blanco.',
'E','Si C36 <> 1 => C39 = blanco vacío o cero ');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2764','NORMA','S',
'Si el campo "TIPO JURÍDICO" (C15) de la SECCIÓN C: ANTECEDENTES DEL PAGADOR DE LAS RENTAS es distinto de 1: "Persona Natural", entonces el campo "NOMBRE" (C17) debe quedar vacío o en blanco.',
'E','Si C15 <> 1 => C17 = blanco vacío o cero ');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2765','NORMA','S',
'Si el campo ""PAÍS DE RESIDENCIA"" (C37) de la SECCIÓN E: ANTECEDENTES DEL AGENTE, MANDATARIO O INTERMEDIARIO es igual al campo ""PAÍS DE RESIDENCIA"" (C25) de la SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA, entonces el campo ""RAZÓN SOCIAL O APELLIDOS"" (C38) de la SECCIÓN E: ANTECEDENTES DEL AGENTE, MANDATARIO O INTERMEDIARIO debe ser distinto del campo ""RAZÓN SOCIAL O APELLIDOS"" (C26)  SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA. ',
'E','Si C37 = C25; entonces C38 <> C26 ');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2766','NORMA','S',
'Si el campo ""PAÍS DE RESIDENCIA"" (C37) de la SECCIÓN E: ANTECEDENTES DEL AGENTE, MANDATARIO O INTERMEDIARIO es igual al campo ""PAÍS DE RESIDENCIA"" (C25) de la SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA, entonces el campo ""NOMBRE"" (C39) de la SECCIÓN E: ANTECEDENTES DEL AGENTE, MANDATARIO O INTERMEDIARIO debe ser distinto del campo ""NOMBRE"" (C27)  SECCIÓN D: ANTECEDENTES DEL PERCEPTOR BENEFICIARIO EFECTIVO DE LA RENTA.',
'E','Si C37 = C25; entonces C39 <> C27');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion" 
("Esquema","Version","Codigo","Tipo","Activa","Descripcion","Severidad","Formula")
VALUES 
('DJ1946','2020','H2767','NORMA','S',
'Si se ingresa un valor al campo ""PAÍS DE RESIDENCIA"" (C37) o ""RAZÓN SOCIAL O APELLIDOS"" (C38) o ""NOMBRE"" (C39) o NÚMERO DE IDENTIFICACIÓN TRIBUTARIA (C40) de la SECCIÓN E: ANTECEDENTES DEL AGENTE, MANDATARIO O INTERMEDIARIO, entonces el campo ""TIPO JURÍDICO"" (C36) de la misma sección, no debe quedar vacío o en blanco o cero. ',
'E','Si C37 <> blanco, vacío .o. cero .o. C38 <> blanco, vacío .o. cero .o. C39 <> blanco, vacío .o. cero  .o. C40 <> blanco, vacío .o. cero; entonces C36 <> blanco, vacío .o. cero');



--SERVICIOS DJ1932
/*INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3312, 'BSCL.Normativa.BD.Vistas::TRI.DJ1932.Invalidos', 0, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3312);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3311, 'BSCL.Normativa.BD.Vistas::TRI.DJ1932.Datos', 20, 'Q', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3311);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3313, 'BSCL.Normativa.BD.Servicios::TRI.DJ1932.EliminarDetalles', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3313);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3314, 'BSCL.Normativa.BD.Servicios::TRI.DJ1932.GrabarDetalle', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3314);
*/

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 6122, 'BSCL.Normativa.BD.Servicios::TRI.DJ1946.ObtenerTotales', 24, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=6122);

-- SCRIPT PARA ASIGNAR LA VISTA CON UN IDSERVICIO
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio", "RutaServicio", "TipoServicio", "RegistraUso") 
SELECT 4290710, 'BSCL.Normativa.BD.Vistas::TRI.DJ1946.Datos','Q','S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290710);


--CONTROL DE PROCESOS
INSERT INTO BSCL."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema", "Version", "EstadoCertificado", "Carga", "Validacion", "PreDeclaracion", "Certificacion")
VALUES('DJ1946', '2020', 'NGE', 'S', 'N', 'N', 'N');


