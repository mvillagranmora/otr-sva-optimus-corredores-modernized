SET SCHEMA "BSCL";

DROP TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1862";

CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1862" (
    "ID"                                BIGINT          NOT NULL ,
    "IdDocumento"                       BIGINT          NOT NULL ,
    "VboUsuario"                        CHAR(1)         NOT NULL ,
    "Observaciones"                     NVARCHAR(250)   NOT NULL ,
    "FuenteDatos"                       NVARCHAR(40)    NOT NULL ,
    "Numero"                            BIGINT          NOT NULL ,
    "IdComprobante"                     BIGINT                   ,
    "RutCliente"                        NVARCHAR(40)    NOT NULL ,
    "TaxID"                             NVARCHAR(40)    NOT NULL ,
    "PaisOtorganteIdentificador"        NVARCHAR(40)    NOT NULL ,
    "Nombre"                            NVARCHAR(100)   NOT NULL ,
    "CodigoMoneda"                      NVARCHAR(10)    NOT NULL ,
    "MontoEnPesos"                      DECIMAL(26,6)   NOT NULL ,
    "NumeroTransaccion"                 BIGINT          NOT NULL ,
    "CodigoModalidadOperacion"          NVARCHAR(20)    NOT NULL ,
    "CodigoConcepto"                    NVARCHAR(20)    NOT NULL ,
    "FechaOperacion"                    DATE            NOT NULL ,
    "NombreContraparte"                 NVARCHAR(100)   NOT NULL ,
    "TaxIDContraparte"                  NVARCHAR(40)    NOT NULL ,
    "CodigoPaisContraparte"             NVARCHAR(40)    NOT NULL ,
    "BancoContraparte"                  NVARCHAR(100)   NOT NULL ,
    "TipoDocumento"                     NVARCHAR(10),
    "NumeroDocumento"                   BIGINT ,
    "FechaDocumento"                    DATE ,
    "IdBaseDocumento"                   BIGINT ,
    "IdCliente"                         BIGINT ,
    "IdPortafolio"                      BIGINT ,
    "TipoTransaccion"                   NVARCHAR(6),
    "IdTipoOperacion"                   NVARCHAR(6),
    "IdTipoProducto"                    NVARCHAR(4),
    PRIMARY KEY ("ID")) UNLOAD PRIORITY 5 AUTO MERGE ;

ALTER TABLE "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1862" 
    ADD CONSTRAINT "FK_Documento_DatosDJ1862" FOREIGN KEY ("IdDocumento") 
        REFERENCES "BSCL"."BSCL.Normativa.BD::DOC.Documento" ("IdDocumento") 
        ON UPDATE RESTRICT ON DELETE RESTRICT ENFORCED VALIDATED INITIALLY IMMEDIATE;



--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1862";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"  WHERE "Esquema"='DJ1862';-- AND "Version"='2016/12';GO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  WHERE "Esquema"='DJ1862';-- AND "Version"='2016/12';GO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" where "Esquema" = 'DJ1862';-- and "Version" = '2016/12';GO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema"='DJ1862';-- AND "Version"='2016/12';GO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" WHERE "Esquema"='DJ1862';-- AND "Version"='2016/12';GO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" where "Esquema" = 'DJ1862';-- and "Version"='2016/12';GO
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1862';-- AND "Version"='2016/12';GO

DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" WHERE  "Esquema" = 'DJ1862';


	
insert into "BSCL.Normativa.BD::DOC.Esquema"(
    "Esquema",
    "Version",
    "Modulo",
    "Periodicidad",
    "Objetivo",
    "PeriodoInicio",
	"IdEntidadReguladora",
    "FechaPresentacionLimite",
    "FechaDatosDesde",
    "FechaDatosHasta",
	"CodigoVersionSII",
    "HabilitadoMantenedor"
)
values(
    'DJ1862',
    '2020',
    'TRI',
    'A',
    '',
    '2020-01-01',
    'SII',
    '2022-01-01',
    '2020-01-01',
    '2020-12-31',
    0,
    'S'
);

insert into "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1862' AS "Esquema"
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

-- Campos 2020
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'ID', '', '', 1, 'Secuencia', 'Ayuda  de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'Numero', '', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '4. DATOS DE CONTROL', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'RutCliente', 'C1', '', 3, 'Rut Cliente', 'Ayuda de Rut Cliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'TaxID', 'C2', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'RUT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'PaisOtorganteIdentificador', 'C3', 'CodigoPais1862', 5, 'Pais Otorgante del Identificador', 'Ayuda de Pais Otorgante del Identificador', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'Nombre', 'C4', '', 6, 'Nombre o Razon Social', 'Ayuda de Nombre o Razon Social', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'CodigoMoneda', 'C5', 'CodigoMoneda1862', 7, 'Codigo Moneda', 'Ayuda de Codigo Moneda', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'MontoEnPesos', 'C6', '', 8, 'Monto En Pesos ($)', 'Ayuda de Monto En Pesos', 'DECIMAL', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 7, 2);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'NumeroTransaccion', 'C7', '', 9, 'Numero de Transaccion', 'Ayuda de Numero de Transaccion', 'QTY', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'CodigoModalidadOperacion', 'C8', 'CodigoOperacionDJ1862', 10, 'Codigo Modalidad Operacion', 'Ayuda de CodigoModalidadOperacion', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'CodigoConcepto', 'C9', 'CodigoConceptoDJ1862', 11, 'Codigo Concepto', 'Ayuda de CodigoConcepto', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'FechaOperacion', 'C10', '', 12, 'Fecha de la Operacion', 'Ayuda de Fecha de la Operacion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'NombreContraparte', 'C11', '', 13, 'Nombre o Razon Social Contraparte', 'Ayuda de Nombre o Razon Social', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'TaxIDContraparte', 'C12', '', 14, 'Tax ID Contraparte', 'Ayuda de Tax ID', 'TEXT', 15, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'CodigoPaisContraparte', 'C13', '', 15, 'Codigo Pais', 'Ayuda de Codigo Pais', 'NEMO', 15, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'BancoContraparte', 'C14', '', 16, 'Banco o entidad intermediarios o corresponsales en el extranjero', 'Ayuda de Banco o entidad intermediarios o corresponsales en el extranjero', 'TEXT', 15, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 15, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'IdDocumento', '', '', 17, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'VboUsuario', '', '', 18, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'Observaciones', '', '', 19, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'FuenteDatos', '', '', 20, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'IdCliente', '', '', 21, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'IdPortafolio', '', '', 22, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2020', 'IdComprobante', '', '', 23, 'Fecha Comprobante', '', 'ID', 0, '0', 'S', 'S', 'N', 'N', 'S', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);
-- Campos 2021
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'ID', '', '', 1, 'Secuencia', 'Ayuda  de Secuencia', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'Numero', '', '', 2, 'Numero', 'Ayuda de Numero', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '4. DATOS DE CONTROL', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'RutCliente', 'C1', '', 3, 'Rut Cliente', 'Ayuda de Rut Cliente', 'RUT_CLIENTE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'TaxID', 'C2', '', 4, 'Tax ID', 'Ayuda de Tax ID', 'RUT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'PaisOtorganteIdentificador', 'C3', 'CodigoPais1862', 5, 'Pais Otorgante del Identificador', 'Ayuda de Pais Otorgante del Identificador', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'Nombre', 'C4', '', 6, 'Nombre o Razon Social', 'Ayuda de Nombre o Razon Social', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. Datos de la persona o entidad a nombre de quien se realiza la operacion', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'CodigoMoneda', 'C5', 'CodigoMoneda1862', 7, 'Codigo Moneda', 'Ayuda de Codigo Moneda', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'MontoEnPesos', 'C6', '', 8, 'Monto En Pesos ($)', 'Ayuda de Monto En Pesos', 'DECIMAL', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 7, 2);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'NumeroTransaccion', 'C7', '', 9, 'Numero de Transaccion', 'Ayuda de Numero de Transaccion', 'QTY', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'CodigoModalidadOperacion', 'C8', 'CodigoOperacionDJ1862', 10, 'Codigo Modalidad Operacion', 'Ayuda de CodigoModalidadOperacion', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'CodigoConcepto', 'C9', 'CodigoConceptoDJ1862', 11, 'Codigo Concepto', 'Ayuda de CodigoConcepto', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'FechaOperacion', 'C10', '', 12, 'Fecha de la Operacion', 'Ayuda de Fecha de la Operacion', 'DATE_FILTER', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. Datos generales', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'NombreContraparte', 'C11', '', 13, 'Nombre o Razon Social Contraparte', 'Ayuda de Nombre o Razon Social', 'TEXT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'TaxIDContraparte', 'C12', '', 14, 'Tax ID Contraparte', 'Ayuda de Tax ID', 'TEXT', 15, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'CodigoPaisContraparte', 'C13', '', 15, 'Codigo Pais', 'Ayuda de Codigo Pais', 'NEMO', 15, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'BancoContraparte', 'C14', '', 16, 'Banco o entidad intermediarios o corresponsales en el extranjero', 'Ayuda de Banco o entidad intermediarios o corresponsales en el extranjero', 'TEXT', 15, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. Datos de la contraparte en el exterior', 15, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'IdDocumento', '', '', 17, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'VboUsuario', '', '', 18, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'Observaciones', '', '', 19, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'FuenteDatos', '', '', 20, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'IdCliente', '', '', 21, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'IdPortafolio', '', '', 22, 'Secuencia Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1862', '2021', 'IdComprobante', '', '', 23, 'Fecha Comprobante', '', 'ID', 0, '0', 'S', 'S', 'N', 'N', 'S', 'N', 'N', 'N', '4. DATOS DE CONTROL', 0, 0);


/* VALIDACIONES  */
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
    'DJ1862' as "Esquema",
    '2020' as "Version",
    'H315' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Debe existir un valor mayor que cero en la columna Monto en pesos ' as "Descripcion",
    'E' as "Severidad",
    'C6 > 0'  as "Formula"
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
    'DJ1862' as "Esquema",
    '2020' as "Version",
    'H317' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Se debe verificar que el valor de la columna Codigo del Concepto sea consistente con las instrucciones de la declaracion jurada respectiva u oficios de S.I.I. ' as "Descripcion",
    'E' as "Severidad",
    'Segun codificacion presentada en Anexo asociado a F1862'  as "Formula"
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
    'DJ1862' as "Esquema",
    '2020' as "Version",
    'H318' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Se debe verificar que el valor de la columna Modalidad de Operacion [C8] sea consistente con las instrucciones de la declaracion jurada respectiva u oficios de S.I.I. SW: SWIFT TX: TELEX CH: Transferencia directa con cheque bancario. OT: Otra modalidad distinta a las anteriores.' as "Descripcion",
    'E' as "Severidad",
    'Puede adquirir los siguientes valores: SW: SWIFT TX: TELEX CH: Transferencia directa con cheque bancario. OT: Otra modalidad distinta a las anteriores.'  as "Formula"
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
    'DJ1862' as "Esquema",
    '2020' as "Version",
    'H319' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'La fecha Operacion presente en el Registro Tipo 2, debe tener formato de fecha (dd/mm/aaaa) y corresponder al anio tributario anterior. ' as "Descripcion",
    'E' as "Severidad",
    '[C10] => (dd/mm/aaaa) .y. 01.01.At-1< [C10] < 31.12.At-1'  as "Formula"
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
    'DJ1862' as "Esquema",
    '2020' as "Version",
    'H320' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Se debe verificar que el valor de la columna Codigo de Moneda sea consistente con las instrucciones de la declaracion jurada respectiva u oficios de S.I.I. ' as "Descripcion",
    'E' as "Severidad",
    'Segun codificacion presentada en Anexo asociado a F1862'  as "Formula"
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
    'DJ1862' as "Esquema",
    '2020' as "Version",
    'H321' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Se debe verificar que el valor del campo Codigo Pais de los datos de la contraparte en el exterior, sea consistente con las instrucciones de la declaracion jurada respectiva u oficios de S.I.I.' as "Descripcion",
    'E' as "Severidad",
    '[C13]= Codigo Pais= {Tabla Codigo Pais Anexo asociado a F1862}'  as "Formula"
from dummy;

/* se agrega validación H754 */
/* insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1862' as "Esquema",
    '2020' as "Version",
    'H754' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'El total de casos informados debe ser igual al número de registros válidos del cuadro Datos de los Informados' as "Descripcion",
    'E' as "Severidad",
    '[C15] = CONTAR [C0]'  as "Formula"
from dummy; */
/* se agrega validación H754 */

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
    'DJ1862' as "Esquema",
    '2020' as "Version",
    'H831' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'En la columna Codigo de Pais [C3] debe estar en Tabla de Paises del Anexo asociado al F1862.' as "Descripcion",
    'E' as "Severidad",
    'Si [C3] <> blanco; entonces [C3]= Codigo Pais= {Tabla Codigo Pais Anexo correspondiente}'  as "Formula"
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
    'DJ1862' as "Esquema",
    '2020' as "Version",
    'H832' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Se debe verificar que al existir Tax ID , exista  valor del campo Codigo Pais y que este ultimo sea consistente con las instrucciones de la declaracion jurada respectiva u oficios de S.I.I.' as "Descripcion",
    'E' as "Severidad",
    'Si [C2] <> blanco; entonces [C3] <> blanco .o. 0 ; [C3] =Codigo Pais= {Tabla Codigo Pais Anexo correspondiente}'  as "Formula"
from dummy;

/* se agrega validación H854 */
/* insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1862' as "Esquema",
    '2020' as "Version",
    'H854' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Los datos del resumen en C16 deben  corresponder con los datos del detalle informado en C6, siempre y cuando el código de concepto (C9) informado corresponde a un INGRESO.' as "Descripcion",
    'E' as "Severidad",
    'C16 = ΣCi6 Para todo i, siempre y cuando : 10000 < = Ci9 < = 11495'  as "Formula"
from dummy; */
/* se agrega validación H854 */

/* se agrega validación H855 */
/* insert into "BSCL.Normativa.BD::DOC.Validacion" (
	"Esquema",
    "Version",
    "Codigo",
    "Tipo",
    "Activa",
    "Descripcion",
    "Severidad",
    "Formula")
select
    'DJ1862' as "Esquema",
    '2020' as "Version",
    'H855' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Los datos del resumen en C17 deben  corresponder con los datos del detalle informado en C6, siempre y cuando el código de concepto (C9) informado corresponde a un EGRESO.' as "Descripcion",
    'E' as "Severidad",
    'C17 = ΣCi6 Para todo i, siempre y cuando : 20000 < = Ci9 < = 21499'  as "Formula"
from dummy; */
/* se agrega validación H855 */

/* VALIDACIONES */


--CONTROL PROCESO

insert into "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1862' AND "Version" IN ('2020');

-- DICCIONARIO
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '999', 1, 'PESO CHILENO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '13', 2, 'DOLAR DE ESTADOS UNIDOS', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '143', 3, 'BAHT TAILANDES', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '134', 4, 'BOLIVAR FUERTE', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '4', 5, 'BOLIVIANO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '153', 6, 'COLON COSTARRICENSE', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '51', 7, 'CORONA DANESA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '96', 8, 'CORONA NORUEGA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '113', 9, 'CORONA SUECA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '135', 10, 'DINAR', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '139', 11, 'DIRHAM', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '36', 12, 'DOLAR AUSTRALIANO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '6', 13, 'DOLAR CANADIENSE', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '97', 14, 'DOLAR DE NUEVA ZELANDA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '136', 15, 'DOLAR DE SINGAPUR', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '154', 16, 'D�LAR FIYIANO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '127', 17, 'DOLAR HONG KONG', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '142', 18, 'EURO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '902', 19, 'FRANCO PACIFIC ISLAND', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '156', 20, 'FRANCO POLINESICO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '82', 21, 'FRANCO SUIZO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '23', 22, 'GUARANI', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '102', 23, 'LIBRA ESTERLINA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '149', 24, 'NUEVA LIRA TURCA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '138', 25, 'NUEVO DOLAR TAIWANES', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '24', 26, 'NUEVO SOL', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '981', 27, 'ONZA TROY ORO 981', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '900', 28, 'OTRAS NO ESPECIFICADAS', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '1', 29, 'PESO ARGENTINO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '129', 30, 'PESO COLOMBIANO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '150', 31, 'PESO FILIPINO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '132', 32, 'PESO MEXICANO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '980', 33, 'PESO ORO SELLADO CHILENO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '26', 34, 'PESO URUGUAYO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '128', 35, 'RAND', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '5', 36, 'REAL', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '152', 37, 'RINGGIT MALASIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '155', 38, 'RUBLO', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '137', 39, 'RUPIA INDIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '151', 40, 'RUPIA INDONESIA', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '144', 41, 'WON DE LA REPUBLICA DE COREA DEL SUR', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '72', 42, 'YEN', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '48', 43, 'YUAN', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '998', 44, 'UNIDAD DE FOMENTO (UF) ', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('CodigoMoneda1862', '2', 45, 'UNIDAD CUENTA (BID) ', 'S', '', '');
