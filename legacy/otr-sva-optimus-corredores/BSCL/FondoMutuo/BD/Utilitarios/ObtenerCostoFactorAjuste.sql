SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.FondoMutuo.BD.Utilitarios::ObtenerCostoFactorAjuste";

--  Proposito: Calcula el costo de factor ajuste
--  Autor: - Gat-Blac 
--  Fecha Creacion: 15/11/2016
CREATE FUNCTION "BSCL.FondoMutuo.BD.Utilitarios::ObtenerCostoFactorAjuste"(
	IN p_EntradaFactorAjuste	"BSCL.FondoMutuo.BD.Tipos::EntradaCostoFactorAjuste"
) RETURNS "BSCL.FondoMutuo.BD.Tipos::SalidaCostoFactorAjuste"
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL AS
BEGIN
		
	v_validaciones = SELECT "NumeroCalculoCostoFactorAjuste",
									"CantidadRescate",
									"CantidadTotalCartera",
									"CostoFactorAjusteTotalCartera",
									CASE WHEN "CantidadRescate" > 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoCantidadRescate",
									CASE WHEN "CantidadTotalCartera" > 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoCantidadTotalCartera",
									CASE WHEN "CostoFactorAjusteTotalCartera" > 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoCostoFactorAjusteTotalCartera"
							 FROM  :p_EntradaFactorAjuste;
						   
	v_datos_cartera_costo_rescate = SELECT "NumeroCalculoCostoFactorAjuste",
									   "CantidadRescate",
									   "CantidadTotalCartera",
									   "CostoFactorAjusteTotalCartera",
									   CASE WHEN 'ERR' IN ("ResultadoCantidadRescate","ResultadoCantidadTotalCartera","ResultadoCostoFactorAjusteTotalCartera")
									   THEN 0 
									   ELSE
									   	TO_DECIMAL(("CantidadRescate" * "CostoFactorAjusteTotalCartera")/("CantidadTotalCartera"),26,8) 
									   END AS "CostoFactorAjusteRescate",
									   CASE WHEN 'ERR' IN ("ResultadoCantidadRescate","ResultadoCantidadTotalCartera","ResultadoCostoFactorAjusteTotalCartera") THEN 'ERR'
									   ELSE 'OK'
									   END AS "Resultado",
									   CASE WHEN "ResultadoCantidadRescate" = 'ERR' THEN 'CANTIDAD_RESCATE_MAYOR_CERO'
									   		WHEN "ResultadoCantidadTotalCartera" = 'ERR' THEN 'CANTIDAD_TOTAL_CARTERA_MAYOR_CERO'
									   		WHEN "ResultadoCostoFactorAjusteTotalCartera" = 'ERR' THEN 'COSTO_FACTOR_REAJUSTE_MAYOR_CERO'
									   ELSE 'UI_EXITO' END AS "Codigo"
								FROM :v_validaciones;

	RETURN SELECT "NumeroCalculoCostoFactorAjuste",
				 "CantidadRescate",
				 "CantidadTotalCartera",
				 "CostoFactorAjusteTotalCartera",
				 "CostoFactorAjusteRescate",
				 TO_DECIMAL("CostoFactorAjusteTotalCartera" - "CostoFactorAjusteRescate",26,8) AS "CostoFactorAjusteRestante",
				 "Resultado",
				 "Codigo"
			FROM :v_datos_cartera_costo_rescate;
				
	
END;
