SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM  "BSCL.SocioNegocio.BD::CLI.TipoSegmento"; 

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoSegmento" ( "IdSegmentoCliente","NombreSegmentoCliente","DescripcionSegmento","IdEmpresa","IdEstado" )  VALUES(1,'INSTITUCIONAL','Cliente Institucional',1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoSegmento" ( "IdSegmentoCliente","NombreSegmentoCliente","DescripcionSegmento","IdEmpresa","IdEstado" )  VALUES(2,'RETAIL','Cliente Retail',1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoSegmento" ( "IdSegmentoCliente","NombreSegmentoCliente","DescripcionSegmento","IdEmpresa","IdEstado" )  VALUES(3,'CARTERAADMINISTRADA','Cartera Administrada',1,'ACT');

-- INICIO DOF
DELETE FROM "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo";
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
	("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('HER','Herencia',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
	("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('ISL','Indemnización de seguro y laboral',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
	("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('JDA','Juegos de azar',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
	("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('JYD','Jubilaciones y desahucios',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
	("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('VBR','Venta bienes raíces',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
	("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('VDP','Venta de propiedades',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
	("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('VDV','Venta de vehículos',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOrigenFondo"
	("IdTipoOrigenFondo","Nombre","ValorDefecto","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('OTR','Otros',0,1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');

DELETE FROM "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos";
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
	("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('COMACN','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
	("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('COMCFI','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
	("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('COMMON','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
	("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('COMSIM','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
	("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('COMACX','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
	("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('APORFM','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
	("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('COMPAC','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoOperacionOrigenFondos" 
	("IdTipoOperacionOrigenFondos","Descripcion","IdUsuarioCreador","FechaIngreso","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado")
	VALUES('ABNMAN','',1,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,CURRENT_UTCTIMESTAMP,1,'ACT');
-- FIN DOF

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
