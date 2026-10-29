	SET SCHEMA "BSCL"; 
	DROP PROCEDURE   "BSCL.Homologacion.BD.Servicios::CargarCuentaInversion";
	CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarCuentaInversion"
	(  
	--  Fecha Creacion : 
	 	IN  p_IdEmpresa		   			BIGINT,			-- ID Empresa destino
     	IN  p_IdUsuario		   			BIGINT,			-- ID del usuario que solicita el servicio
	 	IN  p_IdCarga 	       			BIGINT,          -- ID de la carga 
	 	IN  p_Modulo           			NVARCHAR(3),     -- modulo 
     	IN  P_Modalidad        			NVARCHAR(1),     -- Ingreso I incremental, 
     	IN  p_RutCliente       			NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 	IN  p_FechaInicial              NVARCHAR(10),   --YYYY-MM-DD
		IN  p_FechaFinal				NVARCHAR(10),   --YYYY-MM-DD
		-- Parametros salida del servicio publico
		-- Salidas para mensajes de advertencias y errores
		OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalPortafoliosCuentaACargar" DECIMAL(26,8),"TotalPortafoliosConCuentaInversion" DECIMAL(26,8)),
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
	    DECLARE v_out_CodigoErrorSQL 	BIGINT;
	    
	    DECLARE v_erroroutmsg  			nvarchar(520);    
	    DECLARE v_TotalPortafolioLeidos	BIGINT;	    
	    DECLARE v_TotalCuentasGrabadas	BIGINT;
	    
	    -- Para LOg
	    DECLARE v_out_refID				BIGINT;    
	    DECLARE v_out_ErroresContador	BIGINT; 
	    DECLARE v_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));	
	    DECLARE v_FechaSistema NVARCHAR(40);
	    DECLARE v_MaxPeriodo NVARCHAR(40);
	    DECLARE v_FechaInicioPeriodo NVARCHAR(10);
	    
	    
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
		p_out_ErroresContador		:= 0;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		   	
	   -- tabla de monedas y codigos cuentas de inversion
	   tablas_cuentas = select 'CCLP' as "CodigoCuentaInversion" 
	                   ,'Cuenta de Inversión en Pesos' as "NombreCuentaInversion"
	                   ,'CLP' as "IdMoneda"
	                   from dummy
	                   union all 
	                   select 'CDOL' as "CodigoCuentaInversion" 
	                           ,'Cuenta de Inversión en Dólares' as "NombreCuentaInversion"
	                           ,'USD' as "IdMoneda"
	                   from dummy
	                   union all
	                   select   'CEUR' as "CodigoCuentaInversion" 
	                           ,'Cuenta de Inversión en Euros' as "NombreCuentaInversion"
	                           ,'EUR' as "IdMoneda"
	                   from dummy;

      	--1 ) Ingresar Cuenta de inversion para los portafolio cargados

        -- contabilizamos los portafolios cargados
        select count(*) into v_TotalPortafolioLeidos
      	FROM 
        (select PER."IdPersona",CLI."IdCliente",POR."IdPortafolio"
		 from "BSCL.Portafolio.BD::POR.Portafolio" AS POR
		 inner join "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
			on POR."IdCliente" = CLI."IdCliente"
		 inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC
		    on  DOC."IdPersona" = CLI."IdPersona"
			and DOC."DocumentoIdentidad" = case when p_RutCliente='0-0' then DOC."DocumentoIdentidad" else p_RutCliente end 
		 inner join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
			on PER."IdPersona" = CLI."IdPersona"
			and PER."IdCarga" = p_IdCarga
			and PER."IdOrigen" <> 0) as CLI2 
        ,:tablas_cuentas as CUE
        where not exists (select 1 from "BSCL.CuentaInversion.BD::CTA.CuentaInversion" 
                          where  "IdCliente" = CLI2."IdCliente"
                              and "IdPortafolio" = CLI2."IdPortafolio"
                              and "IdMoneda"     = CUE."IdMoneda");
        

       
        INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion"("IdCuentaInversion",
		             "IdEmpresa", "IdCliente", "IdPortafolio","IdMoneda","Codigo",
			         "Nombre","FechaIngresoRegistro","IdUsuario","IdEstado", "IdCarga") 
        SELECT
        "BSCL.CuentaInversion.BD.Secuencias::CuentaInversion".NEXTVAL as "IdCuentaInversion"
		,p_IdEmpresa 
        ,CLI2."IdCliente"
        ,CLI2."IdPortafolio"
        ,CUE."IdMoneda"
		,CUE."CodigoCuentaInversion"
		,CUE."NombreCuentaInversion"
		,CURRENT_UTCTIMESTAMP
		,p_IdUsuario
		,'ACT'
		,p_IdCarga
        FROM 
        (select PER."IdPersona",CLI."IdCliente",POR."IdPortafolio"
		 from "BSCL.Portafolio.BD::POR.Portafolio" AS POR
		 inner join "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
			on POR."IdCliente" = CLI."IdCliente"
		 inner join "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC
		    on  DOC."IdPersona" = CLI."IdPersona"
			and DOC."DocumentoIdentidad" = case when trim(p_RutCliente)='0-0' then DOC."DocumentoIdentidad" else trim(p_RutCliente) end 
		 inner join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
			on PER."IdPersona" = CLI."IdPersona"
			and PER."IdCarga" = p_IdCarga
			and PER."IdOrigen" <> 0) as CLI2 
        ,:tablas_cuentas as CUE
        where not exists (select 1 from "BSCL.CuentaInversion.BD::CTA.CuentaInversion" 
                          where  "IdCliente" = CLI2."IdCliente"
                              and "IdPortafolio" = CLI2."IdPortafolio"
                              and "IdMoneda"     = CUE."IdMoneda");

      --cuento las cuentas de inversion cargadas
      select count(*) into v_TotalCuentasGrabadas 
      from "BSCL.CuentaInversion.BD::CTA.CuentaInversion"
      where "IdCarga" = p_IdCarga;

      p_ResumenDatosProcesados = select 'CARCTAINVERSION' as "CodigoProceso"
                                    ,'CTA' as "Modulo"
                                    , v_TotalPortafolioLeidos  as "TotalPortafoliosCuentaACargar" 
                                    , v_TotalCuentasGrabadas as "TotalPortafoliosConCuentaInversion"
                                 from dummy
      							 union all 
      							 select * from :p_ResumenDatosProcesados;
      
		SIGNAL SALIDA_CONTROLADA;         
	END;
