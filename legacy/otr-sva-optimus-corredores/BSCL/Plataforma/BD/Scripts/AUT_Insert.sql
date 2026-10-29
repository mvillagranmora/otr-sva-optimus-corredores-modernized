SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
delete from "BSCL.Plataforma.BD::AUT.TipoEvento";
delete from "BSCL.Plataforma.BD::AUT.EstadoAutorizacion";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

/* Table: */
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('ORVTNO',1,'ORD','AUT_ORD_VTA_AC_NOM','Orden venta nominal',1,'S','INAC','VENACN',0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('ANUCAR',1,'CTA','AUT_ANU_CAR_CTA_INV','Anulación cargo en cuenta inversión',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('ANUABO',1,'CTA','AUT_ANU_CAR_CTA_INV','Anulación abono en cuenta inversión',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('ANUABO',1,'CTA','AUT_ANU_CAR_CTA_INV','Anulación abono en cuenta inversión',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('LIMSPT',1,'CTA','AUT_LIM_MES_SPOT','Límite Mesa Spot',1,'S','' ,'' ,0,0,'VIG');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODATR',1,'PLA','MOD_ATR','Modificación de Atributo',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento"
values('CIEMOD',1,'PLA','AUT_CIE_MOD','Cierre de Módulo',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('APEMOD',1,'PLA','AUT_APE_MOD','Apertura de Módulo',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('ENROLU',1,'PLA','AUT_USU_ENRO','Enrolamiento de Usuario',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL"."BSCL.Plataforma.BD::AUT.TipoEvento"("IdTipoEvento","IdEmpresa", "IdModulo", "Codigo", "Nombre", "HorasVigencia", "EtapasSecuenciales", "IdTipoProducto", "IdTipoOperacion", "IdUsuarioSuperAutorizador", "IdRolSuperAutorizador", "IdEstado") 
values('AROLUS',1,'PLA','AUT_USU_ROL','Agregar Rol Usuario',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento"("IdTipoEvento","IdEmpresa", "IdModulo", "Codigo", "Nombre", "HorasVigencia", "EtapasSecuenciales", "IdTipoProducto", "IdTipoOperacion", "IdUsuarioSuperAutorizador", "IdRolSuperAutorizador", "IdEstado") 
values('DROLUS',1,'PLA','AUT_DUSU_ROL','Eliminar Rol Usuario',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MCPCOB',1,'PLA','AUT_CPCOB_MOD','Modificación Cabecera Perfil Cobros',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MDPCOB',1,'PLA','AUT_DPCOB_MOD','Modificación Detalle Perfil Cobros',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODIND',1,'PLA','AUT_IND_MOD','Modificación de Indicador',1,'S','' ,'' ,0,0,'VIG');	


insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('ENROL',1,'SOC','AUT_CLI_ENRO','Enrolamiento de Cliente',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODPER',1,'SOC','AUT_PER_MOD','Modificación Datos Generales de la Persona',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODDOC',1,'SOC','AUT_DOC_MOD','Modificación Documento de Identidad',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUEDOC',1,'SOC','AUT_DOC_NUE','Nuevo Documento de Identidad',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUETEL',1,'SOC','AUT_TEL_NUE','Nuevo Teléfono',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODTEL',1,'SOC','AUT_TEL_MOD','Modificación de Teléfono',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUEDIF',1,'SOC','AUT_DIF_NUE','Nueva Dirección Fisica',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODDIF',1,'SOC','AUT_DIF_MOD','Modificación de Dirección Fisica',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUEDIE',1,'SOC','AUT_DIF_NUE','Nueva Dirección Electrónica',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODDIE',1,'SOC','AUT_DIF_MOD','Modificación de Dirección Electronica',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUECTB',1,'SOC','AUT_CTB_NUE','Nueva Cuenta Bancaria',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODCTB',1,'SOC','AUT_CTB_MOD','Modificación de Cuenta Bancaria',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODTJB',1,'SOC','AUT_TJB_MOD','Modificación de Tarjeta Bancaria',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUETJB',1,'SOC','AUT_TJB_NUE','Nueva Tarjeta Bancaria',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODCLI',1,'SOC','AUT_CLI_MOD','Modificación Datos Generales del Cliente',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CAECLI',1,'SOC','AUT_CLI_CAE','Cambio de Estado del Cliente',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CAEDOC',1,'SOC','AUT_DOC_CAE','Cambio de Estado Documento de Identidad',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODEMP',1,'SOC','AUT_EMP_MOD','Modificación Datos Generales del Empleado',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('ENROLE',1,'SOC','AUT_EMP_ENRO','Enrolamiento de Empleado',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('ENRCNT',1,'SOC','AUT_CNT_ENRO','Enrolamiento de Contraparte',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODCNT',1,'SOC','AUT_CNT_MOD','Modificación Datos Generales de la Contraparte',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CAEEMP',1,'SOC','AUT_EMP_CAE','Cambio de Estado del Empleado',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CAECNT',1,'SOC','AUT_CNT_CAE','Cambio de Estado de la Contraparte',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CAEPER',1,'SOC','AUT_PER_CAE','Cambio de Estado de la Persona',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CAECTB',1,'SOC','AUT_CTB_CAE','Cambio de Estado Cuenta Bancaria',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CAETJB',1,'SOC','AUT_TJB_CAE','Cambio de Estado Tarjeta Bancaria',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('ENRPRO',1,'SOC','AUT_PRO_ENRO','Enrolamiento de Proveedor',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODPRO',1,'SOC','AUT_PRO_MOD','Modificación Datos Generales del Proveedor',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CAEPRO',1,'SOC','AUT_PRO_CAE','Cambio de Estado Proveedor',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODDUP',1,'POR','AUT_POR_MOD','Modificación Datos Uso Portafolio',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUEDUP',1,'POR','AUT_POR_NUE','Creación Datos Uso Portafolio',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('ENRPER',1,'SOC','AUT_PER_ENRO','Enrolamiento de Persona',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CAEPOR',1,'SOC','AUT_POR_CAE','Cambio de Estado Portafolio',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUEPOR',1,'SOC','AUT_POR_NUE','Nuevo Portafolio',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODPOR',1,'SOC','AUT_POR_MOD','Modificación del Portafolio',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUGDIE',1,'SOC','AUT_GDI_NUE','Nuevo Grupo de Direcciones',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MOGDIE',1,'SOC','AUT_GDI_MOD','Modificación de Grupo Direcciones',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NUEREL',1,'SOC','AUT_REL_NUE','Nueva Relación de Personas',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODREL',1,'SOC','AUT_REL_NUE','Modificación Relación de Personas',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NAUTOR',1,'SOC','AUT_AUT_NUE','Nuevo Autorizado a Dar Ordenes',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CAEAUT',1,'SOC','AUT_AUT_CAE','Cambio de Estado Autorizado a Dar Ordenes',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NDPCOB',1,'PLA','AUT_DPCOB_NUE','Nuevo Detalle Perfil Cobros',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NCPCOB',1,'PLA','AUT_DPCOB_NUE','Nuevo Perfil Cobros',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NDPPRO',1,'PLA','AUT_DPPRO_NUE','Nuevo Detalle Perfil Productos',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NCPPRO',1,'PLA','AUT_CPPRO_NUE','Nuevo Perfil Productos',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MDPPRO',1,'PLA','AUT_DPPRO_MOD','Modificación Detalle Perfil Productos',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MCPPRO',1,'PLA','AUT_CPPRO_MOD','Modificación Perfil Productos',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NCPPAG',1,'PLA','AUT_CPPAG_NUE','Nueva Preferencia de Pago',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MCPPAG',1,'PLA','AUT_CPPAG_MOD','Modificación Preferencia de Pago',1,'S','' ,'' ,0,0,'VIG');	
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MODFAT',1,'SOC','AUT_FAT_MOD','Modificación Datos FATCA de la Persona',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('NCPLIM',1,'PLA','AUT_CPLIM_NUE','Nueva Plantilla Límites',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('MCPLIM',1,'PLA','AUT_CPLIM_MOD','Modificación Plantilla Límites',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('NDPLIM',1,'PLA','AUT_DPLIM_NUE','Nuevo Detalle Plantilla Límites',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('MDPLIM',1,'PLA','AUT_DPLIM_MOD','Modificación Detalle Plantilla Límites',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('NUELIM',1,'PLA','AUT_LIM_NUE','Nuevo Límite',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('MODLIM',1,'PLA','AUT_LIM_MOD','Modificación Límite',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('NUEDOF',1,'SOC','AUT_DOF_NUE','Creación Declaración Origen de Fondos',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento" 
VALUES('MODDOF',1,'SOC','AUT_DOF_MOD','Modificación Declaración Origen de Fondos',1,'S','' ,'' ,0,0,'VIG');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LIPPRO',1,'PLA','AUT_LIM_LIPPRO','Evaluación Limite Producto Portafolio',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LIPSGR',1,'PLA','AUT_LIM_LIPSGR','Evaluación Limite Sobregiro',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('NLICTP',1,'PLA','AUT_NLICTP_NUE','Nuevo Limite Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('MLICTP',1,'PLA','AUT_LICTP_MOD','MODIFICACIÓN LIMITE CUENTA PROPIA',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('NELCTP',1,'PLA','AUT_NELCTP_NUE','Agregar Emisor Limite Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('MELCTP',1,'PLA','AUT_MELCTP_MOD','Modificar Emisor Limite Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LIEMCP',1,'PLA','AUT_LIM_LIEMCP','Evaluación Limite Emisor Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LICRCP',1,'PLA','AUT_LIM_LICRCP','Evaluación Limite Clas. Riesgo Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LIMTCP',1,'PLA','AUT_LIM_LIMTCP','Evaluación Limite Margen de Tasa Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LIPLCP',1,'PLA','AUT_LIM_LIPLCP','Evaluación Limite Plazo Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('NRLCTP',1,'PLA','AUT_NRLCTP_NUE','Agregar Rango Limite Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('MRLCTP',1,'PLA','AUT_MRLCTP_MOD','Modificar Rango Limite Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('COLICP',1,'PLA','AUT_LIM_COLICP','COPIAR LÍMITE CUENTA PROPIA',1,'S','' ,'' ,0,0,'VIG');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LISICP',1,'PLA','AUT_LIM_LISICP','Evaluación Limite Posición Interbancaria Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LISDCP',1,'PLA','AUT_LIM_LISDCP','Evaluación Limite Mesa Distribución Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LIFDCP',1,'PLA','AUT_LIM_LIFDCP','Evaluación Limite Forward Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LICACP',1,'PLA','AUT_LIM_LICACP','Evaluación Limite Cartera Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('LIAGCP',1,'PLA','AUT_LIM_LIAGCP','Evaluación Limite Agente Cuenta Propia',1,'S','' ,'' ,0,0,'VIG');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('NGELCP',1,'PLA','AUT_LIM_NGELCP','NUEVO GRUPO EMISOR LÍMITE CUENTA PROPIA',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('MGELCP',1,'PLA','AUT_LIM_MGELCP','MODIFICACIÓN GRUPO EMISOR LÍMITE CUENTA PROPIA',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('NEDLCP',1,'PLA','AUT_LIM_NEDLCP','NUEVO GRUPO EMISOR DETALLE LÍMITE CUENTA PROPIA',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" ("IdTipoEvento","IdEmpresa", "IdModulo","Codigo","Nombre","HorasVigencia","EtapasSecuenciales","IdTipoProducto","IdTipoOperacion","IdUsuarioSuperAutorizador","IdRolSuperAutorizador","IdEstado")
values('MEDLCP',1,'PLA','AUT_LIM_MEDLCP','MODIFICACIÓN GRUPO EMISOR DETALLE LÍMITE CUENTA PROPIA',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('BORTEL',1,'PLA','AUT_TEL_BOR','BORRADO TELEFONO',1,'S','' ,'' ,0,0,'VIG','');



insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('NPCCUS',1,'SOC','AUT_POR_CTA_CUS_NUE','NUEVA CUENTA DE CUSTODIO PORTAFOLIO',1,'S','' ,'' ,0,0,'VIG');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MPCCUS',1,'SOC','AUT_POR_CTA_CUS_MOD','MODIFICACION CUENTA DE CUSTODIO PORTAFOLIO',1,'S','' ,'' ,0,0,'VIG');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('BORDOC',1,'SOC','AUT_DOC_BOR','BORRADO DOCUMENTO DE IDENTIDAD',1,'S','' ,'' ,0,0,'VIG','');




INSERT INTO "BSCL.Plataforma.BD::AUT.EstadoAutorizacion" ( "IdEstadoAutorizacion","IdEstado","NombreEntidad" )  
VALUES(1,'VIG','AUT.Evento');
INSERT INTO "BSCL.Plataforma.BD::AUT.EstadoAutorizacion" ( "IdEstadoAutorizacion","IdEstado","NombreEntidad" )  
VALUES(2,'RZD','AUT.Evento');
INSERT INTO "BSCL.Plataforma.BD::AUT.EstadoAutorizacion" ( "IdEstadoAutorizacion","IdEstado","NombreEntidad" )  
VALUES(3,'APD','AUT.Evento');
INSERT INTO "BSCL.Plataforma.BD::AUT.EstadoAutorizacion" ( "IdEstadoAutorizacion","IdEstado","NombreEntidad" )  
VALUES(4,'VCD','AUT.Evento');
INSERT INTO "BSCL.Plataforma.BD::AUT.EstadoAutorizacion" ( "IdEstadoAutorizacion","IdEstado","NombreEntidad" )  
VALUES(5,'VIG','AUT.Etapa');
INSERT INTO "BSCL.Plataforma.BD::AUT.EstadoAutorizacion" ( "IdEstadoAutorizacion","IdEstado","NombreEntidad" )  
VALUES(6,'VIG','AUT.AutorizadoEtapa');

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
