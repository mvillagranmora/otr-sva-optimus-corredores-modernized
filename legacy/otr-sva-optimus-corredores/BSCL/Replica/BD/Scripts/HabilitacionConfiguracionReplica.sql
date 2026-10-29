--- SELECT  *  FROM  BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE" where "ROUTE_NAME" =  'Replica_ORDEN'
-- update BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"  set ENABLED = '1' where "ROUTE_NAME" =  'Replica_ORDEN'
-- 

-- DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE" ;
-- DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM";

-- UPDATE BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE" SET ROUTE_INSTANCE = 'BrokerAx' where   ROUTE_NAME IN ('Replica_TBCTCURV' , 'Replica_TBCTCUFM' ,'Replica_TBCTCURF' ,'Replica_TBCTCIRV' ,'Replica_TBCTCIIF' ,'Replica_TBCTCIRF' )
--  UPDATE BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE" SET ROUTE_INSTANCE = 'BrokerA' where   ROUTE_NAME IN (  'Replica_TBCTCUFM' ,'Replica_TBCTCIRV' ,'Replica_TBCTCURF' ,'Replica_TBCTCIRV' ,'Replica_TBCTCIIF' ,'Replica_TBCTCIRF' ,'Replica_TBCTCTCL', 'Replica_TBCTCURV' )

---SELECT  *  FROM  BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"  WHERE ROUTE_NAME IN ('Replica_TBCTCURV' , 'Replica_TBCTCUFM' ,'Replica_TBCTCURF' ,'Replica_TBCTCIRV' ,'Replica_TBCTCIIF' ,'Replica_TBCTCIRF' )
--SELECT  *  FROM  BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"  WHERE ROUTE_NAME IN ('Replica_TBCTCURV' , 'Replica_TBCTCUFM' ,'Replica_TBCTCURF' ,'Replica_TBCTCIRV' ,'Replica_TBCTCIIF' ,'Replica_TBCTCIRF' )





DROP TABLE BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_GENERALPROPERTIES";    

CREATE TABLE BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_GENERALPROPERTIES" (
	PROPERTY_KEY VARCHAR(200) NOT NULL,
	PROPERTY_VALUE VARCHAR(600),
	CONSTRAINT GENERAL_PROPERTIES_PK PRIMARY KEY (PROPERTY_KEY)
);

 

DROP TABLE BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE";   

CREATE TABLE BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE" (
	DYNAMIC_ROUTE_ID INTEGER NOT NULL,
	ROUTE_INSTANCE VARCHAR(100),
	ROUTE_NAME VARCHAR(100),
	DESCRIPTION VARCHAR(1000),
	ROUTE_FROM VARCHAR(1000),
	ROUTE_TO VARCHAR(1000),
	ERROR_HANDLER_NAME VARCHAR(100),
	ENABLED INTEGER DEFAULT 0,
	CONSTRAINT "_BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE__PK" PRIMARY KEY (DYNAMIC_ROUTE_ID)
);


DROP TABLE BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM";
 
CREATE TABLE BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (
	PARAM_ID INTEGER NOT NULL,
	PARAM_NAME VARCHAR(150) NOT NULL,
	PARAM_TYPE VARCHAR(20),
	PARAM_VALUE VARCHAR(3000),
	DESCRIPTION VARCHAR(3000),
	CONFIG_GROUP VARCHAR(150),
	CONSTRAINT "_BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM__PK " PRIMARY KEY (PARAM_ID)
);

 
CREATE UNIQUE INDEX VIF_CONFIG_PARAMS_IX1 ON BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (CONFIG_GROUP,PARAM_NAME);



DROP SEQUENCE "BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE" ;

 
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE"       	
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1;


DROP SEQUENCE "BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM" ;

 

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM"       	
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1;

        	
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_GENERALPROPERTIES" ;

 

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_GENERALPROPERTIES"       	
		START     WITH 1
       	INCREMENT BY 1
       	MINVALUE  1;

   


-- SELECT  *  FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
-- SELECT  *  FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"

DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM";  
delete FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE";  

-- TABLAS AUXILIARES 
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'ReplicaTablasAuxiliares', 'Replica de tablas auxiliares', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); 

 


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'BANCO;COMUNA;INDICADOR;REGION;DETALLE_INDICADOR', 'Tablas a replicar', 'ReplicaTablasAuxiliares');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'ReplicaTablasAuxiliares');   


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'ReplicaTablasAuxiliares');   

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'ban_id;com_id;ind_id;reg_id;rtrim(ltrim(din_id)) +'':''+ cast(ind_id as varchar)', 'llaves', 'ReplicaTablasAuxiliares');   

--- FIN TABLAS AUXILIARES


-- TABLAS ESTATICAS 
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'ReplicaTablasEstaticas', 'Replica de tablas estaticas', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);   


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'PARAMETRO;TBPLTMOV', 'Tablas a replicar', 'ReplicaTablasEstaticas');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'ReplicaTablasEstaticas');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'ReplicaTablasEstaticas');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'par_id;ltrim(rtrim(TIPO_REG))+'':''+rtrim(ltrim(CODI))', 'llaves', 'ReplicaTablasEstaticas');  

--- FIN TABLAS ESTATICAS


--  TABLAS Criticidad media

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'ReplicaTablasCriticidadMedia_1', 'Replica de tablas criticidad media', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'PERSONA;CUENTA_BANCARIA;DIRECCION;RELACIONES;TELEFONO;TBPLOPER', 'Tablas a replicar', 'ReplicaTablasCriticidadMedia_1');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'ReplicaTablasCriticidadMedia_1');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'ReplicaTablasCriticidadMedia_1');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'per_id;cba_id;dir_id;cast(per_id_per as varchar) +'':''+  cast(per_id_rel as varchar) +'':''+ par_tre_id;tel_id;COD_OPERARIO', 'llaves', 'ReplicaTablasCriticidadMedia_1');  




--  TABLAS Criticidad media 2
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'ReplicaTablasCriticidadMedia_2', 'Replica de tablas criticidad media', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'CUENTA;TBPLFICL;INDICADORES_CLIENTE;USO_DIRECCIONES;INDICADORES_CUENTA', 'Tablas a replicar', 'ReplicaTablasCriticidadMedia_2');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'ReplicaTablasCriticidadMedia_2');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'ReplicaTablasCriticidadMedia_2');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'cast(per_id as varchar)+'':''+cast(cuenta as varchar);rtrim(ltrim(rut_cli))+'':''+rtrim(ltrim(cast(sec_rut_cli as varchar)));cast(per_id as varchar)+'':''+cast(ind_id as varchar);cast(per_id as varchar)+'':''+cast(CUENTA as varchar)+'':''+cast(dir_id as varchar);cast(per_id as varchar) +'':''+cast(CUENTA as varchar) +'':''+cast(ind_id as varchar)', 'llaves', 'ReplicaTablasCriticidadMedia_2');  




--  TABLAS Criticidad alta unitarias
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTCUFM', 'Replica TBCTCUFM', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTCUFM', 'Tablas a replicar', 'Replica_TBCTCUFM');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTCUFM');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTCUFM');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'COD_PER +'':''+rtrim(ltrim(RUT_CLI))+'':''+ rtrim(ltrim(cast(SEC_RUT_CLI as varchar)))+'':''+ rtrim(ltrim(NEMO))+'':''+ COD_BOVEDA', 'llaves', 'Replica_TBCTCUFM');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTCURF', 'Replica TBCTCURF', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTCURF', 'Tablas a replicar', 'Replica_TBCTCURF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTCURF');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTCURF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'COD_PER +'':''+rtrim(ltrim(RUT_CLI))+'':''+ rtrim(ltrim(cast(SEC_RUT_CLI as varchar)))+'':''+ rtrim(ltrim(NEMO))+'':''+ COD_BOVEDA', 'llaves', 'Replica_TBCTCURF');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTCURV', 'Replica TBCTCURV', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTCURV', 'Tablas a replicar', 'Replica_TBCTCURV');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTCURV');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTCURV');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'COD_PER +'':''+rtrim(ltrim(RUT_CLI))+'':''+ rtrim(ltrim(cast(SEC_RUT_CLI as varchar)))+'':''+ rtrim(ltrim(NEMO))+'':''+ COD_BOVEDA', 'llaves', 'Replica_TBCTCURV');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTCTCL', 'Replica TBCTCTCL', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTCTCL', 'Tablas a replicar', 'Replica_TBCTCTCL');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTCTCL');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTCTCL');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'COD_PER +'':''+rtrim(ltrim(RUT_CLI))+'':''+ rtrim(ltrim(cast(SEC_RUT_CLI as varchar)))+'':''+ rtrim(ltrim(CTA_CLI))', 'llaves', 'Replica_TBCTCTCL');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTCIIF', 'Replica TBCTCIIF', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTCIIF', 'Tablas a replicar', 'Replica_TBCTCIIF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTCIIF');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTCIIF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'COD_PER+'':''+ cast(SEC_CAR_INVER as varchar)  +'':''+ rtrim(ltrim(RUT_CLI)) +'':''+ rtrim(ltrim(SEC_RUT_CLI)) +'':''+rtrim(ltrim(NEMO))', 'llaves', 'Replica_TBCTCIIF');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTCIRF', 'Replica TBCTCIRF', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTCIRF', 'Tablas a replicar', 'Replica_TBCTCIRF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTCIRF');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTCIRF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'COD_PER+'':''+ cast(SEC_CAR_INVER as varchar)  +'':''+ rtrim(ltrim(RUT_CLI)) +'':''+ rtrim(ltrim(SEC_RUT_CLI)) +'':''+rtrim(ltrim(NEMO))', 'llaves', 'Replica_TBCTCIRF');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTCIRV', 'Replica TBCTCIRV', 'timer://DynamicRutes?fixedRate=true&period=15000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTCIRV', 'Tablas a replicar', 'Replica_TBCTCIRV');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTCIRV');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTCIRV');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'cast(SEC_CAR_INVER as varchar)', 'llaves', 'Replica_TBCTCIRV');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTTRTP', 'Replica TBCTTRTP', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTTRTP', 'Tablas a replicar', 'Replica_TBCTTRTP');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTTRTP');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTTRTP');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'COD_PER+'':''+ FEC_VCTO_TP+'':''+ rtrim(ltrim(NEMO))+'':''+ rtrim(ltrim(RUT_CLI))+'':''+ rtrim(ltrim(SEC_RUT_CLI))+'':''+ cast(SEC_MOVTO as varchar)+'':''+ rtrim(ltrim( MERCADO))', 'llaves', 'Replica_TBCTTRTP');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCPDTPC', 'Replica TBCPDTPC', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCPDTPC', 'Tablas a replicar', 'Replica_TBCPDTPC');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCPDTPC');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCPDTPC');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'COD_PER+'':''+   cast(SEC_COMPROM as varchar)+'':''+   cast(LIN_COMPROM as varchar)', 'llaves', 'Replica_TBCPDTPC');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCPMSPC', 'Replica TBCPMSPC', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCPMSPC', 'Tablas a replicar', 'Replica_TBCPMSPC');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCPMSPC');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCPMSPC');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'COD_PER+'':''+   cast(SEC_COMPROM as varchar)', 'llaves', 'Replica_TBCPMSPC');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBPLTBRF', 'Replica TBPLTBRF', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBPLTBRF', 'Tablas a replicar', 'Replica_TBPLTBRF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBPLTBRF');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBPLTBRF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'FEC_TRANS+'':''+ rtrim(ltrim(FOLIO_TRANS))+'':''+ IND_BOLSA+'':''+ TIPO_OPERAC', 'llaves', 'Replica_TBPLTBRF');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBPLTBIF', 'Replica TBPLTBIF', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBPLTBIF', 'Tablas a replicar', 'Replica_TBPLTBIF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBPLTBIF');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBPLTBIF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'FEC_TRANS+'':''+ rtrim(ltrim(FOLIO_TRANS))+'':''+ IND_BOLSA+'':''+ TIPO_OPERAC', 'llaves', 'Replica_TBPLTBIF');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBPLTBRV', 'Replica TBPLTBRV', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBPLTBRV', 'Tablas a replicar', 'Replica_TBPLTBRV');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBPLTBRV');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBPLTBRV');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'FEC_TRANS+'':''+ rtrim(ltrim(FOLIO_TRANS))+'':''+ IND_BOLSA+'':''+ TIPO_OPERAC', 'llaves', 'Replica_TBPLTBRV');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTDTCF', 'Replica TBCTDTCF', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTDTCF', 'Tablas a replicar', 'Replica_TBCTDTCF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTDTCF');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTDTCF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'TIPO_COMP_FAC+'':''+  cast(FOLIO_COMP_FAC as varchar)+'':''+  cast(LIN_COMP_FAC as varchar)+'':''+  FEC_EMI', 'llaves', 'Replica_TBCTDTCF');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTMSCF', 'Replica TBCTMSCF', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTMSCF', 'Tablas a replicar', 'Replica_TBCTMSCF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTMSCF');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTMSCF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'TIPO_COMP_FAC+'':''+  cast(FOLIO_COMP_FAC as varchar)+'':''+  FEC_ING_COMP', 'llaves', 'Replica_TBCTMSCF');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTMVTO', 'Replica TBCTMVTO', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTMVTO', 'Tablas a replicar', 'Replica_TBCTMVTO');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTMVTO');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTMVTO');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'FEC_MVTO+'':''+   cast(SEC_MOVTO as varchar)', 'llaves', 'Replica_TBCTMVTO');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_ASIGNACION_RV', 'Replica ASIGNACION_RV', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'ASIGNACION_RV', 'Tablas a replicar', 'Replica_ASIGNACION_RV');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_ASIGNACION_RV');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_ASIGNACION_RV');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'cast(SEC_ASIGNACION as varchar)', 'llaves', 'Replica_ASIGNACION_RV');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_ASIGNACION_IF', 'Replica ASIGNACION_IF', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'ASIGNACION_IF', 'Tablas a replicar', 'Replica_ASIGNACION_IF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_ASIGNACION_IF');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_ASIGNACION_IF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'cast(SEC_ASIGNACION as varchar)', 'llaves', 'Replica_ASIGNACION_IF');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_ASIGNACION_RF', 'Replica ASIGNACION_RF', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'ASIGNACION_RF', 'Tablas a replicar', 'Replica_ASIGNACION_RF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_ASIGNACION_RF');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_ASIGNACION_RF');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'cast(SEC_ASIGNACION as varchar)', 'llaves', 'Replica_ASIGNACION_RF');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SOLICITUD', 'Replica SOLICITUD', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SOLICITUD', 'Tablas a replicar', 'Replica_SOLICITUD');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SOLICITUD');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SOLICITUD');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'cast(sol_sec as varchar)', 'llaves', 'Replica_SOLICITUD');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_ORDEN', 'Replica ORDEN', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'ORDEN', 'Tablas a replicar', 'Replica_ORDEN');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_ORDEN');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_ORDEN');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'cast(sol_sec as varchar)', 'llaves', 'Replica_ORDEN');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_OPERACION_RV', 'Replica OPERACION_RV', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'OPERACION_RV', 'Tablas a replicar', 'Replica_OPERACION_RV');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_OPERACION_RV');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_OPERACION_RV');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'cast(sol_sec as varchar)', 'llaves', 'Replica_OPERACION_RV');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_FMMOVTOSFORWARD', 'Replica FMMOVTOSFORWARD', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'FMMOVTOSFORWARD', 'Tablas a replicar', 'Replica_FMMOVTOSFORWARD');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_FMMOVTOSFORWARD');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_FMMOVTOSFORWARD');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'cast(FolioTransaccion as varchar)', 'llaves', 'Replica_FMMOVTOSFORWARD');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTGTIA', 'Replica TBCTGTIA', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTGTIA', 'Tablas a replicar', 'Replica_TBCTGTIA');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTGTIA');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTGTIA');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'COD_PER+'':''+ rtrim(ltrim(RUT_CLI))+'':''+ rtrim(ltrim(SEC_RUT_CLI))+'':''+ MERCADO+'':''+ rtrim(ltrim(NEMO))+'':''+ COD_BOVEDA+'':''+ cast(SEC_CAR_INVERas varchar)+'':''+ CONCEPTO_MVTO', 'llaves', 'Replica_TBCTGTIA');  



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTTRAS', 'Replica TBCTTRAS', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTTRAS', 'Tablas a replicar', 'Replica_TBCTTRAS');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTTRAS');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTTRAS');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'MERCADO+'':''+ rtrim(ltrim(NEMO))+'':''+ cast(SEC_TRASPASO as varchar)', 'llaves', 'Replica_TBCTTRAS');  




INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_TBCTTRCO', 'Replica TBCTTRCO', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'TBCTTRCO', 'Tablas a replicar', 'Replica_TBCTTRCO');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_TBCTTRCO');  


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_TBCTTRCO');  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM"
(PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'COD_PER+'':''+  FEC_VCTO_TP+'':''+  rtrim(ltrim(NEMO))+'':''+ rtrim(ltrim(RUT_CLI))+'':''+  rtrim(ltrim(SEC_RUT_CLI))+'':''+ cast(SEC_MOVTO as varchar)+'':''+  cast(FOLIO_PRESTAMO as varchar)+'':''+  cast(Sec_Folio as varchar)+'':''+  rtrim(ltrim(Tipo_Rel))', 'llaves', 'Replica_TBCTTRCO');  


--  VISTAS Optimus
--Cabecera APV_APV

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_APV_APV', 'Replica APV_APV', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO

--Detalle APV_APV
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'APV_APV', 'Tablas a replicar', 'Replica_APV_APV'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_APV_APV'); GO  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_APV_APV'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdMatrizAgrupadorAPV', 'llaves', 'Replica_APV_APV'); GO


--Cabecera APV_Contratos

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_APV_Contratos', 'Replica APV_Contratos', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO

--Detalle APV_Contratos
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'APV_Contratos', 'Tablas a replicar', 'Replica_APV_Contratos'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_APV_Contratos'); GO  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_APV_Contratos'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdContratoSAAPV', 'llaves', 'Replica_APV_Contratos'); GO

--Cabecera CuentaInversion_MovimientosCuentaInversion

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_CuentaInversion_MovimientosCuentaInversion', 'Replica CuentaInversion_MovimientosCuentaInversion', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO

--Detalle CuentaInversion_MovimientosCuentaInversion
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'CuentaInversion_MovimientosCuentaInversion', 'Tablas a replicar', 'Replica_CuentaInversion_MovimientosCuentaInversion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_CuentaInversion_MovimientosCuentaInversion'); GO  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_CuentaInversion_MovimientosCuentaInversion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCargoAbonoMovimiento', 'llaves', 'Replica_CuentaInversion_MovimientosCuentaInversion'); GO

--Cabecera CuentaInversion_SaldoFechaEfectiva

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_CuentaInversion_SaldoFechaEfectiva', 'Replica CuentaInversion_SaldoFechaEfectiva', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO

--Detalle CuentaInversion_SaldoFechaEfectiva
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'CuentaInversion_SaldoFechaEfectiva', 'Tablas a replicar', 'Replica_CuentaInversion_SaldoFechaEfectiva'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_CuentaInversion_SaldoFechaEfectiva'); GO  

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_CuentaInversion_SaldoFechaEfectiva'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCuentaInversion', 'llaves', 'Replica_CuentaInversion_SaldoFechaEfectiva'); GO

--Cabecera CuentaInversion_SaldoFechaMovimiento

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_CuentaInversion_SaldoFechaMovimiento', 'Replica CuentaInversion_SaldoFechaMovimiento', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO 


--Detalle CuentaInversion_SaldoFechaMovimiento
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'CuentaInversion_SaldoFechaMovimiento', 'Tablas a replicar', 'Replica_CuentaInversion_SaldoFechaMovimiento'); GO 

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_CuentaInversion_SaldoFechaMovimiento'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_CuentaInversion_SaldoFechaMovimiento'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCuentaInversion', 'llaves', 'Replica_CuentaInversion_SaldoFechaMovimiento'); GO

--Cabecera Custodia_Cartera

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Custodia_Cartera', 'Replica Custodia_Cartera', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Custodia_Cartera
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Custodia_Cartera', 'Tablas a replicar', 'Replica_Custodia_Cartera'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Custodia_Cartera'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Custodia_Cartera'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCartera', 'llaves', 'Replica_Custodia_Cartera'); GO


--Cabecera Custodia_CarteraHis

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Custodia_CarteraHis', 'Replica Custodia_CarteraHis', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Custodia_CarteraHis
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Custodia_CarteraHis', 'Tablas a replicar', 'Replica_Custodia_CarteraHis'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Custodia_CarteraHis'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Custodia_CarteraHis'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCartera+'':''+FechaRespaldo', 'llaves', 'Replica_Custodia_CarteraHis'); GO

--Cabecera Custodia_CustodiaFechaLiquidacion

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Custodia_CustodiaFechaLiquidacion', 'Replica Custodia_CustodiaFechaLiquidacion', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Custodia_CustodiaFechaLiquidacion
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Custodia_CustodiaFechaLiquidacion', 'Tablas a replicar', 'Replica_Custodia_CustodiaFechaLiquidacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Custodia_CustodiaFechaLiquidacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Custodia_CustodiaFechaLiquidacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdPortafolio+'':''+Nemotecnico', 'llaves', 'Replica_Custodia_CustodiaFechaLiquidacion'); GO


--Cabecera Custodia_CustodiaFechaOperacion

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Custodia_CustodiaFechaOperacion', 'Replica Custodia_CustodiaFechaOperacion', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Custodia_CustodiaFechaOperacion
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Custodia_CustodiaFechaOperacion', 'Tablas a replicar', 'Replica_Custodia_CustodiaFechaOperacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Custodia_CustodiaFechaOperacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Custodia_CustodiaFechaOperacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdPortafolio+'':''+Nemotecnico', 'llaves', 'Replica_Custodia_CustodiaFechaOperacion'); GO

--Cabecera Custodia_CustodiaHis

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Custodia_CustodiaHis', 'Replica Custodia_CustodiaHis', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Custodia_CustodiaHis
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Custodia_CustodiaHis', 'Tablas a replicar', 'Replica_Custodia_CustodiaHis'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Custodia_CustodiaHis'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Custodia_CustodiaHis'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdPortafolio+'':''+Nemotecnico+'':''+FechaRespaldo', 'llaves', 'Replica_Custodia_CustodiaHis'); GO

--Cabecera Custodia_EventoCapital

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Custodia_EventoCapital', 'Replica Custodia_EventoCapital', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Custodia_EventoCapital
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Custodia_EventoCapital', 'Tablas a replicar', 'Replica_Custodia_EventoCapital'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Custodia_EventoCapital'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Custodia_EventoCapital'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdEventoCapital', 'llaves', 'Replica_Custodia_EventoCapital'); GO

--Cabecera Custodia_EventoCapitalCliente

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Custodia_EventoCapitalCliente', 'Replica Custodia_EventoCapitalCliente', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Custodia_EventoCapitalCliente
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Custodia_EventoCapitalCliente', 'Tablas a replicar', 'Replica_Custodia_EventoCapitalCliente'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Custodia_EventoCapitalCliente'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Custodia_EventoCapitalCliente'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdEventoCapitalCliente', 'llaves', 'Replica_Custodia_EventoCapitalCliente'); GO

--Cabecera Custodia_Garantia

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Custodia_Garantia', 'Replica Custodia_Garantia', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1);  GO


--Detalle Custodia_Garantia
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Custodia_Garantia', 'Tablas a replicar', 'Replica_Custodia_Garantia');  GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Custodia_Garantia'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Custodia_Garantia'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdGarantia', 'llaves', 'Replica_Custodia_Garantia'); GO

--Cabecera Custodia_GarantiaHis

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Custodia_GarantiaHis', 'Replica Custodia_GarantiaHis', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Custodia_GarantiaHis
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Custodia_GarantiaHis', 'Tablas a replicar', 'Replica_Custodia_GarantiaHis'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Custodia_GarantiaHis'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Custodia_GarantiaHis'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdGarantia+'':''+FechaRespaldo', 'llaves', 'Replica_Custodia_GarantiaHis'); GO

--Cabecera Custodia_MovimientoCartera

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Custodia_MovimientoCartera', 'Replica Custodia_MovimientoCartera', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Custodia_MovimientoCartera
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Custodia_MovimientoCartera', 'Tablas a replicar', 'Replica_Custodia_MovimientoCartera'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Custodia_MovimientoCartera'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Custodia_MovimientoCartera'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCartera', 'llaves', 'Replica_Custodia_MovimientoCartera'); GO


--Cabecera Facturacion_Comprobante

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Facturacion_Comprobante', 'Replica Facturacion_Comprobante', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Facturacion_Comprobante
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Facturacion_Comprobante', 'Tablas a replicar', 'Replica_Facturacion_Comprobante'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Facturacion_Comprobante'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Facturacion_Comprobante'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdComprobante', 'llaves', 'Replica_Facturacion_Comprobante'); GO

--Cabecera Facturacion_ComprobanteCobro

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Facturacion_ComprobanteCobro', 'Replica Facturacion_ComprobanteCobro', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Facturacion_ComprobanteCobro
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Facturacion_ComprobanteCobro', 'Tablas a replicar', 'Replica_Facturacion_ComprobanteCobro'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Facturacion_ComprobanteCobro'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Facturacion_ComprobanteCobro'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdComprobanteCobro', 'llaves', 'Replica_Facturacion_ComprobanteCobro'); GO

--Cabecera Facturacion_ComprobanteDetalle

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Facturacion_ComprobanteDetalle', 'Replica Facturacion_ComprobanteDetalle', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Facturacion_ComprobanteDetalle
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Facturacion_ComprobanteDetalle', 'Tablas a replicar', 'Replica_Facturacion_ComprobanteDetalle'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Facturacion_ComprobanteDetalle'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Facturacion_ComprobanteDetalle'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdComprobanteDetalle', 'llaves', 'Replica_Facturacion_ComprobanteDetalle'); GO

--Cabecera Facturacion_ComprobanteFacturacionFormaPago

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Facturacion_ComprobanteFacturacionFormaPago', 'Replica Facturacion_ComprobanteFacturacionFormaPago', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Facturacion_ComprobanteFacturacionFormaPago
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Facturacion_ComprobanteFacturacionFormaPago', 'Tablas a replicar', 'Replica_Facturacion_ComprobanteFacturacionFormaPago'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Facturacion_ComprobanteFacturacionFormaPago'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Facturacion_ComprobanteFacturacionFormaPago'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdComprobanteFormaDePago', 'llaves', 'Replica_Facturacion_ComprobanteFacturacionFormaPago'); GO

--Cabecera FondoMutuo_Fondo

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_FondoMutuo_Fondo', 'Replica FondoMutuo_Fondo', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle FondoMutuo_Fondo
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'FondoMutuo_Fondo', 'Tablas a replicar', 'Replica_FondoMutuo_Fondo'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_FondoMutuo_Fondo'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_FondoMutuo_Fondo'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdFondo', 'llaves', 'Replica_FondoMutuo_Fondo'); GO

--Cabecera FondoMutuo_SerieDeFondo

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_FondoMutuo_SerieDeFondo', 'Replica FondoMutuo_SerieDeFondo', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle FondoMutuo_SerieDeFondo
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'FondoMutuo_SerieDeFondo', 'Tablas a replicar', 'Replica_FondoMutuo_SerieDeFondo'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_FondoMutuo_SerieDeFondo'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_FondoMutuo_SerieDeFondo'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdSerieFondo', 'llaves', 'Replica_FondoMutuo_SerieDeFondo'); GO

--Cabecera FondoMutuo_SolicitudAporteRescateFondoMutuo

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_FondoMutuo_SolicitudAporteRescateFondoMutuo', 'Replica FondoMutuo_SolicitudAporteRescateFondoMutuo', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle FondoMutuo_SolicitudAporteRescateFondoMutuo
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, FondoMutuo_SolicitudAporteRescateFondoMutuo) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'FondoMutuo_SolicitudAporteRescateFondoMutuo', 'Tablas a replicar', 'Replica_FondoMutuo_SolicitudAporteRescateFondoMutuo'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, FondoMutuo_SolicitudAporteRescateFondoMutuo) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_FondoMutuo_SolicitudAporteRescateFondoMutuo'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, FondoMutuo_SolicitudAporteRescateFondoMutuo) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_FondoMutuo_SolicitudAporteRescateFondoMutuo'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, FondoMutuo_SolicitudAporteRescateFondoMutuo) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdSolicitudAporteRescate', 'llaves', 'Replica_FondoMutuo_SolicitudAporteRescateFondoMutuo'); GO

--Cabecera Mesa_MovimientosFWD

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Mesa_MovimientosFWD', 'Replica Mesa_MovimientosFWD', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Mesa_MovimientosFWD
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Mesa_MovimientosFWD', 'Tablas a replicar', 'Replica_Mesa_MovimientosFWD'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Mesa_MovimientosFWD'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Mesa_MovimientosFWD'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdOperacion', 'llaves', 'Replica_Mesa_MovimientosFWD'); GO

--Cabecera Mesa_MovimientosSpot

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Mesa_MovimientosSpot', 'Replica Mesa_MovimientosSpot', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Mesa_MovimientosSpot
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Mesa_MovimientosSpot', 'Tablas a replicar', 'Replica_Mesa_MovimientosSpot'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Mesa_MovimientosSpot'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Mesa_MovimientosSpot'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdOperacion', 'llaves', 'Replica_Mesa_MovimientosSpot'); GO

--Cabecera Orden_Asignacion

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Orden_Asignacion', 'Replica Orden_Asignacion', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Orden_Asignacion
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Orden_Asignacion', 'Tablas a replicar', 'Replica_Orden_Asignacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Orden_Asignacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Orden_Asignacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdAsignacion', 'llaves', 'Replica_Orden_Asignacion'); GO

--Cabecera Orden_CobroAsignacion

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Orden_CobroAsignacion', 'Replica Orden_CobroAsignacion', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Orden_CobroAsignacion
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Orden_CobroAsignacion', 'Tablas a replicar', 'Replica_Orden_CobroAsignacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Orden_CobroAsignacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Orden_CobroAsignacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCobroAsignacion', 'llaves', 'Replica_Orden_CobroAsignacion'); GO

--Cabecera Orden_CobroOrden

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Orden_CobroOrden', 'Replica Orden_CobroOrden', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Orden_CobroOrden
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Orden_CobroOrden', 'Tablas a replicar', 'Replica_Orden_CobroOrden'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Orden_CobroOrden'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Orden_CobroOrden'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCobroOrden', 'llaves', 'Replica_Orden_CobroOrden'); GO


--Cabecera Orden_Orden

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Orden_Orden', 'Replica Orden_Orden', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Orden_Orden
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Orden_Orden', 'Tablas a replicar', 'Replica_Orden_Orden'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Orden_Orden'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Orden_Orden'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdOrden', 'llaves', 'Replica_Orden_Orden'); GO

--Cabecera Plataforma_Banco

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_Banco', 'Replica Plataforma_Banco', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_Banco
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_Banco', 'Tablas a replicar', 'Replica_Plataforma_Banco'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_Banco'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_Banco'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdBanco', 'llaves', 'Replica_Plataforma_Banco'); GO

--Cabecera Plataforma_Ciudad

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_Ciudad', 'Replica Plataforma_Ciudad', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_Ciudad
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_Ciudad', 'Tablas a replicar', 'Replica_Plataforma_Ciudad'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_Ciudad'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_Ciudad'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCiudad', 'llaves', 'Replica_Plataforma_Ciudad'); GO

--Cabecera Plataforma_ClasificacionTipoInstrumento

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_ClasificacionTipoInstrumento', 'Replica Plataforma_ClasificacionTipoInstrumento', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_ClasificacionTipoInstrumento
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_ClasificacionTipoInstrumento', 'Tablas a replicar', 'Replica_Plataforma_ClasificacionTipoInstrumento'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_ClasificacionTipoInstrumento'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_ClasificacionTipoInstrumento'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdClasificacionTipoInstrumento', 'llaves', 'Replica_Plataforma_ClasificacionTipoInstrumento'); GO

--Cabecera Plataforma_Concepto

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_Concepto', 'Replica Plataforma_Concepto', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_Concepto
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_Concepto', 'Tablas a replicar', 'Replica_Plataforma_Concepto'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_Concepto'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_Concepto'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdConcepto', 'llaves', 'Replica_Plataforma_Concepto'); GO

--Cabecera Plataforma_DivisionPais

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_DivisionPais', 'Replica Plataforma_DivisionPais', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_DivisionPais
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_DivisionPais', 'Tablas a replicar', 'Replica_Plataforma_DivisionPais'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_DivisionPais'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_DivisionPais'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdDivisionPais', 'llaves', 'Replica_Plataforma_DivisionPais'); GO

--Cabecera Plataforma_Empresa

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_Empresa', 'Replica Plataforma_Empresa', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_Empresa
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_Empresa', 'Tablas a replicar', 'Replica_Plataforma_Empresa'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_Empresa'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_Empresa'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdEmpresa', 'llaves', 'Replica_Plataforma_Empresa'); GO

--Cabecera Plataforma_Estado

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_Estado', 'Replica Plataforma_Estado', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_Estado
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_Estado', 'Tablas a replicar', 'Replica_Plataforma_Estado'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_Estado'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_Estado'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'ID+'':''+Modulo', 'llaves', 'Replica_Plataforma_Estado'); GO

--Cabecera Plataforma_Giro

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_Giro', 'Replica Plataforma_Giro', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_Giro
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_Giro', 'Tablas a replicar', 'Replica_Plataforma_Giro'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_Giro'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_Giro'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdGiro', 'llaves', 'Replica_Plataforma_Giro'); GO

--Cabecera Plataforma_Instrumento

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_Instrumento', 'Replica Plataforma_Instrumento', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_Instrumento
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_Instrumento', 'Tablas a replicar', 'Replica_Plataforma_Instrumento'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_Instrumento'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_Instrumento'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdInstrumento', 'llaves', 'Replica_Plataforma_Instrumento'); GO

--Cabecera Plataforma_MatrizComercialCorredora

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_MatrizComercialCorredora', 'Replica Plataforma_MatrizComercialCorredora', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_MatrizComercialCorredora
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_MatrizComercialCorredora', 'Tablas a replicar', 'Replica_Plataforma_MatrizComercialCorredora'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_MatrizComercialCorredora'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_MatrizComercialCorredora'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdPerfilComercialBase', 'llaves', 'Replica_Plataforma_MatrizComercialCorredora'); GO

--Cabecera Plataforma_MatrizComercialPortafolios

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_MatrizComercialPortafolios', 'Replica Plataforma_MatrizComercialPortafolios', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_MatrizComercialPortafolios
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_MatrizComercialPortafolios', 'Tablas a replicar', 'Replica_Plataforma_MatrizComercialPortafolios'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_MatrizComercialPortafolios'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_MatrizComercialPortafolios'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdPortafolio+'':''+IdTipoProducto+'':''+IdTipoOperacion+'':''+IdCobroClasificacion', 'llaves', 'Replica_Plataforma_MatrizComercialPortafolios'); GO

--Cabecera Plataforma_Moneda

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_Moneda', 'Replica Plataforma_Moneda', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_Moneda
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_Moneda', 'Tablas a replicar', 'Replica_Plataforma_Moneda'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_Moneda'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_Moneda'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdMoneda', 'llaves', 'Replica_Plataforma_Moneda'); GO

--Cabecera Plataforma_Pais

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_Pais', 'Replica Plataforma_Pais', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_Pais
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_Pais', 'Tablas a replicar', 'Replica_Plataforma_Pais'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_Pais'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_Pais'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdPais', 'llaves', 'Replica_Plataforma_Pais'); GO

--Cabecera Plataforma_Paridades

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_Paridades', 'Replica Plataforma_Paridades', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_Paridades
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_Paridades', 'Tablas a replicar', 'Replica_Plataforma_Paridades'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_Paridades'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_Paridades'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'Origen+'':''+IdParidadYTipoCambio', 'llaves', 'Replica_Plataforma_Paridades'); GO

--Cabecera Plataforma_Profesion

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_Profesion', 'Replica Plataforma_Profesion', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_Profesion
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_Profesion', 'Tablas a replicar', 'Replica_Plataforma_Profesion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_Profesion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_Profesion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdProfesion', 'llaves', 'Replica_Plataforma_Profesion'); GO


--Cabecera Plataforma_TipoIndicador

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_TipoIndicador', 'Replica Plataforma_TipoIndicador', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_TipoIndicador
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_TipoIndicador', 'Tablas a replicar', 'Replica_Plataforma_TipoIndicador'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_TipoIndicador'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_TipoIndicador'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdTipoIndicador', 'llaves', 'Replica_Plataforma_TipoIndicador'); GO


--Cabecera Plataforma_TipoInstrumento

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_TipoInstrumento', 'Replica Plataforma_TipoInstrumento', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_TipoInstrumento
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_TipoInstrumento', 'Tablas a replicar', 'Replica_Plataforma_TipoInstrumento'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_TipoInstrumento'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_TipoInstrumento'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdTipoInstrumento', 'llaves', 'Replica_Plataforma_TipoInstrumento'); GO

--Cabecera Plataforma_TipoOperacion

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_TipoOperacion', 'Replica Plataforma_TipoOperacion', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_TipoOperacion
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_TipoOperacion', 'Tablas a replicar', 'Replica_Plataforma_TipoOperacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_TipoOperacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_TipoOperacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdTipoOperacion', 'llaves', 'Replica_Plataforma_TipoOperacion'); GO

--Cabecera Plataforma_TipoProducto

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Plataforma_TipoProducto', 'Replica Plataforma_TipoProducto', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Plataforma_TipoProducto
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Plataforma_TipoProducto', 'Tablas a replicar', 'Replica_Plataforma_TipoProducto'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Plataforma_TipoProducto'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Plataforma_TipoProducto'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdTipoProducto', 'llaves', 'Replica_Plataforma_TipoProducto'); GO

--Cabecera Portafolio_Portafolio

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Portafolio_Portafolio', 'Replica Portafolio_Portafolio', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Portafolio_Portafolio
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Portafolio_Portafolio', 'Tablas a replicar', 'Replica_Portafolio_Portafolio'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Portafolio_Portafolio'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Portafolio_Portafolio'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdPortafolio', 'llaves', 'Replica_Portafolio_Portafolio'); GO

--Cabecera SocioNegocio_Cliente

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_Cliente', 'Replica SocioNegocio_Cliente', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_Cliente
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_Cliente', 'Tablas a replicar', 'Replica_SocioNegocio_Cliente'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_Cliente'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_Cliente'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCliente', 'llaves', 'Replica_SocioNegocio_Cliente'); GO

--Cabecera SocioNegocio_DetalleIndicador

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_DetalleIndicador', 'Replica SocioNegocio_DetalleIndicador', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_DetalleIndicador
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_DetalleIndicador', 'Tablas a replicar', 'Replica_SocioNegocio_DetalleIndicador'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_DetalleIndicador'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_DetalleIndicador'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdDetalleIndicador+'':''+IdTipoIndicador', 'llaves', 'Replica_SocioNegocio_DetalleIndicador'); GO

--Cabecera SocioNegocio_Ejecutivo

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_Ejecutivo', 'Replica SocioNegocio_Ejecutivo', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_Ejecutivo
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_Ejecutivo', 'Tablas a replicar', 'Replica_SocioNegocio_Ejecutivo'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_Ejecutivo'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_Ejecutivo'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdEmpleadoEjecutivo', 'llaves', 'Replica_SocioNegocio_Ejecutivo'); GO

--Cabecera SocioNegocio_Empleado

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_Empleado', 'Replica SocioNegocio_Empleado', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_Empleado
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_Empleado', 'Tablas a replicar', 'Replica_SocioNegocio_Empleado'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_Empleado'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_Empleado'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdEmpleado', 'llaves', 'Replica_SocioNegocio_Empleado'); GO

--Cabecera SocioNegocio_IndicadorCliente

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_IndicadorCliente', 'Replica SocioNegocio_IndicadorCliente', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_IndicadorCliente
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_IndicadorCliente', 'Tablas a replicar', 'Replica_SocioNegocio_IndicadorCliente'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_IndicadorCliente'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_IndicadorCliente'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCliente+'':''+IdTipoIndicador', 'llaves', 'Replica_SocioNegocio_IndicadorCliente'); GO


--Cabecera SocioNegocio_IndicadorPersona

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_IndicadorPersona', 'Replica SocioNegocio_IndicadorPersona', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_IndicadorPersona
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_IndicadorPersona', 'Tablas a replicar', 'Replica_SocioNegocio_IndicadorPersona'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_IndicadorPersona'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_IndicadorPersona'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdPersona+'':''+IdTipoIndicador', 'llaves', 'Replica_SocioNegocio_IndicadorPersona'); GO

--Cabecera SocioNegocio_IndicadorPortafolio

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_IndicadorPortafolio', 'Replica SocioNegocio_IndicadorPortafolio', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_IndicadorPortafolio
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_IndicadorPortafolio', 'Tablas a replicar', 'Replica_SocioNegocio_IndicadorPortafolio'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_IndicadorPortafolio'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_IndicadorPortafolio'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCliente+'':''+IdTipoIndicador', 'llaves', 'Replica_SocioNegocio_IndicadorPortafolio'); GO

--Cabecera SocioNegocio_Persona

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_Persona', 'Replica SocioNegocio_Persona', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_Persona
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_Persona', 'Tablas a replicar', 'Replica_SocioNegocio_Persona'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_Persona'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_Persona'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdPersona', 'llaves', 'Replica_SocioNegocio_Persona'); GO

--Cabecera SocioNegocio_PersonaCuentaBancaria

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_PersonaCuentaBancaria', 'Replica SocioNegocio_PersonaCuentaBancaria', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_PersonaCuentaBancaria
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_PersonaCuentaBancaria', 'Tablas a replicar', 'Replica_SocioNegocio_PersonaCuentaBancaria'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_PersonaCuentaBancaria'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_PersonaCuentaBancaria'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdCuentaBancaria', 'llaves', 'Replica_SocioNegocio_PersonaCuentaBancaria'); GO

--Cabecera SocioNegocio_PersonaDireccionElectronica

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_PersonaDireccionElectronica', 'Replica SocioNegocio_PersonaDireccionElectronica', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_PersonaDireccionElectronica
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_PersonaDireccionElectronica', 'Tablas a replicar', 'Replica_SocioNegocio_PersonaDireccionElectronica'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_PersonaDireccionElectronica'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_PersonaDireccionElectronica'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdDireccionElectronica', 'llaves', 'Replica_SocioNegocio_PersonaDireccionElectronica'); GO

--Cabecera SocioNegocio_PersonaDireccionFisica

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_PersonaDireccionFisica', 'Replica SocioNegocio_PersonaDireccionFisica', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_PersonaDireccionFisica
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_PersonaDireccionFisica', 'Tablas a replicar', 'Replica_SocioNegocio_PersonaDireccionFisica'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_PersonaDireccionFisica'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_PersonaDireccionFisica'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdDireccionFisica', 'llaves', 'Replica_SocioNegocio_PersonaDireccionFisica'); GO

--Cabecera SocioNegocio_PersonaNombreFantasia

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_PersonaNombreFantasia', 'Replica SocioNegocio_PersonaNombreFantasia', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_PersonaNombreFantasia
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_PersonaNombreFantasia', 'Tablas a replicar', 'Replica_SocioNegocio_PersonaNombreFantasia'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_PersonaNombreFantasia'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_PersonaNombreFantasia'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdNombreFantasia', 'llaves', 'Replica_SocioNegocio_PersonaNombreFantasia'); GO

--Cabecera SocioNegocio_PersonaRelacion

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_PersonaRelacion', 'Replica SocioNegocio_PersonaRelacion', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_PersonaRelacion
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_PersonaRelacion', 'Tablas a replicar', 'Replica_SocioNegocio_PersonaRelacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_PersonaRelacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_PersonaRelacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdPersonaOrigen+'':''+IdPersonaDestino+'':''+IdRelacionPersona', 'llaves', 'Replica_SocioNegocio_PersonaRelacion'); GO

--Cabecera SocioNegocio_PersonaTelefono

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_PersonaTelefono', 'Replica SocioNegocio_PersonaTelefono', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_PersonaTelefono
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_PersonaTelefono', 'Tablas a replicar', 'Replica_SocioNegocio_PersonaTelefono'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_PersonaTelefono'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_PersonaTelefono'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdTelefono', 'llaves', 'Replica_SocioNegocio_PersonaTelefono'); GO

--Cabecera SocioNegocio_Supervisor

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_Supervisor', 'Replica SocioNegocio_Supervisor', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_Supervisor
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_Supervisor', 'Tablas a replicar', 'Replica_SocioNegocio_Supervisor'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_Supervisor'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_Supervisor'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdEmpleadoSupervisor', 'llaves', 'Replica_SocioNegocio_Supervisor'); GO

--Cabecera SocioNegocio_TipoRelacion

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_SocioNegocio_TipoRelacion', 'Replica SocioNegocio_TipoRelacion', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle SocioNegocio_TipoRelacion
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'SocioNegocio_TipoRelacion', 'Tablas a replicar', 'Replica_SocioNegocio_TipoRelacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_SocioNegocio_TipoRelacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_SocioNegocio_TipoRelacion'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdTipoRelacion', 'llaves', 'Replica_SocioNegocio_TipoRelacion'); GO

--Cabecera Tesoreria_Comprobante

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Tesoreria_Comprobante', 'Replica Tesoreria_Comprobante', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Tesoreria_Comprobante
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Tesoreria_Comprobante', 'Tablas a replicar', 'Replica_Tesoreria_Comprobante'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Tesoreria_Comprobante'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Tesoreria_Comprobante'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdComprobante', 'llaves', 'Replica_Tesoreria_Comprobante'); GO

--Cabecera Tesoreria_ComprobanteDetalle

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Tesoreria_ComprobanteDetalle', 'Replica Tesoreria_ComprobanteDetalle', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Tesoreria_ComprobanteDetalle
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Tesoreria_ComprobanteDetalle', 'Tablas a replicar', 'Replica_Tesoreria_ComprobanteDetalle'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Tesoreria_ComprobanteDetalle'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Tesoreria_ComprobanteDetalle'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdComprobanteDetalle', 'llaves', 'Replica_Tesoreria_ComprobanteDetalle'); GO

--Cabecera Tesoreria_ComprobanteFormaDePago

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Tesoreria_ComprobanteFormaDePago', 'Replica Tesoreria_ComprobanteFormaDePago', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Tesoreria_ComprobanteFormaDePago
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Tesoreria_ComprobanteFormaDePago', 'Tablas a replicar', 'Replica_Tesoreria_ComprobanteFormaDePago'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Tesoreria_ComprobanteFormaDePago'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Tesoreria_ComprobanteFormaDePago'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdComprobanteFormaDePago', 'llaves', 'Replica_Tesoreria_ComprobanteFormaDePago'); GO

--Cabecera Tesoreria_IngresoEgreso

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_DYNAMIC_ROUTE"
(DYNAMIC_ROUTE_ID, ROUTE_INSTANCE, ROUTE_NAME, DESCRIPTION, ROUTE_FROM, ROUTE_TO, ERROR_HANDLER_NAME, ENABLED)
VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_DYNAMIC_ROUTE".NEXTVAL, 'BrokerA', 'Replica_Tesoreria_IngresoEgreso', 'Replica Tesoreria_IngresoEgreso', 'timer://DynamicRutes?fixedRate=true&period=6000000', 'volante:BCS_InicioReplicas', 'ErrorHandler', 1); GO


--Detalle Tesoreria_IngresoEgreso
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'RawTablas', 'String', 'Tesoreria_IngresoEgreso', 'Tablas a replicar', 'Replica_Tesoreria_IngresoEgreso'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BatchLimit', 'String', '2000', 'limite', 'Replica_Tesoreria_IngresoEgreso'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_deleteQuery', 'String', 'delete from %tablaReplica% where %tablaLLave% in (%keys%)', 'query base de eliminacion', 'Replica_Tesoreria_IngresoEgreso'); GO

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Replica.VIF_CONFIG_PARAM" (PARAM_ID, PARAM_NAME, PARAM_TYPE, PARAM_VALUE, DESCRIPTION, CONFIG_GROUP) VALUES("BSCL.Plataforma.BD.Secuencia::PLA.Replica.VIF_CONFIG_PARAM".NEXTVAL, 'BCS_tablaLLaves', 'String', 'IdIngresoEgreso', 'llaves', 'Replica_Tesoreria_IngresoEgreso'); GO
