SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ModificarEventoCapitalCliente";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ModificarEventoCapitalCliente" (


--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
	IN p_IdEventoCapitalCliente				BIGINT     		default 0,
	IN p_CantidadDerecho					DECIMAL(26,8)	default 0,
	IN p_CantidadNueva						BIGINT     		default 0,
	IN p_MontoCapital						BIGINT     		default 0,
	IN p_MontoInteres						DECIMAL(26,8)	default 0,	
	IN p_TipoCartera						NVARCHAR(3) 	default '',
	IN p_IdBoveda							NVARCHAR(3) 	default '',
	IN p_Total								DECIMAL(26,8)	default 0,	

	IN p_CantidadDerechoArchivo				BIGINT	DEFAULT 0,
	IN p_DiferenciaCantidadDerechoArchivo	BIGINT	DEFAULT 0,
	IN p_CantidadNuevaArchivo				BIGINT			DEFAULT 0,
	IN p_DiferenciaCantidadNuevaArchivo		BIGINT	DEFAULT 0,
	IN p_MontoBeneficioArchivo				BIGINT	DEFAULT 0,
	IN p_DiferenciaMontoBeneficioArchivo	BIGINT	DEFAULT 0,
	IN p_MontoInteresArchivo				BIGINT	DEFAULT 0,
	IN p_DiferenciaMontoInteresArchivo		BIGINT	DEFAULT 0,
	IN p_MontoCapitalArchivo				BIGINT	DEFAULT 0,
	IN p_DiferenciaMontoCapitalArchivo		BIGINT	DEFAULT 0,
	
	IN p_UsaReglasNegocio 					NVARCHAR(1) 	default 'N',

	-- Parametros de Salida
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,   
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)),
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
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
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

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_MiExiste 				NVARCHAR(1);
		DECLARE v_MensajeRegla          NVARCHAR(520);
		DECLARE v_ResultadoRegla        NVARCHAR(40);
		DECLARE v_IdEjecucion 			BIGINT;
		DECLARE v_out_REF_ID			BIGINT;
		
		--variables de proceso
/*		DECLARE v_EstadoCarteraVigente 	NVARCHAR(3);*/
		DECLARE v_FechaSistema 			TIMESTAMP;
		DECLARE v_Secuencia				BIGINT := 0;
		DECLARE v_Total					BIGINT := 0;
		DECLARE v_TotalSP				DECIMAL(26,8);
		DECLARE v_Factor				DECIMAL(26,8);
		DECLARE v_CantidadDerecho		DECIMAL(26,8);
		DECLARE v_TotalEvento			DECIMAL(26,8);
		DECLARE v_EventoCapital			DECIMAL(26,8);
		DECLARE v_TipoSocioNegocio		NVARCHAR(10);
		DECLARE v_Rango					BIGINT;
		DECLARE v_Cuenta				BIGINT DEFAULT 0;
		
		v_Rango := 100;
		
		p_out_TblId = SELECT 0 as "Ids" from dummy where 1=2;
		
		
		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
	 	v_IdEjecucion:=0;
		p_TblErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		v_TablaErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
		
			declare v_resultadoRegla nvarchar(40);
				
			SELECT COUNT("Valor") INTO v_Cuenta
			FROM "BSCL.Custodia.BD::CUS.Configuracion"
			WHERE "IdEmpresa" = p_IdEmpresa 
			  AND "CodigoParametro" = 'CUS.Rango';				
			  
			if (v_Cuenta > 0) then
				SELECT "IdEventoCapital", "CantidadDerecho", "Total", "TipoSocioNegocio" INTO v_EventoCapital, v_CantidadDerecho, v_TotalEvento, v_TipoSocioNegocio
				FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				WHERE "IdEventoCapitalCliente" = p_IdEventoCapitalCliente;
	
				SELECT "Factor" INTO v_Factor
				FROM "BSCL.Custodia.BD::CUS.EventoCapital"
				WHERE "IdEventoCapital" = v_EventoCapital;
	
				SELECT "Total" into v_Total
				FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				WHERE "IdEventoCapitalCliente" = p_IdEventoCapitalCliente;
				
				SELECT "Valor" INTO v_Rango 
				FROM "BSCL.Custodia.BD::CUS.Configuracion"
				WHERE "IdEmpresa" = p_IdEmpresa AND "CodigoParametro" = 'CUS.Rango';
				
				IF (v_Total = p_Total and v_CantidadDerecho != p_CantidadDerecho ) and (v_CantidadDerecho != ABS(round(p_Total / v_Factor))) THEN
					p_out_ErroresContador = 10;
					p_out_ErroresDetalle = select 'UI_MONTO_NO_PERMITIDO' as "Codigo",'UI_MONTO_NO_PERMITIDO' as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;
	
				IF (ABS(p_Total) - ABS(v_Total))> v_Rango THEN
					p_out_ErroresContador = 10;
					p_out_ErroresDetalle = select 'UI_MONTO_SOBREPASA_RANGO' as "Codigo",'UI_MONTO_SOBREPASA_RANGO' as "Descripcion" from dummy;
					SIGNAL SALIDA_CONTROLADA;
				END IF;			
	
				
				--Si existen errores en las validaciones debe salir
				select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
				IF :p_out_ErroresContador > 0 THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;				
			end if;
		END;
		-----------------------------------------------------
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------

		
		-----------------------------------------------------
		--proceso de Negocio
		---------------------------------------------------
		BEGIN
		
		v_TotalSP := p_Total;
		
		SELECT "Total" into v_Total
		FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
		WHERE "IdEventoCapitalCliente" = p_IdEventoCapitalCliente;
		
		IF :v_Total < 0 and :v_TipoSocioNegocio <> 'CNT' THEN
			v_TotalSP = (v_TotalSP * -1);
		END IF;
		
		UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
		SET
			"CantidadDerecho"					= p_CantidadDerecho,
			"CantidadNueva"						= p_CantidadNueva,
			"MontoCapital"						= p_MontoCapital,
			"MontoInteres"						= p_MontoInteres,
			"TipoCartera"						= p_TipoCartera,
			"IdBoveda"							= p_IdBoveda,
			"Total"								= v_TotalSP,
			"CantidadDerechoArchivo"			= p_CantidadDerechoArchivo,
			"DiferenciaCantidadDerecho"			= p_DiferenciaCantidadDerechoArchivo,
			"AccionesDisponiblesArchivo"		= p_CantidadNuevaArchivo,
			"DiferenciaAccionesCantidadNueva"	= p_DiferenciaCantidadNuevaArchivo,
			"MontoBeneficioArchivo"				= p_MontoBeneficioArchivo,
			"DiferenciaMontoBeneficio"			= p_DiferenciaMontoBeneficioArchivo,
			"MontoInteresArchivo"				= p_MontoInteresArchivo,
			"DiferenciaMontoInteres"			= p_DiferenciaMontoInteresArchivo,
			"MontoCapitalArchivo"				= p_MontoCapitalArchivo,
			"DiferenciaMontoCapital"			= p_DiferenciaMontoCapitalArchivo,
			"FechaHoraUltimaActualizacion"		= CURRENT_UTCTIMESTAMP
		WHERE
			"IdEventoCapitalCliente" = p_IdEventoCapitalCliente;
			
		    Select 1 into p_out_Secuencia FROM DUMMY;
    
    		if p_out_ErroresContador > 0 then 
				SIGNAL SALIDA_CONTROLADA;
			end if;
    
		END;
		
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------
--32226,000
	END;
END; 
GO