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
WHERE "IdTipoDocumento" = 'FWDVIGEBOX';

/*==============================================================*/
/* TIPO DOCUMENTO                                               */
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" 
("IdTipoDocumento","Descripcion","IdTipoProgramaExportacion","IdNormativa","Esquema","UI_Visor","IdTipoProgramaProceso","UI_Emision","TitulosSalida","ExtensionSalida","ComprimirSalida","LineasPorSalida","IdReporte")
VALUES ('FWDVIGEBOX','Informe Forwards Vigentes','EXPORTACIONFWDVIGEBOX','INFOFWDEBOX','FWDVIGEBOX','N','','S','S','xlsx','N',0,'');
    
/*==============================================================*/
/* DELETE ESQUEMAS                                              */
/*==============================================================*/
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" 
WHERE "Esquema" = 'FWDVIGEBOX';

/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema","Version","Periodicidad","Objetivo","PeriodoInicio","IdEntidadReguladora","FechaPresentacionLimite","FechaDatosDesde","FechaDatosHasta","CodigoVersionSII","Modulo","HabilitadoMantenedor","TieneCertificado","TieneEnvio") 
VALUES ('FWDVIGEBOX','V1','D','','0001-01-01','BCS','2025-01-01','2025-01-01','9999-01-01',1,'NOR','N','N','N');

/*==============================================================*/
/* DELETE CAMPOS                                                */
/*==============================================================*/
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" 
WHERE "Esquema" = 'FWDVIGEBOX';

/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','FechaOperacion','',1,'Fecha operación','DATE',10,'','N','S','S','S','N','N','N','N','',0,'Ayuda FechaOperacion','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','NumeroContrato','',2,'N° contrato','ID',15,'','N','S','S','S','N','N','N','N','',0,'Ayuda NumeroContrato','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','NumeroContratoCalce','',3,'N° contrato de calce (1)','ID',15,'','N','S','S','S','N','N','N','N','',0,'Ayuda NumeroContratoCalce','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','RutCliente','',4,'Rut cliente','RUT',11,'','N','S','S','S','N','N','N','N','',0,'Ayuda RutCliente','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','NombreCliente','',5,'Nombre cliente','TEXT',50,'','N','S','S','S','N','N','N','N','',0,'Ayuda NombreCliente','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','ClienteRelacionado','',6,'Cliente relacionado (SI / NO) (2)','TEXT',2,'','N','S','S','S','N','N','N','N','',0,'Ayuda IndicadorCobertura','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','TipoOperacion','',7,'Tipo operación (compra/venta) (3)','TEXT',6,'','N','S','S','S','N','N','N','N','',0,'Ayuda TipoOperacion','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','FechaVencimiento','',8,'Fecha vencimiento','DATE',10,'','N','S','S','S','N','N','N','N','',0,'Ayuda FechaVencimiento','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','ModalidadLiquidacion','',9,'Modalidad liquidación (4)','TEXT',3,'','N','S','S','S','N','N','N','N','',0,'Ayuda ModalidadLiquidacion','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','MonedaTasa','',10,'Moneda o Tasa','TEXT',7,'','N','S','S','S','N','N','N','N','',0,'Ayuda MonedaTasa','',0);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','ValorNominal','',11,'Valor Nominal','DECIMAL',20,'','N','S','S','S','N','N','N','N','',0,'Ayuda ValorNominal','',2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','TipoCambioPactado','',12,'Tipo de cambio pactado','DECIMAL',20,'','N','S','S','S','N','N','N','N','',0,'Ayuda TipoCambioPactado','',2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','MontoPactado','',13,'Monto pactado en $ (5)','DECIMAL',20,'','N','S','S','S','N','N','N','N','',0,'Ayuda MontoPactado','',2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','TipoCambioMercado','',14,'Tipo cambio mercado','DECIMAL',20,'','N','S','S','S','N','N','N','N','',0,'Ayuda TipoCambioMercado','',2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','ValorizacionMercado','',15,'Valorización a mercado $ (6)','DECIMAL',20,'','N','S','S','S','N','N','N','N','',0,'Ayuda ValorizacionMercado','',2);

INSERT INTO "BSCL.Normativa.BD::DOC.Campo" ("Esquema","Version","Nombre","TipoDiccionario","Orden","Titulo","TipoDato","Largo","ValorDefecto","Obligatorio","Editable","ModoMasivo","Exportable","OcultoEnGrilla","OcultoEnMantenedor","Comun","Clave","Grupo","OrdenCargaExterna","Ayuda","CodigoSII","CantidadDecimales") 
VALUES ('FWDVIGEBOX','V1','ResultadoOperacion','',16,'Resultado operación utilidad/(pérdida) $','DECIMAL',20,'','N','S','S','S','N','N','N','N','',0,'Ayuda ResultadoOperacion','',0);

/*==============================================================*/
/* DELETE HOMOLOGACION                             				*/
/*==============================================================*/
DELETE FROM "BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'FWDVIGEBOX';

/*==============================================================*/
/*                HOMOLOGACION             						*/
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") 
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'FWDVIGEBOX','V1','MODALIDADCUMPLIMIENTO','COMPENSADA','6','CMP','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") 
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'FWDVIGEBOX','V1','MODALIDADCUMPLIMIENTO','ENTREGA_FISICA','7','EF','ACT');

INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion","Esquema","Version","Tipo","Descripcion","ValorOrigen","ValorDestino","IdEstado") 
VALUES ("BSCL.Normativa.BD.Secuencias::DOC.IdHomologacion".NEXTVAL,'FWDVIGEBOX','V1','MONEDA','UNIDAD DE FOMENTO','UF','CLF','ACT');

/*==============================================================*/
/*                DICCIONARIO             						*/
/*==============================================================*/
UPSERT "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario","CodLenguaje","Descripcion","Abreviado")
VALUES ('UI_INFORME_FORWARDS_VIGENTES','CL','Informe Forwards Vigentes','')
WHERE ("CodDiccionario","CodLenguaje") = ('UI_INFORME_FORWARDS_VIGENTES','CL');


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

COMMIT;