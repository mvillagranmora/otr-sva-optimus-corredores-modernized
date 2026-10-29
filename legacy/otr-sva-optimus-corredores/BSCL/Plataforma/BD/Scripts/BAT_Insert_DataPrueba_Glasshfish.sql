SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.InsertDataPruebaGlasshfish";

CREATE PROCEDURE
-- 
--  Proposito: Calcula Saldo custodia para ingreso de garantias
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::BAT.InsertDataPruebaGlasshfish" (
	-- Parametros entrada del servicio publico

	IN p_Today					NVARCHAR(200)
	 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

 		declare v_fechaEjecucion SECONDDATE;
 		
 		select add_seconds(now(),(60*60)*-3) into v_fechaEjecucion from dummy;
		delete from "BSCL.Plataforma.BD::BAT.ParametrosPrograma" cascade;
		delete from "BSCL.Plataforma.BD::BAT.ParametrosAgenda" cascade;
		delete from "BSCL.Plataforma.BD::BAT.Programa" cascade;
		delete from "BSCL.Plataforma.BD::BAT.Agenda" cascade;
		delete from "BSCL.Plataforma.BD::BAT.TipoPrograma" cascade;


  
--Ejecución correcta   cl.bcs.scheduler.orders.BcsJobOrderChronologicAllocation
		 INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('ASIG_CRONO_AUTOMATIC', 'Asignación crónologica automatica', 3,'Ordenes','cl.bcs.scheduler.orders.bcsjob.BcsJobOrderChronologiAllocationReader','JAVA');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('ASIG_CRONO', 'Asignación crónologica', 3,'Ordenes','cl.bcs.scheduler.orders.bcsjob.BcsJobOrderChronologicAllocation','JAVA');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('JOB_DATE_WEEKDAY', 'Asignación por dia especifico', 3,'Ordenes','cl.bcs.scheduler.tests.BcsBeanJobTestTwo','JAVA');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('JOB_DATE_MONTH', 'Asignación por mes ', 3,'Ordenes','cl.bcs.scheduler.tests.BcsBeanJobTestThree','JAVA');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('JOB_DATE_DAY', 'Asignación pr día ', 3,'Ordenes','cl.bcs.scheduler.tests.BcsBeanJobTestFour','JAVA');
 --Ejecución incorrecta. Motivo : Tipo de programa no existe.
		 INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('TEST_01', 'Descripcion TEST_01', 3,'TEST_01','BcsJobOrderChronologicAllocation','EXE');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('ASIG_TEST', 'Asignación diaria', 3,'Ordenes','cl.bcs.scheduler.tests.BcsJobManagerBeanTest666','BAT');
		 
--Ejecución incorrecta. Motivo : Nombre de programa no existe.
		 INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('TEST_02', 'Descripcion TEST_02', 3,'TEST_02','cl.cl.cl','JAVA');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('TEST_04', 'Descripcion TEST_04', 3,'TEST_02','cl.cl.cl','JAVA');
--Ejecución incorrecta. Motivo : alcanzó maximo de intentos
		 INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('TEST_03', 'Descripcion TEST_03', 1,'TEST_03','cl','JAVA'); 
		 
		
		 --Ejecutar/Agendar todos los días a las 19:45
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Agenda" VALUES(1, 'ASIG_CRONO_AUTOMATIC', 'N','**','*','**','**','50','N','N','N','S'); 
		 --Ejecutar/Agendar todos los días martes a las 19:45
		/* INSERT INTO "BSCL.Plataforma.BD::BAT.Agenda" VALUES(2, 'JOB_DATE_WEEKDAY', 'N','**','*','**','**','45','N','N','N','S');
		 --Ejecutar todos los febreros a als 19:45
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Agenda" VALUES(3, 'JOB_DATE_MONTH', 'N','**','*','02','22','45','N','N','N','S');
	  	 --Ejecutar todo los días 25 a las 19:45
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Agenda" VALUES(4, 'JOB_DATE_DAY', 'N','01','*','**','22','00','N','N','N','S'); */
	/*	 
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Agenda" VALUES(5, 'ASIG_TEST', 'N','**','*','**','19','45','N','N','N','S');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Agenda" VALUES(9, 'TEST_04', 'N','**','*','**','19','45','N','N','N','S');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Agenda" VALUES(6, 'TEST_01', 'N','**','*','**','19','45','N','N','N','S');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Agenda" VALUES(7, 'TEST_02', 'N','**','*','**','19','45','N','N','N','S');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Agenda" VALUES(8, 'TEST_03', 'N','**','*','**','19','45','N','N','N','S');*/
		 
  /**    INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" VALUES(1,1,0,'ASIG_CRONO',0,'System','PEN','Esperando ejecución','2015-12-14 11:43:00',v_fechaEjecucion,'0001-01-01 00:00:00');
	     INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" VALUES(2,1,0,'JOB_DATE_WEEKDAY',0,'System','PEN','Esperando ejecución','2015-12-14 11:43:00',v_fechaEjecucion,'0001-01-01 00:00:00');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" VALUES(3,1,0,'JOB_DATE_MONTH',0,'System','PEN','Esperando ejecución','2015-12-14 11:43:00',v_fechaEjecucion,'0001-01-01 00:00:00');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" VALUES(4,1,0,'JOB_DATE_DAY',0,'System','PEN','Esperando ejecución','2015-12-14 11:43:00',v_fechaEjecucion,'0001-01-01 00:00:00');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" VALUES(5,1,0,'ASIG_TEST',0,'System','PEN','Esperando ejecución','2015-12-14 11:43:00',v_fechaEjecucion,'0001-01-01 00:00:00');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" VALUES(6,1,0,'TEST_01',0,'System','PEN','Esperando ejecución','2015-12-14 11:43:00',v_fechaEjecucion,'0001-01-01 00:00:00');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" VALUES(7,1,0,'TEST_02',0,'System','PEN','Esperando ejecución','2015-12-14 11:43:00',v_fechaEjecucion,'0001-01-01 00:00:00');
		 INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" VALUES(8,1,0,'TEST_03',1,'System','PEN','Esperando ejecución','2015-12-14 11:43:00',v_fechaEjecucion,'0001-01-01 00:00:00');*/
         
         insert into "BSCL"."BSCL.Plataforma.BD::BAT.ParametrosAgenda" values(1,0,'IdLibroOrdenes','INT','1');
         insert into "BSCL"."BSCL.Plataforma.BD::BAT.ParametrosAgenda" values(3,0,'IdLibroOrdenes','INT','2');
         
 		 
 
	 
		 delete from "BSCL.Plataforma.BD::PLA.Log" where "IdProceso" in (select "IdPrograma" from "BSCL.Plataforma.BD::BAT.Programa");
 END;
