SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Tables                                                */
/*==============================================================*/



--DELETE FROM "BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema"='UAFDOF';
DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='UAFDOF';
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema"='UAFDOF';
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='UAFDOF';


--COMPROBANTE DOF
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema"='UAFDOFCOMP';
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='UAFDOFCOMP';

--INFORME DOF
/*
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema"='UAFDOFINFO';
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='UAFDOFINFO';
*/


/*==============================================================*/
/* Insert Table: TipoDocumento                     	                */
/*==============================================================*/



INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion")
VALUES ('UAFDOF','Transacciones con Declaración Origen de Fondos','UAF','UAFDOF','EXPORTACIONUAFDOF');


INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion","UI_Emision")
VALUES ('UAFDOFCOMP','Comprobante Declaración Origen de Fondos','UAF','UAFDOFCOMP','','N');

/*
INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion","UI_Emision")
VALUES ('UAFDOFINFO','Informe Declaración Origen de Fondos','UAF','UAFDOFINFO','','N');
*/

/*==============================================================*/
/* Insert Table: Esquema                     	                */
/*==============================================================*/

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('UAFDOF', 'V1', 'NOR', 'M', '', '', 'BCS', '20160110', '20160101', '99990101', 1);

--INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
--VALUES ('UAFDOF', 'V2', 'NOR', 'M', '', '', 'BCS', '20170110', '20170101', '0001-01-01', 2);

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('UAFDOFCOMP', 'V1', 'NOR', 'M', '', '', 'BCS', '20160110', '20160101', '99990101', 1);
/*
INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('UAFDOFINFO', 'V1', 'NOR', 'M', '', '', 'BCS', '20160110', '20160101', '99990101', 1);
*/
/*==============================================================*/
/* Insert Table: Campo          	                            */
/*==============================================================*/

--DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='UAFDOF';
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdComprobanteFormaDePago','','',1,'Comprobante Forma De Pago','Ayuda de Comprobante Forma De Pago','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','FechaLiquidacion','','',2,'Fecha Liquidacion','Ayuda de Fecha Liquidacion','TEXT',10,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdComprobante','','',3,'Id Comprobante','Ayuda de Id Comprobante','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdTipoOperacion','','',4,'Id Tipo Operacion','Ayuda de IdTipoOperacion','TEXT',6,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','TipoOperacion','','',5,'Tipo Operacion','Ayuda de Tipo Operacion','TEXT',6,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdPersona','','',6,'Id Persona','Ayuda de Id Persona','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','Identificacion','','',7,'Identificacion','Ayuda de Identificacion','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','TipoDocumento','','',8,'TipoDocumento','Ayuda de Tipo Documento','TEXT',10,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','TipoPersona','','',9,'Tipo Persona','Ayuda de Tipo Persona','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);

insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','NombreCompleto','','',10,'Nombre Completo','Ayuda de Nombre','TEXT',400,'','N','N','N','S','N','N','N','N','',0,0);
/*
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','RazonSocial','','',11,'Razon Social','Ayuda de Razon Social','TEXT',100,'','N','N','N','N','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','ApellidoPaterno','','',12,'Apellido Paterno','Ayuda de Apellido Paterno','TEXT',100,'','N','N','N','N','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','ApellidoMaterno','','',13,'Apellido Materno','Ayuda de Apellido Materno','TEXT',100,'','N','N','N','N','S','N','N','N','',0,0);
*/

insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdPersonaTitularPago','','',14,'Id PersonaTitularPago','Ayuda de Id PersonaTitularPago','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','MontoOperacion','','',15,'Monto Operacion','Ayuda de Monto Operacion','QTY',0,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdMonedaOperacion','','',16,'Id MonedaOperacion','Ayuda de Id Moneda Operacion','TEXT',3,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','MontoComprobanteFormaDePago','','',17,'Monto ComprobanteFormaDePago','Ayuda de Monto Comprobante Forma De Pago','QTY',0,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdMonedaFormaDePago','','',18,'Id MonedaFormaDePago','Ayuda de Id Moneda Forma De Pago','TEXT',3,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdMedioDePago','','',19,'Id MedioDePago','Ayuda de Id Medio De Pago','TEXT',3,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','NombreMedioDePago','','',20,'Nombre MedioDePago','Ayuda de Nombre Medio De Pago','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdModalidadDePago','','',21,'Id ModalidadDePago','Ayuda de Id Modalidad De Pago','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','NombreModalidadDePago','','',22,'Nombre ModalidadDePago','Ayuda de Nombre Modalidad De Pago','TEXT',100,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdEmpleadoDigitador','','',23,'Id Empleado Digitador','Ayuda de Id Empleado Digitador','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','NombreAgente','','',24,'Nombre Agente','Ayuda de Nombre Agente','TEXT',345,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdSucursal','','',25,'Id Sucursal','Ayuda de Id Sucursal','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','CodigoSucursal','','',26,'Codigo Sucursal','Ayuda de Codigo Sucursal','TEXT',40,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','NombreSucursal','','',27,'Nombre Sucursal','Ayuda de Nombre Sucursal','TEXT',520,'','N','N','N','S','N','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdCliente','','',28,'Id Cliente','Ayuda de Id Cliente','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','IdDeclaracionOrigenFondos','','',29,'Id DeclaracionOrigenFondos','Ayuda de Id Declaracion Origen Fondos','ID',0,'','N','N','N','S','S','N','N','N','',0,0);
insert into "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version","Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo",  "Exportable",  "OcultoEnGrilla", "OcultoEnMantenedor",  "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES ('UAFDOF','V1','Comentario','','',30,'Comentario','Ayuda de Comentario','TEXT',500,'','N','N','N','S','N','N','N','N','',0,0);
															 						 						    		
												 						 						    		
									

						
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
