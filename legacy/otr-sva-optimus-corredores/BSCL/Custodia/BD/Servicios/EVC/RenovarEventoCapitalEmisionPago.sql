SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.RenovarEventoCapitalEmisionPago";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.RenovarEventoCapitalEmisionPago" (


--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
	IN p_IdEventoCapital					BIGINT     		default 0,
	IN p_UsaReglasNegocio 					NVARCHAR(1) 	default 'N',
	
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


	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------

		--variables de proceso
		DECLARE v_FechaSistema 			TIMESTAMP;
		DECLARE v_IdEventoCapital		BIGINT := 0;
		
		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
	   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
--			declare v_resultadoRegla nvarchar(40);

		
			IF (p_out_ErroresContador > 0) THEN				
				p_out_ErroresDetalle = SELECT 'EVENTO_CAPITAL_CLIENTE' as "Codigo", 'ERROR' as "Descripcion" FROM DUMMY;
		 	END IF;
			
		-- Recuperamos numero de secuencia que usaremos para en el evento
	   		SELECT     "BSCL.Custodia.BD.Secuencias::EventoCapital".NEXTVAL INTO v_IdEventoCapital FROM DUMMY;


		-- Inserta Evento Capital 'EP'

				INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapital"
			    (
					"IdEventoCapital",
					"IdEmpresa",
					"IdTipoEventoCapital",
					"Secuencia",
					"Descripcion",
					"EsInscrito",
					"IdClasificacionTipoInstrumento",
					"IdTipoInstrumento",
					"IdInstrumento",
					"Nemo",
					"Factor",
					"Valor",
					"IdMoneda",
					"IdInstrumentoNuevo",
					"NemoNuevo",
					"FactorNuevo",
					"FechaActualizacion",
					"FechaLimite",
					"FechaPago",
					"NumeroDividendo",
					"IdTipoDividendo",
					"PrecioAnterior",
					"FechaHoraIngresoRegistro",
					"FechaHoraUltimaActualizacion",
					"TipoIngreso",
					"IdUsuario",
					"IdEstado"
				)
				SELECT 
					:v_IdEventoCapital,
					"IdEmpresa",
					'EP',								
					"Secuencia",
					"Descripcion",
					"EsInscrito",
					"IdClasificacionTipoInstrumento",
					"IdTipoInstrumento",
					"IdInstrumento",
					"Nemo",
					"Factor",
					"Valor",
					"IdMoneda",
					"IdInstrumentoNuevo",
					"NemoNuevo",
					"FactorNuevo",
					"FechaActualizacion",
					v_FechaSistema,
					v_FechaSistema,
					"NumeroDividendo",
					"IdTipoDividendo",
					"PrecioAnterior",
					"FechaHoraIngresoRegistro",
					"FechaHoraUltimaActualizacion",
					"TipoIngreso",
					"IdUsuario",
					'VIG'
				FROM 
					"BSCL.Custodia.BD::CUS.EventoCapital"
				WHERE
					"IdEventoCapital" = p_IdEventoCapital;	
		END;
		
		SELECT v_IdEventoCapital INTO  p_out_Secuencia FROM DUMMY;
    
		END;
		
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------

--	END;
END; 
