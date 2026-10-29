SET SCHEMA BSCL;

DROP TABLE "BSCL.Normativa.BD::TRI.DatosDJ1919";

CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1919" 
("ID"                                                   BIGINT          NOT NULL ,
"IdDocumento"                                           BIGINT          NOT NULL ,
"VboUsuario"                                            CHAR(1)         NOT NULL ,
"Observaciones"                                         NVARCHAR(250)   NOT NULL ,
"FuenteDatos"                                           NVARCHAR(40)    NOT NULL ,
"Numero"                                                BIGINT          NOT NULL ,
"IdCliente"                                             BIGINT  ,
"IdOperacion"                                           BIGINT          ,
"RutEmisor"                                             NVARCHAR(40)    NOT NULL ,
"TaxID"                                                 NVARCHAR(40)    NOT NULL ,
"RutCliente"                                            NVARCHAR(40)    ,
"CodigoPaisConstitucion"                                NVARCHAR(10)    NOT NULL ,
"CodigoPaisResidencia"                                  NVARCHAR(10)    NOT NULL ,
"PorcentajePropiedadDirectaIndirecta"                   DECIMAL(26,8)   NOT NULL ,
"ParticipacionDirectaIndirectaControl"                  BIGINT          NOT NULL ,
"NemotecnicoSVS"                                        NVARCHAR(100)   NOT NULL ,
"FechaOperacion"                                        DATE            NOT NULL ,
"NumeroCuentaDCV"                                       NVARCHAR(50),
"Moneda"                                                NVARCHAR(3)     NOT NULL ,
"ValorEmision"                                          DECIMAL(26,8)   NOT NULL ,
"ValorColocacion"                                       DECIMAL(26,8)   NOT NULL ,
"ValorAdquisicion"                                      DECIMAL(26,8)   NOT NULL ,
"ValorAdquisicionEnPesos"                               DECIMAL(26,8)   NOT NULL ,
"ValorEnajenacion"                                      DECIMAL(26,8)   NOT NULL ,
"ValorEnajenacionEnPesos"                               DECIMAL(26,8)   NOT NULL ,
"MayorValorEnajenacion"                                 DECIMAL(26,8)   NOT NULL ,
"MayorValorEnajenacionEnPesos"                          DECIMAL(26,8)   NOT NULL ,
"IdPortafolio"                                          BIGINT  ,
"TipoTransaccion"                                       NVARCHAR(6),
"IdTipoProducto"                                        NVARCHAR(4),
"IdBaseDocumento"                                       BIGINT,
"FechaDocumento"                                        DATE ,
"NumeroDocumento"                                       BIGINT ,
"TipoDocumento"                                         NVARCHAR(10),
"IdTipoOperacion"                                       NVARCHAR(6),
PRIMARY KEY ("ID"));


--Limpiar todas las versiones        
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1919";
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"     WHERE "Esquema"	= 'DJ1919';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"  		WHERE "Esquema"	= 'DJ1919';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" 				WHERE "Esquema" = 'DJ1919';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" 		WHERE "Esquema"	= 'DJ1919';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Validacion" 			WHERE "Esquema"	= 'DJ1919';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Documento" 			WHERE "Esquema" = 'DJ1919';
DELETE FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" 			WHERE "Esquema"	= 'DJ1919';
DELETE FROM "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso" 		WHERE "Esquema"	= 'DJ1919';

-- Esquema
INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
VALUES
('DJ1919','2020','TRI','A','','2020-01-01','SII','2021-01-01','2020-01-01','2020-12-31',0,'S');

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Modulo","Periodicidad","Objetivo","PeriodoInicio",
		     "IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta",
		     "CodigoVersionSII","HabilitadoMantenedor")
VALUES
('DJ1919','2021','TRI','A','','2021-01-01','SII','2022-01-01','2021-01-01','2021-12-31',0,'S');


-- Documento 2020
INSERT INTO "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1919' AS "Esquema"
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

-- Documento 2021
INSERT INTO "BSCL.Normativa.BD::DOC.Documento"
("IdDocumento","IdTipoDocumento","Esquema","Version","IdEmpresa","FechaLimite","IdEstadoDocumento",
"FechaCreacion","FechaPresentacion","DocumentoIdentidadDeclarante",
"DocumentoIdentidadRepresentante","DireccionElectronicaDeclaracion",
"DireccionFisicaDeclaracion","ComunaDeclaracion","TelefonoDeclaracion",
"FolioDeclaracion","EnProceso","FechaInicioUltimoProceso","AnoMes")
SELECT "BSCL"."BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL AS "IdDocumento",
'DECLACORR' AS "IdTipoDocumento"
,'DJ1919' AS "Esquema"
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


--- TABLA CAMPOS 2020
INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ID','','',1,'Secuencia','Ayuda  de Secuencia','ID',0,'0','N','N','N','S','N','N','S','N','',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','Numero','C2','',2,'Numero','Ayuda de Numero','ID',0,'0','N','N','N','S','N','N','N','N','1. Datos de las Operaciones',1,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','RutEmisor','C3','',3,'Rut Emisor','Ayuda de Rut Emisor','RUT',0,'','N','S','N','S','N','N','N','S','1. Datos de las Operaciones',2,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','TaxID','C4','',4,'Tax ID Inversionista','Ayuda de Tax ID','RUT',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',3,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','RutCliente','C5','',5,'Rut Cliente','Ayuda de Rut Cliente','RUT_CLIENTE',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',4,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','CodigoPaisConstitucion','C6','CodigoPais',6,'Codigo Pais Constitucion','Ayuda de Codigo Pais Constitucion','NEMO',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',5,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','CodigoPaisResidencia','C7','CodigoPais',7,'Codigo Pais Residencia','Ayuda de Codigo Pais Residencia','NEMO',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',6,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','PorcentajePropiedadDirectaIndirecta','C8','',8,'% de Propiedad o Participación directa o indirecta en el Capital o Utilidades del emisor','Ayuda de % de Propiedad o Participación directa o indirecta en el Capital o Utilidades del emisor','DECIMAL',0,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',7,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ParticipacionDirectaIndirectaControl','C9','',9,'Participación directa o indirecta en el control del Emisor','Ayuda de Participación directa o indirecta en el control del Emisor','QTY',0,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',8,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES	 ('DJ1919','2020','NemotecnicoSVS','C10','',10,'Nemotécnico SVS','Ayuda de Nemotécnico SVS','NEMO',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',9,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','FechaOperacion','C11','',11,'Fecha Operación','Ayuda de Fecha Operación','DATE_FILTER',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',10,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','NumeroCuentaDCV','C12','',12,'N° Cuenta DCV','Ayuda de N° Cuenta DCV','TEXT',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',11,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','Moneda','C13','CodigoMonedaDJ1919',13,'Moneda o unidad de reajuste','Ayuda de moneda o unidad de reajuste','TEXT',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',12,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ValorEmision','C14','',14,'Valor de emisión en moneda o unidad de reajuste','Ayuda de valor de emisión en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',13,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ValorColocacion','C15','',15,'Valor de colocación en moneda o unidad de reajuste','Ayuda de valor de colocación en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',14,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ValorAdquisicion','C16','',16,'Valor de adquisición en moneda o unidad de reajuste','Ayuda de valor de adquisición en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',15,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ValorAdquisicionEnPesos','C17','',17,'Valor de adquisición en Pesos','Ayuda de valor de adquisición en Pesos','AMOUNT',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',16,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ValorEnajenacion','C18','',18,'Valor de enajenación','Ayuda de valor de enajenación','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',17,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','ValorEnajenacionEnPesos','C19','',19,'Valor de enajenación en Pesos','Ayuda de valor de enajenación en Pesos','AMOUNT',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',18,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','MayorValorEnajenacion','C20','',20,'Mayor valor enagenación en moneda o unidad de reajuste','Ayuda de Mayor valor enagenación en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',19,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','MayorValorEnajenacionEnPesos','C21','',21,'Mayor valor enagenación en Pesos','Ayuda de Mayor valor enagenación en Pesos','AMOUNT',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',20,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','IdDocumento','','',22,'Secuencia Docto','Ayuda de Secuencia Docto','ID',0,'0','N','N','N','S','S','S','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','VboUsuario','','',23,'VoBo','Ayuda de VoBo','TEXT',1,'N','N','S','S','S','N','N','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','Observaciones','','',24,'Observaciones','Ayuda de Observaciones','TEXT',250,'','N','S','S','S','N','N','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','FuenteDatos','','',25,'Fuente','Ayuda de Fuente','TEXT',0,'INICIAL','N','N','N','S','N','N','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','IdCliente','','',26,'Secuencia Cliente','Ayuda de Secuencia Cliente','ID_CLIENTE',0,'0','S','S','N','S','N','N','N','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','IdPortafolio','','',27,'Secuencia Portafolio','Ayuda de Secuencia Portafolio','ID_PORTAFOLIO',0,'0','S','N','N','S','N','N','N','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2020','IdOperacion','','',28,'Id Operacion','','ID',0,'0','N','S','N','S','S','S','N','S','1. Transacciones informadas',0,0);


INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ID','','',1,'Secuencia','Ayuda  de Secuencia','ID',0,'0','N','N','N','S','N','N','S','N','',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','Numero','C2','',2,'Numero','Ayuda de Numero','ID',0,'0','N','N','N','S','N','N','N','N','1. Datos de las Operaciones',1,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','RutEmisor','C3','',3,'Rut Emisor','Ayuda de Rut Emisor','RUT',0,'','N','S','N','S','N','N','N','S','1. Datos de las Operaciones',2,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','TaxID','C4','',4,'Tax ID Inversionista','Ayuda de Tax ID','RUT',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',3,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','RutCliente','C5','',5,'Rut Cliente','Ayuda de Rut Cliente','RUT_CLIENTE',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',4,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','CodigoPaisConstitucion','C6','CodigoPais',6,'Codigo Pais Constitucion','Ayuda de Codigo Pais Constitucion','NEMO',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',5,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','CodigoPaisResidencia','C7','CodigoPais',7,'Codigo Pais Residencia','Ayuda de Codigo Pais Residencia','NEMO',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',6,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','PorcentajePropiedadDirectaIndirecta','C8','',8,'% de Propiedad o Participación directa o indirecta en el Capital o Utilidades del emisor','Ayuda de % de Propiedad o Participación directa o indirecta en el Capital o Utilidades del emisor','DECIMAL',0,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',7,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ParticipacionDirectaIndirectaControl','C9','',9,'Participación directa o indirecta en el control del Emisor','Ayuda de Participación directa o indirecta en el control del Emisor','QTY',0,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',8,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES	 ('DJ1919','2021','NemotecnicoSVS','C10','',10,'Nemotécnico SVS','Ayuda de Nemotécnico SVS','NEMO',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',9,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','FechaOperacion','C11','',11,'Fecha Operación','Ayuda de Fecha Operación','DATE_FILTER',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',10,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','NumeroCuentaDCV','C12','',12,'N° Cuenta DCV','Ayuda de N° Cuenta DCV','TEXT',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',11,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','Moneda','C13','CodigoMonedaDJ1919',13,'Moneda o unidad de reajuste','Ayuda de moneda o unidad de reajuste','TEXT',0,'','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',12,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ValorEmision','C14','',14,'Valor de emisión en moneda o unidad de reajuste','Ayuda de valor de emisión en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',13,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ValorColocacion','C15','',15,'Valor de colocación en moneda o unidad de reajuste','Ayuda de valor de colocación en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',14,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ValorAdquisicion','C16','',16,'Valor de adquisición en moneda o unidad de reajuste','Ayuda de valor de adquisición en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',15,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ValorAdquisicionEnPesos','C17','',17,'Valor de adquisición en Pesos','Ayuda de valor de adquisición en Pesos','AMOUNT',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',16,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ValorEnajenacion','C18','',18,'Valor de enajenación','Ayuda de valor de enajenación','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',17,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','ValorEnajenacionEnPesos','C19','',19,'Valor de enajenación en Pesos','Ayuda de valor de enajenación en Pesos','AMOUNT',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',18,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','MayorValorEnajenacion','C20','',20,'Mayor valor enagenación en moneda o unidad de reajuste','Ayuda de Mayor valor enagenación en moneda o unidad de reajuste','DECIMAL',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',19,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','MayorValorEnajenacionEnPesos','C21','',21,'Mayor valor enagenación en Pesos','Ayuda de Mayor valor enagenación en Pesos','AMOUNT',15,'0','N','S','N','S','N','N','N','N','1. Datos de las Operaciones',20,2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','IdDocumento','','',22,'Secuencia Docto','Ayuda de Secuencia Docto','ID',0,'0','N','N','N','S','S','S','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','VboUsuario','','',23,'VoBo','Ayuda de VoBo','TEXT',1,'N','N','S','S','S','N','N','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','Observaciones','','',24,'Observaciones','Ayuda de Observaciones','TEXT',250,'','N','S','S','S','N','N','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','FuenteDatos','','',25,'Fuente','Ayuda de Fuente','TEXT',0,'INICIAL','N','N','N','S','N','N','S','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','IdCliente','','',26,'Secuencia Cliente','Ayuda de Secuencia Cliente','ID_CLIENTE',0,'0','S','S','N','S','N','N','N','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','IdPortafolio','','',27,'Secuencia Portafolio','Ayuda de Secuencia Portafolio','ID_PORTAFOLIO',0,'0','S','N','N','S','N','N','N','N','5. DATOS DE CONTROL',0,0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" 
("Esquema","Version","Nombre","CodigoSII","TipoDiccionario","Orden","Titulo","Ayuda","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo",
"Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","CantidadDecimales") 
VALUES ('DJ1919','2021','IdOperacion','','',28,'Id Operacion','','ID',0,'0','N','S','N','S','S','S','N','S','1. Transacciones informadas',0,0);


-- HOMOLOGACION
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1919','2020','UNIDADMONEDA','PESO CHILENO','CLP','1','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1919','2020','UNIDADMONEDA','DOLAR AMERICANO','USD','2','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1919','2020','UNIDADMONEDA','EURO','EUR','3','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1919','2020','UNIDADMONEDA','UNIDAD DE FOMENTO','UF','4','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1919','2020','UNIDADMONEDA','INDICE VALOR PROMEDIO','IVP','5','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1919','2020','UNIDADMONEDA','OTRO','OTR','6','ACT' FROM DUMMY;


INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1919','2021','UNIDADMONEDA','PESO CHILENO','CLP','1','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1919','2021','UNIDADMONEDA','DOLAR AMERICANO','USD','2','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1919','2021','UNIDADMONEDA','EURO','EUR','3','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1919','2021','UNIDADMONEDA','UNIDAD DE FOMENTO','UF','4','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1919','2021','UNIDADMONEDA','INDICE VALOR PROMEDIO','IVP','5','ACT' FROM DUMMY;
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado")
SELECT "BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'DJ1919','2021','UNIDADMONEDA','OTRO','OTR','6','ACT' FROM DUMMY;


-- DICCIONARIO MONEDA DJ1919

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario" ("Tipo","Codigo","Orden","Descripcion","Activo","Objetivo","Producto")
SELECT DISTINCT 'CodigoMonedaDJ1919' AS "Tipo", h."ValorOrigen" AS "Codigo", h."ValorDestino" AS "Orden", h."Descripcion" AS "Descripcion",'S' AS "Activo",'' AS "Objetivo",'' AS "Producto" 
FROM "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" h WHERE h."Esquema" = 'DJ1919' AND h."Tipo" = 'UNIDADMONEDA';


-- VALIDACIONES 2020
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2020', 'H1798', 'NORMA', 'S', 'Debe Existir valor en todas las columnas: (C2), (C3), (C4), (C5), (C6), (C7), (C8), (C9), (C10), (C11), (C12), (C13), (C14), (C15), (C16), (C17),  (C18), (C19), (C20), (C21)', 'E', 'C2,C3,C4,C5,C6,C9,C10,C12,C13 <> Blanco .o. vacío .y. C7,C8,C11,C14,C15,C16,C17,C18,C19,C20  <>  vacío .o. 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2020', 'H1803', 'NORMA', 'S', 'El campo "Código país de Constitución"(C6) debe encontrarse en el listado que para tal efecto hay en el suplemento de ddjj', 'E', 'C6 = listado código de países');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2020', 'H1804', 'NORMA', 'S', 
'El campo "Código país de Residencia"(C7) debe encontrarse dentro del listado que para tal efecto hay en el suplemento de ddjj', 'E', 'C7 = listado cod de países');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2020', 'H1805', 'NORMA', 'S', 
'El campo "Participación Directo o Indirecta en el control del emisor" debe tomar valor 1 o 2, sin decimales.', 'E', 'C9 = 1.o.2');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2020', 'H1806', 'NORMA', 'S', 
'El  campo "Moneda o Unidad de Reajuste" debe tomar valores 1, 2, 3, 4, 5 o 6', 'E', 'C13 = 1.o.2.o.3.o.4.o.5.o.6');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2020', 'H1808', 'NORMA', 'S', 
'Cuando registra valor numerico mayor que cero en el Campo "Mayor Valor Enajenación (en moneda o unidad de reajuste), el campo "Valor de Enajenación en Moneda o Unidad de reajuste" debe ser mayor que el campo "Valor de adquisición en moneda o unidad de reajuste".', 
'E', 'C18 > C16');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2020', 'H1809', 'NORMA', 'S', 
'En los campos (% de propiedad o participación directa o indirecta en el capital o utilidades del emisor", "Valor de Emisión en Moneda o Unidad de Reajuste" , "Valor de Colocación en Moneda o Unidad de Reajuste" , "Valor de Adquisición en moneda o unidad de reajuste" , "Valor de Enajenación en Moneda o Unidad de reajuste", "Mayor Valor (en moneda o unidad de reajuste") debe registrar valores numericos con un maximo de dos decimales.', 
'E', 'C8 > 0  C14  > 0 C15 > C16 >0 C18 > C20 > 0 Y C21 >0; valores pueden máximo 2 decimales');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2020', 'H1799', 'NORMA', 'S', 
'Los campos: "Rut Emisor"(C3), "Rut Inversionista" (C5) deben cumplir con modulo 11 (verificacion digito verificador)', 'E', 'C3 <> Blanco o Vacío .y. C5 .y M11{(C3)} .y.  = M11{(C5)}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2020', 'H1802', 'NORMA', 'S', 
'En el campo "Tax ID Inversinista" no se debe ingresar el valor  55.555.555-5', 'E', 'C4 <> 55.555.555-5');

--VALIDACION 2021
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2021', 'H1798', 'NORMA', 'S', 'Debe Existir valor en todas las columnas: (C2), (C3), (C4), (C5), (C6), (C7), (C8), (C9), (C10), (C11), (C12), (C13), (C14), (C15), (C16), (C17),  (C18), (C19), (C20), (C21)', 'E', 'C2,C3,C4,C5,C6,C9,C10,C12,C13 <> Blanco .o. vacío .y. C7,C8,C11,C14,C15,C16,C17,C18,C19,C20  <>  vacío .o. 0');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2021', 'H1803', 'NORMA', 'S', 'El campo "Código país de Constitución"(C6) debe encontrarse en el listado que para tal efecto hay en el suplemento de ddjj', 'E', 'C6 = listado código de países');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2021', 'H1804', 'NORMA', 'S', 
'El campo "Código país de Residencia"(C7) debe encontrarse dentro del listado que para tal efecto hay en el suplemento de ddjj', 'E', 'C7 = listado cod de países');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2021', 'H1805', 'NORMA', 'S', 
'El campo "Participación Directo o Indirecta en el control del emisor" debe tomar valor 1 o 2, sin decimales.', 'E', 'C9 = 1.o.2');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2021', 'H1806', 'NORMA', 'S', 
'El  campo "Moneda o Unidad de Reajuste" debe tomar valores 1, 2, 3, 4, 5 o 6', 'E', 'C13 = 1.o.2.o.3.o.4.o.5.o.6');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2021', 'H1808', 'NORMA', 'S', 
'Cuando registra valor numerico mayor que cero en el Campo "Mayor Valor Enajenación (en moneda o unidad de reajuste), el campo "Valor de Enajenación en Moneda o Unidad de reajuste" debe ser mayor que el campo "Valor de adquisición en moneda o unidad de reajuste".', 
'E', 'C18 > C16');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2021', 'H1809', 'NORMA', 'S', 
'En los campos (% de propiedad o participación directa o indirecta en el capital o utilidades del emisor", "Valor de Emisión en Moneda o Unidad de Reajuste" , "Valor de Colocación en Moneda o Unidad de Reajuste" , "Valor de Adquisición en moneda o unidad de reajuste" , "Valor de Enajenación en Moneda o Unidad de reajuste", "Mayor Valor (en moneda o unidad de reajuste") debe registrar valores numericos con un maximo de dos decimales.', 
'E', 'C8 > 0  C14  > 0 C15 > C16 >0 C18 > C20 > 0 Y C21 >0; valores pueden máximo 2 decimales');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2021', 'H1799', 'NORMA', 'S', 
'Los campos: "Rut Emisor"(C3), "Rut Inversionista" (C5) deben cumplir con modulo 11 (verificacion digito verificador)', 'E', 'C3 <> Blanco o Vacío .y. C5 .y M11{(C3)} .y.  = M11{(C5)}');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Validacion"
("Esquema", "Version", "Codigo", "Tipo", "Activa", "Descripcion", "Severidad", "Formula")
VALUES('DJ1919', '2021', 'H1802', 'NORMA', 'S', 
'En el campo "Tax ID Inversinista" no se debe ingresar el valor  55.555.555-5', 'E', 'C4 <> 55.555.555-5');




-- Control proceso
INSERT INTO "BSCL"."BSCL.Normativa.BD::TRI.ControlProceso"
("Esquema","Version","EstadoCertificado","Carga","Validacion","PreDeclaracion","Certificacion")
SELECT "Esquema","Version",'NGE','N','N','N','N' FROM "BSCL"."BSCL.Normativa.BD::DOC.Esquema" WHERE  "Esquema" = 'DJ1919' AND "Version" IN ('2020');