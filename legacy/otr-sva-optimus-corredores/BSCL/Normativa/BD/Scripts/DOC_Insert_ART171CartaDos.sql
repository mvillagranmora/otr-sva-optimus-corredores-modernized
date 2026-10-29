SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Tables                                                */
/*==============================================================*/



--
--DELETE FROM "BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema"='ART171Car2';
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='ART171Car2';
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema"='ART171Car2';
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='ART171Car2';

/*==============================================================*/
/* Insert Table: TipoDocumento                     	                */
/*==============================================================*/

INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion")
VALUES ('ART171Car2','Carta 2 Articulo 171','ART171CartaDos','ART171Car2','EXPORTACIONCARTA2ART171');


/*==============================================================*/
/* Insert Table: Esquema                     	                */
/*==============================================================*/


INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('ART171Car2', 'V1', 'NOR', 'M', '', '', 'SVS', '20160110', '20160101', '99990101', 1);

/*==============================================================*/
/* Insert Table: Campo          	                            */
/*==============================================================*/
--SELECT * FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='ART171Car2'
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='ART171Car2'
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','NombreDestinatario','','',1,'Nombre Destinatario','Ayuda de Nombre Destinatario','TEXT',100,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','CargoDestinatario','','',2,'Cargo Destinatario','Ayuda de Cargo Destinatario','TEXT',520,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','FechaOperacion','','',3,'Fecha Operación','Ayuda de Fecha Operación','DATE_FILTER',10,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','IdTipoOperacion','','',4,'IdTipoOperacion','Ayuda de Id Tipo Operacion','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','NombreTipoOperacion','','',5,'Nombre Tipo Operación','Ayuda de Nombre Tipo Operación','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','IdClasificacionTipoInstrumento','','',6,'Id ClasificaciÓn Tipo Instrumento','Ayuda de Id ClasificaciÓn Tipo Instrumento','TEXT',15,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','IdInstrumento','','',7,'Id Instrumento','Ayuda de Id Instrumento','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','Nemotecnico','','',8,'Nemo técnico','Ayuda de Nemo técnico','NEMO',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','Cantidad','','',9,'Cantidad','Ayuda de Cantidad','QTY',0,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','Precio','','',10,'Precio','Ayuda de Precio','QTY',0,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','Monto','','',11,'Monto','Ayuda de Monto','QTY',0,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','IdMoneda','','',12,'Id Moneda','Ayuda de Id Moneda','TEXT',4,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','Moneda','','',13,'Moneda','Ayuda de Moneda','TEXT',40,'','N','N','N','N','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','IdCliente','','',14,'Id Cliente','Ayuda de Id Cliente','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','NombreCliente','','',15,'Nombre Cliente','Ayuda de Nombre Cliente','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','DocumentoIdentidad','','',16,'Documento Identidad','Ayuda de Documento Identidad','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','TipoDocumentoIdentidad','','',17,'Tipo Documento Identidad','Ayuda de Tipo Documento Identidad','TEXT',10,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','NombreTipoDocumentoIdentidad','','',18,'Nombre Tipo Documento Identidad','Ayuda de Nombre Tipo Documento Identidad','TEXT',5,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','IdTipoRelacion','','',19,'Id Tipo Relacion','Ayuda de Id Tipo Relacion','TEXT',6,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','NombreTipoRelacion','','',20,'Nombre Tipo RelaciÓn','Ayuda de Nombre Tipo RelaciÓn','TEXT',100,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','CodigoRelacionNorma','','',21,'Código Relación Norma','Ayuda de Nombre Tipo RelaciÓn','TEXT',10,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('ART171Car2','V1','DescripcionRelacionNorma','','',22,'Descripción Relación Norma','Ayuda de Descripción Relación Norma','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
						
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
