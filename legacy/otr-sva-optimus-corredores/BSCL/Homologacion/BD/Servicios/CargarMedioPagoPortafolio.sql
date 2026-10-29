SET SCHEMA "BSCL"; 
	DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarMedioPagoPortafolio";
	CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarMedioPagoPortafolio"
	 (
	 -- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	 OUT p_out_ErroresContador BIGINT,
	 OUT p_out_ErroresDetalle  table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))	
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
	    DECLARE v_out_CodigoErrorSQL 	BIGINT;
	    
	    DECLARE v_erroroutmsg  					nvarchar(520);    
	    DECLARE v_TotalMedioPago_leidos			BIGINT:=0;
	    DECLARE v_TotalMedioPago_grabados		BIGINT:=0;

	    DECLARE v_PerId							NVARCHAR(11);
	    
	    -- Para RECIBIR SALIDA DEL LOG
	    DECLARE v_out_ErroresContador	BIGINT; 
	    DECLARE v_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));	
	    DECLARE v_Clave VARCHAR(520);      
	    
	
		--------------------------------------------------------- CONTROL DE ERRORES
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

		
		select count(*) into v_TotalMedioPago_leidos from "BSCL_VIRTUAL"."GES_MEDIO_PAGO";
				
		
        INSERT INTO "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" ( 
        "IdPreferenciaPagoPortafolio","IdPortafolio","IdPreferenciaPago","IdPersonaRelacionada","IdCuentaBancaria"
        ,"FechaCreacion","FechaModificacion","IdUsuarioCreador","IdEstado","IdEmpresa","IdTarjetaBancaria","FechaIngreso"
        ,"IdEstadoAutorizacion","PorDefecto","IdCarga")
		select 
                "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio".NEXTVAL as "IdPreferenciaPagoPortafolio"
				,PORTAFOLIO_OPTIMUS."IdPortafolio" as "IdPortafolio"
				,HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino" as "IdPreferenciaPago"
				,PERSONA_OPTIMUS."IdPersona"  as "IdPersonaRelacionada"
				,coalesce(CuentaBancariaOptimus."IdCuentaBancaria",0) as  "IdCuentaBancaria"
				,'0001-01-01' as "FechaCreacion"
				,'0001-01-01' as "FechaModificacion"
				,1 as "IdUsuarioCreador"
				,'ACT' as "IdEstado" --ver si puedo homologar
				,1 as "IdEmpresa"
				,0  as "IdTarjetaBancaria"
				,'0001-01-01' as "FechaIngreso"
				,'ING' as "IdEstadoAutorizacion"
				,'N' as "PorDefecto"
				,p_IdCarga as "IdCarga"		
		        from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	inner join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	inner join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	inner join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   		on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				inner join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				inner join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where  HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL;   
			     
        -- cuantos se cargaron en el sistema  
        select count(*) into v_TotalMedioPago_grabados from "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
        where "IdCarga"  = p_IdCarga;	

       --INSERTAMOS LOS LOG

       -- debe existir la entidad Portafolio  
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'PREF_PAGO_PORTAFOLIO' as "Entidad"
                    ,'Persona No tiene Documento Optimus' as "Mensaje"
                    , 'per_num_doc' as "NombreCampoGenerico1"
                    , to_char("per_num_doc") as "ValorCampoGenerico1" 
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
              from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        left join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	inner join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	inner join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	inner join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   		on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				inner join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				inner join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where  DOCUMENTO_OPTIMUS."IdPersona" IS NULL
			       and HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL;
			   
			  -- la persona debe existir en optimus        
			  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'PREF_PAGO_PORTAFOLIO' as "Entidad"
                    ,'no existe persona en optimus' as "Mensaje"
                    , 'per_num_doc' as "NombreCampoGenerico1"
                    , to_char("per_num_doc") as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'IdPersona' as "NombreCampoGenerico6"
                    , PERSONA_OPTIMUS."IdPersona" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	inner join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	inner join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	inner join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   		on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				inner join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				inner join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where  PERSONA_OPTIMUS."IdPersona" IS NULL
			       and HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL;      

              -- el cliente debe existir
			  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'PREF_PAGO_PORTAFOLIO' as "Entidad"
                    ,'no existe cliente en optimus' as "Mensaje"
                    , 'IdPersona' as "NombreCampoGenerico1"
                    , to_char("per_num_doc") as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'IdPersona' as "NombreCampoGenerico6"
                    , PERSONA_OPTIMUS."IdPersona" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	left join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	inner join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	inner join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   		on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				inner join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				inner join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where  CLIENTE_OPTIMUS."IdPersona" IS NULL 
			       and HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL;   			      
           
          -- cliente no cuenta con cuentas sebra
 		  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'PREF_PAGO_PORTAFOLIO' as "Entidad"
                    ,'cliente no tiene cuentas creadas en sebra' as "Mensaje"
                    , 'per_id' as "NombreCampoGenerico1"
                    , PERSONA_SEBRA."per_id" as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'IdPersona' as "NombreCampoGenerico6"
                    , PERSONA_OPTIMUS."IdPersona" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	inner join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	left join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	inner join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   	    on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				inner join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				inner join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where  CUENTA_SEBRA."per_id" IS NULL
			       and HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL;   
			          
			                             
          -- cliente no Tiene portafolio optimus creado
 		  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'PREF_PAGO_PORTAFOLIO' as "Entidad"
                    ,'Cliente no tiene portafolio optimus creado' as "Mensaje"
                    , 'per_id' as "NombreCampoGenerico1"
                    , to_varchar(PERSONA_SEBRA."per_id") as "ValorCampoGenerico1" 
                    , 'IdCliente' as "NombreCampoGenerico2" 
                    , to_varchar(CLIENTE_OPTIMUS."IdCliente") as "ValorCampoGenerico2"
                    , 'CUENTA' as "NombreCampoGenerico3"
                    , to_varchar(CUENTA_SEBRA."CUENTA") as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'IdPersona' as "NombreCampoGenerico6"
                    , PERSONA_OPTIMUS."IdPersona" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	inner join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	inner join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	left join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   	    on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				inner join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				inner join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where   PORTAFOLIO_OPTIMUS."IdCliente" IS NULL 
			        and HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL; 
			        
			  -- portafolio no tiene formas de pago
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'PREF_PAGO_PORTAFOLIO' as "Entidad"
                    , 'Cuenta no tiene medios de pago configuradas en sebra' as "Mensaje"
                    , 'per_id' as "NombreCampoGenerico1"
                    , to_varchar(PERSONA_SEBRA."per_id") as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "ValorCampoGenerico2"
                    , 'CUENTA' as "NombreCampoGenerico3"
                    , to_varchar(CUENTA_SEBRA."CUENTA") as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'IdPersona' as "NombreCampoGenerico6"
                    , PERSONA_OPTIMUS."IdPersona" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	inner join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	inner join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	inner join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   	    on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				left join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				inner join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where   MEDIO_PAGO_SEBRA."per_id" IS NULL
			        and HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL; 
			         
			   -- medio de pago no se encuentra en forma_medio_pago_grupo  
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'PREF_PAGO_PORTAFOLIO' as "Entidad"
                    , 'medio de pago sebra no pertenece a ninguna forma pago grupo sebra' as "Mensaje"
                    , 'fpa_id_pref' as "NombreCampoGenerico1"
                    , MEDIO_PAGO_SEBRA."fpa_id_pref" as "ValorCampoGenerico1" 
                    , 'gop_id' as "NombreCampoGenerico2" 
                    , MEDIO_PAGO_SEBRA."gop_id"  as "ValorCampoGenerico2"
                    , 'gou_tip_operacion' as "NombreCampoGenerico3"
                    , MEDIO_PAGO_SEBRA."gou_tip_operacion" as "ValorCampoGenerico3"
                    , 'gou_tip_uso' as "NombreCampoGenerico4"
                    , MEDIO_PAGO_SEBRA."gou_tip_uso" as "ValorCampoGenerico4"
                    , 'mon_id' as "NombreCampoGenerico5"
                    , MEDIO_PAGO_SEBRA."mon_id" as "ValorCampoGenerico5"
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
              from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	inner join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	inner join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	inner join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   	    on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				inner join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	left join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				inner join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where   FORMA_PAGO_GRUPO_SEBRA."fpa_id" IS NULL
			        and HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL; 
			         
       
              -- no existe el grupo operacion en sebra del medio de pago sebra
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    , p_IdCarga as "IdCarga"
                    , 'PREF_PAGO_PORTAFOLIO' as "Entidad"
                    , 'grupo operacion no se encuentra en grupo operacion sebra' as "Mensaje"
                    , '' as "NombreCampoGenerico1"
                    , '' as "ValorCampoGenerico1" 
                    , 'gop_id' as "NombreCampoGenerico2" 
                    , MEDIO_PAGO_SEBRA."gop_id"  as "ValorCampoGenerico2"
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
                from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	inner join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	inner join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	inner join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   	    on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				inner join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				left join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where   GRUPO_OPERACION_SEBRA."gop_id" IS NULL
			        and HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL;

        -- la forma de pago debe existir
        insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select  "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    , p_IdCarga as "IdCarga"
                    , 'PREF_PAGO_PORTAFOLIO' as "Entidad"
                    , 'la forma de pago no existe en tabla forma pago sebra' as "Mensaje"
                    ,  'fpa_id_pref' as "NombreCampoGenerico1"
                    ,  MEDIO_PAGO_SEBRA."fpa_id_pref" as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    , '' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    , '' as "NombreCampoGenerico9"
                    , '0001-01-01' as "ValorCampoGenerico9"
                    , '' as "NombreCampoGenerico10"
                    , '0001-01-01' as "ValorCampoGenerico10" 
                from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	inner join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	inner join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	inner join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   	    on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				inner join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				inner join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				left join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where   FORMA_PAGO_SEBRA."fpa_id" IS NULL
			        and HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL;
			        
			  -- los medios de pago deben estar homologados
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select  "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    , p_IdCarga as "IdCarga"
                    , 'PREF_PAGO_PORTAFOLIO' as "Entidad"
                    , 'el medio de pago no esta homologados' as "Mensaje"
                    , 'TipoValor'  as "NombreCampoGenerico1"
                    , 'MedioPagoTesoreria' as "ValorCampoGenerico1" 
                    , 'gop_id' as "NombreCampoGenerico2" 
                    , MEDIO_PAGO_SEBRA."gop_id" as "ValorCampoGenerico2"
                    , 'gop_cod_gestion' as "NombreCampoGenerico3"
                    , FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion" as "ValorCampoGenerico3"
                    , 'fpa_id_pref' as "NombreCampoGenerico4"
                    , MEDIO_PAGO_SEBRA."fpa_id_pref" as "ValorCampoGenerico4"
                    , 'mon_id' as "NombreCampoGenerico5"
                    , MEDIO_PAGO_SEBRA."mon_id" as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    , '' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    , '' as "NombreCampoGenerico9"
                    , '0001-01-01' as "ValorCampoGenerico9"
                    , '' as "NombreCampoGenerico10"
                    , '0001-01-01' as "ValorCampoGenerico10" 
                from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	inner join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	inner join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	inner join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   	    on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				inner join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				inner join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where   HOMOLOGACION_MEDIO_PAGO."TipoValor" IS NULL
			        and HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL;
			        
			  --- los grupo operacion no se encuentran homologados
   				insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select  "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    , p_IdCarga as "IdCarga"
                    , 'PREF_PAGO_PORTAFOLIO' as "Entidad"
                    , 'el grupo operacion sebra no esta homologado ' as "Mensaje"
                    ,  'TipoValor' as "NombreCampoGenerico1"
                    , 'HomologacionGrupoOperacion' as "ValorCampoGenerico1" 
                    , 'gop_id' as "NombreCampoGenerico2" 
                    , MEDIO_PAGO_SEBRA."gop_id" as "ValorCampoGenerico2"
                    , 'gou_tip_operacion'  as "NombreCampoGenerico3"
                    , MEDIO_PAGO_SEBRA."gou_tip_operacion"  as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    , '' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    , '' as "NombreCampoGenerico9"
                    , '0001-01-01' as "ValorCampoGenerico9"
                    , '' as "NombreCampoGenerico10"
                    , '0001-01-01' as "ValorCampoGenerico10" 
                from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	inner join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	inner join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	inner join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   	    on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				inner join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				inner join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where   HOMOLOGACION_GRUPO_OPERACION."TipoValor" IS NULL
			        and HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL;           

             -- la preferencia de pago no se encuentra homologada   				
             insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select  "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    , p_IdCarga as "IdCarga"
                    , 'PREF_PAGO_PORTAFOLIO' as "Entidad"
                    , 'la preferencia de pago no esta homologada Tipo valor HomologacionPreferenciaPago' as "Mensaje"
                    , 'gop_id' as "NombreCampoGenerico1"
                    , MEDIO_PAGO_SEBRA."gop_id" as "ValorCampoGenerico1" 
                    , 'IdMedioPago' as "NombreCampoGenerico2" 
                    , HOMOLOGACION_MEDIO_PAGO."ValorDestino" as "ValorCampoGenerico2"
                    , 'mon_id' as "NombreCampoGenerico3"
                    , MEDIO_PAGO_SEBRA."mon_id" as "ValorCampoGenerico3"
                    , 'gou_tip_operacion' as "NombreCampoGenerico4"
                    , MEDIO_PAGO_SEBRA."gou_tip_operacion" as "ValorCampoGenerico4"
                    , 'IdTipoOperacion' as "NombreCampoGenerico5"
                    , HOMOLOGACION_GRUPO_OPERACION."ValorDestino" as "ValorCampoGenerico5"
                    , '' as "NombreCampoGenerico6"
                    , 0 as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    , '' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    , '' as "NombreCampoGenerico9"
                    , '0001-01-01' as "ValorCampoGenerico9"
                    , '' as "NombreCampoGenerico10"
                    , '0001-01-01' as "ValorCampoGenerico10" 
                from "BSCL_VIRTUAL"."GES_PERSONA" as PERSONA_SEBRA
		        inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOCUMENTO_OPTIMUS
		        	on upper(trim(DOCUMENTO_OPTIMUS."DocumentoIdentidad")) = upper(trim("per_num_doc"))
			    inner join "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PERSONA_OPTIMUS
			    	on  PERSONA_OPTIMUS."IdPersona" =  DOCUMENTO_OPTIMUS."IdPersona"
			   	inner join "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as CLIENTE_OPTIMUS
			   		on CLIENTE_OPTIMUS."IdPersona" = PERSONA_OPTIMUS."IdPersona"
			   	inner join "BSCL_VIRTUAL"."GES_CUENTA" as CUENTA_SEBRA
					on (CUENTA_SEBRA."per_id" = PERSONA_SEBRA."per_id")--22.006
			   	inner join "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" as PORTAFOLIO_OPTIMUS
			   	    on  PORTAFOLIO_OPTIMUS."IdCliente" = CLIENTE_OPTIMUS."IdCliente"
			   		and PORTAFOLIO_OPTIMUS."CodigoPortafolio" = CUENTA_SEBRA."CUENTA"||'' --21.988
				inner join "BSCL_VIRTUAL"."GES_MEDIO_PAGO" as MEDIO_PAGO_SEBRA--16.630
					on (     MEDIO_PAGO_SEBRA."per_id" = CUENTA_SEBRA."per_id"
					     and MEDIO_PAGO_SEBRA."CUENTA" = CUENTA_SEBRA."CUENTA"
					   )
              	inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO_GRUPO" as FORMA_PAGO_GRUPO_SEBRA
		            on (FORMA_PAGO_GRUPO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref"
		                and FORMA_PAGO_GRUPO_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id" 
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_operacion" = MEDIO_PAGO_SEBRA."gou_tip_operacion"
		                and FORMA_PAGO_GRUPO_SEBRA."gou_tip_uso" = MEDIO_PAGO_SEBRA."gou_tip_uso"
		                and FORMA_PAGO_GRUPO_SEBRA."mon_id" = MEDIO_PAGO_SEBRA."mon_id")-- 8.041
				inner join "BSCL_VIRTUAL"."GES_GRUPO_OPERACION" as GRUPO_OPERACION_SEBRA
					on (GRUPO_OPERACION_SEBRA."gop_id" = MEDIO_PAGO_SEBRA."gop_id")
				inner join "BSCL_VIRTUAL"."GES_FORMA_PAGO" as FORMA_PAGO_SEBRA 
					on (FORMA_PAGO_SEBRA."fpa_id" = MEDIO_PAGO_SEBRA."fpa_id_pref")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_MEDIO_PAGO-- me faltan homologaciones 
				     on (HOMOLOGACION_MEDIO_PAGO."TipoValor"    = 'MedioPagoTesoreria' 
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen2" = FORMA_PAGO_GRUPO_SEBRA."gop_cod_gestion"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen3" = MEDIO_PAGO_SEBRA."fpa_id_pref"
				     and HOMOLOGACION_MEDIO_PAGO."ValorOrigen4" = MEDIO_PAGO_SEBRA."mon_id")
				inner join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_GRUPO_OPERACION
				     on (    HOMOLOGACION_GRUPO_OPERACION."TipoValor"    = 'HomologacionGrupoOperacion' 
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen"  = MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_GRUPO_OPERACION."ValorOrigen2" = MEDIO_PAGO_SEBRA."gou_tip_operacion"  
				         )
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_PREFERENCIA_PAGO       
				     on (    HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"     = 'HomologacionPreferenciaPago'
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen"   =  MEDIO_PAGO_SEBRA."gop_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen2"  =  HOMOLOGACION_MEDIO_PAGO."ValorDestino"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen3"  =  MEDIO_PAGO_SEBRA."mon_id"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen4"  =  MEDIO_PAGO_SEBRA."gou_tip_operacion"
				         and HOMOLOGACION_PREFERENCIA_PAGO."ValorOrigen5"  =  HOMOLOGACION_GRUPO_OPERACION."ValorDestino"
				        )
				left join "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA" as CUENTA_BANCARIA_SEBRA
					on CUENTA_BANCARIA_SEBRA."cba_id" = MEDIO_PAGO_SEBRA."cba_id"
				left join "BSCL_VIRTUAL"."GES_COR_BANCO" as BANCO_SEBRA
					on BANCO_SEBRA."ban_id" = CUENTA_BANCARIA_SEBRA."ban_id"
				left join "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOMOLOGACION_BANCO
					on (HOMOLOGACION_BANCO."TipoValor"   = 'BancosHomologados'
					    and UPPER(TRIM(HOMOLOGACION_BANCO."ValorOrigen")) = UPPER(TRIM(BANCO_SEBRA."ban_nombre")))
			    left join "BSCL"."BSCL.Plataforma.BD::BAN.Banco" AS BANCO_OPTIMUS
			    	on (BANCO_OPTIMUS."NombreBanco" =  HOMOLOGACION_BANCO."ValorDestino")
			    left join "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CuentaBancariaOptimus
			        on (CuentaBancariaOptimus."NumeroCuentaBancaria" = TRIM(CUENTA_BANCARIA_SEBRA."cba_cuenta"))
			    left join "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" as HOM_PREF_PAGO_POR
			    	on ( HOM_PREF_PAGO_POR."IdPortafolio" =  PORTAFOLIO_OPTIMUS."IdPortafolio"
			    	     and HOM_PREF_PAGO_POR."IdPreferenciaPago" =  HOMOLOGACION_PREFERENCIA_PAGO."ValorDestino"
			    	)
			    where   HOMOLOGACION_PREFERENCIA_PAGO."TipoValor"  IS NULL
			        and HOM_PREF_PAGO_POR."IdPreferenciaPago" IS NULL;           
    
	    IF v_TotalMedioPago_grabados <>  v_TotalMedioPago_leidos THEN		
	    --- Listar clientes no grabados en Clientes, eliminarlos del modelo completo (persona, Documento, documentopersona y portafolio) y grabar en LOG
			v_hash= select 'DatosLeidos'   as "Codigo", v_TotalMedioPago_leidos   as "Valor" FROM DUMMY UNION ALL
			        select 'DatosGrabados' as "Codigo", v_TotalMedioPago_grabados as "Valor" FROM DUMMY;
			CALL "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('CARGA_CON_DIFERENCIAS',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'CARGA_CON_DIFERENCIAS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy; 
			p_out_ErroresContador := p_out_ErroresContador + 1;
		END IF ;

		p_ResumenDatosProcesados =  select 'CARGAMEDIOPAGO' as "CodigoProceso" ,'SOC' as "Modulo" ,v_TotalMedioPago_leidos as "TotalRegistrosSebra",v_TotalMedioPago_grabados as "TotalRegistrosSap"  FROM DUMMY;	 			
        SIGNAL SALIDA_CONTROLADA; 	       
	END;
	
	
