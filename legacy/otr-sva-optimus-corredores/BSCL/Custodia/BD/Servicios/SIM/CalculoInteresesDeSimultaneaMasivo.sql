SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::SIM.CalculoInteresesDeSimultaneaMasivo";

CREATE PROCEDURE   
-- 
--  Proposito: Realizar cálculo de interes para las simultanea
--  Fecha Creacion: 18-02-2015
--   
"BSCL.Custodia.BD.Servicios::SIM.CalculoInteresesDeSimultaneaMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
    IN p_IdEmpresa					BIGINT,
    IN p_IdUsuario					BIGINT, 
    IN p_IdModulo					NVARCHAR(3),        
    
    IN p_FechaSistema				NVARCHAR(40),
-- p_tbl=BSCL.Custodia.BD::GBL.CalculoInteresesDeSimultaneaMasivo_Entrada_tbl


--SALIDAS
-- p_out_AdvertenciasDetalle=BSCL.Custodia.BD::GBL.CalculoInteresesDeSimultaneaMasivo_Salida_AdvertenciasDetalle;
-- p_out_ErroresDetalle=BSCL.Custodia.BD::GBL.CalculoInteresesDeSimultaneaMasivo_Salida_ErroresDetalle;
-- p_tbl_out=BSCL.Custodia.BD::GBL.CalculoInteresesDeSimultaneaMasivo_Salida_tbl_out;
	OUT p_out_AdvertenciasContador  INT,
	OUT p_out_ErroresContador       INT
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
		SELECT COUNT(1) INTO p_out_ErroresContador FROM "BSCL.Custodia.BD::GBL.CalculoInteresesDeSimultaneaMasivo_Salida_ErroresDetalle";
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM "BSCL.Custodia.BD::GBL.CalculoInteresesDeSimultaneaMasivo_Salida_AdvertenciasDetalle";	 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt nvarchar(40);

		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);
			
			INSERT INTO "BSCL.Custodia.BD::GBL.CalculoInteresesDeSimultaneaMasivo_Salida_ErroresDetalle" ("Codigo","Descripcion")
			SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM DUMMY;
								   				
			SELECT COUNT(1) INTO p_out_ErroresContador FROM "BSCL.Custodia.BD::GBL.CalculoInteresesDeSimultaneaMasivo_Salida_ErroresDetalle";									
		ELSE
			--Error interno no manejado  (El log ser? captado por la capa de XS control)		
			RESIGNAL;
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;

	 

	BEGIN
/***************************	
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
		v_ValorPresente		:= v_Monto * (1 + v_TasaPorDiasTranscurrido);
		
************************************/
		DECLARE v_tbl_out TABLE(
			"Id" 				BIGINT,
			"Precio"			DECIMAL(26,8),
			"Plazo"				DECIMAL(26,8),
			"PlazoResidual"		DECIMAL(26,8),
			"Base"				DECIMAL(26,8),
			"Cantidad"			DECIMAL(26,8),
			"Tasa"				DECIMAL(26,8),
			"DiasTranscurrido" 	DECIMAL(26,8),
			"Monto" 			DECIMAL(26,8),
			"TasaPorPlazo"		DECIMAL(26,8),
			"TasaPorDiasTranscurrido" DECIMAL(26,8),
			"InteresDiario"		DECIMAL(26,8),
			"InteresAcumulado" 	DECIMAL(26,8),
			"ValorPresente"		DECIMAL(26,8),
			"MontoAPlazo"		DECIMAL(26,8)
		);

		v_tbl_out = select 
						"Id", "Precio", "Plazo", "PlazoResidual", "Base", "Cantidad", "Tasa",
						
						CAST("Plazo" - "PlazoResidual" as DECIMAL(26,8)) as "DiasTranscurrido",
	    				CAST("Precio" * "Cantidad" as DECIMAL(26,8)) as "Monto",
	    			 	CAST(("Tasa"/100)*("Plazo"/"Base") as DECIMAL(26,8)) as "TasaPorPlazo",
	    			 	CAST(("Tasa"/100)*(("Plazo" - "PlazoResidual")/"Base") as DECIMAL(26,8)) as "TasaPorDiasTranscurrido",
	    			 	
						0 as "InteresDiario",
		    			0 as "InteresAcumulado",
		    			0 as "ValorPresente",
		    			0 as "MontoAPlazo"
    			 	FROM "BSCL.Custodia.BD::GBL.CalculoInteresesDeSimultaneaMasivo_Entrada_tbl";


		INSERT INTO "BSCL.Custodia.BD::GBL.CalculoInteresesDeSimultaneaMasivo_Salida_tbl_out" ("Id","Precio","Plazo","PlazoResidual","Base","Cantidad","Tasa",
			"DiasTranscurrido","Monto","TasaPorPlazo","TasaPorDiasTranscurrido","InteresDiario","InteresAcumulado","ValorPresente","MontoAPlazo")
    	SELECT "Id", "Precio", "Plazo", "PlazoResidual", "Base", "Cantidad", "Tasa",
						"DiasTranscurrido",
	    				"Monto",
	    			 	"TasaPorPlazo",
	    			 	"TasaPorDiasTranscurrido",
	    			 	
						CAST(("Monto" * (1 + "TasaPorPlazo")) - ("Monto" * (1 + "TasaPorDiasTranscurrido")) as DECIMAL(26,8)) as "InteresDiario",
		    			CAST(("Monto" * (1 + "TasaPorDiasTranscurrido")) - "Monto" as DECIMAL(26,8)) as "InteresAcumulado",
		    			CAST("Monto" * (1 + "TasaPorDiasTranscurrido") as DECIMAL(26,8)) as "ValorPresente",
		    			CAST("Monto" * (1 + "TasaPorPlazo") as DECIMAL(26,8)) as "MontoAPlazo"
    	FROM :v_tbl_out;

	END;

END;
