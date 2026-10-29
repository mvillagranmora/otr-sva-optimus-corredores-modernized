SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::CrearCoberturaGarantia";
GO
CREATE PROCEDURE
-- 
--  Proposito: Crear un registro de Cobertura de Garantias(Mantenedor de Coberturas)
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 13/10/2015
--  
"BSCL.Plataforma.BD.Servicios::CrearCoberturaGarantia" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN  p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo						NVARCHAR(3),  	--ID del Modulo que lo invoca	
	IN  p_simularOperacion				NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)
	IN  p_UsaReglasNegocio	 			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	
	-- Parametros entrada
	IN p_IdEntidadDestinoGarantia 		BIGINT,
    IN p_IdTipoInstrumentoExigeGarantia NVARCHAR(3),
    IN p_IdInstrumentoExigeGarantia  	BIGINT,
    IN p_IdTipoInstrumentoComoGarantia  NVARCHAR(3),        
    IN p_IdTipoClasificacionRiesgo   	NVARCHAR(4),
    IN p_ClasificacionRiesgo       	    NVARCHAR(20),
	IN p_PlazoTope					 	INTEGER,
	IN p_UnidadPlazo				 	NVARCHAR(1),    --A: año M: mes D: dia
	IN p_CoberturaEntidad		     	DECIMAL(5,2),
	IN p_CoberturaCorredora	         	DECIMAL(5,2),
    IN p_IdEstado                    	NVARCHAR(3),
    IN p_IdEntidadClasificadoraRiesgo   NVARCHAR(10),

	-- Parametros salida del servicio publico
    OUT p_out_REF_ID                 	BIGINT,	
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  nvarchar(5000);	   	     	
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
			--Error interno no manejado  (El log será captado por la capa de XS control)		
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
		DECLARE v_resultadoRegla 				NVARCHAR(40);    
		DECLARE v_ErroresDetalle			    TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000));
	
		v_ErroresDetalle						= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;

	
		-----------------------------------------------------
		-- VALIDACIONES
		-----------------------------------------------------

		IF p_UsaReglasNegocio = 'S' THEN
		BEGIN
		
			-- Validación básica de Empresa, Usuario, Módulo
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle,:v_resultadoRegla);
			p_out_ErroresDetalle = SELECT "Codigo","Descripcion" from :v_out_ErroresDetalle;
			IF :v_resultadoRegla = 'RECH' THEN
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;

			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosEditarCoberturaGarantia" (0, :p_IdEntidadDestinoGarantia, :p_IdTipoInstrumentoExigeGarantia, :p_IdInstrumentoExigeGarantia, :p_IdTipoInstrumentoComoGarantia, :p_IdEntidadClasificadoraRiesgo, :p_ClasificacionRiesgo, :v_ErroresDetalle, :v_resultadoRegla);
			IF :v_resultadoRegla = 'RECH' THEN			
				p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :v_ErroresDetalle UNION ALL
						  			   SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle;
				SIGNAL SALIDA_CONTROLADA; 	
			ELSEIF :v_resultadoRegla = 'ADVE' THEN
				--LLamada al servicio de autorizacion si se requiere
				p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_ErroresDetalle;
				SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;	
			END IF;  -- IF :v_resultadoRegla = 'RECH'

		END; 
		END IF;


		-- Solo fue una simulacion del servicio
		if (p_simularOperacion = 'S') then
			SIGNAL SALIDA_CONTROLADA;
		end if;

		--------------------
		--Negocio Aqui
		--------------------	
		BEGIN
			-- Genera Id para nueva Cobertura de Garantia
	   	    SELECT "BSCL.Plataforma.BD.Secuencias::CoberturaGarantia".NEXTVAL INTO p_out_REF_ID FROM DUMMY;

			INSERT INTO "BSCL.Plataforma.BD::GTA.CoberturaGarantia"
		    (
				"IdCoberturaGarantia",
				"IdTipoInstrumentoExigeGarantia",
			    "IdInstrumentoExigeGarantia",
				"IdTipoInstrumentoComoGarantia",
			    "IdTipoClasificacionRiesgo",				
				"PlazoTope",
				"UnidadPlazo",
				"CoberturaEntidad",   
				"CoberturaCorredora",
			    "IdEstado", 	
			    "IdEntidadClasificadoraRiesgo",
			    "ClasificacionRiesgo", 
   				"IdEntidadDestinoGarantia"
			)
			VALUES(
				:p_out_REF_ID,
			    COALESCE(:p_IdTipoInstrumentoExigeGarantia,''),
			    COALESCE(:p_IdInstrumentoExigeGarantia,0),			    
				COALESCE(:p_IdTipoInstrumentoComoGarantia,''),
			    COALESCE(:p_IdTipoClasificacionRiesgo,''),		    
				COALESCE(:p_PlazoTope,0),
				COALESCE(:p_UnidadPlazo,''),
				COALESCE(:p_CoberturaEntidad,0),
				COALESCE(:p_CoberturaCorredora,0),
			    COALESCE(:p_IdEstado,''),
			    COALESCE(:p_IdEntidadClasificadoraRiesgo,''),
			    COALESCE(:p_ClasificacionRiesgo,''),
			    COALESCE(:p_IdEntidadDestinoGarantia,0)
			);
	    END;
	END;	
END;
