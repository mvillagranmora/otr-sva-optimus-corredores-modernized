SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM  "BSCL.SocioNegocio.BD::PRO.AreaProveedor"; 
 

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(1,'PROVEDORES DE MATERIAS PRIMAS E INSUMOS','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(2,'PROVEEDORES DE MERCANCÍAS O PRODUCTOS TERMINADOS PARA LA COMERCIALIZACIÓN O DISTRIBUCIÓN','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(3,'PROVEEDORES DE MUEBLES Y ENSERES','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(4,'PROVEEDORES DE BIENES DE CAPITAL','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(5,'PROVEEDORES DE TECNOLOGÍA DURA Y BLANDA','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(6,'PROVEEDORES DE SERVICIOS PÚBLICOS','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(7,'PROVEEDORES DE SUMINISTROS INDUSTRIALES Y DE OFICINA','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(8,'PROVEEDORES DE SERVICIOS GENERALES','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(9,'PROVEEDORES DE HIGIENE Y SEGURIDAD INDUSTRIAL','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(10,'PROVEEDORES DE CONSULTORÍA, ASESORÍA Y ASISTENCIA TÉCNICA','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(11,'PROVEEDORES DE SERVICIOS DE TRANSPORTE Y ALMACENAMIENTO','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(12,'PROVEEDORES DE SERVICIOS DE IMPORTACIÓN Y EXPORTACIÓN DE PRODUCTOS','ACT');
INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor" ("IdAreaProveedor","NombreArea","IdEstado") VALUES(13,'PROVEEDORES DE OUTSOURCING, LEASING Y FACTORING','ACT');


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
