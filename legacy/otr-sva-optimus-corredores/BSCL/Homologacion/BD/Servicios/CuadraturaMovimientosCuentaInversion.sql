	SET SCHEMA "BSCL"; 
	DROP PROCEDURE   "BSCL.Homologacion.BD.Servicios::CuadraturaMovimientosCuentaInversion";
	CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CuadraturaMovimientosCuentaInversion"
    
	 (
		 -- Parametros entrada comunes en todos los servicios publicos
		IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
	    IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
		IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
		IN  p_Modulo              NVARCHAR(3),     -- modulo 
	    IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
	    IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
		IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	    IN  p_FechaFinal		  NVARCHAR(10),   --YYYY-MM-DD
		-- Parametros salida del servicio publico
		OUT p_out_ErroresContador int,
		OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
	 ) 
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA 
	AS
	BEGIN
		
		--------- CONTROL DE ERRORES
		--- Controlado
		DECLARE SALIDA_CONTROLADA CONDITION;    	
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
		BEGIN
			IF :p_out_ErroresContador = 0 THEN	 
		 	    p_out_ErroresDetalle       =  select 'INGRESO OK' as "Codigo",'LOG REGISTRADO' as "Descripcion" from dummy;-- UNION ALL
			END IF;		 					 	
		END;	
	
		--- Excepcion
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			declare v_errorExt nvarchar(40);
			IF ::SQL_ERROR_CODE > 10000 THEN
				call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
				p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
									   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
				select count(1) into p_out_ErroresContador 
				from :p_out_ErroresDetalle;										
			ELSE
				--Error interno no manejado  (El log será captado por la capa de XS control)
				p_out_ErroresDetalle       =  select 'ERROR' as "Codigo", ::SQL_ERROR_CODE as "Descripcion" from dummy;-- UNION ALL		
				RESIGNAL; 
			END IF;			
		END;
		BEGIN
		-- VARIABLES VALIDACIONES BASICAS
	    DECLARE v_erroroutmsg  			nvarchar(520);    
	    DECLARE v_TotalMovimientosLeidos	BIGINT;	    
	    DECLARE v_TotalMovimientosGrabados	BIGINT;

	    -- Para LOg  
	   DECLARE v_FechaHoraSistema SECONDDATE;
	   p_out_ErroresContador		:= 0;
	   p_out_ErroresDetalle		    = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	   
       ---------------------------------------------------------
       -- recuperamos la fecha hora del sistema
       ---------------------------------------------------------
       
       CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,:v_FechaHoraSistema);		
       
       -- convierto el rut a formato sebra 
	   select lpad(p_RutCliente,11) into p_RutCliente from dummy;
       ------------------------------------------------------------------
       -- cuento los movimientos totales que deberian haber sido cargados
       -- custodia contado  debo ver la custodia  plazo 
       ------------------------------------------------------------------
 	   DATOS_SEBRA =	SELECT TRIM(MVTO."RUT_CLI") as "RUT_CLI"
				        	   ,MVTO."IND_TIT_CCTE"
				        	   ,MVTO."SEC_RUT_CLI"
				        	   ,MVTO."MONTO" 
						FROM "BSCL_VIRTUAL"."GES_TBCTMVTO"	AS MVTO
				  		WHERE MVTO."TIPO_COMP_ADJ" BETWEEN '  ' AND 'ZZ' 
				  		AND MVTO."IND_TIT_CCTE" IN ('C','V','U')
				  		AND MVTO."EST_MOVTO" BETWEEN 'P' AND 'T'   -- BETWEEN 'T' AND 'T'
				 	 	AND MVTO."IND_SALDO_APLAZO" BETWEEN ' ' AND 'Z'
				  		AND MVTO."IND_ANULADO" BETWEEN 'I'  AND 'I'
				  		AND MVTO."IND_CUST_CTDO" BETWEEN ' '  AND ' '
				  		AND MVTO."IND_CUST_APLAZO" BETWEEN ' '  AND ' '-- EN BLANCO NO TIENEN CUSTODIA  PLAZO
				  		AND MVTO."IND_SALDO_CTDO" BETWEEN 'S' AND 'S'
				  		AND MVTO."FEC_MVTO" BETWEEN p_FechaInicial AND p_FechaFinal
				  		AND MVTO."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN MVTO."RUT_CLI" ELSE p_RutCliente END;      
      
      	DATOS_SAP =    SELECT 
	                    TRIM(CLI."DocumentoIdentidad") AS "RUT_CLI"
                       ,CASE WHEN CTA_INV."IdMoneda" = 'CLP' THEN 'C'
                 		     WHEN CTA_INV."IdMoneda" = 'EUR' THEN 'V'
            				 WHEN CTA_INV."IdMoneda" = 'USD' THEN 'U'
        			     END as "IND_TIT_CCTE"
        			    ,POR."CodigoPortafolio" as "SEC_RUT_CLI"
        			    ,ABO."IdCliente"
        			    ,POR."IdPortafolio"
        			    ,ABO."IdCuentaInversion"
        			    ,ABO."Monto"
					   FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" AS ABO
					   INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
					   	ON CLI."IdCliente" = ABO."IdCliente"
					   	AND CLI."DocumentoIdentidad" = CASE WHEN  p_RutCliente ='        0-0' THEN CLI."DocumentoIdentidad"  ELSE TRIM(p_RutCliente) END
					   INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" AS CTA_INV
					   	ON CTA_INV."IdCuentaInversion" = ABO."IdCuentaInversion"
					   INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR
					   	ON POR."IdCliente" = CLI."IdCliente"
					   	AND POR."IdPortafolio" = ABO."IdPortafolio"
					   WHERE "IdCarga" = p_IdCarga
					   AND "SecuenciaMovimientoMigracion" > 0 ;
	 
	   -------------------------------------------------------------------------------------------				   
	   -- se coloca el filtro que la secuencia de migracion sea mayor que cero
       -- para no afectar a la cuadraduta por el efecto del abono inicial que se genera
       -- en el servicio de creacion de la cuenta de inversion y que puede tener el mismo id carga
       --------------------------------------------------------------------------------------------			   
					  
       SELECT COUNT(*) INTO v_TotalMovimientosLeidos FROM :DATOS_SEBRA;
       SELECT COUNT(*) INTO v_TotalMovimientosGrabados FROM :DATOS_SAP;     
     
        -------------------------------------------------------------------
        -- agrupamos los movimientos para ver las diferencias por        --  
        -- rut_cli                                                       --
        -- sec_rut_cli                                                   --  
        -- moneda cuenta corriente										 --
        -------------------------------------------------------------------
        DATOS_SEBRA_AGRUPADOS =  SELECT "RUT_CLI"
		        					,"IND_TIT_CCTE"
		        					,"SEC_RUT_CLI"
		        					,SUM("MONTO") as "Monto"
		        					,COUNT(*) as "CantidadRegistros"
		        					FROM :DATOS_SEBRA
			          				GROUP BY "RUT_CLI","IND_TIT_CCTE","SEC_RUT_CLI";

		DATOS_SAP_AGRUPADOS =    SELECT "RUT_CLI"
										,"IND_TIT_CCTE"
        			    				,"SEC_RUT_CLI"
        			    				,"IdCliente"
        			                    ,"IdPortafolio"
        			                    ,"IdCuentaInversion"
        			                    ,SUM("Monto") as "Monto"
                        				,count(*) as "CantidadRegistros" 
							  	 FROM :DATOS_SAP
								 GROUP BY "RUT_CLI" ,"IND_TIT_CCTE" ,"SEC_RUT_CLI","IdCliente","IdPortafolio","IdCuentaInversion";
   
	   ----------------------------------------------------------------------
       -- contamos los movimientos sebra vs cargados en sap -----------------
       ----------------------------------------------------------------------
       IF v_TotalMovimientosLeidos <> v_TotalMovimientosGrabados THEN
	    	v_hash= select 'Valor1' as "Codigo", v_TotalMovimientosLeidos   as "Valor" FROM DUMMY UNION ALL
			        select 'Valor2' as "Codigo", v_TotalMovimientosGrabados as "Valor" FROM DUMMY;
			CALL "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ERROR_CARGA_CTA_INV',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'ERROR_CARGA_CTA_INV' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy; 
			p_out_ErroresContador := p_out_ErroresContador + 1;
	
	    END IF;
	    
        -----------------------------------------------------------------------------------
        --- ahora buscaremos que clientes se encuentras descuadrados a nivel de movimientos
        --- portafolio y cuenta de inversion	    
	    -----------------------------------------------------------------------------------

	    CLIENTES_DESCUADRADOS  = SELECT  SEBRA."RUT_CLI"
	    								,SEBRA."IND_TIT_CCTE"
	    								,SEBRA."SEC_RUT_CLI"
	    								,IFNULL(SAP."IdCliente",0) as "IdCliente"
	    								,IFNULL(SAP."IdPortafolio",0) as "IdPortafolio"
	    								,IFNULL(SAP."IdCuentaInversion",0) as "IdCuentaInversion"
	    								,SEBRA."CantidadRegistros" as "CantidadRegistrosSebra"
	    								,IFNULL(SAP."CantidadRegistros",0) as "CantidadRegistrosSap"
	    								,SEBRA."CantidadRegistros" - IFNULL(SAP."CantidadRegistros",0) as "DiferenciaRegistros"
	    								,SEBRA."Monto" - IFNULL(SAP."Monto",0) as "DiferenciaMonto"
	                             FROM :DATOS_SEBRA_AGRUPADOS as SEBRA
	                             LEFT JOIN :DATOS_SAP_AGRUPADOS as SAP
	                             	ON SAP."RUT_CLI" = SEBRA."RUT_CLI"
	                                AND SAP."SEC_RUT_CLI" = SEBRA."SEC_RUT_CLI"
	                                AND SAP."IND_TIT_CCTE" = SEBRA."IND_TIT_CCTE";
	                                
	    SELECT * FROM :CLIENTES_DESCUADRADOS;
	    
       	SIGNAL SALIDA_CONTROLADA;
       END;    	            
	END;
