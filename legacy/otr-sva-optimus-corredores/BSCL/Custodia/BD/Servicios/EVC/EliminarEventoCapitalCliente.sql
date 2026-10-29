SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.EliminarEventoCapitalCliente";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.EliminarEventoCapitalCliente" (


--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
	IN p_IdEventoCapitalCliente				BIGINT     		default 0,

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
		
		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
	   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
--			declare v_resultadoRegla nvarchar(40);
			DECLARE v_Quantity   INT;
			
			v_Quantity :=0;	   	
			SELECT count(1) INTO p_out_ErroresContador
		    FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
			WHERE "IdEventoCapitalCliente" = p_IdEventoCapitalCliente
			  and "IdEstado" != 'VIG';

			
			IF (p_out_ErroresContador > 0) THEN				
				p_out_ErroresDetalle = SELECT 'EVENTO_CAPITAL_CLIENTE' as "Codigo", 'ERROR' as "Descripcion" FROM DUMMY;
		 	END IF;
			
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			ELSE
				DELETE FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
				WHERE "IdEventoCapitalCliente" = p_IdEventoCapitalCliente;
			END IF;				
		END;
		
		
    
		END;
		
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------

--	END;
END; 
