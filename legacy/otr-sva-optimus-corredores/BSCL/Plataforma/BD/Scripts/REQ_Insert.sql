SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM "BSCL.Plataforma.BD::REQ.CondicionOperativa";
DELETE FROM "BSCL.Plataforma.BD::REQ.EstadoCondicionOperativa"	;
DELETE FROM "BSCL.Plataforma.BD::REQ.TipoEtapa";
DELETE FROM "BSCL.Plataforma.BD::REQ.TipoComportamiento";
DELETE FROM "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario";
DELETE FROM "BSCL.Plataforma.BD::REQ.RequisitoOperativo";
DELETE FROM "BSCL.Plataforma.BD::REQ.ModeloRequisitoOperativo";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

/*
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('VIGDOCID','Vigencia Documento de Identidad','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('PEP','Persona Políticame Expuesta (PEP)','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('COMPLIANCE','Cliente en Lista Compliance','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('DOF','Requiere declaración origen de Fondos (DOF)','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('SALDONEG','Operar Saldo Negativo','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('NOMINAL','Opera Nominal','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
*/
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('PERNATFALLJURDIS','PERSONA NATURAL FALLECIDA/JURÍDICA DISUELTA','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('PORTBLOQ','PORTAFOLIO BLOQUEADO','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONTCONDGENFUT','CONTRATO CONDICIONES GENERALES FUTURO','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONTSIM','CONTRATO DE SIMULTANEAS','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONTVENCOR','CONTRATO DE VENTA CORTA','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONTCONDFORW','CONTRATO CONDICIONES DE FORWARD','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.CondicionOperativa" ("IdCondicionOperativa","Nombre","RequiereAtributo1","RequiereAtributo2","IdTipoComportamientoDefecto","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CONORDVERESC','CONFIRMACIÓN ORDEN VERBAL O ESCRITA','N','N','AUTORIZACION',1,NOW(),NOW(),1,'ACT');


INSERT INTO "BSCL.Plataforma.BD::REQ.EstadoCondicionOperativa" ("IdEstadoCondicionOperativa","IdEstado")
VALUES(1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.EstadoCondicionOperativa" ("IdEstadoCondicionOperativa","IdEstado")
VALUES(2,'INA');


/*
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESO','INGRESO',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ASINACION','ASIGNACIÓN',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ENVFACT','ENVÍO A FACTURAR',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('GCOMFACT','GENERACIÓN DE COMPROBANTE DE FACTURACIÓN',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('FACTDTE','FACTURACIÓN DTE',1,NOW(),NOW(),1,'ACT');
*/
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESOORDEN','INGRESO ORDEN',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESOORDENSIM','INGRESO ORDEN SIMULTANEAS',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESOORDENVC','INGRESO ORDEN VENTA CORTA',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('INGRESOFORWARD','INGRESO FORWARD',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ASIGNARORDEN','ASIGNAR ORDEN',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoEtapa" ("IdTipoEtapa","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('FACTURAR','FACTURAR',1,NOW(),NOW(),1,'ACT');


INSERT INTO "BSCL.Plataforma.BD::REQ.TipoComportamiento" ("IdTipoComportamiento","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ADVERTENCIA','ADVERTENCIA',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoComportamiento" ("IdTipoComportamiento","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('RECHAZO','RECHAZO',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoComportamiento" ("IdTipoComportamiento","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('AUTORIZACION','AUTORIZACION',1,NOW(),NOW(),1,'ACT');



INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ENROCLI','Enrolamiento Cliente',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('ENROPER','Enrolamiento Persona',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('NUEPORTF','Creación Portafolio',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('COMACN','Compra Acciones RV',1,NOW(),NOW(),1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REQ.TipoOperacionUsuario" ("IdTipoOperacionUsuario","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('VENACN','Venta Acciones RV',1,NOW(),NOW(),1,'ACT');


INSERT INTO "BSCL.Plataforma.BD::REQ.ModeloRequisitoOperativo"
("IdModeloRequisitoOperativo","Descripcion","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
VALUES('CORREDORA','MODELO DE CORREDORA',1,NOW(),NOW(),1,'ACT');


INSERT INTO "BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo","IdModeloRequisitoOperativo","IdCondicionOperativa","IdTipoEtapa",
"IdTipoComportamiento","IdTipoCanalIngreso","IdMoneda","ValorAtributo1","ValorAtributo2","IdUsuarioCreador","FechaCreacion",
"FechaModificacion","IdEmpresa","IdEstado")
SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo".NEXTVAL,'CORREDORA','PERNATFALLJURDIS','INGRESOORDEN',
'AUTORIZACION','0','','','',1,NOW(),NOW(),1,'ACT' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo","IdModeloRequisitoOperativo","IdCondicionOperativa","IdTipoEtapa",
"IdTipoComportamiento","IdTipoCanalIngreso","IdMoneda","ValorAtributo1","ValorAtributo2","IdUsuarioCreador","FechaCreacion",
"FechaModificacion","IdEmpresa","IdEstado")
SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo".NEXTVAL,'CORREDORA','PERNATFALLJURDIS','ASIGNARORDEN',
'AUTORIZACION','0','','','',1,NOW(),NOW(),1,'ACT' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo","IdModeloRequisitoOperativo","IdCondicionOperativa","IdTipoEtapa",
"IdTipoComportamiento","IdTipoCanalIngreso","IdMoneda","ValorAtributo1","ValorAtributo2","IdUsuarioCreador","FechaCreacion",
"FechaModificacion","IdEmpresa","IdEstado")
SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo".NEXTVAL,'CORREDORA','PERNATFALLJURDIS','FACTURAR',
'AUTORIZACION','0','','','',1,NOW(),NOW(),1,'ACT' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo","IdModeloRequisitoOperativo","IdCondicionOperativa","IdTipoEtapa",
"IdTipoComportamiento","IdTipoCanalIngreso","IdMoneda","ValorAtributo1","ValorAtributo2","IdUsuarioCreador","FechaCreacion",
"FechaModificacion","IdEmpresa","IdEstado")
SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo".NEXTVAL,'CORREDORA','PORTBLOQ','INGRESOORDEN',
'AUTORIZACION','0','','','',1,NOW(),NOW(),1,'ACT' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo","IdModeloRequisitoOperativo","IdCondicionOperativa","IdTipoEtapa",
"IdTipoComportamiento","IdTipoCanalIngreso","IdMoneda","ValorAtributo1","ValorAtributo2","IdUsuarioCreador","FechaCreacion",
"FechaModificacion","IdEmpresa","IdEstado")
SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo".NEXTVAL,'CORREDORA','CONTCONDGENFUT','INGRESOORDEN',
'AUTORIZACION','0','','','',1,NOW(),NOW(),1,'ACT' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo","IdModeloRequisitoOperativo","IdCondicionOperativa","IdTipoEtapa",
"IdTipoComportamiento","IdTipoCanalIngreso","IdMoneda","ValorAtributo1","ValorAtributo2","IdUsuarioCreador","FechaCreacion",
"FechaModificacion","IdEmpresa","IdEstado")
SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo".NEXTVAL,'CORREDORA','CONTCONDGENFUT','ASIGNARORDEN',
'AUTORIZACION','0','','','',1,NOW(),NOW(),1,'ACT' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo","IdModeloRequisitoOperativo","IdCondicionOperativa","IdTipoEtapa",
"IdTipoComportamiento","IdTipoCanalIngreso","IdMoneda","ValorAtributo1","ValorAtributo2","IdUsuarioCreador","FechaCreacion",
"FechaModificacion","IdEmpresa","IdEstado")
SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo".NEXTVAL,'CORREDORA','CONTSIM','INGRESOORDENSIM',
'AUTORIZACION','0','','','',1,NOW(),NOW(),1,'ACT' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo","IdModeloRequisitoOperativo","IdCondicionOperativa","IdTipoEtapa",
"IdTipoComportamiento","IdTipoCanalIngreso","IdMoneda","ValorAtributo1","ValorAtributo2","IdUsuarioCreador","FechaCreacion",
"FechaModificacion","IdEmpresa","IdEstado")
SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo".NEXTVAL,'CORREDORA','CONTVENCOR','INGRESOORDENVC',
'AUTORIZACION','0','','','',1,NOW(),NOW(),1,'ACT' FROM DUMMY;
INSERT INTO "BSCL.Plataforma.BD::REQ.RequisitoOperativo"
("IdRequisitoOperativo","IdModeloRequisitoOperativo","IdCondicionOperativa","IdTipoEtapa",
"IdTipoComportamiento","IdTipoCanalIngreso","IdMoneda","ValorAtributo1","ValorAtributo2","IdUsuarioCreador","FechaCreacion",
"FechaModificacion","IdEmpresa","IdEstado")
SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::IdRequisitoOperativo".NEXTVAL,'CORREDORA','CONTCONDFORW','INGRESOFORWARD',
'AUTORIZACION','0','','','',1,NOW(),NOW(),1,'ACT' FROM DUMMY;



/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
