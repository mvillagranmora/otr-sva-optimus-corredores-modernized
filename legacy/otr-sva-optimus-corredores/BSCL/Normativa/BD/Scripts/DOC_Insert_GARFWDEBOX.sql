SET SCHEMA "BSCL";

/*==============================================================*/
/* INSERT NORMATIVA                                            */
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::NOR.Normativa" 
("IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino","NombreDestinatario","CargoDestinatario","UI_Emision","PermiteSolicitar")
SELECT 'INFOFWDEBOX','Informes Forwards para Ebox','ACT','','BCS','','','S','S'
FROM DUMMY
WHERE NOT EXISTS (
    SELECT 1 FROM "BSCL.Normativa.BD::NOR.Normativa"
    WHERE "IdNormativa" = 'INFOFWDEBOX');

/*==============================================================*/
/* DELETE TIPO DOCUMENTO                                        */
/*==============================================================*/
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" 
WHERE "IdTipoDocumento" = 'GARFWDEBOX';

/*==============================================================*/
/* TIPO DOCUMENTO                                               */
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" 
("IdTipoDocumento","Descripcion","IdTipoProgramaExportacion","IdNormativa","Esquema","UI_Visor","IdTipoProgramaProceso","UI_Emision","TitulosSalida","ExtensionSalida","ComprimirSalida","LineasPorSalida","IdReporte")
VALUES ('GARFWDEBOX','Informe Garantias Forward','EXPORTACIONGARFWDEBOX','INFOFWDEBOX','GARFWDEBOX','N','','S','S','xlsx','N',0,'');

/*==============================================================*/
/* DELETE ESQUEMAS                                              */
/*==============================================================*/
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" 
WHERE "Esquema" = 'GARFWDEBOX';

/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Periodicidad","Objetivo","PeriodoInicio","IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta","CodigoVersionSII","Modulo","HabilitadoMantenedor","TieneCertificado","TieneEnvio") 
VALUES ('GARFWDEBOX','V1','D','','0001-01-01','BCS','2025-01-01','2025-01-01','9999-01-01',1,'NOR','N','N','N');

/*==============================================================*/
/* DELETE CAMPOS                                                */
/*==============================================================*/
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" 
WHERE "Esquema" = 'GARFWDEBOX';

/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('GARFWDEBOX','V1','RutCliente','',1,'Rut cliente','RUT',11,'','N','S','S','S','N','N','N','N','',0,'Ayuda RutCliente','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('GARFWDEBOX','V1','NombreCliente','',2,'Nombre cliente','TEXT',50,'','N','S','S','S','N','N','N','N','',0,'Ayuda NombreCliente','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('GARFWDEBOX','V1','GarantiaExigida','',3,'Garantías exigidas $','DECIMAL',34,'','N','S','S','S','N','N','N','N','',0,'Ayuda GarantiaExigida','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('GARFWDEBOX','V1','GarantiaEnterada','',4,'Garantías enteradas $','DECIMAL',34,'','N','S','S','S','N','N','N','N','',0,'Ayuda GarantiaEnterada','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('GARFWDEBOX','V1','TipoLinea','',5,'Tipo de Línea','TEXT',3,'','N','S','S','S','N','N','N','N','',0,'Ayuda TipoLinea','',0);

/*==============================================================*/
/*                DICCIONARIO             						*/
/*==============================================================*/
UPSERT "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario","CodLenguaje","Descripcion","Abreviado")
VALUES ('UI_INFORME_GARANTIAS_FORWARD','CL','Informe Garantías Forward','')
WHERE ("CodDiccionario","CodLenguaje") = ('UI_INFORME_GARANTIAS_FORWARD','CL');

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

COMMIT;

