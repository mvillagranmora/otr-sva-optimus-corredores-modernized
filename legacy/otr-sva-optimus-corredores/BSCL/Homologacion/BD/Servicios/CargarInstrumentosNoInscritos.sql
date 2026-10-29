SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarInstrumentosNoInscritos";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarInstrumentosNoInscritos"
	(  
	 	--IN  p_IdEmpresa		   			BIGINT,			-- ID Empresa destino
     	--IN  p_IdUsuario		   			BIGINT,			-- ID del usuario que solicita el servicio
	 	--IN  p_IdCarga 	       			BIGINT,          -- ID de la carga 
	 	--IN  p_IdModulo         			NVARCHAR(3),     -- modulo 
     	--IN  P_Modalidad        			NVARCHAR(1),     -- Ingreso I incremental, 
     	--IN  p_RutCliente       			NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 	--IN  p_FechaInicial              NVARCHAR(10),   --YYYY-MM-DD
		--IN  p_FechaFinal				NVARCHAR(10),   --YYYY-MM-DD
		-- Parametros salida del servicio publico
		-- Salidas para mensajes de advertencias y errores
		OUT p_out_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
		OUT p_out_ErroresContador int,
		OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
    ) 
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA 
	AS   
	BEGIN
		-- VARIABLES VALIDACIONES BASICAS
		DECLARE v_out_Resultado  		CHAR(1); 	   --RESULTADO S/N/E
	    DECLARE v_out_Mensaje	 		NVARCHAR(520); --MENSAJE SALIDA
	    
	    DECLARE v_erroroutmsg  			nvarchar(520);    
	    DECLARE v_TotalInstrumentosLeidos	BIGINT;	    
	    DECLARE v_TotalInstrumentosGrabadas	BIGINT;
	     
	    
	    -- Para LOg
	    DECLARE v_out_refID				BIGINT;    
	    DECLARE v_out_ErroresContador	BIGINT; 
	    DECLARE v_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));	
	    DECLARE v_FechaSistema NVARCHAR(40);
	    DECLARE v_MonedaSistema NVARCHAR(10);
	        
		--------- CONTROL DE ERRORES
		--- Controlado----

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
		p_out_ErroresContador		:= 0;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;   	
        p_out_ResumenDatosProcesados    = select '' as "CodigoProceso", '' as "Modulo", 0 as "TotalRegistrosSebra",0 as "TotalRegistrosSap" from dummy;
        
   		
   				select count(*) into v_TotalInstrumentosLeidos
				from "BSCL_VIRTUAL"."GES_TBPLMAIN" as TBPLMAIN
  				left join "BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO
  				 	on INSTRUMENTO."Nemotecnico" = trim(TBPLMAIN."NEMO")
				 	where INSTRUMENTO."Nemotecnico" is null;
				 	
				select count(1) into v_TotalInstrumentosGrabadas 	
				FROM "BSCL_VIRTUAL"."GES_TBPLMAIN" as TBPLMAIN
  				LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_MONEDA
					ON HOM_TIPO_MONEDA."TipoValor" = 'TipoMonedaInstrumentoFFMM'
					AND HOM_TIPO_MONEDA."ValorOrigen" = TRIM(TBPLMAIN."TIPO_FAM") 
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_INSTRUMENTO
					ON HOM_TIPO_INSTRUMENTO."TipoValor" = 'TipoInstrumentoSegunMercado'
					AND HOM_TIPO_INSTRUMENTO."ValorOrigen" = TRIM(TBPLMAIN."MERCADO")
  				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO
  					ON INSTRUMENTO."Nemotecnico" = TRIM(TBPLMAIN."NEMO")
				WHERE INSTRUMENTO."Nemotecnico" IS NULL; 	
				 	
			    insert into "BSCL.Plataforma.BD::INS.Instrumento" 
					("IdInstrumento"	,"Nemotecnico"	,"ISIN"	,"CorteMinimo"	,"EstaInscrito"	,"EsDivisible"	,"RazonSocial"	,"Lista"	,"FechaEmision"	,"FechaVencimiento"	,"IdInstrumentoRelacionado"
					,"IdEmisor"	,"IdTipoReajuste"	,"Reajustable"	,"IdMoneda"	,"IdTipoInstrumento"	,"IdEstado"	,"DecimalesPrecio","Precio"
					)
				select		
						"BSCL"."BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL as IdInstrumento
						,TRIM(TBPLMAIN."NEMO")	  as Nemotecnico
						,'' 					  as ISIN
						, 0						  as CorteMinimo
						,TBPLMAIN."IND_INSCRITO"  as EstaInscrito
						,TBPLMAIN."IND_DIVISIBLE" as EsDivisible
						,TBPLMAIN."RAZON_SOCIAL"  as RazonSocial
						, 0						  as Lista
						, '0001-01-01'			  as FechaEmision
						, '0001-01-01'			  as FechaVencimiento
						, 0						  as IdInstrumentoRelacionado
						, CASE WHEN TBPLMAIN.MERCADO ='FM' then TBPLMAIN."EMISOR" else '' end  as IdEmisor 
						, ''					  as IdTipoReajuste
						, 'N'					  as Reajustable
						, CASE WHEN TBPLMAIN.MERCADO ='FM' THEN HOM_TIPO_MONEDA."ValorDestino"
						           ELSE CASE WHEN TBPLMAIN.MERCADO IN ('CO','AO','ST') THEN 'USD'
						                ELSE 'CLP'
						                END
						  END as IdMoneda
						, HOM_TIPO_INSTRUMENTO."ValorDestino" as IdTipoInstrumento	
						, CASE WHEN TBPLMAIN."ESTADO" = 'V' THEN 'ACT' ELSE 'INA' END AS IdEstado 
						, 0	AS DecimalesPrecio
						, TBPLMAIN."VALOR"	as "Precio"	
  				FROM "BSCL_VIRTUAL"."GES_TBPLMAIN" as TBPLMAIN
  				LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_MONEDA
					ON HOM_TIPO_MONEDA."TipoValor" = 'TipoMonedaInstrumentoFFMM'
					AND HOM_TIPO_MONEDA."ValorOrigen" = TRIM(TBPLMAIN."TIPO_FAM") 
				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_INSTRUMENTO
					ON HOM_TIPO_INSTRUMENTO."TipoValor" = 'TipoInstrumentoSegunMercado'
					AND HOM_TIPO_INSTRUMENTO."ValorOrigen" = TRIM(TBPLMAIN."MERCADO")
  				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO
  					ON INSTRUMENTO."Nemotecnico" = TRIM(TBPLMAIN."NEMO")
				WHERE INSTRUMENTO."Nemotecnico" IS NULL;

              -- debe tener una homologacion de instrumento  
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,0 as "IdCarga"
                    ,'INSTRUMENTO' as "Entidad"
                    ,'Instrumento No inscrito no cargado' as "Mensaje"
                    , 'NEMO' as "NombreCampoGenerico1"
                    ,  TBPLMAIN."NEMO" as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              FROM "BSCL_VIRTUAL"."GES_TBPLMAIN" as TBPLMAIN
  				LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_MONEDA
					ON HOM_TIPO_MONEDA."TipoValor" = 'TipoMonedaInstrumentoFFMM'
					AND HOM_TIPO_MONEDA."ValorOrigen" = TRIM(TBPLMAIN."TIPO_FAM") 
				LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_INSTRUMENTO
					ON HOM_TIPO_INSTRUMENTO."TipoValor" = 'TipoInstrumentoSegunMercado'
					AND HOM_TIPO_INSTRUMENTO."ValorOrigen" = TRIM(TBPLMAIN."MERCADO")
  				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO
  					ON INSTRUMENTO."Nemotecnico" = TRIM(TBPLMAIN."NEMO")
				WHERE INSTRUMENTO."Nemotecnico" IS NULL
				   AND  HOM_TIPO_INSTRUMENTO."IdHomologacion" IS NULL;

				-- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	   			p_out_ResumenDatosProcesados =  select 'CARGA-INS-NO-INSCRITOS' as "CodigoProceso" 
	   											,'PLA' as "Modulo" 
	   											,v_TotalInstrumentosLeidos as "TotalRegistrosSebra"
	   											,v_TotalInstrumentosGrabadas as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_out_ResumenDatosProcesados;

                IF v_TotalInstrumentosLeidos <> v_TotalInstrumentosGrabadas THEN 
				     p_out_ErroresDetalle =  select 'ORD_CARGA_INSTRUMENTOS-NO-INCRITOS' as "Codigo",'falla en la carga de instrumento no inscritos ver errores tabla errores' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			END IF;

         SIGNAL SALIDA_CONTROLADA;         
	END;
