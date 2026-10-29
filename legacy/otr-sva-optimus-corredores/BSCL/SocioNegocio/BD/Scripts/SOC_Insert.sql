SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/

DELETE FROM  "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio"; 
DELETE FROM  "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"; 
DELETE FROM  "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"; 
/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" ( "IdTipoSocioDeNegocio","NombreTipoSocioDeNegocio" )  
VALUES('CLI','Cliente');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" ( "IdTipoSocioDeNegocio","NombreTipoSocioDeNegocio" )  
VALUES('PRO','Proveedor');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" ( "IdTipoSocioDeNegocio","NombreTipoSocioDeNegocio" )  
VALUES('EMP','Empleado');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" ( "IdTipoSocioDeNegocio","NombreTipoSocioDeNegocio" )  
VALUES('CNT','Contraparte');

INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" ( "IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo" )  
VALUES('BNOUSO','CLI','Bloqueo por no Uso');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" ( "IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo" )  
VALUES('BMUERT','CLI','Bloqueo por Muerte');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" ( "IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo" )  
VALUES('BFRAUD','CLI','Bloqueo por Fraude');

INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(1,'ACT','PERSONA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(2,'BLO','PERSONA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(3,'ACT','CLIENTE');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(4,'BLO','CLIENTE');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(5,'ACT','RELACION');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(6,'INA','RELACION');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(7,'ACT','EMPLEADO');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(8,'BLO','EMPLEADO');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(9,'ACT','DOCUMENTOIDENTIDAD');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(10,'VCD','DOCUMENTOIDENTIDAD');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(11,'ACT','CUENTABANCARIA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(12,'INA','CUENTABANCARIA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(13,'ACT','TELEFONO');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(14,'INA','TELEFONO');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(15,'ACT','DIRECCIONELECTRONICA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(16,'INA','DIRECCIONELECTRONICA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(17,'ACT','DIRECCIONFISICA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(18,'INA','DIRECCIONFISICA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(19,'ACT','TARJETABANCARIA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(20,'INA','TARJETABANCARIA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(21,'ACT','PROVEEDOR');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(22,'BLO','PROVEEDOR');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(23,'PEN','PERSONA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(24,'PEN','CLIENTE');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(25,'PEN','RELACION');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(26,'PEN','EMPLEADO');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(27,'PEN','DOCUMENTOIDENTIDAD');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(28,'PEN','CUENTABANCARIA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(29,'PEN','TELEFONO');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(30,'PEN','DIRECCIONELECTRONICA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(31,'PEN','DIRECCIONFISICA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(32,'PEN','TARJETABANCARIA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(33,'PEN','PROVEEDOR');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(34,'PEN','NOMBREFANTASIA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(35,'INA','NOMBREFANTASIA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(36,'ACT','NOMBREFANTASIA');

INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(37,'ACT','GRUPODIRECCIONELECTRONICA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(38,'INA','GRUPODIRECCIONELECTRONICA');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(39,'PEN','GRUPODIRECCIONELECTRONICA');

/*
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(37,'ACT','PORTAFOLIO');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(38,'BLO','PORTAFOLIO');
INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad" )  VALUES(39,'PEN','PORTAFOLIO');
*/
 
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
