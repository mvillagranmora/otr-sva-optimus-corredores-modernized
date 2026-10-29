SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM  "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado";


/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )  
VALUES(1,1,'JEFCUS','JEFE DE CUSTODIA','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,2,'PERADM','PERSONAL ADMINISTRATIVO','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,3,'TRA','TRADER','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,4,'TES','TESORERO','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )  
VALUES(1,5,'AGE','AGENTE','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )  
VALUES(1,6,'SUP','SUPERVISOR','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )   
VALUES(1,7,'OPEOTC','OPERADOR','N');--Operador otra corredora
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,8,'ASEFIN','ASESOR FINANCIERO','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,9,'DIR','DIRECTOR','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,10,'GERENT','GERENTE','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )   
VALUES(1,11,'GERGEN','GERENTE GENERAL','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,12,'GERSUB','GERENTE DE AREA O SUBGERENTE','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,13,'ADMIN','ADMINISTRADOR','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,14,'EMPLEA','EMPLEADO','S');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,15,'JEFMES','JEFE MESA DE DINERO','N');
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado","UtilizaDetalle" )    
VALUES(1,16,'OPEMES','OPERADOR MESA DE DINERO','N');
  


/* DESDE EL ORIGEN
INSERT INTO "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" ( "IdEmpresa","IdTipoCargoEmpleado","CodigoTipoCargoEmpleado","NombreTipoCargoEmpleado" )  
VALUES(1,16,'OPEMES','Operador de Mesa de Dinero');
*/





	

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
