SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Tables                                                */
/*==============================================================*/



--DELETE FROM "BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema"='UAFROE';
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='UAFROE';
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema"='UAFROE';
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='UAFROE';
DELETE FROM  "BSCL.Normativa.BD::DOC.Homologacion"  WHERE "Esquema"='UAFROE';
/*==============================================================*/
/* Insert Table: TipoDocumento                     	                */
/*==============================================================*/



INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion")
VALUES ('UAFROE','Reporte de Operaciones en Efectivo','UAF','UAFROE','EXPORTACIONUAFROE');


/*==============================================================*/
/* Insert Table: Esquema                     	                */
/*==============================================================*/

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('UAFROE', 'V1', 'NOR', 'M', '', '', 'BCS', '20160110', '99990101', '20161231', 1);

/*
INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('UAFROE', 'V2', 'NOR', 'M', '', '', 'BCS', '20170110', '20170101', '0001-01-01', 2);
*/
/*==============================================================*/
/* Insert Table: Campo          	                            */
/*==============================================================*/


insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RegionSucursal','','',1,'Region Sucursal','Ayuda de Region Sucursal','TEXT',2,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','LocalidadSucursal','','',2,'Localidad Sucursal','Ayuda de Localidad Sucursal','TEXT',6,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','CalleSucursal','','',3,'Calle Sucursal','Ayuda de Calle Sucursal','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','NumeroSucursal','','',4,'Numero Sucursal','Ayuda de Numero Sucursal','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','DeptoSucursal','','',5,'Depto Sucursal','Ayuda de Depto Sucursal','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','TelefonoSucursal','','',6,'Telefono Sucursal','Ayuda de Telefono Sucursal','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','VinculoTransferencia','','',7,'Vinculo Transferencia','Ayuda de Vinculo Transferencia','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','IdMonedaEntrante','','',8,'Moneda Entrante','Ayuda de Moneda Entrante','TEXT',3,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','MontoEntrante','','',9,'Monto Entrante','Ayuda de Monto Entrante','QTY',0,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','DenBilleteEntrante','','',10,'Den Billete Entrante','Ayuda de Den Billete Entrante','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);

insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','IdMonedaSaliente','','',11,'Moneda Saliente','Ayuda de Moneda Saliente','TEXT',3,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','MontoSaliente','','',12,'Monto Saliente','Ayuda de Monto Saliente','QTY',0,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','DenBilleteSaliente','','',13,'Den Billete Saliente','Ayuda de Den Billete Saliente','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);

insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','NumComprobante','','',14,'Num Comprobante','Ayuda de NumComprobante','ID',0,'','N','N','N','S','N','N','N','N','',0,0);				
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','Fecha','','',15,'Fecha','Ayuda de Fecha','TEXT',10,'','N','N','N','S','N','N','N','N','',0,0);

insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','Identificacion','','',16,'Identificacion','Ayuda de Identificacion','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','TipoDocumento','','',17,'Tipo Documento','Ayuda de Tipo Documento','TEXT',10,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RazonSocial','','',18,'Razon Social','Ayuda de Razon Social','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','ApellidoPaterno','','',19,'Apellido Paterno','Ayuda de Apellido Paterno','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','ApellidoMaterno','','',20,'Apellido Materno','Ayuda de Apellido Materno','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','Nacionalidad','','',21,'Nacionalidad','Ayuda de Nacionalidad','TEXT',3,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','Habitualidad','','',22,'Habitualidad','Ayuda de Habitualidad','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','Pais','','',23,'Pais','Ayuda de Pais','TEXT',3,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','Region','','',24,'Region','Ayuda de Region','TEXT',2,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','Comuna','','',25,'Comuna','Ayuda de Comuna','TEXT',6,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','Calle','','',26,'Calle','Ayuda de Calle','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','Numero','','',27,'Numero','Ayuda de Numero','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','Departamento','','',28,'Departamento','Ayuda de Departamento','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','Telefono','','',29,'Telefono','Ayuda de Telefono','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepIdentificacion','','',30,'Identificacion','Ayuda de Identificacion','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepTipoDocumento','','',31,'Tipo Documento','Ayuda de Tipo Documento','TEXT',10,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepRazonSocial','','',32,'Razon Social','Ayuda de Razon Social','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepApellidoPaterno','','',33,'Apellido Paterno','Ayuda de Apellido Paterno','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepApellidoMaterno','','',34,'Apellido Materno','Ayuda de Apellido Materno','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepNacionalidad','','',35,'Nacionalidad','Ayuda de Nacionalidad','TEXT',3,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepHabitualidad','','',36,'Habitualidad','Ayuda de Habitualidad','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepPais','','',37,'Pais','Ayuda de Pais','TEXT',3,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepRegion','','',38,'Region','Ayuda de Region','TEXT',2,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepComuna','','',39,'Comuna','Ayuda de Comuna','TEXT',6,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepCalle','','',40,'Calle','Ayuda de Calle','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepNumero','','',41,'Numero','Ayuda de Numero','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepDepartamento','','',42,'Departamento','Ayuda de Departamento','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFROE','V1','RepTelefono','','',43,'Telefono','Ayuda de Telefono','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);

							
		
		
		
		
		
/*==============================================================*/
/* Insert Table: Homologacion          	                            */
/*==============================================================*/
		
---MONEDA
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (144,'UAFROE','V1','Moneda','REAL BRASILERO','BRL','005','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (145,'UAFROE','V1','Moneda','DOLAR CANADIENSE','CAD','006','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (146,'UAFROE','V1','Moneda','DOLAR','USD','013','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (147,'UAFROE','V1','Moneda','SOL PERUANO','PEN','024','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (148,'UAFROE','V1','Moneda','DOLAR AUSTRALIA','AUD','036','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (149,'UAFROE','V1','Moneda','RENMINBI CHINA','RMB','048','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (150,'UAFROE','V1','Moneda','CORONA DANESA','DKK','051','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (151,'UAFROE','V1','Moneda','YEN JAPONES','JPY','072','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (152,'UAFROE','V1','Moneda','FRANCO SUIZO','CHF','082','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (153,'UAFROE','V1','Moneda','CORONA NORUEGA','NOK','096','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (154,'UAFROE','V1','Moneda','DOLAR NUEVA ZELANDA','NZD','097','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (155,'UAFROE','V1','Moneda','LIBRA ESTERLINA','GBP','102','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (156,'UAFROE','V1','Moneda','CORONA SUECA','SEK','113','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (157,'UAFROE','V1','Moneda','DOLAR HONG KONG','HKD','127','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (158,'UAFROE','V1','Moneda','PESO MEXICANO','MXN','132','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (159,'UAFROE','V1','Moneda','EURO','EUR','142','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (160,'UAFROE','V1','Moneda','PESO COLOMBIANO','COP','051','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (161,'UAFROE','V1','Moneda','PESO CUBANO','CUP','800','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (162,'UAFROE','V1','Moneda','UNIDAD DE FOMENTO','UF','800','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (163,'UAFROE','V1','Moneda','PESO CHILENO','CLP','999','ACT');
		
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")		
VALUES (164	,'UAFROE','V1','Pais','AFGHANISTAN','1','012','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (165	,'UAFROE','V1','Pais','ALBANIA','2','024','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (166	,'UAFROE','V1','Pais','ARGELIA','3','224','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (167	,'UAFROE','V1','Pais','SAMOA AMERICANA','4','868','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (168	,'UAFROE','V1','Pais','ANDORRA','5','004','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (169	,'UAFROE','V1','Pais','ANGOLA','6','032','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (170	,'UAFROE','V1','Pais','ANGUILA','7','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (171	,'UAFROE','V1','Pais','ANTARTIDA','8','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (172	,'UAFROE','V1','Pais','ANTIGUA Y BARBUDA','9','016','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (173	,'UAFROE','V1','Pais','ARGENTINA','10','040','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (174	,'UAFROE','V1','Pais','ARMENIA','11','900','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (175	,'UAFROE','V1','Pais','ARUBA','12','056','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (176	,'UAFROE','V1','Pais','AUSTRALIA','13','052','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (177	,'UAFROE','V1','Pais','AUSTRIA','14','048','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (178	,'UAFROE','V1','Pais','AZERBAIYAN','15','904','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (179	,'UAFROE','V1','Pais','BAHAMAS','16','108','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (180	,'UAFROE','V1','Pais','BAHREIN','17','080','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (181	,'UAFROE','V1','Pais','BANGLADESH','18','064','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (182	,'UAFROE','V1','Pais','BARBADOS','19','060','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (183	,'UAFROE','V1','Pais','BELGICA','20','068','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (184	,'UAFROE','V1','Pais','BELICE','21','132','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (185	,'UAFROE','V1','Pais','BENIN','22','088','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (186	,'UAFROE','V1','Pais','BERMUDAS','23','096','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (187	,'UAFROE','V1','Pais','BHUTAN','24','950','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (188	,'UAFROE','V1','Pais','BOLIVIA','25','100','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (189	,'UAFROE','V1','Pais','BOSNIA Y HERZEGOVINA','26','954','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (190	,'UAFROE','V1','Pais','BOTSWANA','27','124','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (191	,'UAFROE','V1','Pais','ISLA BOUVET','28','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (192	,'UAFROE','V1','Pais','BRASIL','29','104','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (193	,'UAFROE','V1','Pais','TERRITORIO BRITANICO DEL OCEANO INDICO','30','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (194	,'UAFROE','V1','Pais','ISLAS VIRGENES BRITANICAS','31','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (195	,'UAFROE','V1','Pais','BRUNEI DARUSSALAM','32','106','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (196	,'UAFROE','V1','Pais','BULGARIA','33','076','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (197	,'UAFROE','V1','Pais','BURKINA FASO','34','072','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (198	,'UAFROE','V1','Pais','BURUNDI','35','084','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (199	,'UAFROE','V1','Pais','BELARUS','36','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (200	,'UAFROE','V1','Pais','CAMBOYA','37','420','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (201	,'UAFROE','V1','Pais','CAMERUN','38','164','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (202	,'UAFROE','V1','Pais','CANADA','39','136','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (203	,'UAFROE','V1','Pais','CABO VERDE','40','188','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (204	,'UAFROE','V1','Pais','ISLAS CAIMAN','41','386','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (205	,'UAFROE','V1','Pais','REPUBLICA CENTROAFRICANA','42','144','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (206	,'UAFROE','V1','Pais','CHAD','43','764','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (207	,'UAFROE','V1','Pais','CHILE','44','160','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (208	,'UAFROE','V1','Pais','CHINA','45','168','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (209	,'UAFROE','V1','Pais','ISLA CHRISTMAS','46','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (210	,'UAFROE','V1','Pais','ISLAS COCOS (KEELING)','47','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (211	,'UAFROE','V1','Pais','COLOMBIA','48','172','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (212	,'UAFROE','V1','Pais','COMORAS','49','428','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (213	,'UAFROE','V1','Pais','CONGO','50','174','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (214	,'UAFROE','V1','Pais','ISLAS COOK','51','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (215	,'UAFROE','V1','Pais','COSTA RICA','52','176','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (216	,'UAFROE','V1','Pais','COSTA DE MARFIL/CÔTE D IVOIRE','53','152','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (217	,'UAFROE','V1','Pais','CROACIA','54','908','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (218	,'UAFROE','V1','Pais','CUBA','55','184','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (219	,'UAFROE','V1','Pais','CHIPRE','56','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (220	,'UAFROE','V1','Pais','REPUBLICA CHECA','57','912','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (221	,'UAFROE','V1','Pais','REPUBLICA POPULAR DEMOCRATICA DE COREA','58','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (222	,'UAFROE','V1','Pais','DINAMARCA','59','212','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (223	,'UAFROE','V1','Pais','DJIBOUTI','60','208','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (224	,'UAFROE','V1','Pais','DOMINICA','61','216','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (225	,'UAFROE','V1','Pais','REPUBLICA DOMINICANA','62','220','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (226	,'UAFROE','V1','Pais','TIMOR ORIENTAL','63','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (227	,'UAFROE','V1','Pais','ECUADOR','64','228','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (228	,'UAFROE','V1','Pais','EGIPTO','65','232','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (229	,'UAFROE','V1','Pais','EL SALVADOR','66','748','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (230	,'UAFROE','V1','Pais','GUINEA ECUATORIAL','67','316','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (231	,'UAFROE','V1','Pais','ERITREA','68','962','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (232	,'UAFROE','V1','Pais','ESTONIA','69','958','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (233	,'UAFROE','V1','Pais','ETIOPIA','70','244','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (234	,'UAFROE','V1','Pais','ISLAS MALVINAS (FALKLAND)','71','400','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (235	,'UAFROE','V1','Pais','ISLAS FEROE','72','387','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (236	,'UAFROE','V1','Pais','REPUBLICA FEDERAL DE ALEMANIA','73','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (237	,'UAFROE','V1','Pais','FIJI','74','252','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (238	,'UAFROE','V1','Pais','FINLANDIA','75','248','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (239	,'UAFROE','V1','Pais','FRANCIA','76','268','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (240	,'UAFROE','V1','Pais','FRANCIA METROPOLITANA','77','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (241	,'UAFROE','V1','Pais','GUAYANA FRANCESA','78','968','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (242	,'UAFROE','V1','Pais','POLINESIA FRANCESA','79','654','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (243	,'UAFROE','V1','Pais','TERRITORIOS AUSTRALES FRANCESES','80','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (244	,'UAFROE','V1','Pais','GABON','81','272','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (245	,'UAFROE','V1','Pais','GAMBIA','82','304','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (246	,'UAFROE','V1','Pais','GEORGIA','83','928','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (247	,'UAFROE','V1','Pais','GHANA','84','292','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (248	,'UAFROE','V1','Pais','GIBRALTAR','85','310','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (249	,'UAFROE','V1','Pais','GRECIA','86','320','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (250	,'UAFROE','V1','Pais','GROENLANDIA','87','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (251	,'UAFROE','V1','Pais','GRANADA','88','924','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (252	,'UAFROE','V1','Pais','GUADALUPE','89','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (253	,'UAFROE','V1','Pais','GUAM','90','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (254	,'UAFROE','V1','Pais','GUATEMALA','91','324','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (255	,'UAFROE','V1','Pais','GUINEA','92','308','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (256	,'UAFROE','V1','Pais','GUINEA-BISSAU','93','964','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (257	,'UAFROE','V1','Pais','GUYANA','94','968','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (258	,'UAFROE','V1','Pais','HAITI','95','352','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (259	,'UAFROE','V1','Pais','ISLAS HEARD Y MCDONALD','96','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (260	,'UAFROE','V1','Pais','HONDURAS','97','348','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (261	,'UAFROE','V1','Pais','HONG KONG','98','340','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (262	,'UAFROE','V1','Pais','HUNGRIA','99','356','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (263	,'UAFROE','V1','Pais','ISLANDIA','100','392','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (264	,'UAFROE','V1','Pais','INDIA','101','376','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (265	,'UAFROE','V1','Pais','INDONESIA','102','360','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (266	,'UAFROE','V1','Pais','IRAN (REPUBLICA ISLAMICA DEL)','103','388','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (267	,'UAFROE','V1','Pais','IRAQ','104','384','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (268	,'UAFROE','V1','Pais','IRLANDA','105','364','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (269	,'UAFROE','V1','Pais','ISRAEL','106','368','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (270	,'UAFROE','V1','Pais','ITALIA','107','396','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (271	,'UAFROE','V1','Pais','JAMAICA','108','404','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (272	,'UAFROE','V1','Pais','JAPON','109','412','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (273	,'UAFROE','V1','Pais','JORDANIA','110','408','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (274	,'UAFROE','V1','Pais','KAZAJSTAN','111','932','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (275	,'UAFROE','V1','Pais','KENYA','112','416','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (276	,'UAFROE','V1','Pais','KIRIBATI','113','424','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (277	,'UAFROE','V1','Pais','KUWAIT','114','444','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (278	,'UAFROE','V1','Pais','KIRGUISTAN','115','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (279	,'UAFROE','V1','Pais','REPUBLICA DEMOCRATICA POPULAR LAO','116','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (280	,'UAFROE','V1','Pais','LETONIA','117','976','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (281	,'UAFROE','V1','Pais','LIBANO','118','456','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (282	,'UAFROE','V1','Pais','LESOTHO','119','476','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (283	,'UAFROE','V1','Pais','LIBERIA','120','472','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (284	,'UAFROE','V1','Pais','JAMAHIRIYA ARABE LIBIA','121','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (285	,'UAFROE','V1','Pais','LIECHTENSTEIN','122','464','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (286	,'UAFROE','V1','Pais','LITUANIA','123','980','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (287	,'UAFROE','V1','Pais','LUXEMBURGO','124','480','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (288	,'UAFROE','V1','Pais','MACAO','125','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (289	,'UAFROE','V1','Pais','MADAGASCAR','126','496','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (290	,'UAFROE','V1','Pais','MALAWI','127','544','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (291	,'UAFROE','V1','Pais','MALASIA','128','552','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (292	,'UAFROE','V1','Pais','MALDIVAS','129','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (293	,'UAFROE','V1','Pais','MALI','130','504','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (294	,'UAFROE','V1','Pais','MALTA','131','532','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (295	,'UAFROE','V1','Pais','ISLAS MARSHALL','132','500','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (296	,'UAFROE','V1','Pais','MARTINICA','133','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (297	,'UAFROE','V1','Pais','MAURITANIA','134','524','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (298	,'UAFROE','V1','Pais','MAURICIO','135','536','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (299	,'UAFROE','V1','Pais','MAYOTTE','136','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (300	,'UAFROE','V1','Pais','MEXICO','137','548','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (301	,'UAFROE','V1','Pais','MICRONESIA','138','260','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (302	,'UAFROE','V1','Pais','MOLDOVA','139','992','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (303	,'UAFROE','V1','Pais','MONACO','140','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (304	,'UAFROE','V1','Pais','MONGOLIA','141','508','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (305	,'UAFROE','V1','Pais','MONTSERRAT','142','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (306	,'UAFROE','V1','Pais','MARRUECOS','143','488','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (307	,'UAFROE','V1','Pais','MOZAMBIQUE','144','556','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (308	,'UAFROE','V1','Pais','MYANMAR(EX BIRMANIA)','145','988','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (309	,'UAFROE','V1','Pais','NAMIBIA','146','560','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (310	,'UAFROE','V1','Pais','NAURU','147','564','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (311	,'UAFROE','V1','Pais','NEPAL','148','592','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (312	,'UAFROE','V1','Pais','PAISES BAJOS','149','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (313	,'UAFROE','V1','Pais','ANTILLAS NEERLANDESAS','150','028','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (314	,'UAFROE','V1','Pais','NUEVA CALEDONIA','151','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (315	,'UAFROE','V1','Pais','NUEVA ZELANDA','152','608','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (316	,'UAFROE','V1','Pais','NICARAGUA','153','580','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (317	,'UAFROE','V1','Pais','NIGER','154','568','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (318	,'UAFROE','V1','Pais','NIGERIA','155','576','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (319	,'UAFROE','V1','Pais','ISLA NIUE','156','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (320	,'UAFROE','V1','Pais','ISLA NORFOLK','157','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (321	,'UAFROE','V1','Pais','ISLAS MARIANAS DEL NORTE','158','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (322	,'UAFROE','V1','Pais','NORUEGA','159','588','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (323	,'UAFROE','V1','Pais','OMAN','160','612','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (324	,'UAFROE','V1','Pais','PAKISTAN','161','636','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (325	,'UAFROE','V1','Pais','PALAU','162','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (326	,'UAFROE','V1','Pais','PANAMA','163','616','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (327	,'UAFROE','V1','Pais','PAPUA NUEVA GUINEA','164','628','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (328	,'UAFROE','V1','Pais','PARAGUAY','165','664','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (329	,'UAFROE','V1','Pais','PERU','166','620','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (330	,'UAFROE','V1','Pais','FILIPINAS','167','632','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (331	,'UAFROE','V1','Pais','ISLA PITCAIRN','168','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (332	,'UAFROE','V1','Pais','POLONIA','169','640','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (333	,'UAFROE','V1','Pais','PORTUGAL','170','656','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (334	,'UAFROE','V1','Pais','PUERTO RICO','171','652','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (335	,'UAFROE','V1','Pais','QATAR','172','672','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (336	,'UAFROE','V1','Pais','REPUBLICA DE COREA','173','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (337	,'UAFROE','V1','Pais','REUNION','174','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (338	,'UAFROE','V1','Pais','RUMANIA','175','680','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (339	,'UAFROE','V1','Pais','FEDERACION DE RUSIA','176','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (340	,'UAFROE','V1','Pais','RWANDA','177','684','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (341	,'UAFROE','V1','Pais','SAINT KITTS Y NEVIS','178','432','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (342	,'UAFROE','V1','Pais','SANTA LUCIA','179','994','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (343	,'UAFROE','V1','Pais','SAMOA','180','868','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (344	,'UAFROE','V1','Pais','SAN MARINO','181','990','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (345	,'UAFROE','V1','Pais','SANTO TOME Y PRINCIPE','182','740','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (346	,'UAFROE','V1','Pais','ARABIA SAUDITA/ARABIA SAUDI','183','688','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (347	,'UAFROE','V1','Pais','SENEGAL','184','728','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (348	,'UAFROE','V1','Pais','SEYCHELLES','185','696','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (349	,'UAFROE','V1','Pais','SIERRA LEONA','186','720','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (350	,'UAFROE','V1','Pais','SINGAPUR','187','708','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (351	,'UAFROE','V1','Pais','REPUBLICA ESLOVACA/ESLOVAQUIA','188','916','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (352	,'UAFROE','V1','Pais','ESLOVENIA','189','920','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (353	,'UAFROE','V1','Pais','ISLAS SALOMON','190','972','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (354	,'UAFROE','V1','Pais','SOMALIA','191','732','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (355	,'UAFROE','V1','Pais','SUDAFRICA','192','884','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (356	,'UAFROE','V1','Pais','GEORGIA DEL SUR E ISLAS SANDWICH DEL SUR','193','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (357	,'UAFROE','V1','Pais','ESPAÑA','194','240','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (358	,'UAFROE','V1','Pais','SRI LANKA','195','468','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (359	,'UAFROE','V1','Pais','SANTA ELENA','196','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (360	,'UAFROE','V1','Pais','SAN PEDRO Y MIQUELON','197','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (361	,'UAFROE','V1','Pais','SAN VICENTE Y LAS GRANADINAS','198','996','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (362	,'UAFROE','V1','Pais','SUDAN','199','700','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (363	,'UAFROE','V1','Pais','SURINAME','200','736','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (364	,'UAFROE','V1','Pais','ISLAS SVALBARD Y JAN MAYEN','201','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (365	,'UAFROE','V1','Pais','SWAZILANDIA','202','756','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (366	,'UAFROE','V1','Pais','SUECIA','203','704','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (367	,'UAFROE','V1','Pais','SUIZA','204','200','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (368	,'UAFROE','V1','Pais','REPUBLICA ARABE SIRIA','205','752','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (369	,'UAFROE','V1','Pais','TAIWAN','206','808','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (370	,'UAFROE','V1','Pais','TAYIKISTAN','207','940','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (371	,'UAFROE','V1','Pais','TAILANDIA','208','776','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (372	,'UAFROE','V1','Pais','TOGO','209','772','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (373	,'UAFROE','V1','Pais','TOKELAU','210','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (374	,'UAFROE','V1','Pais','TONGA','211','986','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (375	,'UAFROE','V1','Pais','TRINIDAD Y TABAGO','212','800','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (376	,'UAFROE','V1','Pais','TUNEZ','213','784','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (377	,'UAFROE','V1','Pais','TURQUIA','214','796','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (378	,'UAFROE','V1','Pais','TURKMENISTAN','215','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (379	,'UAFROE','V1','Pais','ISLAS TURCAS Y CAICOS','216','380','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (380	,'UAFROE','V1','Pais','TUVALU','217','814','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (381	,'UAFROE','V1','Pais','UGANDA','218','820','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (382	,'UAFROE','V1','Pais','UCRANIA','219','816','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (383	,'UAFROE','V1','Pais','EMIRATOS ARABES UNIDOS','220','008','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (384	,'UAFROE','V1','Pais','REINO UNIDO DE GRAN BRETAÑA E IRLANDA DEL NORTE','221','276','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (385	,'UAFROE','V1','Pais','REPUBLICA UNIDA DE TANZANIA','222','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (386	,'UAFROE','V1','Pais','ESTADOS UNIDOS DE AMERICA','223','828','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (387	,'UAFROE','V1','Pais','ISLAS PERIFERICAS MENORES DE LOS ESTADOS UNIDOS','224','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (388	,'UAFROE','V1','Pais','ISLAS VIRGENES DE LOS ESTADOS UNIDOS','225','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (389	,'UAFROE','V1','Pais','URUGUAY','226','832','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (390	,'UAFROE','V1','Pais','UZBEKISTAN','227','946','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (391	,'UAFROE','V1','Pais','VANUATU','228','860','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (392	,'UAFROE','V1','Pais','ESTADO DE LA CIUDAD DEL VATICANO (SANTA SEDE)','229','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (393	,'UAFROE','V1','Pais','VENEZUELA','230','844','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (394	,'UAFROE','V1','Pais','VIET NAM','231','856','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (395	,'UAFROE','V1','Pais','ISLAS WALLIS Y FUTUNA','232','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (396	,'UAFROE','V1','Pais','SAHARA OCCIDENTAL','233','','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (397	,'UAFROE','V1','Pais','YEMEN','234','872','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (398	,'UAFROE','V1','Pais','YUGOSLAVIA','235','880','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (399	,'UAFROE','V1','Pais','REPUBLICA DEMOCRATICA DEL CONGO EX ZAIRE','236','174','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (400	,'UAFROE','V1','Pais','ZAMBIA','237','888','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (401	,'UAFROE','V1','Pais','ZIMBABWE','238','896','ACT');


--REGIONES
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (402	,'UAFROE','V1','ClasificacionDivisionPais','TARAPACÁ	','1'	,'01','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (403	,'UAFROE','V1','ClasificacionDivisionPais','ANTOFAGASTA	','2'	,'02','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (404	,'UAFROE','V1','ClasificacionDivisionPais','ATACAMA	','3'	,'03','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (405	,'UAFROE','V1','ClasificacionDivisionPais','COQUIMBO	','4'	,'04','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (406	,'UAFROE','V1','ClasificacionDivisionPais','VALPARAÍSO	','5'	,'05','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (407	,'UAFROE','V1','ClasificacionDivisionPais','LIBERTADOR BERNANDO OHIGGINS	','6'	,'06','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (408	,'UAFROE','V1','ClasificacionDivisionPais','MAULE	','7'	,'07','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (409	,'UAFROE','V1','ClasificacionDivisionPais','BIO BÍO	','8'	,'08','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (410	,'UAFROE','V1','ClasificacionDivisionPais','LA ARAUCANÍA	','9'	,'09','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (411	,'UAFROE','V1','ClasificacionDivisionPais','LOS LAGOS	','10'	,'10','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (412	,'UAFROE','V1','ClasificacionDivisionPais','AYSÉN	','11'	,'11','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (413	,'UAFROE','V1','ClasificacionDivisionPais','MAGALLANES Y LA ANTÁRTICA CHILENA	','12'	,'12','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (414	,'UAFROE','V1','ClasificacionDivisionPais','METROPOLITANA DE SANTIAGO	','13'	,'13','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (415	,'UAFROE','V1','ClasificacionDivisionPais','DE LOS RÍOS	','14'	,'14','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (416	,'UAFROE','V1','ClasificacionDivisionPais','ARICA Y PARINACOTA	','15'	,'15','ACT');

/*
--TipoDocumentoIdentidad
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (417	,'UAFROE','V1','TipoDocumentoIdentidad','PASAPORTE','P'	,'1','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.Homologacion" ("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES (418	,'UAFROE','V1','TipoDocumentoIdentidad','RUT','R'	,'6','ACT');
*/

---COMUNAS (DivisionPais)

		
--SELECT * FROM 	"BSCL"."BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad"
						
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
