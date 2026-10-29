SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarOrdenesIF";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarOrdenesIF"(
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
	   DECLARE v_total_ord_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_ord_cargadas DECIMAL(26,8):=0; 
	   DECLARE v_Total_operaciones BIGINT:=0;
	   DECLARE v_Total_Ord_Cargados BIGINT:=0;
	   DECLARE v_Total_Asig_Cargados BIGINT:=0;
	   DECLARE v_Total_Operaciones_Asig BIGINT:=0;
	   DECLARE v_Total_Cobros_Ordenes BIGINT:= 0;
	   DECLARE v_Total_Cobros_Cargados BIGINT:=0; 
       DECLARE v_GrupoCobro BIGINT:=0;

    
		--------------------
		--Negocio Aqui
		--------------------

		-- convierto el rut a formato sebra 
		IF P_Modalidad = 'R' THEN --REPROCESO
		 DELETE FROM "BSCL.Orden.BD::ORD.Orden"
         WHERE "IdCarga" = 	p_IdCarga;	
		 
		 DELETE FROM "BSCL.Homologacion.BD::HOM.Orden"
         WHERE "IdCarga" = 	p_IdCarga;	
		 
		 DELETE FROM "BSCL.Orden.BD::ORD.CobroOrden"
		 WHERE "IdCarga" = 	p_IdCarga;
		END IF;
		 
		      call  "BSCL.Homologacion.BD.Servicios::CargarOrdenesIF_Insert"(
		      	  p_IdEmpresa		 
     			 ,p_IdUsuario		   
	 			 ,p_IdCarga 			  
	             ,p_Modulo             
                 ,P_Modalidad         
                 ,p_RutCliente         
	             ,p_FechaInicial     
	             ,p_FechaFinal		 
	             ,p_fechaCambio        
		      );

              ---------------------------------------------------------------------------
              -- llenamos la tabla de errores de homologacion esta contiene --
              -- los el error por el cual el registro no fue cargado y contiene
              -- los identificadores para poder localizar el registro  en sebra 
              -- los campos son genericos se utilizaran de acuerdo a la necesidad de cada 
              -- servicio
              ---------------------------------------------------------------------------
             
               select count(*) into v_total_ord_gestion
               FROM "BSCL_VIRTUAL"."GES_OPERACION_IF" AS OPE_ANT 
			   INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
					ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
					AND SOLI_ANT."sol_estado" not in ('NN','SR')
					AND SOLI_ANT."MERCADO" = 'IF'
					AND SOLI_ANT."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END
			   INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
			   ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec" 
               WHERE  OPE_ANT."oif_fec_orden" >=p_FechaInicial
				 AND OPE_ANT."oif_fec_orden" <= p_FechaFinal;
				 
              -- todas las operaciones if deben tener un registro el la solicitud
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'ORDENESIF' as "Entidad"
                    ,'OPERACION IF sin registro de SOLICITUD en sebra' as "Mensaje"
                    , '' as "NombreCampoGenerico1"
                    , '' as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SOL_SEC' as "NombreCampoGenerico6"
                    , coalesce(OPE_ANT."sol_sec",0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_OPERACION_IF" AS OPE_ANT 
              LEFT JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
              ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
			   AND SOLI_ANT."sol_estado" not in ('NN','SR')--VERIFICAR ESTOS ESTADOS
               AND SOLI_ANT."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END 
			   --AND SOLI_ANT."sol_fecha" >=p_FechaInicial
			   --AND SOLI_ANT."sol_fecha" <= p_FechaFinal
			  AND SOLI_ANT."MERCADO" = 'IF'
			  WHERE  SOLI_ANT."sol_sec" IS NULL
			   AND OPE_ANT."oif_fec_orden" >=p_FechaInicial
			   AND OPE_ANT."oif_fec_orden" <= p_FechaFinal;
			  
			  -- todas las solicitudes deben tener un registro en orden 
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'ORDENESIF' as "Entidad"
                    ,'SOLICITUD IF sin registro de ORDEN en sebra' as "Mensaje"
                    , '' as "NombreCampoGenerico1"
                    , '' as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'sol_sec' as "NombreCampoGenerico6"
                    , coalesce(SOLI_ANT."sol_sec",0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_OPERACION_IF" AS OPE_ANT 
              INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
              ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
			   AND SOLI_ANT."sol_estado" not in ('NN','SR')--VERIFICAR ESTOS ESTADOS
			   --AND SOLI_ANT."sol_fecha" >=p_FechaInicial
			   --AND SOLI_ANT."sol_fecha" <= p_FechaFinal
               AND SOLI_ANT."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END 
			   AND SOLI_ANT."MERCADO" = 'IF'
			  LEFT JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
					ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec"
			  WHERE ORD_ANT."sol_sec" IS NULL
			  	AND OPE_ANT."oif_fec_orden" >=p_FechaInicial
			   	AND OPE_ANT."oif_fec_orden" <= p_FechaFinal;

			  
			 -- todas las operaciones rv su instrumento debe existir en optimus
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'ORDENESIF' as "Entidad"
                    ,'NEMOTECNICO de la OPERACION IF no existe en OPTIMUS' as "Mensaje"
                    , 'oif_nemo' as "NombreCampoGenerico1"
                    , coalesce(TRIM(OPE_ANT."oif_nemo"),'') as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'sol_sec' as "NombreCampoGenerico6"
                    , coalesce(OPE_ANT."sol_sec",0) as "ValorCampoGenerico6"
                     ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_OPERACION_IF" AS OPE_ANT 
			    INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
					ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
					AND SOLI_ANT."sol_estado" not in ('NN','SR')--VERIFICAR ESTOS ESTADOS
					--AND SOLI_ANT."sol_fecha" >= p_FechaInicial
			        --AND SOLI_ANT."sol_fecha" <= p_FechaFinal
                    AND SOLI_ANT."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END  
			        AND SOLI_ANT."MERCADO" = 'IF'
				INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
					ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento 
					ON Instrumento."Nemotecnico" = TRIM(OPE_ANT."oif_nemo")
				WHERE Instrumento."Nemotecnico" IS NULL
					AND OPE_ANT."oif_fec_orden" >=p_FechaInicial
			   		AND OPE_ANT."oif_fec_orden" <= p_FechaFinal;
			
			 -- el tipo instrumento del instrumento optimus no existe
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'ORDENESIF' as "Entidad"
                    ,'TIPO INSTRUMENTO del INSTRUMENTO no existe en OPTIMUS' as "Mensaje"
                    , 'orf_nemo' as "NombreCampoGenerico1"
                    , coalesce(TRIM(OPE_ANT."oif_nemo"),'') as "ValorCampoGenerico1" 
                    ,'IdTipoInstrumento' as "NombreCampoGenerico2" 
                    , coalesce(Instrumento."IdTipoInstrumento",0) as "NombreCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'sol_sec' as "NombreCampoGenerico6"
                    , coalesce(OPE_ANT."sol_sec",0) as "ValorCampoGenerico6"
                    ,'IdInstrumento' as "NombreCampoGenerico7"
                    , coalesce(Instrumento."IdInstrumento",0) as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
				FROM "BSCL_VIRTUAL"."GES_OPERACION_IF" AS OPE_ANT 
				INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
					ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
					AND SOLI_ANT."sol_estado" not in ('NN','SR')--VERIFICAR ESTOS ESTADOS
					--AND SOLI_ANT."sol_fecha" >= p_FechaInicial
			        --AND SOLI_ANT."sol_fecha" <= p_FechaFinal
					AND SOLI_ANT."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END  
			        AND SOLI_ANT."MERCADO" = 'IF'
				INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
					ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec"
				INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento 
					ON Instrumento."Nemotecnico" = TRIM(OPE_ANT."oif_nemo")
				LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
				    ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
				WHERE TipoInstrumento."IdTipoInstrumento" IS NULL
					AND OPE_ANT."oif_fec_orden" >=p_FechaInicial
			   		AND OPE_ANT."oif_fec_orden" <= p_FechaFinal;

			 -- no existe cliente creado para el rut de la operacion
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'ORDENESIF' as "Entidad"
                    ,'el CLIENTE no tiene creado la CUENTA en sebra' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , coalesce(SOLI_ANT."RUT_CLI",'') as "ValorCampoGenerico1" 
                    , 'CUENTA' as "NombreCampoGenerico2" 
                    , coalesce(to_char(SOLI_ANT."CUENTA"),'') as "NombreCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'sol_sec' as "NombreCampoGenerico6"
                    , coalesce(OPE_ANT."sol_sec",0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              FROM "BSCL_VIRTUAL"."GES_OPERACION_IF" AS OPE_ANT 
				INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
					ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
					AND SOLI_ANT."sol_estado" not in ('NN','SR')--VERIFICAR ESTOS ESTADOS
					--AND SOLI_ANT."sol_fecha" >= p_FechaInicial
			        --AND SOLI_ANT."sol_fecha" <= p_FechaFinal
					AND SOLI_ANT."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END  
			        AND SOLI_ANT."MERCADO" = 'IF'
				INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
					ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec"
				INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento 
					ON Instrumento."Nemotecnico" = TRIM(OPE_ANT."oif_nemo")
				INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
				    ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
				LEFT JOIN "BSCL_VIRTUAL"."GES_CUENTA" AS CUENTA 
					ON CUENTA."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END
					AND CUENTA."CUENTA" = SOLI_ANT."CUENTA"
                WHERE CUENTA."RUT_CLI" IS NULL
                	AND OPE_ANT."oif_fec_orden" >=p_FechaInicial
			   		AND OPE_ANT."oif_fec_orden" <= p_FechaFinal;
				
			 --  no existe el cliente optimus 
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'ORDENESIF' as "Entidad"
                    ,'EL CLIENTE sebra no tiene cargado su DOCUMENTO IDENTIDAD en optimus' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , coalesce(TRIM(SOLI_ANT."RUT_CLI"),'') as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "NombreCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'sol_sec' as "NombreCampoGenerico6"
                    , coalesce(OPE_ANT."sol_sec",0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              FROM "BSCL_VIRTUAL"."GES_OPERACION_IF" AS OPE_ANT 
				INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
					ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
					AND SOLI_ANT."sol_estado" not in ('NN','SR')--VERIFICAR ESTOS ESTADOS
                    AND SOLI_ANT."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END  
					--AND SOLI_ANT."sol_fecha" >= p_FechaInicial
			        --AND SOLI_ANT."sol_fecha" <= p_FechaFinal
			        AND SOLI_ANT."MERCADO" = 'IF'
				INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
					ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec"
				INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento 
					ON Instrumento."Nemotecnico" = TRIM(OPE_ANT."oif_nemo")
				INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
				    ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
				INNER JOIN "BSCL_VIRTUAL"."GES_CUENTA" AS CUENTA 
					ON CUENTA."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END
					AND CUENTA."CUENTA" =  SOLI_ANT."CUENTA"
				LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC
   					ON DOC."DocumentoIdentidad" = TRIM(CUENTA."RUT_CLI")
                WHERE DOC."DocumentoIdentidad" IS NULL
                AND OPE_ANT."oif_fec_orden" >=p_FechaInicial
			   		AND OPE_ANT."oif_fec_orden" <= p_FechaFinal;
                
                --  revisamos si tiene cargada la persona 
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'ORDENESIF' as "Entidad"
                    ,'la PERSONA sebra no tiene cargado entidad PERSONA en optimus' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , COALESCE(TRIM(SOLI_ANT."RUT_CLI"),'') as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "NombreCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'sol_sec' as "NombreCampoGenerico6"
                    , COALESCE(OPE_ANT."sol_sec",0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              FROM "BSCL_VIRTUAL"."GES_OPERACION_IF" AS OPE_ANT 
				INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
					ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
					AND SOLI_ANT."sol_estado" not in ('NN','SR')--VERIFICAR ESTOS ESTADOS
					--AND SOLI_ANT."sol_fecha" >= p_FechaInicial
			        --AND SOLI_ANT."sol_fecha" <= p_FechaFinal
                    AND SOLI_ANT."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END  
			        AND SOLI_ANT."MERCADO" = 'IF'
				INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
					ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec"
				INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento 
					ON Instrumento."Nemotecnico" = TRIM(OPE_ANT."oif_nemo")
				INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
				    ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
				INNER JOIN "BSCL_VIRTUAL"."GES_CUENTA" AS CUENTA 
					ON CUENTA."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END
					AND CUENTA."CUENTA" =  SOLI_ANT."CUENTA"
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC
   					ON DOC."DocumentoIdentidad" = TRIM(CUENTA."RUT_CLI")
   				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER
    				 ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"	
                WHERE PER."IdDocumentoIdentidad" IS NULL
                	AND OPE_ANT."oif_fec_orden" >=p_FechaInicial
			   		AND OPE_ANT."oif_fec_orden" <=p_FechaFinal; 
                
              -- debe exixtir la entidad cliente  
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'ORDENESIF' as "Entidad"
                    ,'la CLIENTE sebra no tiene cargado entidad CLIENTE en optimus' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , COALESCE(TRIM(SOLI_ANT."RUT_CLI"),'') as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "NombreCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'sol_sec' as "NombreCampoGenerico6"
                    , COALESCE(OPE_ANT."sol_sec",0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              FROM "BSCL_VIRTUAL"."GES_OPERACION_IF" AS OPE_ANT 
				INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
					ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
					AND SOLI_ANT."sol_estado" not in ('NN','SR')--VERIFICAR ESTOS ESTADOS
					--AND SOLI_ANT."sol_fecha" >= p_FechaInicial
			        --AND SOLI_ANT."sol_fecha" <= p_FechaFinal
                    AND SOLI_ANT."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END  
			        AND SOLI_ANT."MERCADO" = 'IF'
				INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
					ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec"
				INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento 
					ON Instrumento."Nemotecnico" = TRIM(OPE_ANT."oif_nemo")
				INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
				    ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
				INNER JOIN "BSCL_VIRTUAL"."GES_CUENTA" AS CUENTA 
					ON CUENTA."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END
					AND CUENTA."CUENTA" =  SOLI_ANT."CUENTA"
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC
   					ON DOC."DocumentoIdentidad" = TRIM(CUENTA."RUT_CLI")
   				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER
    				 ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"	
    			LEFT JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as Cliente
    				ON Cliente."IdPersona" = PER."IdPersona"	 
                WHERE Cliente."IdPersona" IS NULL
                	AND OPE_ANT."oif_fec_orden" >=p_FechaInicial
			   		AND OPE_ANT."oif_fec_orden" <=p_FechaFinal;
                   
              -- debe existir la entidad Portafolio  
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'ORDENES' as "Entidad"
                    ,'la CUENTA sebra no tiene cargado entidad PORTAFOLIO en optimus' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , COALESCE(TRIM(SOLI_ANT."RUT_CLI"),'') as "ValorCampoGenerico1" 
                    , 'CUENTA' as "NombreCampoGenerico2" 
                    , COALESCE(to_char(CUENTA."CUENTA"),'') as "NombreCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'sol_sec' as "NombreCampoGenerico6"
                    , COALESCE(OPE_ANT."sol_sec",0) as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              FROM "BSCL_VIRTUAL"."GES_OPERACION_IF" AS OPE_ANT 
				INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
					ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
					AND SOLI_ANT."sol_estado" not in ('NN','SR')--VERIFICAR ESTOS ESTADOS
					--AND SOLI_ANT."sol_fecha" >= p_FechaInicial
			        --AND SOLI_ANT."sol_fecha" <= p_FechaFinal
                    AND SOLI_ANT."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END  
			        AND SOLI_ANT."MERCADO" = 'IF'
				INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
					ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec"
				INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento 
					ON Instrumento."Nemotecnico" = TRIM(OPE_ANT."oif_nemo")
				INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
				    ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
				INNER JOIN "BSCL_VIRTUAL"."GES_CUENTA" AS CUENTA 
					ON CUENTA."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END
					AND CUENTA."CUENTA" =  SOLI_ANT."CUENTA"
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC
   					ON DOC."DocumentoIdentidad" = TRIM(CUENTA."RUT_CLI")
   				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER
    				 ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"	
    			INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as Cliente
    				ON Cliente."IdPersona" = PER."IdPersona"
    			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio 
					ON Portafolio."IdCliente" = Cliente."IdCliente"
					AND Portafolio."CodigoPortafolio" = TO_VARCHAR(CUENTA."CUENTA")
                WHERE Portafolio."IdPortafolio" IS NULL
                    AND OPE_ANT."oif_fec_orden" >=p_FechaInicial
			   		AND OPE_ANT."oif_fec_orden" <=p_FechaFinal;
                
               select count(*) into v_total_ord_gestion
               FROM "BSCL_VIRTUAL"."GES_OPERACION_IF" AS OPE_ANT 
			   INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
					ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
					AND SOLI_ANT."sol_estado" not in ('NN','SR')
				    AND SOLI_ANT."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END
			        AND SOLI_ANT."MERCADO" = 'IF'
			   INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
			   ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec"
               WHERE OPE_ANT."oif_fec_orden" >=p_FechaInicial
			   	 AND OPE_ANT."oif_fec_orden" <=p_FechaFinal;
			   		
                -- CONTABILIZAMOS EL MONTO DE OPERACIONES
             	SELECT COUNT(*) INTO v_Total_operaciones FROM "BSCL.Homologacion.BD::HOM.Orden"
             	WHERE "IdCarga" = p_IdCarga
             	  AND "MERCADO" ='IF';

                -- CARGAR LAS ASIGNACIONES ASOCIADAS A LAS ORDENES CARGADAS 
                -- REVISAMOS SI HUBO MOVIMIENTOS PARA CARGAR
                -- SI NO HAY ORDENES CON ESTADOS VALIDOS SE SALE
  
                IF v_Total_operaciones = 0 THEN
                  	    v_hash= select 'ORD_CARGA_CERO' as "Codigo", '' as "Valor" FROM DUMMY;
				        CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ORD_CARGA_CERO',:v_hash,:v_erroroutmsg);
				        p_out_ErroresDetalle =  select 'ORD_CARGA_CERO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
						        				select * from :p_out_ErroresDetalle;	  
                END IF;

	   			  
	   			  -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ORDEN
	   			  p_ResumenDatosProcesados =  select 'CARGAORDIF' as "CodigoProceso" 
	   			  									,'ORD' as "Modulo" 
	   			 									,v_total_ord_gestion as "TotalRegistrosSebra"
	   			  									,v_Total_operaciones as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_ResumenDatosProcesados;
	   			  
	   			  IF v_total_ord_gestion <> v_Total_operaciones THEN 
	   			  	 v_hash= select 'ORD_CARGA_ERR_ORD' as "Codigo", '' as "Valor" FROM DUMMY;
				     CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ORD_CARGA_ERR_ORD',:v_hash,:v_erroroutmsg);
				     p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_ORD' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			  END IF;
	  
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
