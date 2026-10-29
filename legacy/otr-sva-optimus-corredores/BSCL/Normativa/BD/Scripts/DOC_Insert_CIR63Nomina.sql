SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Tables                                                */
/*==============================================================*/



--DELETE FROM "BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema"='CI63Nomina';
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='CI63Nomina';
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema"='CI63Nomina';
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='CI63Nomina';

/*==============================================================*/
/* Insert Table: TipoDocumento                     	                */
/*==============================================================*/

INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion")
VALUES ('CI63Nomina','Nomina de Personas Relacionadas','CIR63','CI63Nomina','EXPORTACIONCI63NOMINA');


/*==============================================================*/
/* Insert Table: Esquema                     	                */
/*==============================================================*/

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('CI63Nomina', 'V1', 'NOR', 'D', '', '', 'BCS', '20160110', '99990101', '99991231', 1);

/*==============================================================*/
/* Insert Table: Campo          	                            */
/*==============================================================*/
--DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='CI63Nomina'
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','NombreDestinatario','','',1,'Nombre Destinatario','Ayuda de Nombre Destinatario','TEXT',100,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','CargoDestinatario','','',2,'Cargo Destinatario','Ayuda de Cargo Destinatario','TEXT',520,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','Direccion','','',3,'Direccion','Ayuda de Direccion','TEXT',255,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','Comuna','','',4,'Comuna','Ayuda de Comuna','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','Ciudad','','',5,'Ciudad','Ayuda de Ciudad','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','IdPersona','','',6,'Id Persona','Ayuda de Id Persona','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','NombrePersona','','',7,'Nombre Persona','Ayuda de Nombre Persona','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','IdPersonaEmpleador','','',8,'Id Persona Empleador','Ayuda de Id Persona Empleador','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','NombreEmpleador','','',9,'Nombre Empleador','Ayuda de Nombre Empleador','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','DocumentoIdentidad','','',10,'Documento Identidad','Ayuda de Documento Identidad','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','TipoDocumentoIdentidad','','',11,'Tipo Documento Identidad','Ayuda de Tipo Documento Identidad','TEXT',10,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','NombreTipoDocumentoIdentidad','','',12,'Nombre Tipo Documento Identidad','Ayuda de Nombre Tipo Documento Identidad','TEXT',5,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','IdTipoRelacion','','',13,'Id Tipo Relacion','Ayuda de Id Tipo Relacion','TEXT',6,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','NombreTipoRelacion','','',14,'Nombre Tipo RelaciÓn','Ayuda de Nombre Tipo RelaciÓn','TEXT',100,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','CodigoRelacionNorma','','',15,'Código Relación Norma','Ayuda de Nombre Tipo RelaciÓn','TEXT',10,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','DescripcionRelacionNorma','','',16,'Descripción Relación Norma','Ayuda de Descripción Relación Norma','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('CI63Nomina','V1','Periodo','','',17,'Periodo','Periodo de emición','TEXT',7,'','N','N','N','S','N','N','N','N','',0,0);
						

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
