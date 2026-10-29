SET SCHEMA "BSCL";


/*==============================================================*/
/* Delete Tables                                                */
/*==============================================================*/

--1.- CIRCULARIZACION - NOMINA

DELETE FROM "BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema"='CIRCNomina';
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='CIRCNomina';
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema" IN ('CIRCNomina','CIRCCarta');
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento" IN ('CIRCNomina','CIRCCarta');

/*==============================================================*/
/* Insert Table: TipoDocumento                     	                */
/*==============================================================*/


---Abarca la Nomina y El Resumen de la Circularizacion
INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaProceso")
VALUES ('CIRCNomina','Circularizacion Nomina','CIRCULARIZACION','CIRCNomina','PROCESONOMINACIRCULARIZACION');

INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaProceso")
VALUES ('CIRCCarta','Circularizacion Carta Custodia','CIRCULARIZACION','CIRCCarta','PROCESOMASIVOCARTACIRCULARIZACION');


/*==============================================================*/
/* Insert Table: Esquema                     	                */
/*==============================================================*/

---Esquema Indefinido
INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('CIRCNomina', 'V1', 'NOR', 'M', '', '', 'BCS', '20160101', '20160101', '99990101', 0);

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('CIRCCarta', 'V1', 'NOR', 'M', '', '', 'BCS', '20160101', '20160101', '99990101', 0);

/*==============================================================*/
/* Insert Table: Campo          	                            */
/*==============================================================*/
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CIRCNomina','V1','IdCliente','','',1,'Id Cliente','Ayuda de Id Cliente','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
--insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
--VALUES ('CIRCNomina','V1','FechaOperacion','','',2,'Fecha Operación','Ayuda de Fecha Operación','DATE_FILTER',0,'','N','N','N','S','N','N','N','N','',0,0);

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
