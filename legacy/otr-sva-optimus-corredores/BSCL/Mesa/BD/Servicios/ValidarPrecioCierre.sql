SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::ValidarPrecioCierre";
-- 
--  Proposito: Validar Rango Limites Precio Cierre en ingreso operacion Spot o Forward respecto a Mantenedor de PYTC.
--  Fecha Creacion: 28/09/2017
--
CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ValidarPrecioCierre"(
	IN p_IdEmpresa			BIGINT,
	IN p_IdUsuario			BIGINT,
	IN p_IdModulo			NVARCHAR(3),
	IN p_FechaOperacion		DATE,
	IN p_Nemotecnico		NVARCHAR(40),
	IN p_IdMonedaPrincipal	NVARCHAR(3),
	IN p_TipoCambioCierre	DECIMAL(26,8),
	IN p_ParidadCierre		DECIMAL(26,8),
	IN p_TipoCambioCosto	DECIMAL(26,8),
	IN p_ParidadCosto		DECIMAL(26,8),
	
	OUT p_out_Salida TABLE ("CodigoTCCierre" NVARCHAR(100), "CodigoTCCosto" NVARCHAR(100), "LimiteInferior" DECIMAL(26,8), "LimiteSuperior" DECIMAL(26,8), "Limites" NVARCHAR(100))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE v_Count BIGINT;
	
	v_RangoLimites = 
		SELECT
			COALESCE(PYT."ParidadMercado",0) + COALESCE(PYT."LimiteSuperiorParidad",0) 			AS "LimiteSuperiorPAR",
			COALESCE(PYT."ParidadMercado",0) - COALESCE(PYT."LimiteInferiorParidad",0) 			AS "LimiteInferiorPAR",
			COALESCE(PYT."TipoCambioMercado",0) + COALESCE(PYT."LimiteSuperiorTipoCambio",0) 	AS "LimiteSuperiorTC",
			COALESCE(PYT."TipoCambioMercado",0) - COALESCE(PYT."LimiteInferiorTipoCambio",0) 	AS "LimiteInferiorTC"
		FROM "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PYT 
		WHERE PYT."IdMoneda" = :p_IdMonedaPrincipal
		  AND PYT."Fecha" = TO_VARCHAR(:p_FechaOperacion,'YYYY-MM-DD');
	
	SELECT COUNT(1) INTO v_Count FROM :v_RangoLimites;
	IF v_Count = 0 THEN
		v_RangoLimites = 
			SELECT 0 AS "LimiteSuperiorPAR", 0 AS "LimiteInferiorPAR", 0 AS "LimiteSuperiorTC", 0 AS "LimiteInferiorTC"
			FROM DUMMY;
	END IF;
	
	v_validacion = 
		SELECT
			"LimiteInferiorPAR", "LimiteSuperiorPAR", "LimiteInferiorTC", "LimiteSuperiorTC",
			CASE WHEN :p_Nemotecnico IN ('DISTRI','INTERB','SECANO','SEINFL') THEN
					CASE WHEN :p_TipoCambioCierre < "LimiteInferiorTC" OR :p_TipoCambioCierre > "LimiteSuperiorTC" THEN 2 ELSE 1 END
				 WHEN :p_Nemotecnico IN ('ARBDIS','ARBINT','ARBITR') THEN
					CASE WHEN :p_ParidadCierre < "LimiteInferiorPAR" OR :p_ParidadCierre > "LimiteSuperiorPAR" THEN 3 ELSE 1 END
				 ELSE 4 END AS "ResultadoTCCierre",
			CASE WHEN :p_Nemotecnico ='DISTRI' THEN
					CASE WHEN :p_TipoCambioCosto < "LimiteInferiorTC" OR :p_TipoCambioCosto > "LimiteSuperiorTC" THEN 6 ELSE 5 END
				 WHEN :p_Nemotecnico = 'ARBDIS' THEN
					CASE WHEN :p_ParidadCosto < "LimiteInferiorPAR" OR :p_ParidadCosto > "LimiteSuperiorPAR" THEN 7 ELSE 5 END
				 ELSE 8 END AS "ResultadoTCCosto"
		FROM :v_RangoLimites;
	
	p_out_Salida =
		SELECT
			CASE WHEN "ResultadoTCCierre" = 2 THEN 'UI_TCCIERRE_FUERA_RANGO'
				 WHEN "ResultadoTCCierre" = 3 THEN 'UI_PACIERRE_FUERA_RANGO'
				 ELSE 'OK' END AS "CodigoTCCierre",
			CASE WHEN "ResultadoTCCosto" = 6 THEN 'UI_TCCOSTO_FUERA_RANGO'
				 WHEN "ResultadoTCCosto" = 7 THEN 'UI_PACOSTO_FUERA_RANGO'
				 ELSE 'OK' END AS "CodigoTCCosto",
			CASE WHEN :p_Nemotecnico IN ('DISTRI','INTERB','SECANO','SEINFL') THEN "LimiteInferiorTC"
				 WHEN :p_Nemotecnico IN ('ARBDIS','ARBINT','ARBITR') THEN "LimiteInferiorPAR"
				 ELSE 0 END AS "LimiteInferior",
			CASE WHEN :p_Nemotecnico IN ('DISTRI','INTERB','SECANO','SEINFL') THEN "LimiteSuperiorTC"
				 WHEN :p_Nemotecnico IN ('ARBDIS','ARBINT','ARBITR') THEN "LimiteSuperiorPAR"
				 ELSE 0 END AS "LimiteSuperior",
			CASE WHEN :p_Nemotecnico IN ('DISTRI','INTERB','SECANO','SEINFL') THEN "LimiteInferiorTC"||' - '||"LimiteSuperiorTC"
				 WHEN :p_Nemotecnico IN ('ARBDIS','ARBINT','ARBITR') THEN "LimiteInferiorPAR"||' - '||"LimiteSuperiorPAR"
				 ELSE '0 - 0' END AS "Limites"
		FROM :v_validacion;
	
END;
