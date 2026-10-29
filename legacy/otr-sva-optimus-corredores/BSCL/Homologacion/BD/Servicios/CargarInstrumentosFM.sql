SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarInstrumentosFM";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarInstrumentosFM"
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
						, TBPLMAIN."EMISOR"		  as IdEmisor -- LINKEAR ID EMISOR ANTERIORMENTE CREADO
						, ''					  as IdTipoReajuste
						, 'N'					  as Reajustable
						, HOM_TIPO_MONEDA."ValorDestino" as IdMoneda				--BUSCAR(RV.MONEDA_EX Y OBTENER ID MONEDA 	)
						, 'FFM'					  as IdTipoInstrumento	-- Linkear Tipo Instrumentos
						, CASE WHEN TBPLMAIN."ESTADO" = 'V' THEN 'ACT' ELSE 'INA' END AS IdEstado --AC.ESTADO	--: homologar 1 - ACT
						, 0	AS DecimalesPrecio
						, TBPLMAIN."VALOR"	as "Precio"	
  				FROM "BSCL_VIRTUAL"."GES_TBPLMAIN" as TBPLMAIN
  				INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOM_TIPO_MONEDA
					ON HOM_TIPO_MONEDA."TipoValor" = 'TipoMonedaInstrumentoFFMM'
					AND HOM_TIPO_MONEDA."ValorOrigen" = TRIM(TBPLMAIN."TIPO_FAM") 
  				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO
  					ON INSTRUMENTO."Nemotecnico" = TRIM(TBPLMAIN."NEMO")	 
				WHERE INSTRUMENTO."Nemotecnico" IS NULL;
				
				v_TotalInstrumentosGrabadas:= v_TotalInstrumentosLeidos;--no tengo como diferenciar los intrumentos cargados
                -- de los ya existentes en la tabla de instrumentos
				
				-- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	   			p_out_ResumenDatosProcesados =  select 'CARGACARTERAFMM' as "CodigoProceso" 
	   											,'FMM' as "Modulo" 
	   											,v_TotalInstrumentosLeidos as "TotalRegistrosSebra"
	   											,v_TotalInstrumentosGrabadas as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_out_ResumenDatosProcesados;

                IF v_TotalInstrumentosLeidos <> v_TotalInstrumentosGrabadas THEN 
				     p_out_ErroresDetalle =  select 'ORD_CARGA_INSTRUMENTOS' as "Codigo",'falla en la carga fondos mutuos' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			END IF;

         SIGNAL SALIDA_CONTROLADA;         
	END;
