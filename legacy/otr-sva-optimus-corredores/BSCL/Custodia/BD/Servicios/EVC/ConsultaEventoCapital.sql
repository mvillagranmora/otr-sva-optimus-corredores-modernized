SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ConsultaEventoCapital";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ConsultaEventoCapital" (

--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
	IN p_IdEventoCapital					BIGINT     		default 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2)		default '',	

	IN p_UsaReglasNegocio 					NVARCHAR(1) 	default 'N',
	
	
	-- Parametros salida del servicio publico

	OUT p_out_IdTipoEventoCapital				NVARCHAR(2),
	OUT p_out_TipoEvento						NVARCHAR(50),
	OUT p_out_IdTipoInstrumento					NVARCHAR(3),
	OUT p_out_IdClasificacionTipoInstrumento	NVARCHAR(3),
	OUT p_out_TipoInstrumento					NVARCHAR(40),
	OUT p_out_IdInstrumento						BIGINT,
	OUT p_out_Nemo								NVARCHAR(40),
	OUT p_out_FechaPago							DATE,
	OUT p_out_Valor								DECIMAL(26,8),
	OUT p_out_Factor							DECIMAL(26,8),
	OUT p_out_DescripcionEvento					NVARCHAR(100),
	OUT p_out_ISFUT								NVARCHAR(1),
	OUT p_out_RETART104							NVARCHAR(1),
	OUT p_out_TasaEfectiva						DECIMAL(26,8),
	
											  
	-- Parametros de Salida
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

	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
	
		--variables de proceso
		DECLARE v_FechaSistema 			TIMESTAMP;
		DECLARE v_Secuencia_max			BIGINT DEFAULT 0;
		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
	   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
--			declare v_resultadoRegla nvarchar(40);
			DECLARE v_Quantity   INT;

			SELECT MAX("Secuencia") INTO v_Secuencia_max
		    FROM "BSCL.Custodia.BD::CUS.EventoCapital"
			WHERE "NumeroDividendo"		= p_IdEventoCapital
			  AND "IdTipoEventoCapital" = p_IdTipoEventoCapital;
			  			
			v_Quantity :=0;	   	
			SELECT count(1) INTO p_out_ErroresContador
		    FROM "BSCL.Custodia.BD::CUS.EventoCapital"
--			WHERE "Secuencia" 			= p_IdEventoCapital
			WHERE "NumeroDividendo"		= p_IdEventoCapital
			  AND "Secuencia"			= :v_Secuencia_max
			  AND "IdTipoEventoCapital" = p_IdTipoEventoCapital;

	
			p_out_IdTipoEventoCapital 				:= '';
			p_out_TipoEvento						:= '';
			p_out_IdTipoInstrumento					:= '';
			p_out_IdClasificacionTipoInstrumento 	:= '';
			p_out_TipoInstrumento					:= '';
			p_out_IdInstrumento						:= 0;
			p_out_Nemo								:= '';
			p_out_FechaPago							:= '0001-01-01';
			p_out_Valor								:= 0;
			p_out_Factor							:= 0;
			p_out_DescripcionEvento					:= '';
			p_out_ISFUT								:= 'N';
			p_out_RETART104							:= 'N';
			p_out_TasaEfectiva						:= 0;

			IF (p_out_ErroresContador = 0) THEN				
				p_out_ErroresDetalle = SELECT 'EVENTO_CAPITAL' as "Codigo", 'ERROR' as "Descripcion" FROM DUMMY;
		 	END IF;
			
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			
			IF p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			ELSE
				SELECT 	"Secuencia",
						"IdTipoEventoCapital",
						"TipoEvento", 
						"IdTipoInstrumento", 
						"IdClasificacionTipoInstrumento",
						"TipoInstrumento", 
						"IdInstrumento", 
						"Nemo", 
						"FechaPago", 
						"Valor", 
						"Factor" , 
						"DescripcionEvento" ,
						"ISFUT",
						"RETART104",
						"TasaEfectiva"
						
						into 	p_out_Secuencia,
								p_out_IdTipoEventoCapital,
								p_out_TipoEvento,
								p_out_IdTipoInstrumento,
								p_out_IdClasificacionTipoInstrumento,
								p_out_TipoInstrumento,
								p_out_IdInstrumento,
								p_out_Nemo,
								p_out_FechaPago,
								p_out_Valor,
								p_out_Factor,
								p_out_DescripcionEvento,
								p_out_ISFUT,
								p_out_RETART104,
								p_out_TasaEfectiva
								
				FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalFull" 
--				WHERE "Secuencia" 			= p_IdEventoCapital
			WHERE "NumeroDividendo"			= p_IdEventoCapital
			  AND "Secuencia"				= :v_Secuencia_max
			  AND "IdTipoEventoCapital" 	= p_IdTipoEventoCapital;
				
			END IF;				

		END;
		
		
    
		END;
		
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------

--	END;
END; 
