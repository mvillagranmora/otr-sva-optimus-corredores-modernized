	SET SCHEMA "BSCL"; 
	DROP PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarUsuarios";
	CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarUsuarios"
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
	    DECLARE v_TotalEmpleados_leidos			BIGINT:=0;
	    DECLARE v_TotalUsuarios_grabados		BIGINT:=0;

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
		
        -- contabilizamos los registros 
        SELECT count(1) INTO v_TotalEmpleados_leidos
        FROM "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU
		   ON TRIM(USU."CodigoUsuario") = TRIM(EMP."CodigoEmpleado")
		where EMP."IdCarga" = p_IdCarga
		and USU."IdUsuario" IS NULL;

        -- cargamos los usuarios
        INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" 
		(
			"IdUsuario"
			,"IdEmpresa"
			,"CodigoUsuario"
			,"Clave"
			,"FechaIngreso"
			,"FechaVigencia"
			,"FechaUltimaConexion"
			,"DiasVencimientoClave"
			,"Conectado"
			,"Hora"
			,"Terminal"
			,"IdEstado"
			,"IntentosConexion"
			,"FechaBloqueoTemporal"
			,"TipoEntidad"
			,"IdEntidad"
			,"EsAutorizado"
		)  
		SELECT 
			"BSCL.Plataforma.BD.Secuencias::Usuario".NEXTVAL
			,p_IdEmpresa
			,TRIM(EMP."CodigoEmpleado")
			,'D8BDF9A0CB27A193A1127DE2924B6E5A9E4C2D3B3FE42E935E160C011F3DF1FC'-- equivale al numero 2
			,TO_DATE('0001-01-01', 'YYYY-MM-DD')
			,TO_DATE('2999-01-01', 'YYYY-MM-DD')
			,TO_DATE('0001-01-01', 'YYYY-MM-DD')
			,365
			,'S'
			,'00:00'
			,'Terminal Nro'
			,'ACT'
			, 0
			,CURRENT_UTCTIMESTAMP
			,'EMP'
			,EMP."IdEmpleado"
			,'S'
		FROM "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP
		LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU
		   ON USU."CodigoUsuario" = EMP."CodigoEmpleado"
		where EMP."IdCarga" = p_IdCarga
		and USU."IdUsuario" IS NULL;
		
       SELECT count(1) INTO v_TotalUsuarios_grabados
        FROM "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP
		INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU
		   ON TRIM(USU."CodigoUsuario") = TRIM(EMP."CodigoEmpleado")
		where EMP."IdCarga" = p_IdCarga;
    	    	    
	    IF v_TotalUsuarios_grabados <>  v_TotalEmpleados_leidos THEN		
	    --- Listar clientes no grabados en Clientes, eliminarlos del modelo completo (persona, Documento, documentopersona y portafolio) y grabar en LOG
			v_hash= select 'DatosLeidos'   as "Codigo", v_TotalUsuarios_grabados   as "Valor" FROM DUMMY UNION ALL
			        select 'DatosGrabados' as "Codigo", v_TotalEmpleados_leidos as "Valor" FROM DUMMY;
			CALL "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('CARGA_CON_DIFERENCIAS',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'CARGA_CON_DIFERENCIAS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy; 
			p_out_ErroresContador := p_out_ErroresContador + 1;
		END IF ;

		p_ResumenDatosProcesados =  select 'CARGAUSUARIOS' as "CodigoProceso" ,'PLA' as "Modulo" ,v_TotalEmpleados_leidos as "TotalRegistrosSebra",v_TotalUsuarios_grabados as "TotalRegistrosSap"  FROM DUMMY;	 			
        SIGNAL SALIDA_CONTROLADA; 	       
	END;
	
	