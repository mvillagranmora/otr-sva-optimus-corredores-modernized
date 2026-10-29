SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarCobrosComprobanteDetalle";

CREATE PROCEDURE
--  
--  Proposito: servicios de carga de MOVIMIENTOS A FACTURAR RV
--  renta variable 
--  Autor: - EAR 
"BSCL.Homologacion.BD.Servicios::CargarCobrosComprobanteDetalle"(

	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD

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

	-- declaracion variables
	   DECLARE v_total_detcom_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_detcom_cargadas DECIMAL(26,8):=0;	    
	   
	   DECLARE v_Quantity BIGINT:=0;
	   DECLARE v_PerId NVARCHAR(11);
		--------------------
		--Negocio Aqui
		--------------------

		-- convierto el rut a formato sebra 
		select lpad(p_RutCliente,11) into p_RutCliente from dummy;
		select TRIM(SUBSTRING(p_RutCliente,1,9)) into v_PerId from dummy;

		 
		delete from "BSCL"."BSCL.Homologacion.BD::HOM.Error" where "Entidad" = 'DETALLECOMPROBANTE'
		and "IdCarga" = p_IdCarga;


		 IF P_Modalidad = 'R' THEN --REPROCESO

	        DELETE FROM "BSCL.Homologacion.BD::HOM.ComprobanteCobro" AS HOM
	        WHERE HOM."IdComprobanteCobro" IN ( SELECT HOM."IdComprobanteCobro" from "BSCL.Homologacion.BD::HOM.ComprobanteCobro" AS HOM
							                      LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteCobro"  AS DETALLE
							           				 ON DETALLE."IdComprobanteCobro" = HOM."IdComprobanteCobro"
							           				  WHERE HOM."IdCarga" = p_IdCarga
							           				  AND DETALLE."IdComprobanteCobro" IS NULL);
		 END IF;

	v_cobros = 	SELECT TBCTMSCF."MONTO_COMISION" as "MONTO",TBCTMSCF."FOLIO_COMP_FAC",TBCTMSCF."TIPO_COMP_FAC",'COFI' as "TIPO_COBRO"  
					FROM "BSCL.Homologacion.BD::HOM.Comprobante" AS HOM
					INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" AS TBCTMSCF
					ON TBCTMSCF."FOLIO_COMP_FAC" =  HOM."FOLIO_COMP_FAC"
					AND TBCTMSCF."TIPO_COMP_FAC" = HOM."TIPO_COMP_FAC"
					AND TBCTMSCF."MONTO_COMISION" > 0
					union all
					select TBCTMSCF."MONTO_GASTO" as "MONTO",TBCTMSCF."FOLIO_COMP_FAC" ,TBCTMSCF."TIPO_COMP_FAC",'GAFI'as "TIPO_COBRO" 
					FROM "BSCL.Homologacion.BD::HOM.Comprobante"  AS HOM
					INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" AS TBCTMSCF
					ON TBCTMSCF."FOLIO_COMP_FAC" =  HOM."FOLIO_COMP_FAC"
					AND TBCTMSCF."TIPO_COMP_FAC" = HOM."TIPO_COMP_FAC"
					AND TBCTMSCF."MONTO_GASTO" >0
					union all
					select TBCTMSCF."MONTO_DERECHOS" as "MONTO" ,TBCTMSCF."FOLIO_COMP_FAC" ,TBCTMSCF."TIPO_COMP_FAC",'DEFI' as "TIPO_COBRO"
					FROM "BSCL.Homologacion.BD::HOM.Comprobante"  AS HOM
					INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" AS TBCTMSCF
					ON TBCTMSCF."FOLIO_COMP_FAC" =  HOM."FOLIO_COMP_FAC"
					AND TBCTMSCF."TIPO_COMP_FAC" = HOM."TIPO_COMP_FAC"
					AND TBCTMSCF."MONTO_DERECHOS" >0
					union all
					select TBCTMSCF."MONTO_IVA" as "MONTO",TBCTMSCF."FOLIO_COMP_FAC" ,TBCTMSCF."TIPO_COMP_FAC" ,'IVA'as "TIPO_COBRO"
					FROM "BSCL.Homologacion.BD::HOM.Comprobante"  AS HOM
					INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" AS TBCTMSCF
					ON TBCTMSCF."FOLIO_COMP_FAC" =  HOM."FOLIO_COMP_FAC"
					AND TBCTMSCF."TIPO_COMP_FAC" = HOM."TIPO_COMP_FAC"
					AND TBCTMSCF."MONTO_IVA" >0
					union all
					select TBCTMSCF."MONTO_EXENTO" as "MONTO" ,TBCTMSCF."FOLIO_COMP_FAC" ,TBCTMSCF."TIPO_COMP_FAC" ,'COEX'as "TIPO_COBRO"
					FROM "BSCL.Homologacion.BD::HOM.Comprobante"  AS HOM
					INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" AS TBCTMSCF
					ON TBCTMSCF."FOLIO_COMP_FAC" =  HOM."FOLIO_COMP_FAC"
					AND TBCTMSCF."TIPO_COMP_FAC" = HOM."TIPO_COMP_FAC"
					AND TBCTMSCF."MONTO_EXENTO" >0;
					
		INSERT INTO "BSCL.Homologacion.BD::HOM.ComprobanteCobro"(    
				 "IdComprobanteCobro","IdCobro","IdComprobante","IdMoneda","Monto","Porcentaje","FechaSistema","FechaIngresoRegistro" 
				,"IdEstado","IdMonedaComprobanteCobro","IdMonedaLiquidacion","MontoComprobanteCobro","MontoLiquidacion","IdCarga"
				,"FOLIO_COMP_FAC","TIPO_COMP_FAC")
        SELECT "BSCL.Facturacion.BD.Secuencias::ComprobanteCobro".NEXTVAL  as "IdComprobanteCobro" 
			,COBROS_TEMP."TIPO_COBRO" as "IdCobro" 
			,HOMOLOGACION."IdComprobante" as "IdComprobante" 
			,TBCTMSCF."MON_MORIGEN" as "IdMoneda" 
			,COBROS_TEMP."MONTO" as "Monto" 
			,0 as "Porcentaje" 
			,TBCTMSCF."FEC_ING_COMP" as "FechaSistema" 
			,TBCTMSCF."FEC_ING_COMP" as "FechaIngresoRegistro" 
			,'ACT' as "IdEstado" 
			,TBCTMSCF."MON_MORIGEN" as "IdMonedaComprobanteCobro" 
			,TBCTMSCF."MON_MORIGEN" as "IdMonedaLiquidacion" 
			,COBROS_TEMP."MONTO" as "MontoComprobanteCobro" 
			,COBROS_TEMP."MONTO" as "MontoLiquidacion" 
			,p_IdCarga	as "IdCarga"
			,TBCTMSCF."FOLIO_COMP_FAC" as "FOLIO_COMP_FAC"
			,TBCTMSCF."TIPO_COMP_FAC" as "TIPO_COMP_FAC"
		FROM :v_cobros AS COBROS_TEMP
		INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" AS TBCTMSCF
			ON TBCTMSCF."FOLIO_COMP_FAC" = COBROS_TEMP."FOLIO_COMP_FAC"
			AND TBCTMSCF."TIPO_COMP_FAC" = COBROS_TEMP."TIPO_COMP_FAC"
		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" as HOMOLOGACION
			ON HOMOLOGACION."FOLIO_COMP_FAC" = COBROS_TEMP."FOLIO_COMP_FAC"
			AND HOMOLOGACION."TIPO_COMP_FAC" = COBROS_TEMP."TIPO_COMP_FAC"
		LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteCobro" as COBROS_HOM
		    ON COBROS_HOM."FOLIO_COMP_FAC" = HOMOLOGACION."FOLIO_COMP_FAC"
			AND COBROS_HOM."TIPO_COMP_FAC" = HOMOLOGACION."TIPO_COMP_FAC"
			and COBROS_HOM."IdCobro" = COBROS_TEMP."TIPO_COBRO"
		WHERE COBROS_HOM."FOLIO_COMP_FAC" IS NULL;

		select count(*) into v_total_detcom_gestion from :v_cobros;
		select count(*) into v_total_detcom_cargadas from "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteCobro"
		where "IdCarga" = p_IdCarga;
    
		-- SACAMOS LOS REGISTROS  comprobantes
	   	p_ResumenDatosProcesados =  select 'CARGACOMPROBANTECOBRO' as "CodigoProceso" 
   			  							,'FAC' as "Modulo" 
   			 							,v_total_detcom_gestion as "TotalRegistrosSebra"
   			  							,v_total_detcom_cargadas as "TotalRegistrosSap"  FROM DUMMY
   							    	union all
   							    	select * from :p_ResumenDatosProcesados;
    	
   		IF v_total_detcom_gestion <> v_total_detcom_cargadas THEN 
			p_out_ErroresDetalle =  select 'ERR_CARGA_COBROS_COMPROBANTE' as "Codigo",'existen diferencias en la carga de cobros' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;
			p_out_ErroresContador= p_out_ErroresContador+1;		    	       			 	
	   	END IF;
	   	
	   	-- todas las operaciones rv deben tener un registro el la solicitud
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'COBROSCOMPROBANTE' as "Entidad"
                    ,'No se cargo el comprobante en HOM Comprobante' as "Mensaje"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico1"
                    , TBCTMSCF."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FOLIO_COMP_FAC' as "NombreCampoGenerico6"
                    , TBCTMSCF."FOLIO_COMP_FAC" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
				  FROM :v_cobros AS COBROS_TEMP
			INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" AS TBCTMSCF
				ON TBCTMSCF."FOLIO_COMP_FAC" = COBROS_TEMP."FOLIO_COMP_FAC"
				AND TBCTMSCF."TIPO_COMP_FAC" = COBROS_TEMP."TIPO_COMP_FAC"
			LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" as HOMOLOGACION
				ON HOMOLOGACION."FOLIO_COMP_FAC" = COBROS_TEMP."FOLIO_COMP_FAC"
				AND HOMOLOGACION."TIPO_COMP_FAC" = COBROS_TEMP."TIPO_COMP_FAC"
			WHERE HOMOLOGACION."FOLIO_COMP_FAC" IS NULL;	

		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
