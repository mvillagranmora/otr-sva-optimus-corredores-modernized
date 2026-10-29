SET SCHEMA BSCL;

DROP TABLE "BSCL.Normativa.BD::TRI.DatosDJ1932";

CREATE COLUMN TABLE "BSCL.Normativa.BD::TRI.DatosDJ1932" (
	"ID" 										BIGINT 			NOT NULL,
    "IdDocumento" 								BIGINT 			NOT NULL,
    "VboUsuario" 								CHAR(1) 		NOT NULL,
    "Observaciones" 							NVARCHAR(250) 	NOT NULL,
    "FuenteDatos" 								NVARCHAR(40) 	NOT NULL,
	"Numero"									BIGINT 			NOT NULL,
	"Mes"										BIGINT 			NOT NULL,
	"RutDepositanteInversionistaPrestamista"	NVARCHAR(40)	NOT NULL,
	"TaxID"										NVARCHAR(40)	NOT NULL,
	"Pais"										NVARCHAR(40)	NOT NULL,
	"DepositosInversionesPrestamos"				DECIMAL(26,8)	NOT NULL,
	"RescatesDevoluciones"						DECIMAL(26,8)	NOT NULL,
	"InteresesPercibidas"						DECIMAL(26,8)	NOT NULL,
	"DividendosPercibidas"						DECIMAL(26,8)	NOT NULL,
	"ContratosPorDiferenciaPercibidas"			DECIMAL(26,8)	NOT NULL,
	"OtrasRentasPercibidas"						DECIMAL(26,8)	NOT NULL,
	"InteresesDevengadas"						DECIMAL(26,8)	NOT NULL,
	"OtrasRentasDevengadas"						DECIMAL(26,8)	NOT NULL,
	"FechaOperacion"							DATE			NOT NULL,
	--"TipoDocumento" 							NVARCHAR(10),
	--"NumeroDocumento" 							BIGINT,
	--"FechaDocumento" 							DATE,
	--"IdBaseDocumento" 							BIGINT,
	"IdCliente" 								BIGINT,
	--"IdPortafolio" 								BIGINT,
	--"TipoTransaccion" 							NVARCHAR(6),
	--"IdTipoOperacion" 							NVARCHAR(6),
	"CodPaisRenta"								NVARCHAR(40) 	NOT NULL,
	--"IdTipoProducto" 							NVARCHAR(4),
    PRIMARY KEY ("ID")
);

ALTER TABLE "BSCL.Normativa.BD::TRI.DatosDJ1932"
    ADD CONSTRAINT "FK_Documento_DatosDJ1932" FOREIGN KEY ("IdDocumento")
        REFERENCES "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        ON DELETE RESTRICT ON UPDATE RESTRICT;


--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1932";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" WHERE "Esquema"	= 'DJ1932';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  	WHERE "Esquema"	= 'DJ1932';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" 			WHERE "Esquema" = 'DJ1932';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" 	WHERE "Esquema"	= 'DJ1932';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" 		WHERE "Esquema"	= 'DJ1932';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" 		WHERE "Esquema" = 'DJ1932';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" 		WHERE "Esquema"	= 'DJ1932';
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" 	WHERE "Esquema"	= 'DJ1932';


INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
VALUES
('DJ1932','2020','TRI','A','Declaracion Jurada anual sobre rentas obtenidas por terceros.','2020-01-01','SII','2021-01-01','2020-01-01','2020-12-31',0,'S');


INSERT INTO "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")

SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1932' AS "Esquema"
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


/* CAMPOS PARA DJ 1932 - 26 AGOSTO 2021 - CARGA 2020*/
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'ID', '', '', 1, 'Secuencia', '', 'ID', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'Numero', 'C1', '', 2, 'Numero', '', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '6. DATOS DE CONTROL', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'IdDocumento', '', '', 2, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'S', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '2. Control', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'Mes', 'C3', '', 3, 'MES', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS (Beneficiarios de la Renta) Y MONTO DE LAS OPERACIONES Y RENTAS', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'RutDepositanteInversionistaPrestamista', 'C4', '', 4, 'RUT DEPOSITANTE, INVERSIONISTA O PRESTAMISTA', '', 'RUT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '2. ANTECEDENTES DE LOS DEPOSITANTES, INVERSIONISTAS O PRESTAMISTAS', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'TaxID', 'C5', '', 5, 'TAX-ID DEL DEPOSITANTE, INVERSIONISTA O PRESTAMISTA', '', 'RUT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. ANTECEDENTES DE LOS DEPOSITANTES, INVERSIONISTAS O PRESTAMISTAS', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'Pais', 'C6', '', 6, 'PAIS', '', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. ANTECEDENTES DE LOS DEPOSITANTES, INVERSIONISTAS O PRESTAMISTAS', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'DepositosInversionesPrestamos', 'C7', '', 7, 'DEPOSITOS, INVERSIONES O PRESTAMOS', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. MONTO DE LAS OPERACIONES', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'RescatesDevoluciones', 'C8', '', 8, 'RESCATES O DEVOLUCIONES', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. MONTO DE LAS OPERACIONES', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'InteresesPercibidas', 'C9', '', 9, 'INTERESES', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. PERCIBIDAS', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'DividendosPercibidas', 'C10', '', 10, 'DIVIDENDOS', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. PERCIBIDAS', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'ContratosPorDiferenciaPercibidas', 'C11', '', 11, 'CONTRATOS POR DIFERENCIAS', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. PERCIBIDAS', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'OtrasRentasPercibidas', 'C12', '', 12, 'OTRAS RENTAS', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. PERCIBIDAS', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'InteresesDevengadas', 'C13', '', 13, 'INTERESES', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. DEVENGADAS', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'OtrasRentasDevengadas', 'C14', '', 14, 'OTRAS RENTAS', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. DEVENGADAS', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'CodPaisRenta', 'C24', '', 15, 'Codigo Pais Renta', 'Ayuda CodPaisRenta', 'TEXT', 250, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. DEVENGADAS', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'VboUsuario', '', '', 16, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'Observaciones', '', '', 17, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'FuenteDatos', '', '', 18, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'IdCliente', '', '', 19, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '6. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1932', '2020', 'FechaOperacion', '', '', 20, 'Fecha Operacion', '', 'DATE_FILTER', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS (Beneficiarios de la Renta) Y MONTO DE LAS OPERACIONES Y RENTAS', 15, 0);


-- CONTROL PROCESO
insert into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1932' AND "Version" IN ('2020');

--QUERYS PARA VALIDACION DJ1932
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1932', '2020', 'H2192', 'NORMA', 'S', 'El Mes debe ser alguno de los siguientes numeros: 21: Saldo Inicial 1: Enero 2: Febrero 3: Marzo 4: Abril 5: Mayo 6: Junio 7: Julio 8: Agosto 9: Septiembre 10: Octubre 11: Noviembre 12: Diciembre 29: Saldo Final', 'E', 'C3 = 21.o.1.o.2.o.34.o.5.o.6.o.7.o.8.o.9.o.10.o.11.o.12.o.29');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1932', '2020', 'H2193', 'NORMA', 'S', 'El  campo "Pais" debe indicar valores segun tabla general instruida para codigos pais del AT respectivo', 'E', 'C6  <> blanco o vacio .y. deben indicar valores segun tabla general instruida para codigos pais del AT respectivo');

--SERVICIOS DJ1932
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
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

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 3315, 'BSCL.Normativa.BD.Servicios::TRI.DJ1932.ObtenerTotales', 24, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=3315);

-- Grabar Masivo
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290716, 'BSCL.Normativa.BD.Servicios::TRI.DJ1932.GrabarMasivo', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290716);