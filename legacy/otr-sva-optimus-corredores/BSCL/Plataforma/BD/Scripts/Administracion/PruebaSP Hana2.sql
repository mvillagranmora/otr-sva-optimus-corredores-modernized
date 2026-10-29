--global temporal de entrada
--tabla entrada
--escalares de entrada
--defaults en la entrada
--nulls en la entrada

--global temporal de salida
--tabla de salida
--select de salida
--escalares de salida

drop table "BSCL"."BSCL.Facturacion.BD::Entrada1LeTest";
create global temporary table BSCL."BSCL.Facturacion.BD::Entrada1LeTest" ("Campo1" BIGINT,"Campo2" NVARCHAR(10),"Campo3" TIME,"Campo4" TIMESTAMP,"Campo5" DATE,"Campo6" SECONDDATE) on commit delete rows;
drop table "BSCL"."BSCL.Facturacion.BD::Entrada2LeTest";
create global temporary table BSCL."BSCL.Facturacion.BD::Entrada2LeTest" ("Campo1" BIGINT,"Campo2" NVARCHAR(10),"Campo3" TIME,"Campo4" TIMESTAMP,"Campo5" DATE,"Campo6" SECONDDATE) on commit delete rows;


drop table "BSCL"."BSCL.Facturacion.BD::Salida1LeTest";
create global temporary table BSCL."BSCL.Facturacion.BD::Salida1LeTest" ("Campo1" BIGINT,"Campo2" NVARCHAR(10),"Campo3" TIME,"Campo4" TIMESTAMP,"Campo5" DATE,"Campo6" SECONDDATE) on commit delete rows;
drop table "BSCL"."BSCL.Facturacion.BD::Salida2LeTest";
create global temporary table BSCL."BSCL.Facturacion.BD::Salida2LeTest" ("Campo1" BIGINT,"Campo2" NVARCHAR(10),"Campo3" TIME,"Campo4" TIMESTAMP,"Campo5" DATE,"Campo6" SECONDDATE) on commit delete rows;

CALL BSCL."BSCL.Facturacion.BD.Servicios::LeTest" ( P_IDEMPRESA => ?,P_IDUSUARIO => ?,P_IDMODULO => ?,P_SIMULAROPERACION => ?,
P_USAREGLASNEGOCIO => ?,P_ESINGRESOAUTOMATICO => ?,P_OUT_DATA => ?,P_OUT_INFORMACIONCONTADOR => ?,
P_OUT_INFORMACIONDETALLE => ?,P_OUT_RESULTADOPROCESO => ?,P_OUT_ADVERTENCIASCONTADOR => ?,P_OUT_ADVERTENCIASDETALLE => ?,
P_OUT_ERRORESCONTADOR => ?,P_OUT_ERRORESDETALLE => ? ) 

CALL "BSCL.Facturacion.BD.Servicios::LeTest"(
	p_IdEmpresa => 1,
	p_IdUsuario => 1,
	p_IdModulo => '',
	p_simularOperacion => 'S',
	p_UsaReglasNegocio => 'S',
	p_EsIngresoAutomatico => '',
	p_out_data => ?,
	p_out_InformacionContador => ?,
	p_out_InformacionDetalle => ?,
	p_out_ResultadoProceso => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
)

SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::LeTest";

CREATE PROCEDURE "BSCL.Facturacion.BD.Servicios::LeTest"
  (
     -- Parametros de Validacion
     IN p_IdEmpresa BIGINT,  
     IN p_IdUsuario BIGINT,   
     IN p_IdModulo  NVARCHAR(3), 
     
     IN p_simularOperacion    NVARCHAR(1) DEFAULT NULL,	-- S/N se usa para simular operacion (algunos casos)                  
     IN p_UsaReglasNegocio    NVARCHAR(1) ,	-- S/N se usa en caso de servicios anidados. 
     IN p_EsIngresoAutomatico NVARCHAR(1) Default 'N',
     IN p_table_in			 table ("Campo1" BIGINT,"Campo2" NVARCHAR(10),"Campo3" TIME,"Campo4" TIMESTAMP,"Campo5" DATE,"Campo6" SECONDDATE) DEFAULT EMPTY,   
	
     OUT p_out_data					  	 NVARCHAR(20),
     OUT p_out_InformacionContador  	 INT,
     OUT p_out_InformacionDetalle   	 table ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)) DEFAULT EMPTY,   
     OUT p_out_ResultadoProceso		     table ("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)) DEFAULT EMPTY,
     OUT p_out_AdvertenciasContador      int,
     OUT p_out_AdvertenciasDetalle       table ("IdMovimientoComprobante" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,	
     OUT p_out_ErroresContador           int,   
     OUT p_out_ErroresDetalle            table ("IdMovimientoComprobante" BIGINT, "Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) DEFAULT EMPTY,
     OUT p_out_bigint			         bigint,
     OUT p_out_seconddate 		 		 seconddate,
     OUT p_out_date      		 		 date,
     OUT p_out_timestamp		         timestamp,
     OUT p_out_time				         time
  )   
    
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 

AS
BEGIN
    
	p_out_ErroresDetalle = SELECT 1 AS "IdMovimientoComprobante",'pepito1' as "Codigo",'p_out_ErroresDetalle' as "Descripcion" from dummy union
							SELECT 2 AS "IdMovimientoComprobante",'pepito2' as "Codigo",'p_out_ErroresDetalle' as "Descripcion" from dummy;

	p_out_AdvertenciasDetalle = SELECT 1 AS "IdMovimientoComprobante",'pepito1' as "Codigo",'S' AS "RequiereAutorizacion",'p_out_AdvertenciasDetalle' as "Descripcion" from dummy union
							SELECT 2 AS "IdMovimientoComprobante",'pepito2' as "Codigo",'S' AS "RequiereAutorizacion",'p_out_AdvertenciasDetalle' as "Descripcion" from dummy;

	p_out_InformacionDetalle = SELECT 'pepito1' as "Codigo",'p_out_InformacionDetalle' as "Descripcion" from dummy union
							SELECT 'pepito2' as "Codigo",'p_out_InformacionDetalle' as "Descripcion" from dummy;

	p_out_ResultadoProceso = SELECT 'campo1' AS "Campo",1 as "Id",'p_out_ResultadoProceso' as "Servicio",'exito' as "Resultado" from dummy union
							SELECT 'campo2' AS "Campo",2 as "Id",'p_out_ResultadoProceso' as "Servicio",'error' as "Resultado" from dummy ;

	
	p_out_InformacionContador  := 1;
	p_out_AdvertenciasContador := 2;
	p_out_ErroresContador	   := 3;
	p_out_data = 'RENZOOO';
	p_out_bigint			         := 9999;
 	p_out_seconddate 		 		 := '2021-01-01 10:10:10';
     p_out_date      		 		 := '2022-01-01';
     p_out_timestamp		         := '2023-01-01 10:10:10';
     p_out_time				         := '10:01:01';
	
	Begin --Logica
       --------------------
	   --Declare Variables
	   --------------------
		SELECT * FROM BSCL."BSCL.Facturacion.BD::Entrada1LeTest";
		SELECT * FROM BSCL."BSCL.Facturacion.BD::Entrada2LeTest";

		select 'blabla1' as columna,1 as t from dummy;
		select 'blabla2' as columna,2 as t from dummy;
		select 'blabla3' as columna,3 as t from dummy;
		select * from :p_table_in;


		insert into "BSCL"."BSCL.Facturacion.BD::Salida1LeTest"("Campo1","Campo2","Campo3","Campo4","Campo5","Campo6")
		values(99,'asdf','00:01:01','2021-01-01 00:01:02','2020-02-02','2021-01-01 00:01:02');

		insert into "BSCL"."BSCL.Facturacion.BD::Salida2LeTest"("Campo1","Campo2","Campo3","Campo4","Campo5","Campo6")
		values(44,'qwert','10:11:11','2121-01-01 10:01:02','2120-02-02','2121-01-01 00:01:02');

	end;
END;   

