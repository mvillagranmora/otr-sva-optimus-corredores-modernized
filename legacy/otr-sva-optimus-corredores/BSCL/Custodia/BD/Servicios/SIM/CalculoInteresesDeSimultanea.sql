SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::SIM.CalculoInteresesDeSimultanea";

CREATE PROCEDURE   
-- 
--  Proposito: Realizar cálculo de interes para las simultanea
--  Autor: NRM - BCS
--  Fecha Creacion: 16/11/2015  
--   
"BSCL.Custodia.BD.Servicios::SIM.CalculoInteresesDeSimultanea" (
	-- Parametros entrada comunes en todos los servicios publicos
    IN p_IdEmpresa					BIGINT,
    IN p_IdUsuario					BIGINT, 
    IN p_IdModulo					NVARCHAR(3),        
    
    IN p_FechaSistema				NVARCHAR(40),
    IN p_Precio						DECIMAL(26,8),				    
    IN p_Plazo						DECIMAL(26,8),
    IN p_PlazoResidual				DECIMAL(26,8),
    IN p_Base						DECIMAL(26,8),    
	IN p_Cantidad					DECIMAL(26,8),
    IN p_Tasa						DECIMAL(26,8),

	-- Parametros salida del servicio publico	
	OUT p_out_InteresDiario			DECIMAL(26,8),
	OUT p_out_InteresAcumulado		DECIMAL(26,8),
	OUT p_out_ValorPresente			DECIMAL(26,8),
	OUT p_out_MontoAPlazo			DECIMAL(26,8),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  INT,
	OUT p_out_AdvertenciasDetalle   TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) default empty,	
	OUT p_out_ErroresContador       INT,
	OUT p_out_ErroresDetalle        TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000)) default empty	
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
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;	 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt nvarchar(40);

		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;
								   				
			SELECT COUNT(1) INTO p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log ser? captado por la capa de XS control)		
			RESIGNAL;
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle		= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	 

	BEGIN
		DECLARE v_InteresDiario				DECIMAL(26,8) := 0;		
		DECLARE v_InteresAcumulado			DECIMAL(26,8) := 0;
		DECLARE v_ValorPresente				DECIMAL(26,8) := 0;
		DECLARE v_DiasTranscurrido			INTEGER := 0;
		DECLARE v_Monto						DECIMAL(26,8) := 0;
		DECLARE v_TasaPorPlazo				DECIMAL(26,8) := 0;
		DECLARE v_TasaPorDiasTranscurrido	DECIMAL(26,8) := 0;
		DECLARE v_MontoAPlazo				DECIMAL(26,8) := 0;
		 
		
		v_DiasTranscurrido 	:= p_Plazo - p_PlazoResidual;
		v_Monto 			:= p_Precio * p_Cantidad;
		
		
		v_TasaPorPlazo 				:= (p_Tasa/100)*(p_Plazo/p_Base);
		v_TasaPorDiasTranscurrido	:= (p_Tasa/100)*(v_DiasTranscurrido/p_Base);
				
		
		v_InteresDiario		:= (v_Monto * (1 + v_TasaPorPlazo)) - (v_Monto * (1 + v_TasaPorDiasTranscurrido));		
		v_InteresAcumulado 	:= (v_Monto * (1 + v_TasaPorDiasTranscurrido)) - v_Monto;
		
		
		v_MontoAPlazo		:= v_Monto * (1 + v_TasaPorPlazo);
		v_ValorPresente     := ROUND(ROUND(p_Precio * (1 + (p_Tasa * p_PlazoResidual) /3000), 3) * p_Cantidad, 0);

		/*select 
			v_DiasTranscurrido as "v_DiasTranscurrido",
			v_Monto as "Monto",
			v_TasaPorPlazo as "TasaPorPlazo",
			v_TasaPorDiasTranscurrido as "TasaPorDiasTranscurrido",			
			v_InteresDiario as "InteresDiario",
			v_InteresAcumulado as "InteresAcumulado",
			v_ValorPresente as "ValorPresente",
			v_MontoAPlazo as "MontoAPlazo"
		FROM DUMMY;*/
		
		p_out_InteresDiario		:= v_InteresDiario;
		p_out_InteresAcumulado	:= v_InteresAcumulado;
		p_out_ValorPresente		:= v_ValorPresente;		
		p_out_MontoAPlazo		:= v_MontoAPlazo;
	END;

END;
