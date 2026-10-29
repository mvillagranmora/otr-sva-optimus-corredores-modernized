SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ModificarEventoCapital";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ModificarEventoCapital" (


--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
	IN p_IdEventoCapital					BIGINT     		default 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	default '',
	IN p_Secuencia							BIGINT 			default 0,
	IN p_Descripcion						NVARCHAR(100) 	default '',
	IN p_EsInscrito							NVARCHAR(1) 	default '',
	IN p_IdClasificacionTipoInstrumento		NVARCHAR(3) 	default '',
	IN p_IdTipoInstrumento					NVARCHAR(3) 	default '',  
	IN p_IdInstrumento						BIGINT 			default 0,
	IN p_Nemo								NVARCHAR(40) 	default '',
	IN p_Factor								DECIMAL(26,14) 	default 0,
	IN p_Impuesto							DECIMAL(26,8) 	default 0,
	IN p_IdMoneda							NVARCHAR(3) 	default '',
	IN p_IdMonedaLiquidacion				NVARCHAR(3) 	default '',
	IN p_Paridad							DECIMAL(26,8) 	default 0,
	IN p_IdInstrumentoNuevo					BIGINT 			default 0,
	IN p_NemoNuevo							NVARCHAR(40) 	default '',
	IN p_FactorNuevo						DECIMAL(26,8) 	default 0,
	IN p_FechaActualizacion					NVARCHAR(10) 	default '0001-01-01',     
	IN p_FechaLimite						NVARCHAR(10) 	default '0001-01-01',     
	IN p_FechaPago							NVARCHAR(10) 	default '0001-01-01',     
	IN p_NumeroDividendo					BIGINT 			default 0,
	IN p_IdTipoDividendo					NVARCHAR(2) 	default '',
	IN p_PrecioAnterior						DECIMAL(26,8) 	default 0,
	IN p_FechaHoraIngresoRegistro			NVARCHAR(10) 	default '0001-01-01',     
	IN p_FechaHoraUltimaActualizacion		NVARCHAR(10) 	default '0001-01-01',     
	IN p_TipoIngreso						BIGINT 			default 0,
	IN p_IdEstado							NVARCHAR(3) 	default '',
	IN p_UsaReglasNegocio 					NVARCHAR(1) 	default 'N',
	
	-- Parametros de Salida
	OUT p_out_InformacionContador  		INT,
	OUT p_out_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),
	
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	
	OUT p_out_ErroresContador 			int,   
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000)),
	
	OUT p_out_Secuencia bigint
	 )   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN SEQUENTIAL EXECUTION
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE abort int default 0;	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
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
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;

	v_out_hash 					= Select 0 As "IdInterno", '' As "Codigo", '' As "Descripcion" From Dummy;
	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_FechaSistema 			TIMESTAMP;
		DECLARE v_Factor				INT DEFAULT 0;
		DECLARE v_IdEstado				NVARCHAR(3);
		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
	   	
	   	
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			declare v_resultadoRegla nvarchar(40);
			
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;				
		END;
		-----------------------------------------------------
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		-----------------------------------------------------
		-- Cambio de Factor
		---------------------------------------------------

		select count(1) into v_Factor
		from  "BSCL.Custodia.BD::CUS.EventoCapital"		
		where
			"IdEventoCapital" 	= p_IdEventoCapital	and
			"Factor"			<> p_Factor;
		
		
		IF 	v_Factor > 0 THEN
			v_IdEstado = 'MOD';
		END IF;	
		-----------------------------------------------------
		-- Fin Cambio de Factor
		---------------------------------------------------
		
		-----------------------------------------------------
		--proceso de Negocio
		---------------------------------------------------
		BEGIN
		
		DECLARE v_fechaHoraSistema NVARCHAR(40);
		DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(5000), "ValorNuevo" nvarchar(5000));
			
		UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
		SET
			"IdTipoEventoCapital"			= p_IdTipoEventoCapital,
			"FechaLimite"					= TO_DATE(p_FechaLimite, 'YYYY-MM-DD'),
			"FechaPago"						= TO_DATE(p_FechaPago, 'YYYY-MM-DD'),
			"NumeroDividendo"				= p_NumeroDividendo,
			"Descripcion"					= p_Descripcion,
			"EsInscrito"					= p_EsInscrito,
			"Factor"						= p_Factor,
			"Impuesto"						= p_Impuesto,
			"FactorNuevo"					= p_FactorNuevo,
			"IdMoneda"						= p_IdMoneda,
			"IdMonedaLiquidacion"			= p_IdMonedaLiquidacion,
			"Paridad"						= p_Paridad,
            "FechaHoraUltimaActualizacion"  = CURRENT_UTCTIMESTAMP,
            "IdEstado"						= CASE v_Factor
									            WHEN 0 THEN "IdEstado"
									            ELSE v_IdEstado
									         END
		WHERE
			"IdEventoCapital" = p_IdEventoCapital;


           	v_TablaCodigosMensajes =	Select 	p_NumeroDividendo As "IdInterno", 
											'UI_EVENTO_CAPITAL_MODIFICADO' As "Codigo",
											'p_NumeroDividendo' As "CodigoDato1", 
											Cast(p_NumeroDividendo As NVARCHAR(520)) as "ValorDato1",
											'' As "CodigoDato2", 
											'' As "ValorDato2" 
									From dummy;
		                        
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
					
			p_out_InformacionContador = 1;
			p_out_InformacionDetalle = 	Select "Codigo", "Descripcion" 
										From :v_out_hash;
										
 --   Select 1 into p_out_Secuencia FROM DUMMY;
    
    	if p_out_ErroresContador > 0 then 
				SIGNAL SALIDA_CONTROLADA;
			end if;
			
				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa => p_IdEmpresa, p_out_FechaHoraSistema => :v_fechaHoraSistema);
		        
		v_DetalleLog = 	SELECT 'MODIFICACION' AS "SubRegistro", 
		        		'IdEventoCapital' AS "CampoRegistro", 
		        		:p_IdEventoCapital AS "ValorAntiguo", 
		        		'' AS "ValorNuevo" FROM dummy;
		        		

		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					 p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => 'CUS' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa
					,p_FechaSistemaLog => TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => 'MODIFICARVENTOCAPITAL'
					,p_IdRegistro => :p_out_Secuencia 
					,p_DetalleLog => :v_DetalleLog  --hacia arriba obligatorios
					,p_NombreProceso => 'BSCL.Custodia.BD.Servicios::EVC.ModificarEventoCapital' -- hacia abajo opcionales
					,p_EstadoProceso => ''
					,p_Cantidad => 0
					,p_Nemo => '');
		END;
    
		END;
		
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------

	END;
END; 
GO