SET SCHEMA "BSCL"; 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"; 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 1,'JEFE DE CUSTODIA','N','JEFCUS',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 2,'PERSONAL ADMINISTRATIVO','N','PERADM',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 3,'TRADER','N','TRA',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 4,'TESORERO','N','TES',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 5,'AGENTE','N','AGE',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 6,'SUPERVISOR','N','SUP',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 7,'OPERADOR','N','OPEOTC',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 8,'ASESOR FINANCIERO','N','ASEFIN',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 9,'DIRECTOR','N','DIR',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 10,'GERENTE','N','GERENT',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 11,'GERENTE GENERAL','N','GERGEN',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 12,'GERENTE DE AREA O SUBGERENTE','N','GERSUB',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 13,'ADMINISTRADOR','N','ADMIN',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 14,'EMPLEADO','N','EMPLEA',1,'S'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 15,'JEFE MESA DE DINERO','N','JEFMES',1,'N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"  ( "IdTipoCargoEmpleado","NombreTipoCargoEmpleado","Borrado","CodigoTipoCargoEmpleado","IdEmpresa","UtilizaDetalle") VALUES ( 16,'OPERADOR MESA DE DINERO','N','OPEMES',1,'N'); 
 COMMIT; 
 END; 
 
 
