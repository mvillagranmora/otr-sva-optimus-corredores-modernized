SET SCHEMA "BSCL"; 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::PRO.AreaProveedor"; 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 1,'PROVEDORES DE MATERIAS PRIMAS E INSUMOS','ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 2,'PROVEEDORES DE MERCANCIAS O PRODUCTOS TERMINADOS PARA LA COMERCIALIZACION O DISTRIBUCION','ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 3,'PROVEEDORES DE MUEBLES Y ENSERES','ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 4,'PROVEEDORES DE BIENES DE CAPITAL','ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 5,'PROVEEDORES DE TECNOLOGIA DURA Y BLANDA','ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 6,'PROVEEDORES DE SERVICIOS PUBLICOS','ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 7,'PROVEEDORES DE SUMINISTROS INDUSTRIALES Y DE OFICINA','ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 8,'PROVEEDORES DE SERVICIOS GENERALES','ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 9,'PROVEEDORES DE HIGIENE Y SEGURIDAD INDUSTRIAL','ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 10,'PROVEEDORES DE CONSULTORIA, ASESORIA Y ASISTENCIA TECNICA','ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 11,'PROVEEDORES DE SERVICIOS DE TRANSPORTE Y ALMACENAMIENTO','ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 12,'PROVEEDORES DE SERVICIOS DE IMPORTACION Y EXPORTACION DE PRODUCTOS','ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::PRO.AreaProveedor"  ( "IdAreaProveedor","NombreArea","IdEstado","Borrado") VALUES ( 13,'PROVEEDORES DE OUTSOURCING, LEASING Y FACTORING','ACT','N'); 
 COMMIT; 
 END; 
 
 
