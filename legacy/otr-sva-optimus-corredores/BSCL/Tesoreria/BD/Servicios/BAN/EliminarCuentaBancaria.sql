SET SCHEMA "BSCL";   

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.BAN::EliminarCuentaBancaria";

-- 
--  Proposito: Elimina una cuenta bancaria de la corredora(tesorería).
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 05/02/2016  
-- 
CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.BAN::EliminarCuentaBancaria"
  (
   -- Parametros de Validacion                                                                                            
   IN  p_IdEmpresa                     			BIGINT,                                                                            
   IN  p_IdUsuario                     			BIGINT,                                                                            
   IN  p_IdModulo                      			NVARCHAR(3),                                                                       
   IN  p_simularOperacion              			NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)                  
   IN  p_UsaReglasNegocio              			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.                          

   -- Parámetros para lógica
   IN  p_IdCuentaBanco                          BIGINT,         -- Id de la cuenta bancaria a eliminar
   
   -- Parametros de Salida
   OUT p_out_AdvertenciasContador      INT,
   OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   OUT p_out_ErroresContador           INT,   
   OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
) 
   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
	
BEGIN  
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				table ("Codigo" nvarchar(100),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  				nvarchar(520);	        	
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN	
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 	    from :p_out_ErroresDetalle;		
		select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;
	END;	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
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

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN --LOGICA

	    --------------------
		-- Declare Variables
		--------------------
		DECLARE v_Obligatorios					NVARCHAR(250);
		DECLARE v_resultadoRegla 				NVARCHAR(40);
		DECLARE v_FechaSistema                  NVARCHAR(10);
		
		-- Para el Diccionario
        DECLARE p_TablaCodigosMensajes			TABLE ("CodigoMensaje" NVARCHAR(40),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
    	DECLARE p_out_TablaMensajes				TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));    	
    	    	
	    DECLARE v_AdvertenciasContador 			INT;
	    DECLARE v_ErroresContador      			INT;
	    DECLARE v_AdvertenciasDetalle  			TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000));	  		     
	    DECLARE v_ErroresDetalle       			TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));
    	
    	
    	DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
    	
    	p_TablaCodigosMensajes = Select '' as "CodigoMensaje", '' as "CodigoDato", '' as "ValorDato" from dummy where 1=2;
		p_out_TablaMensajes = Select '' as "Codigo", '' as "Descripcion" from dummy where 1=2;		


		--------------------------------------------
		-- VALIDACIONES
		-------------------------------------------- 
		IF p_UsaReglasNegocio = 'S' THEN
			BEGIN
				DECLARE v_resultadoRegla  NVARCHAR(40);
			 
				-- Validación básica de Empresa, Usuario, Módulo
				CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_Errores,:v_resultadoRegla);
				IF :v_resultadoRegla = 'RECH' THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;
	
				CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEliminarCuentaBancaria" (:p_IdCuentaBanco, :v_ErroresDetalle, :v_AdvertenciasDetalle, :v_resultadoRegla);

				IF :v_resultadoRegla = 'RECH' THEN	
					p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalle;
					SIGNAL SALIDA_CONTROLADA; 	
				ELSEIF :v_resultadoRegla = 'ADVE' THEN
					--LLamada al servicio de autorizacion si se requiere
					p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_ErroresDetalle;
					SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
				END IF;

			END;
		END IF;

 	   	-- Obtiene Fecha de Sistema
	   	Call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_FechaSistema);
		

		-- Solo fue una simulacion del servicio.
		If p_simularOperacion = 'S' then
			SIGNAL SALIDA_CONTROLADA;
		end if;

		DELETE
		FROM "BSCL.Contabilidad.BD::ITZ.EventoTipoDatoValor" ETDV
		WHERE LTRIM(RTRIM(ETDV."ValorVarchar")) IN (SELECT LTRIM(RTRIM("NombreCuentaBancaria")) FROM "BSCL.Tesoreria.BD::TES.CuentaBanco" WHERE "IdCuentaBanco" = :p_IdCuentaBanco AND "IdEmpresa" = :p_IdEmpresa)
		AND ETDV."IdTipoDatoEvento" = 'AliasBancoCaja';

		--------------------------------------------
		-- BORRA CUENTA CORRIENTE
		-------------------------------------------- 
 		DELETE
	  	FROM "BSCL.Tesoreria.BD::TES.CuentaBanco"
		WHERE "IdCuentaBanco" = p_IdCuentaBanco
		AND "IdEmpresa"       = p_IdEmpresa;

		--------------------------------------------
		-- LOG
		-------------------------------------------- 

		v_DatoLog = Select '' AS "SubRegistro", '' AS "CampoRegistro" , '' AS "ValorAntiguo", '' AS "ValorNuevo" 
							From DUMMY Where 1 = 2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				 p_IdTipoLog 		 => 'ELIMINACION' 
				,p_IdModulo 		 => 'TES' 
				,p_IdUsuario 		 => :p_IdUsuario 
				,p_IdEmpresa 		 => :p_IdEmpresa
				,p_FechaSistemaLog 	 => :v_FechaSistema
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro 	 => 'CUENTASBANCARIA'
				,p_IdProceso 		 => p_IdCuentaBanco
				,p_NombreProceso 	 => 'Eliminación Cuenta Bancaria'
				,p_DetalleLog 		 => :v_DatoLog
		);		

		
    END; 
END;




