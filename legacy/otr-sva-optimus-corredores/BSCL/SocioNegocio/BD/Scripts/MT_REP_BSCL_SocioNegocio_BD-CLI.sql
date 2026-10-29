SET SCHEMA "BSCL"; 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::CLI.TipoCliente"; 
   INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoCliente"  ( "IdTipoCliente","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado") VALUES ( 'FAMILYOFFICE','FAMILY OFFICE',1,'2016-04-07 17:44:50','2016-04-07 17:44:50',1,'ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoCliente"  ( "IdTipoCliente","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado") VALUES ( 'RETAIL','RETAIL',1,'2016-04-07 17:44:51','2016-04-07 17:44:51',1,'ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoCliente"  ( "IdTipoCliente","Nombre","IdUsuarioCreador","FechaCreacion","FechaModificacion","IdEmpresa","IdEstado") VALUES ( 'INSTITUCIONAL','INSTITUCIONAL',1,'2016-04-07 17:44:51','2016-04-07 17:44:51',1,'ACT'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::CLI.TipoSegmento"; 
   INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoSegmento"  ( "IdSegmentoCliente","NombreSegmentoCliente","DescripcionSegmento","IdEmpresa","IdEstado","Borrado") VALUES ( 1,'INSTITUCIONAL','CLIENTE INSTITUCIONAL',1,'ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoSegmento"  ( "IdSegmentoCliente","NombreSegmentoCliente","DescripcionSegmento","IdEmpresa","IdEstado","Borrado") VALUES ( 2,'RETAIL','CLIENTE RETAIL',1,'ACT','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::CLI.TipoSegmento"  ( "IdSegmentoCliente","NombreSegmentoCliente","DescripcionSegmento","IdEmpresa","IdEstado","Borrado") VALUES ( 3,'CARTERAADMINISTRADA','CARTERA ADMINISTRADA',1,'ACT','N'); 
 COMMIT; 
 END; 
 
 
