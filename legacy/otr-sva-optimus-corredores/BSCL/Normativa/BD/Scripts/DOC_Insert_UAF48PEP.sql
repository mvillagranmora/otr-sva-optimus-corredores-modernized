SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Tables                                                */
/*==============================================================*/



--DELETE FROM "BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema"='UAF48PEP';
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='UAF48PEP';
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema"='UAF48PEP';
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='UAF48PEP';

/*==============================================================*/
/* Insert Table: TipoDocumento                     	                */
/*==============================================================*/



INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion")
VALUES ('UAF48PEP','Personas Expuestas Políticamente (PEP)','UAF','UAF48PEP','EXPORTACIONUAFCIR48PEP');


/*==============================================================*/
/* Insert Table: Esquema                     	                */
/*==============================================================*/

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('UAF48PEP', 'V1', 'NOR', 'M', '', '', 'BCS', '20160110', '99990101', '20161231', 1);
/*
INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('UAF48PEP', 'V2', 'NOR', 'M', '', '', 'BCS', '20170110', '20170101', '0001-01-01', 2);
*/
/*==============================================================*/
/* Insert Table: Campo          	                            */
/*==============================================================*/


insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAF48PEP','V1','IdPersona','','',1,'Id Persona','Ayuda de Id Persona','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAF48PEP','V1','IdCliente','','',2,'Id Cliente','Ayuda de Id Cliente','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAF48PEP','V1','CodigoPortafolio','','',3,'Codigo Portafolio','Ayuda de Codigo Portafolio','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAF48PEP','V1','NombreCliente','','',4,'Nombre Cliente','Ayuda de Nombre Cliente','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAF48PEP','V1','RutCliente','','',5,'Nombre Rut Cliente','Ayuda de Rut Cliente','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAF48PEP','V1','CodigoEjecutivo','','',6,'Nombre Codigo Ejecutivo','Ayuda de Codigo Ejecutivo','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAF48PEP','V1','NombreEjecutivo','','',7,'Nombre Ejecutivo','Ayuda de Nombre Ejecutivo','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAF48PEP','V1','FechaConsulta','','',8,'Fecha Consulta','Ayuda de Nombre Ejecutivo','TEXT',10,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAF48PEP','V1','Titulos','','',9,'Titulos','Ayuda de Titulos','TEXT',255,'','N','N','N','S','N','N','N','N','',0,0);

						
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
