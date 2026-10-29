SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarAsignacion";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de ASIGNACION 
--  renta variable
--  Autor: - EAR
 "BSCL.Homologacion.BD.Servicios::CargarAsignacion"(
	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
     IN  p_fechaCambio         NVARCHAR(20),   --'YYYY-MM-DD HH:MM:SS' 
	-- Salidas para mensajes de advertencias y errores
	OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  NVARCHAR(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		----------------------------------------------------- 
	-- declaracion variables 
	   DECLARE v_Total_Operaciones_Asig BIGINT:=0;
	   DECLARE v_Total_Asig_Cargados BIGINT :=0;
  
		-- COMENZAMOS A SELECCIONAR LOS DATOS DE LA ASIGNACION
			INSERT INTO "BSCL.Homologacion.BD::HOM.Asignacion"
				( "IdAsignacion","FechaAsignacion","FechaIngresoRegistro","CantidadAsignada","MontoAsignado"
					,"ValorRescateAsignado","CantidadAsignadaRef","ValorRescateAsignadoRef","IdEstadoAsignacion","IdEstadoFacturacion"
					,"IdTransaccion","IdCarga","IdOrden","IdLibroOrdenes","EsAPlazo","IdAsignacionRef","IdCompFacturacionRef","EsOD"
					,"SecAsignacionMigracion","sol_sec","FEC_TRANS","FOLIO_TRANS","MERCADO","SEC_ASIGNACION","TIPO_OPERAC","NEMO"
					,"RUT_CLI","SEC_RUT_CLI","IND_BOLSA","IND_SIMUL","IND_LIQUID","SEC_MOVTO_FAC"
				)
			SELECT 
					 "BSCL"."BSCL.Orden.BD.Secuencias::Asignacion".NEXTVAL									 as "IdAsignacion"               
					, CASE WHEN p_fechaCambio = '0001-01-01 00:00:00' THEN  
					       CONCAT (SUBSTRING(ASIGNACION."FEC_TRANS",1,10), CONCAT(' ',SUBSTRING (ASIGNACION."FEC_TRANS", 12, 8))) 
					  ELSE
					       CONCAT (SUBSTRING(p_fechaCambio,1,10), CONCAT(' ',SUBSTRING (p_fechaCambio, 12, 8))) 
					  END as "FechaAsignacion"        
					, CASE WHEN p_fechaCambio = '0001-01-01 00:00:00' THEN 
					       CONCAT (SUBSTRING(ASIGNACION."FEC_TRANS",1,10), CONCAT(' ',SUBSTRING (ASIGNACION."FEC_TRANS", 12, 8))) 
					  ELSE 
					       CONCAT (SUBSTRING(p_fechaCambio,1,10), CONCAT(' ',SUBSTRING (p_fechaCambio, 12, 8))) 
					  END as "FechaIngresoRegistro"   
					, ASIGNACION."CAN_ASIGNAC" 																 as "CantidadAsignada"       
					, ASIGNACION."MONTO"																	 as "MontoAsignado"          
					, 0																						 as "ValorRescateAsignado"   
					, 0 																					 as "CantidadAsignadaRef"    
					, 0 																					 as "ValorRescateAsignadoRef"
					, 'ING'																					 as "IdEstadoAsignacion"     
					, CASE WHEN (ASIGNACION."SEC_MOVTO_FAC" = 0 OR IFNULL(MOVFACTURARV."SEC_MOVTO_FAC",0) = 0 OR MOVFACTURARV.EST_MOVTO_FAC ='A') THEN 'ANF'
			  		  ELSE 'AEF' 
			  		 END 																				 as "IdEstadoFacturacion"    
					, COALESCE(PALOS_RV."IdTransaccion",0)													 as "IdTransaccion"          
					, p_Idcarga 																			 as "IdCarga"                
					, COALESCE(HOM_ORDEN."IdOrden",0)														 as "IdOrden"                
					, 0																						 as "IdLibroOrdenes" 			
					, CASE WHEN  ASIGNACION."FEC_VCTO_TP" = '1900-01-01 0:00:00.0' THEN 'N' ELSE 'S' END	 as "EsAPlazo" 				
					, 0 																					 as "IdAsignacionRef"			
					, 0																						 as "IdCompFacturacionRef"		
					, CASE WHEN (COALESCE(PALOS_RV."IND_OD",'')='OE' OR COALESCE(PALOS_RV."IND_OD",'')='OD') THEN 'S' ELSE 'N' END as "EsOD"						
					, ASIGNACION."SEC_ASIGNACION" 															 as "SecAsignacionMigracion" 
					, ASIGNACION."sol_sec"																	 as "sol_sec"					
					, ASIGNACION."FEC_TRANS"																 as "FEC_TRANS"              
					, ASIGNACION."FOLIO_TRANS"																 as "FOLIO_TRANS"				
					, ASIGNACION."MERCADO"																	 as "MERCADO"					
					, ASIGNACION."SEC_ASIGNACION"															 as "SEC_ASIGNACION"			
					, ASIGNACION."TIPO_OPERAC"																 as "TIPO_OPERAC"				
					, ASIGNACION."NEMO"																		 as "NEMO"						
					, ''			 																		 as "RUT_CLI"					
					, 0																						 as "SEC_RUT_CLI"				
					, ASIGNACION."IND_BOLSA"																 as "IND_BOLSA"				
					, ASIGNACION."IND_SIMUL"																 as "IND_SIMUL"				
					, ASIGNACION."IND_LIQUID"																 as "IND_LIQUID"
					, ASIGNACION."SEC_MOVTO_FAC"															 as "SEC_MOVTO_FAC"													   	
					FROM "BSCL_VIRTUAL"."GES_ASIGNACION_RV" AS ASIGNACION 
					LEFT JOIN "BSCL.Homologacion.BD::HOM.Orden" AS HOM_ORDEN
					 	ON HOM_ORDEN."sol_sec" = ASIGNACION."sol_sec"
					LEFT JOIN "BSCL.Homologacion.BD::HOM.Transaccion" AS PALOS_RV
					    ON  PALOS_RV."FOLIO_TRANS" = ASIGNACION."FOLIO_TRANS"
					    AND PALOS_RV."IND_BOLSA" = ASIGNACION."IND_BOLSA"
					    AND PALOS_RV."TIPO_OPERAC" = ASIGNACION."TIPO_OPERAC"
					    AND PALOS_RV."FEC_TRANS" = ASIGNACION."FEC_TRANS"
					    AND PALOS_RV."MERCADO" = ASIGNACION."MERCADO"
					LEFT JOIN  "BSCL_VIRTUAL"."GES_TBCTMFRV" AS MOVFACTURARV
					 	ON MOVFACTURARV."SEC_MOVTO_FAC" = ASIGNACION."SEC_MOVTO_FAC"
					LEFT JOIN "BSCL.Homologacion.BD::HOM.Asignacion" AS HOM_ASIG
						ON HOM_ASIG."SEC_ASIGNACION" = ASIGNACION."SEC_ASIGNACION"					
					AND HOM_ASIG."SEC_ASIGNACION" IS NULL;

                  --REVISAMOS CUANTAS ASIGNACIONES SERAN CARGADAS
                  SELECT COUNT(*) INTO v_Total_Operaciones_Asig 
                  FROM "BSCL.Homologacion.BD::HOM.Asignacion"
                  WHERE "IdCarga" = p_IdCarga;
                  
                  --insertamos y evitamos que se inserten registros ya cargados
 				  INSERT INTO "BSCL.Orden.BD::ORD.Asignacion"
 				   (
 				    "IdAsignacion","FechaAsignacion","FechaIngresoRegistro","CantidadAsignada","MontoAsignado"
					,"ValorRescateAsignado","CantidadAsignadaRef","ValorRescateAsignadoRef","IdEstadoAsignacion","IdEstadoFacturacion"
					,"IdTransaccion","IdCarga","IdOrden","IdLibroOrdenes","EsAPlazo","IdAsignacionRef","IdCompFacturacionRef","EsOD"
					,"SecAsignacionMigracion"
				   )
				  SELECT 
				     HOM_ASIG."IdAsignacion",HOM_ASIG."FechaAsignacion",HOM_ASIG."FechaIngresoRegistro",HOM_ASIG."CantidadAsignada",HOM_ASIG."MontoAsignado"
					,HOM_ASIG."ValorRescateAsignado",HOM_ASIG."CantidadAsignadaRef",HOM_ASIG."ValorRescateAsignadoRef",HOM_ASIG."IdEstadoAsignacion",HOM_ASIG."IdEstadoFacturacion"
					,HOM_ASIG."IdTransaccion",HOM_ASIG."IdCarga",HOM_ASIG."IdOrden",HOM_ASIG."IdLibroOrdenes",HOM_ASIG."EsAPlazo",HOM_ASIG."IdAsignacionRef",HOM_ASIG."IdCompFacturacionRef",HOM_ASIG."EsOD"
					,HOM_ASIG."SecAsignacionMigracion"
				  FROM "BSCL.Homologacion.BD::HOM.Asignacion" AS HOM_ASIG
				  LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ORD_ASIG 
				  ON ORD_ASIG."SecAsignacionMigracion" = HOM_ASIG."SecAsignacionMigracion"
				  WHERE HOM_ASIG."IdCarga" = p_IdCarga
				  AND ORD_ASIG."SecAsignacionMigracion" IS NULL;
			
				  -- REVISAMOS CUANTOS REGISTROS FUERON CARGADOS
                  SELECT COUNT(*) INTO v_Total_Asig_Cargados FROM 
                  "BSCL.Orden.BD::ORD.Asignacion" WHERE "IdCarga" = p_IdCarga;
                  
                 -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	   			  p_ResumenDatosProcesados =  select 'CARGAASIGNACIONES' as "CodigoProceso" 
	   			  									,'ORD' as "Modulo" 
	   			 									,v_Total_Operaciones_Asig as "TotalRegistrosSebra"
	   			  									,v_Total_Asig_Cargados as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_ResumenDatosProcesados;

                  IF v_Total_Operaciones_Asig <> v_Total_Asig_Cargados THEN 
	   			  	 v_hash= select 'ORD_CARGA_ERR_ASIG' as "Codigo", '' as "Valor" FROM DUMMY;
				     CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ORD_CARGA_ERR_ASIG',:v_hash,:v_erroroutmsg);
				     p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_ASIG' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			  END IF;
	   			  
                  -------------------------------------------------
                  -- cargamos los cobros de la asignacion----------
				  -------------------------------------------------
                  -- cargamos los gastos
                  INSERT INTO  "BSCL.Orden.BD::ORD.CobroAsignacion"(
                     "IdCobroAsignacion","IdAsignacion" ,"IdCobro" ,"FechaIngresoCobro" ,"FechaIngresoRegistro" 
					 ,"IdPerfilCobro" ,"FormaCobro" ,"ValorCobro" ,"ValorCobroAplicado" ,"MontoCobroCalculado" 
					 ,"IdCarga"
					 )		
                   SELECT
					 "BSCL.Orden.BD.Secuencias::CobroAsignacion".NEXTVAL as "IdCobroAsignacion"
					,"IdAsignacion" as "IdAsignacion"
					,COBRO."IdCobro"
					,MOV_FAC_RV."FEC_ING_MVTO" as "FechaIngresoCobro"
					,MOV_FAC_RV."FEC_ING_MVTO" as "FechaIngresoRegistro"
					,'COBROS_MIGRACION' as "IdPerfilCobro" 
					, COBRO."FormaCobro" as "FormaCobro"
					, 0 as "ValorCobro"
					,ROUND((MOV_FAC_RV."GASTOS" * 100 )/(CASE WHEN MOV_FAC_RV."MONTO" = 0 THEN 1 ELSE MOV_FAC_RV."MONTO" END),2) AS "ValorCobroAplicado"  
					,MOV_FAC_RV."GASTOS" as  "MontoCobroCalculado"
					,p_IdCarga
					FROM "BSCL_VIRTUAL"."GES_ASIGNACION_RV" as ASIG
					INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" as SOLI
					ON SOLI."sol_sec" =  ASIG."sol_sec"
					INNER JOIN "BSCL_VIRTUAL"."GES_OPERACION_RV" as OPE_RV
					ON OPE_RV."sol_sec" = SOLI."sol_sec"
					INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMFRV" as MOV_FAC_RV
					ON ASIG."SEC_MOVTO_FAC" = MOV_FAC_RV."SEC_MOVTO_FAC"
					INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" as COBRO
					ON COBRO."IdCobro" = SOLI."sol_tipo_gasto"
					INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" SAP_ASIG
					ON SAP_ASIG."SecAsignacionMigracion" = ASIG."SEC_ASIGNACION";
					
				  -- cargamos los derechos
                  INSERT INTO  "BSCL.Orden.BD::ORD.CobroAsignacion"(
                     "IdCobroAsignacion","IdAsignacion" ,"IdCobro" ,"FechaIngresoCobro" ,"FechaIngresoRegistro" 
					 ,"IdPerfilCobro" ,"FormaCobro" ,"ValorCobro" ,"ValorCobroAplicado" ,"MontoCobroCalculado" 
					 ,"IdCarga"
					 )
                   SELECT 
					 "BSCL.Orden.BD.Secuencias::CobroAsignacion".NEXTVAL as "IdCobroAsignacion"
					,"IdAsignacion" as "IdAsignacion"
					,COBRO."IdCobro"
					,MOV_FAC_RV."FEC_ING_MVTO" as "FechaIngresoCobro"
					,MOV_FAC_RV."FEC_ING_MVTO" as "FechaIngresoRegistro"
					,'COBROS_MIGRACION' as "IdPerfilCobro" 
					, COBRO."FormaCobro" as "FormaCobro"
					, 0 as "ValorCobro"
					,ROUND((MOV_FAC_RV."DERECHOS" * 100 )/(CASE WHEN MOV_FAC_RV."MONTO" = 0 THEN 1 ELSE MOV_FAC_RV."MONTO" END),2) AS "ValorCobroAplicado"  
					,MOV_FAC_RV."DERECHOS" as  "MontoCobroCalculado"
					,p_IdCarga
					FROM "BSCL_VIRTUAL"."GES_ASIGNACION_RV" as ASIG
					INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" as SOLI
					ON SOLI."sol_sec" =  ASIG."sol_sec"
					INNER JOIN "BSCL_VIRTUAL"."GES_OPERACION_RV" as OPE_RV
					ON OPE_RV."sol_sec" = SOLI."sol_sec"
					INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMFRV" as MOV_FAC_RV
					ON ASIG."SEC_MOVTO_FAC" = MOV_FAC_RV."SEC_MOVTO_FAC"
					INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" as COBRO
					ON COBRO."IdCobro" = SOLI."sol_tipo_derecho"
					INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" SAP_ASIG
					ON SAP_ASIG."SecAsignacionMigracion" = ASIG."SEC_ASIGNACION";
				--comision
                  INSERT INTO  "BSCL.Orden.BD::ORD.CobroAsignacion"(
                     "IdCobroAsignacion","IdAsignacion" ,"IdCobro" ,"FechaIngresoCobro" ,"FechaIngresoRegistro" 
					 ,"IdPerfilCobro" ,"FormaCobro" ,"ValorCobro" ,"ValorCobroAplicado" ,"MontoCobroCalculado" 
					 ,"IdCarga"
					 )		
                   SELECT
					 "BSCL.Orden.BD.Secuencias::CobroAsignacion".NEXTVAL as "IdCobroAsignacion"
					,"IdAsignacion" as "IdAsignacion"
					,COBRO."IdCobro"
					,MOV_FAC_RV."FEC_ING_MVTO" as "FechaIngresoCobro"
					,MOV_FAC_RV."FEC_ING_MVTO" as "FechaIngresoRegistro"
					,'COBROS_MIGRACION' as "IdPerfilCobro" 
					, COBRO."FormaCobro" as "FormaCobro"
					, 0 as "ValorCobro"
					,ROUND((MOV_FAC_RV."COMISION" * 100 )/(CASE WHEN MOV_FAC_RV."MONTO" = 0 THEN 1 ELSE MOV_FAC_RV."MONTO" END),2) AS "ValorCobroAplicado"  
					,MOV_FAC_RV."COMISION" as  "MontoCobroCalculado"
					,p_IdCarga
					FROM "BSCL_VIRTUAL"."GES_ASIGNACION_RV" as ASIG
					INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" as SOLI
					ON SOLI."sol_sec" =  ASIG."sol_sec"
					INNER JOIN "BSCL_VIRTUAL"."GES_OPERACION_RV" as OPE_RV
					ON OPE_RV."sol_sec" = SOLI."sol_sec"
					INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMFRV" as MOV_FAC_RV
					ON ASIG."SEC_MOVTO_FAC" = MOV_FAC_RV."SEC_MOVTO_FAC"
					INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" as COBRO
					ON COBRO."IdCobro" = SOLI."sol_tipo_comision"
					INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" SAP_ASIG
					ON SAP_ASIG."SecAsignacionMigracion" = ASIG."SEC_ASIGNACION";

		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
