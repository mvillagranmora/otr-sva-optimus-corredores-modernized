SET SCHEMA "BSCL"; 
	DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCuentasBancarias";
	CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCuentasBancarias"
	 (
	 -- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_RutCliente          VARCHAR(11) default '0-0',      -- Rut cliente
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
	    
	    DECLARE v_erroroutmsg  				nvarchar(520);    
	    DECLARE v_TotalCuentas_leidos		BIGINT:=0;
	    DECLARE v_TotalCuentas_grabados		BIGINT:=0;
	    

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

	SELECT TRIM(SUBSTR_BEFORE (p_RutCliente,'-')) INTO v_PerId from dummy;	
	

	cuentas_bancarias = select distinct "ban_id","mon_id",trim("cba_cuenta") as "cba_cuenta" ,"par_tcu_id","per_id" 
	                    from "BSCL_VIRTUAL"."GES_CUENTA_BANCARIA"; 
 	
	-- contamos los registros sebra
    -- contamos las cuentas que no estan en sebra

      select count(*) into v_TotalCuentas_leidos
       FROM :cuentas_bancarias as CTA_BAN
      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER
       	ON PER."IdOrigen" = CTA_BAN."per_id"
      INNER JOIN "BSCL_VIRTUAL"."GES_COR_BANCO" as COR_BAN
      	ON TRIM(CTA_BAN."ban_id") = TRIM(COR_BAN."ban_id")
      INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOM_BAN 
        ON  HOM_BAN."TipoValor" = 'BancosHomologados'
		AND HOM_BAN."ValorOrigen" = COR_BAN."ban_nombre" 
      INNER JOIN "BSCL"."BSCL.Plataforma.BD::BAN.Banco" as BANCO
      	ON BANCO."NombreBanco" = HOM_BAN."ValorDestino"
      INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_CUENTA
        ON  TIPO_CUENTA."TipoValor" = 'TipoCuentaCorriente'
		AND  TIPO_CUENTA."ValorOrigen" = CTA_BAN."par_tcu_id"
      LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CTA_BAN_OPT
        ON CTA_BAN_OPT."IdBanco" =  BANCO."IdBanco"
        AND CTA_BAN_OPT."NumeroCuentaBancaria" =  TRIM(CTA_BAN."cba_cuenta")
        AND CTA_BAN_OPT."IdPersona" = PER."IdPersona"
     WHERE CTA_BAN_OPT."NumeroCuentaBancaria" IS NULL;
      

    -- insertamos las cuentas corrientes
	insert into "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria"      
    ("IdCuentaBancaria","IdBanco","IdTipoCuentaBancaria","IdMoneda","NumeroCuentaBancaria","IdPersona"
     ,"FechaIngreso","FechaCreacion","FechaModificacion","IdEstado","IdEstadoAutorizacion","Borrado"
     ,"IdUsuarioCreador","IdPlaza","IdCarga")
    select 
          "BSCL.Plataforma.BD.Secuencias::BAN.IdCuentaBancaria".NEXTVAL
          ,COALESCE(BANCO."IdBanco",0) AS "IdBanco"
          ,TIPO_CUENTA."ValorDestino" as "TipoCuentaBancaria"
          ,TRIM(CTA_BAN."mon_id") as "Moneda"
          ,TRIM(CTA_BAN."cba_cuenta") as "NumeroCuentaBancaria"
          ,PER."IdPersona" as "IdPersona"
          ,'0001-01-01' as "FechaIngreso"
          ,'0001-01-01' as "FechaCreacion"
          ,'0001-01-01' as "FechaModificacion"
          ,'ACT' as "IdEstado"
          ,'AUT' as "IdAutorizado" 
          ,'N' as "Borrado"
          ,1 as "IdUsuarioCreador"
          ,0 as "Plaza"
          ,p_IdCarga as "IdCarga"
      FROM :cuentas_bancarias as CTA_BAN
      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER
       	ON PER."IdOrigen" = case when v_PerId = '0' then CTA_BAN."per_id" else v_PerId end
      INNER JOIN "BSCL_VIRTUAL"."GES_COR_BANCO" as COR_BAN
      	ON TRIM(CTA_BAN."ban_id") = TRIM(COR_BAN."ban_id")
      INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOM_BAN 
        ON  HOM_BAN."TipoValor" = 'BancosHomologados'
		AND  HOM_BAN."ValorOrigen" = COR_BAN."ban_nombre" 
      INNER JOIN "BSCL"."BSCL.Plataforma.BD::BAN.Banco" as BANCO
      	ON BANCO."NombreBanco" = HOM_BAN."ValorDestino"
      INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_CUENTA
        ON  TIPO_CUENTA."TipoValor" = 'TipoCuentaCorriente'
		AND  TIPO_CUENTA."ValorOrigen" = CTA_BAN."par_tcu_id"
      LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CTA_BAN_OPT
        ON CTA_BAN_OPT."IdBanco" =  BANCO."IdBanco"
        AND CTA_BAN_OPT."NumeroCuentaBancaria" =  TRIM(CTA_BAN."cba_cuenta")
        AND CTA_BAN_OPT."IdPersona" = PER."IdPersona"
      WHERE CTA_BAN_OPT."NumeroCuentaBancaria" IS NULL;
      
      
     --contamos todo lo que fue cargado 
     select count(*) into v_TotalCuentas_grabados from  "BSCL"."BSCL.SocioNegocio.BD::PER.CuentaBancaria" 
     where "IdCarga" = p_IdCarga;
     
     -- insertamos en el log

	 	--la persona debe existir
                  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'CUENTABANCARIA' as "Entidad"
                    , 'la persona no existe en optimus' as "Mensaje"
                    , 'per_id' as "NombreCampoGenerico1"
                    , CTA_BAN."per_id" as "ValorCampoGenerico1" 
                    , 'cba_cuenta' as "NombreCampoGenerico2" 
                    , CTA_BAN."cba_cuenta" as "ValorCampoGenerico2"
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
         	FROM :cuentas_bancarias as CTA_BAN
		      LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER
		       	ON PER."IdOrigen" = case when v_PerId = '0' then CTA_BAN."per_id" else v_PerId end
		      INNER JOIN "BSCL_VIRTUAL"."GES_COR_BANCO" as COR_BAN
		      	ON TRIM(CTA_BAN."ban_id") = TRIM(COR_BAN."ban_id")
		      INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOM_BAN 
		        ON  HOM_BAN."TipoValor" = 'BancosHomologados'
		        AND  HOM_BAN."ValorOrigen" = COR_BAN."ban_nombre" 
		      INNER JOIN "BSCL"."BSCL.Plataforma.BD::BAN.Banco" as BANCO
		      	ON BANCO."NombreBanco" = HOM_BAN."ValorDestino"
		      INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_CUENTA
		        ON  TIPO_CUENTA."TipoValor" = 'TipoCuentaCorriente'
				AND  TIPO_CUENTA."ValorOrigen" = CTA_BAN."par_tcu_id"	
		      LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CTA_BAN_OPT
		        ON CTA_BAN_OPT."IdBanco" =  BANCO."IdBanco"
		        AND CTA_BAN_OPT."NumeroCuentaBancaria" =  TRIM(CTA_BAN."cba_cuenta")
		        AND CTA_BAN_OPT."IdPersona" = PER."IdPersona"
		      WHERE CTA_BAN_OPT."NumeroCuentaBancaria" IS NULL
		        AND PER."IdOrigen" IS NULL;
		      
		      -- EL BANCO DEBE EXISTIR EN LA tabla corbas bancos
  				insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'CUENTABANCARIA' as "Entidad"
                    , 'el id del banco no se encuentra en la corbas de bancos' as "Mensaje"
                    , 'per_id' as "NombreCampoGenerico1"
                    , CTA_BAN."per_id" as "ValorCampoGenerico1" 
                    , 'ban_id' as "NombreCampoGenerico2" 
                    , CTA_BAN."ban_id" as "ValorCampoGenerico2"
                    , 'cba_cuenta' as "NombreCampoGenerico3"
                    , CTA_BAN."cba_cuenta" as "ValorCampoGenerico3"
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
         	FROM  :cuentas_bancarias as CTA_BAN
		      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER
		       	ON PER."IdOrigen" = case when v_PerId = '0' then CTA_BAN."per_id" else v_PerId end
		      LEFT JOIN "BSCL_VIRTUAL"."GES_COR_BANCO" as COR_BAN
		      	ON TRIM(CTA_BAN."ban_id") = TRIM(COR_BAN."ban_id")
		      INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOM_BAN 
		        ON  HOM_BAN."TipoValor" = 'BancosHomologados'
		        AND  HOM_BAN."ValorOrigen" = COR_BAN."ban_nombre" 
		      INNER JOIN "BSCL"."BSCL.Plataforma.BD::BAN.Banco" as BANCO
		      	ON BANCO."NombreBanco" = HOM_BAN."ValorDestino"
		      INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_CUENTA
		        ON  TIPO_CUENTA."TipoValor" = 'TipoCuentaCorriente'
				AND  TIPO_CUENTA."ValorOrigen" = CTA_BAN."par_tcu_id"		      	
		      LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CTA_BAN_OPT
		        ON CTA_BAN_OPT."IdBanco" =  BANCO."IdBanco"
		        AND CTA_BAN_OPT."NumeroCuentaBancaria" =  TRIM(CTA_BAN."cba_cuenta")
		        AND CTA_BAN_OPT."IdPersona" = PER."IdPersona"
		      WHERE CTA_BAN_OPT."NumeroCuentaBancaria" IS NULL
		        AND COR_BAN."ban_id" IS NULL;
		        
           -- la homologacion debe existir
     		  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'CUENTABANCARIA' as "Entidad"
                    , 'el nombre del banco de la corbas no esta homologado' as "Mensaje"
                    , 'per_id' as "NombreCampoGenerico1"
                    , CTA_BAN."per_id" as "ValorCampoGenerico1" 
                    , 'ban_id' as "NombreCampoGenerico2" 
                    , CTA_BAN."ban_id" as "ValorCampoGenerico2"
                    , 'cba_cuenta' as "NombreCampoGenerico3"
                    , CTA_BAN."cba_cuenta" as "ValorCampoGenerico3"
                    , 'TipoValor' as "NombreCampoGenerico4"
                    , 'BancosHomologados' as "ValorCampoGenerico4"
                    , 'ban_nombre' as "NombreCampoGenerico5"
                    , COR_BAN."ban_nombre"  as "ValorCampoGenerico5"
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
         	FROM  :cuentas_bancarias as CTA_BAN
		      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER
		       	ON PER."IdOrigen" = case when v_PerId = '0' then CTA_BAN."per_id" else v_PerId end
		      INNER JOIN "BSCL_VIRTUAL"."GES_COR_BANCO" as COR_BAN
		      	ON TRIM(CTA_BAN."ban_id") = TRIM(COR_BAN."ban_id")
		      LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOM_BAN 
		        ON  HOM_BAN."TipoValor" = 'BancosHomologados'
		        AND  HOM_BAN."ValorOrigen" = COR_BAN."ban_nombre" 
		      INNER JOIN "BSCL"."BSCL.Plataforma.BD::BAN.Banco" as BANCO
		      	ON BANCO."NombreBanco" = HOM_BAN."ValorDestino"
		      INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_CUENTA
		        ON  TIPO_CUENTA."TipoValor" = 'TipoCuentaCorriente'
				AND  TIPO_CUENTA."ValorOrigen" = CTA_BAN."par_tcu_id"
		      LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CTA_BAN_OPT
		        ON CTA_BAN_OPT."IdBanco" =  BANCO."IdBanco"
		        AND CTA_BAN_OPT."NumeroCuentaBancaria" =  TRIM(CTA_BAN."cba_cuenta")
		        AND CTA_BAN_OPT."IdPersona" = PER."IdPersona"
		     WHERE CTA_BAN_OPT."NumeroCuentaBancaria" IS NULL
		      AND HOM_BAN."ValorOrigen" IS NULL;  
		    
               --- el banco debe existir en la tabla banco
               insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'CUENTABANCARIA' as "Entidad"
                    , 'el banco homologado no esta en tabla de banco optimus' as "Mensaje"
                    , 'per_id' as "NombreCampoGenerico1"
                    , CTA_BAN."per_id" as "ValorCampoGenerico1" 
                    , 'ban_id' as "NombreCampoGenerico2" 
                    , CTA_BAN."ban_id" as "ValorCampoGenerico2"
                    , 'cba_cuenta' as "NombreCampoGenerico3"
                    , CTA_BAN."cba_cuenta" as "ValorCampoGenerico3"
                    , 'TipoValor' as "NombreCampoGenerico4"
                    , 'BancosHomologados' as "ValorCampoGenerico4"
                    , 'ValorDestino' as "NombreCampoGenerico5"
                    , HOM_BAN."ValorDestino"  as "ValorCampoGenerico5"
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
         	FROM  :cuentas_bancarias as CTA_BAN
		      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER
		       	ON PER."IdOrigen" = case when v_PerId = '0' then CTA_BAN."per_id" else v_PerId end
		      INNER JOIN "BSCL_VIRTUAL"."GES_COR_BANCO" as COR_BAN
		      	ON TRIM(CTA_BAN."ban_id") = TRIM(COR_BAN."ban_id")
		      INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOM_BAN 
		        ON  HOM_BAN."TipoValor" = 'BancosHomologados'
		        AND  HOM_BAN."ValorOrigen" = COR_BAN."ban_nombre" 
		      LEFT JOIN "BSCL"."BSCL.Plataforma.BD::BAN.Banco" as BANCO
		      	ON BANCO."NombreBanco" = HOM_BAN."ValorDestino"
		      INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_CUENTA
		        ON  TIPO_CUENTA."TipoValor" = 'TipoCuentaCorriente'
				AND  TIPO_CUENTA."ValorOrigen" = CTA_BAN."par_tcu_id"
		      LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CTA_BAN_OPT
		        ON CTA_BAN_OPT."IdBanco" =  BANCO."IdBanco"
		        AND CTA_BAN_OPT."NumeroCuentaBancaria" =  TRIM(CTA_BAN."cba_cuenta")
		        AND CTA_BAN_OPT."IdPersona" = PER."IdPersona"
		      WHERE CTA_BAN_OPT."NumeroCuentaBancaria" IS NULL
		        AND BANCO."IdBanco" IS NULL;
    	    	    
    	     --- el tipo de cuenta bancaria no se encuentra homologado
               insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (  "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'CUENTABANCARIA' as "Entidad"
                    , 'el tipo cuenta bancaria no esta homologado' as "Mensaje"
                    , 'per_id' as "NombreCampoGenerico1"
                    , CTA_BAN."per_id" as "ValorCampoGenerico1" 
                    , 'ban_id' as "NombreCampoGenerico2" 
                    , CTA_BAN."ban_id" as "ValorCampoGenerico2"
                    , 'cba_cuenta' as "NombreCampoGenerico3"
                    , CTA_BAN."cba_cuenta" as "ValorCampoGenerico3"
                    , 'TipoValor' as "NombreCampoGenerico4"
                    , 'TipoCuentaCorriente' as "ValorCampoGenerico4"
                    , "par_tcu_id" as "NombreCampoGenerico5"
                    ,CTA_BAN."par_tcu_id"  as "ValorCampoGenerico5"
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
         	FROM  :cuentas_bancarias as CTA_BAN
		      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER
		       	ON PER."IdOrigen" = case when v_PerId = '0' then CTA_BAN."per_id" else v_PerId end
		      INNER JOIN "BSCL_VIRTUAL"."GES_COR_BANCO" as COR_BAN
		      	ON TRIM(CTA_BAN."ban_id") = TRIM(COR_BAN."ban_id")
		      INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as HOM_BAN 
		        ON  HOM_BAN."TipoValor" = 'BancosHomologados'
		        AND  HOM_BAN."ValorOrigen" = COR_BAN."ban_nombre" 
		      INNER JOIN "BSCL"."BSCL.Plataforma.BD::BAN.Banco" as BANCO
		      	ON BANCO."NombreBanco" = HOM_BAN."ValorDestino"
		      LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_CUENTA
		        ON  TIPO_CUENTA."TipoValor" = 'TipoCuentaCorriente'
				AND  TIPO_CUENTA."ValorOrigen" = CTA_BAN."par_tcu_id"
		      LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" as CTA_BAN_OPT
		        ON CTA_BAN_OPT."IdBanco" =  BANCO."IdBanco"
		        AND CTA_BAN_OPT."NumeroCuentaBancaria" =  TRIM(CTA_BAN."cba_cuenta")
		        AND CTA_BAN_OPT."IdPersona" = PER."IdPersona"
		      WHERE  CTA_BAN_OPT."NumeroCuentaBancaria" IS NULL
		        AND TIPO_CUENTA."IdHomologacion" IS NULL;    	    
 
	    IF v_TotalCuentas_grabados <>  v_TotalCuentas_leidos THEN		
	    --- Listar clientes no grabados en Clientes, eliminarlos del modelo completo (persona, Documento, documentopersona y portafolio) y grabar en LOG
			v_hash= select 'DatosLeidos'   as "Codigo", v_TotalCuentas_grabados   as "Valor" FROM DUMMY UNION ALL
			        select 'DatosGrabados' as "Codigo", v_TotalCuentas_leidos as "Valor" FROM DUMMY;
			CALL "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('CARGA_CON_DIFERENCIAS',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'CARGA_CON_DIFERENCIAS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy; 
			p_out_ErroresContador := p_out_ErroresContador + 1;
		END IF ;

		p_ResumenDatosProcesados =  select 'CARGACUENTASBANCARIAS' as "CodigoProceso" ,'SOC' as "Modulo" ,v_TotalCuentas_leidos as "TotalRegistrosSebra",v_TotalCuentas_grabados as "TotalRegistrosSap"  FROM DUMMY;	 			
        SIGNAL SALIDA_CONTROLADA; 	       
	END
