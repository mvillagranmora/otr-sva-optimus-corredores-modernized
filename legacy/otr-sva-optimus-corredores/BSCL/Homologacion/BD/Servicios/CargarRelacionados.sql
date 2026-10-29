SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarRelacionados";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarRelacionados"
	(  
		IN p_IdCarga BIGINT,
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
	    
	    DECLARE v_erroroutmsg  				nvarchar(520);    
	    DECLARE v_TotalRelacionesLeidos		BIGINT:=0;	    
	    DECLARE v_TotalRelacionesGrabadas	BIGINT:=0;
	     
	    -- Para LOg 

	    DECLARE v_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));	

	        
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
        
   		
   			select count(*) into v_TotalRelacionesLeidos
			from "BSCL_VIRTUAL"."GES_RELACIONES" as RELACION_SEBRA
					  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_ORIGEN_RELACION
					  	ON PERSONA_ORIGEN_RELACION."IdOrigen" = RELACION_SEBRA."per_id_per"
					  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_DESTINO_RELACION
					  	ON PERSONA_DESTINO_RELACION."IdOrigen" = RELACION_SEBRA."per_id_rel"
					  INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOMOLOGACION
					  	ON HOMOLOGACION."TipoValor" = 'CodigosRelacionados'
					  	AND HOMOLOGACION."ValorOrigen" = RELACION_SEBRA."par_tre_id"
					  LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::REL.RelacionPersona" AS RELACION
					  	ON RELACION."IdPersonaOrigen"	= PERSONA_ORIGEN_RELACION."IdPersona"
					  	AND RELACION."IdPersonaDestino" = PERSONA_DESTINO_RELACION."IdPersona"
					  	AND RELACION."IdTipoRelacion"   = HOMOLOGACION."ValorDestino"
			          WHERE RELACION."IdRelacionPersona" IS NULL;
			          		
			--CargarRelacionados
	        insert into "BSCL"."BSCL.SocioNegocio.BD::REL.RelacionPersona" 
	        (	 "IdRelacionPersona","IdTipoRelacion","IdPersonaOrigen","IdPersonaDestino","IdEstado","FechaIngreso","FechaInicioRelacion"
				,"FechaFinRelacion","FechaCreacion","FechaModificacion","IdUsuarioCreador","IdEstadoAutorizacion","PorcentajeParticipacion"
	            ,"RetiraDocumentos","IdCarga"
	        ) 
			select "BSCL.SocioNegocio.BD.Secuencias::RelacionPersona".NEXTVAL  as "IdRelacionPersona"
					,HOMOLOGACION."ValorDestino" as "IdTipoRelacion"
					,PERSONA_ORIGEN_RELACION."IdPersona" as "IdPersonaOrigen"
					,PERSONA_DESTINO_RELACION."IdPersona" as "IdPersonaDestino"
							,'ACT' as "IdEstado"
							,RELACION_SEBRA."rel_fec_inicio" as "FechaIngreso"
							,RELACION_SEBRA."rel_fec_inicio" as "FechaInicioRelacion"
							,RELACION_SEBRA."rel_fec_termino"as "FechaFinRelacion"
							,RELACION_SEBRA."rel_fec_inicio" as "FechaCreacion"
							,'0001-01-01' as "FechaModificacion"
							,1 as "IdUsuarioCreador"
							,'ING' as "IdEstadoAutorizacion"
							,0 as "PorcentajeParticipacion"
							,'N' as "RetiraDocumentos"
							,p_IdCarga	
					  from "BSCL_VIRTUAL"."GES_RELACIONES" as RELACION_SEBRA
					  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_ORIGEN_RELACION
					  	ON PERSONA_ORIGEN_RELACION."IdOrigen" = RELACION_SEBRA."per_id_per"
					  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_DESTINO_RELACION
					  	ON PERSONA_DESTINO_RELACION."IdOrigen" = RELACION_SEBRA."per_id_rel"
					  INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOMOLOGACION
					  	ON HOMOLOGACION."TipoValor" = 'CodigosRelacionados'
					  	AND HOMOLOGACION."ValorOrigen" = RELACION_SEBRA."par_tre_id"
					  LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::REL.RelacionPersona" AS RELACION
					  	ON RELACION."IdPersonaOrigen"	= PERSONA_ORIGEN_RELACION."IdPersona"
					  	AND RELACION."IdPersonaDestino" = PERSONA_DESTINO_RELACION."IdPersona"
					  	AND RELACION."IdTipoRelacion"   = HOMOLOGACION."ValorDestino"
			          WHERE RELACION."IdRelacionPersona" IS NULL;	

                   select count(1) into v_TotalRelacionesGrabadas
                   from "BSCL"."BSCL.SocioNegocio.BD::REL.RelacionPersona"
                   where "IdCarga" = p_IdCarga;
                   
              -- la persona origen debe existir  
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'RELACIONADOS' as "Entidad"
                    ,'la persona origen de la relacion no esta en optimus' as "Mensaje"
                    , 'per_id_rel' as "NombreCampoGenerico1"
                    , to_varchar(RELACION_SEBRA."per_id_per") as "ValorCampoGenerico1" 
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
              from "BSCL_VIRTUAL"."GES_RELACIONES" as RELACION_SEBRA
					  LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_ORIGEN_RELACION
					  	ON PERSONA_ORIGEN_RELACION."IdOrigen" = RELACION_SEBRA."per_id_per"
					  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_DESTINO_RELACION
					  	ON PERSONA_DESTINO_RELACION."IdOrigen" = RELACION_SEBRA."per_id_rel"
					  INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOMOLOGACION
					  	ON HOMOLOGACION."TipoValor" = 'CodigosRelacionados'
					  	AND HOMOLOGACION."ValorOrigen" = RELACION_SEBRA."par_tre_id"
					  LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::REL.RelacionPersona" AS RELACION
					  	ON RELACION."IdPersonaOrigen"	= PERSONA_ORIGEN_RELACION."IdPersona"
					  	AND RELACION."IdPersonaDestino" = PERSONA_DESTINO_RELACION."IdPersona"
					  	AND RELACION."IdTipoRelacion"   = HOMOLOGACION."ValorDestino"
			          WHERE RELACION."IdRelacionPersona" IS NULL
			            AND PERSONA_ORIGEN_RELACION."IdOrigen" IS NULL;
			            
			      -- la persona destino debe existir  
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'RELACIONADOS' as "Entidad"
                    ,'la persona destino de la relacion no esta en optimus' as "Mensaje"
                    , 'per_id_rel' as "NombreCampoGenerico1"
                    , to_varchar(RELACION_SEBRA."per_id_per") as "ValorCampoGenerico1" 
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
              from "BSCL_VIRTUAL"."GES_RELACIONES" as RELACION_SEBRA
					  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_ORIGEN_RELACION
					  	ON PERSONA_ORIGEN_RELACION."IdOrigen" = RELACION_SEBRA."per_id_per"
					  LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_DESTINO_RELACION
					  	ON PERSONA_DESTINO_RELACION."IdOrigen" = RELACION_SEBRA."per_id_rel"
					  INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOMOLOGACION
					  	ON HOMOLOGACION."TipoValor" = 'CodigosRelacionados'
					  	AND HOMOLOGACION."ValorOrigen" = RELACION_SEBRA."par_tre_id"
					  LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::REL.RelacionPersona" AS RELACION
					  	ON RELACION."IdPersonaOrigen"	= PERSONA_ORIGEN_RELACION."IdPersona"
					  	AND RELACION."IdPersonaDestino" = PERSONA_DESTINO_RELACION."IdPersona"
					  	AND RELACION."IdTipoRelacion"   = HOMOLOGACION."ValorDestino"
			          WHERE RELACION."IdRelacionPersona" IS NULL
			            AND PERSONA_DESTINO_RELACION."IdOrigen" IS NULL;  
			   
			   -- el codigo de la relacion debe existir

                insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'RELACIONADOS' as "Entidad"
                    ,'el tipo de relacion no se encuentra homologado en tabla homologacion' as "Mensaje"
                    , 'TipoValor' as "NombreCampoGenerico1"
                    , 'CodigosRelacionados' as "ValorCampoGenerico1" 
                    , 'par_tre_id' as "NombreCampoGenerico2" 
                    , RELACION_SEBRA."par_tre_id" as "ValorCampoGenerico2"
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
              from "BSCL_VIRTUAL"."GES_RELACIONES" as RELACION_SEBRA
					  INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_ORIGEN_RELACION
					  	ON PERSONA_ORIGEN_RELACION."IdOrigen" = RELACION_SEBRA."per_id_per"
					   INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_DESTINO_RELACION
					  	ON PERSONA_DESTINO_RELACION."IdOrigen" = RELACION_SEBRA."per_id_rel"
					  LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS HOMOLOGACION
					  	ON HOMOLOGACION."TipoValor" = 'CodigosRelacionados'
					  	AND HOMOLOGACION."ValorOrigen" = RELACION_SEBRA."par_tre_id"
					  LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::REL.RelacionPersona" AS RELACION
					  	ON RELACION."IdPersonaOrigen"	= PERSONA_ORIGEN_RELACION."IdPersona"
					  	AND RELACION."IdPersonaDestino" = PERSONA_DESTINO_RELACION."IdPersona"
					  	AND RELACION."IdTipoRelacion"   = HOMOLOGACION."ValorDestino"
			          WHERE RELACION."IdRelacionPersona" IS NULL
			            AND HOMOLOGACION."IdHomologacion" IS NULL;     
	                
				-- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	   			p_out_ResumenDatosProcesados =  select 'CARGA-RELACIONES-PERSONA' as "CodigoProceso" 
	   											,'PLA' as "Modulo" 
	   											,v_TotalRelacionesLeidos as "TotalRegistrosSebra"
	   											,v_TotalRelacionesGrabadas as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_out_ResumenDatosProcesados;

                IF v_TotalRelacionesLeidos <> v_TotalRelacionesGrabadas THEN 
				     p_out_ErroresDetalle =  select 'CARGA-RELACIONES-PERSONA' as "Codigo",'falla en la carga de relacionados ver errores tabla errores' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			END IF;

         SIGNAL SALIDA_CONTROLADA;         
	END;
