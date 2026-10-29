SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::Principal_Carga_IF";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::Principal_Carga_IF"
	(  
			IN  pIdCarga 	       		BIGINT,           -- ID de la carga 
			IN  pModalidad        	 	NVARCHAR(5000),   -- Ingreso I incremental, 
			IN  pInversa 				NVARCHAR(5000),	  -- S carga cartera inversa perdiodo anterios N no,
			IN  pRutCliente       	 	NVARCHAR(5000) default '0-0',    
			IN  pPeriodoInicial     	NVARCHAR(5000)   	
    ) 
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA 
	AS   
	BEGIN
		-- VARIABLES VALIDACIONES BASICAS
		DECLARE  p_IdUsuario		   	BIGINT DEFAULT 66;						-- ID del usuario que solicita el servicio
		DECLARE p_IdModulo         	 	NVARCHAR(5000) DEFAULT 'CUS';    -- modulo 
		DECLARE p_IdEmpresa		   		BIGINT DEFAULT 1;						-- ID Empresa destino
		
		DECLARE v_out_Resultado  		CHAR(1); 	   --RESULTADO S/N/E
	    DECLARE v_out_Mensaje	 		NVARCHAR(5000); --MENSAJE SALIDA
	    
	    DECLARE v_erroroutmsg  			nvarchar(5000);    
	    DECLARE v_TotalPortafolioLeidos	BIGINT;	    
	    DECLARE v_TotalCuentasGrabadas	BIGINT;
	    
	    DECLARE v_ErroresContador		INT :=0;
	    
	    -- Para LOg
	    DECLARE v_out_refID				BIGINT;    
	    DECLARE v_out_ErroresContador	BIGINT; 
	    DECLARE v_out_ErroresDetalle 	table ("Codigo" nvarchar(5000),"Descripcion" nvarchar(520));	
	    
	    DECLARE v_FechaSistema 			NVARCHAR(5000);
	    DECLARE v_MaxPeriodo			NVARCHAR(5000);
	    DECLARE v_FechaInicioPeriodo 	NVARCHAR(5000);
	    DECLARE v_RutCli 				NVARCHAR(5000):='0-0';
	    DECLARE v_MonedaSistema 		NVARCHAR(5000);
	    DECLARE Concepto_Egre 			NVARCHAR(5000) default '';
	    DECLARE Concepto_Ingre 			NVARCHAR(5000) default '';

      	DECLARE in_FechaInicialant      NVARCHAR(5000);
      	DECLARE	in_out_Contador 		int;
--	    DECLARE v_periodo  				DATE := CONCAT(SUBSTRING(pPeriodoInicial,1,7),'-01');
--      DECLARE v_periodo_ant  			DATE := CONCAT(SUBSTRING(pPeriodoInicial,1,7),'-01');


	CALL "BSCL.Homologacion.BD.Servicios::EnhControlIntegridad_IF"
		(  
		 	   p_IdCarga 				=> :pIdCarga         	--	IN  p_IdCarga 	       	 BIGINT,          	-- ID de la carga 
	   		,  p_Periodo_Inicial 		=> :pPeriodoInicial 	--  IN  p_Periodo_Inicial    NVARCHAR(07),   	--YYYY-MM-DD
		); 	

	select count(*) into v_ErroresContador
	  from "BSCL"."BSCL.Homologacion.BD::HOM.Error" as Errores
	 where  Errores."IdCarga" = pIdCarga;

	IF v_ErroresContador > 0 THEN
    	select Errores.* 
	  	  from "BSCL"."BSCL.Homologacion.BD::HOM.Error" as Errores
	 	 where  Errores."IdCarga" = pIdCarga;
	ELSE
		CALL "BSCL"."BSCL.Homologacion.BD.Servicios::CargarCarteraIF"
 			(	
			  p_IdCarga   		=> :pIdCarga 		-- IN  p_IdCarga 		 
			 ,P_Modalidad 		=> :pModalidad 		-- 'I' Ingreso I incremental -- borra el id de carga en homolog. y tabla sap. carga con nuevos parametros, 
			 ,P_Inversa			=> :pInversa		-- 'N'  S carga cartera inversa periodo anterior -- N no carga cartera inversa periodo anterior, 
			 ,p_RutCliente 		=> :pRutCliente		-- IN  p_RutCliente 
			 ,p_Periodo_Inicial => :pPeriodoInicial -- periodo inicial
	 	); 	 	 

		---------------------------------------------------------------------------------
		---- cuadra cartera sebra vs sap, deben ingresar parametros a cuadrar.
		---------------------------------------------------------------------------------
		select 	periodo,rut,cuenta,nemo
				,sum(ValorRescate_optimus_positivo) as cant_optimus_positivo
				,sum(ValorRescate_sebra_positivo) 	as cant_sebra_positivo
				,sum(ValorRescate_optimus_negativo) as cant_optimus_negativo
				,sum(ValorRescate_sebra_negativo)   as cant_sebra_negativo 
				,sum(ValorRescate_Reversa)   		as Reversa_optimus 
				
		from (
		
				SELECT 	GES_TBCTCIIF."COD_PER" as periodo ,
						GES_TBCTCIIF."RUT_CLI" as rut ,
						GES_TBCTCIIF."SEC_RUT_CLI" as cuenta ,
						TRIM(GES_TBCTCIIF."NEMO_SVS") as nemo,
						0 as ValorRescate_optimus_positivo, 
						SUM(GES_TBCTCIIF."VAL_RESC") as ValorRescate_sebra_positivo,
						0 as ValorRescate_optimus_negativo,
						0 as ValorRescate_sebra_negativo,
						0 as ValorRescate_Reversa
				FROM 	"BSCL_VIRTUAL"."GES_TBCTCIIF" as GES_TBCTCIIF
				WHERE 	GES_TBCTCIIF."COD_PER" = pPeriodoInicial
				  AND 	GES_TBCTCIIF."CAN_ACTUAL" > 0
				GROUP 	BY 
						GES_TBCTCIIF."COD_PER",
						GES_TBCTCIIF."SEC_RUT_CLI",
						GES_TBCTCIIF."RUT_CLI",
						TRIM(GES_TBCTCIIF."NEMO_SVS")
						
				UNION
				SELECT 	GES_TBCTCIIF."COD_PER" 			as periodo ,
						GES_TBCTCIIF."RUT_CLI" 			as rut ,
						GES_TBCTCIIF."SEC_RUT_CLI" 		as cuenta ,
						TRIM(GES_TBCTCIIF."NEMO_SVS") 	as nemo,
						0 								as ValorRescate_optimus_positivo, 
						0 								as ValorRescate_sebra_positivo,
						0 								as ValorRescate_optimus_negativo,
						SUM(GES_TBCTCIIF."VAL_RESC") 	as ValorRescate_sebra_negativo,
						0 as ValorRescate_Reversa
				FROM 	"BSCL_VIRTUAL"."GES_TBCTCIIF" as GES_TBCTCIIF
				WHERE 	GES_TBCTCIIF."COD_PER" = pPeriodoInicial
				  AND 	GES_TBCTCIIF."CAN_ACTUAL"  <  0
				GROUP 	BY 
						GES_TBCTCIIF."COD_PER",
						GES_TBCTCIIF."SEC_RUT_CLI",
						GES_TBCTCIIF."RUT_CLI",
						TRIM(GES_TBCTCIIF."NEMO_SVS")
						
				UNION 
				SELECT 	Transito."COD_PER" as periodo ,
						Transito."RUT_CLI" as rut ,
						Transito."SEC_RUT_CLI" as cuenta ,
						TRIM(Transito."NemoSVS")as nemo,
						sum(Transito."INGValorRescate") ,
						-- sum(Transito."EGRValorRescate") as ValorRescate_optimus_positivo,
						0 as ValorRescate_sebra_positivo,
						0 as ValorRescate_optimus_negativo,
						0 as ValorRescate_sebra_negativo,
						sum(Transito."EGRValorRescate") as ValorRescate_Reversa
				FROM 	"BSCL"."BSCL.Homologacion.BD::HOM.Cartera" as Transito
				WHERE 	Transito."IdCarga" = pIdCarga
				GROUP BY
						Transito."COD_PER",				 
						Transito."SEC_RUT_CLI",
						Transito."RUT_CLI",
						TRIM(Transito."NemoSVS")
									
				UNION
				SELECT 	Transito."COD_PER" as periodo ,
						Transito."RUT_CLI" as rut ,
						Transito."SEC_RUT_CLI" as cuenta ,
						TRIM(Transito."NemoSVS")as nemo,
						0 as ValorRescate_optimus_positivo,																
						0 as ValorRescate_sebra_positivo,
						sum(Transito."ValorRescate") as ValorRescate_optimus_negativo,
						0 as ValorRescate_sebra_negativo,
						0 as ValorRescate_Reversa
				FROM 	"BSCL"."BSCL.Homologacion.BD::HOM.MovimientoCarteraEnTransito" as Transito
				WHERE 	Transito."IdCarga" = pIdCarga
				GROUP BY
						Transito."COD_PER",				 
						Transito."SEC_RUT_CLI",
						Transito."RUT_CLI",
						TRIM(Transito."NemoSVS")
						
			) as data
		where 
		periodo = pPeriodoInicial 
		group by periodo,rut,cuenta,nemo			
		order by 1,2,3,4;
				
	END IF;
	
	
--------------------------------------------------------------------------------------------------------------------				
--------------------------------------------------------------------------------------------------------------------
--SIGNAL SALIDA_CONTROLADA;
-- ENB 2017-06-25 02:15:00

END;
