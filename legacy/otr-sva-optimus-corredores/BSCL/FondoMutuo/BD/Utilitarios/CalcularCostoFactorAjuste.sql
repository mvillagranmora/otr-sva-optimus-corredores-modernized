SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.FondoMutuo.BD.Utilitarios::CalcularCostoFactorAjuste";

CREATE PROCEDURE
--  Proposito: Calcular Costo Factor Ajuste con lo que se rescatara
--  Autor: - Gat-Blac 
--  Fecha Creacion: 14/11/2016
"BSCL.FondoMutuo.BD.Utilitarios::CalcularCostoFactorAjuste" (
	IN p_DatosCartera 	TABLE (
								"NumeroCalculoCostoFactorAjuste"	BIGINT,
								"CantidadRescate"					DECIMAL(26,8),
								"CantidadTotalCartera"				DECIMAL(26,8),
								"CostoFactorAjusteTotalCartera"		DECIMAL(26,8)
								),
	OUT p_out_Resultado  TABLE(
								"NumeroCalculoCostoFactorAjuste"	BIGINT,
								"CantidadRescate"					DECIMAL(26,8),
								"CantidadTotalCartera"				DECIMAL(26,8),
								"CostoFactorAjusteTotalCartera"		DECIMAL(26,8),
								"CostoFactorAjusteRescate"			DECIMAL(26,8),
								"CostoFactorAjusteRestante"			DECIMAL(26,8)
								),
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("NumeroCalculoCostoFactorAjuste"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("NumeroCalculoCostoFactorAjuste"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("NumeroCalculoCostoFactorAjuste" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("NumeroCalculoCostoFactorAjuste" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "NumeroCalculoCostoFactorAjuste" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "NumeroCalculoCostoFactorAjuste" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			RESIGNAL;
		END;
		
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
		BEGIN
			select count(1) into p_out_ErroresContador 
			from :v_out_ErroresDetalle;
			select count(1) into p_out_AdvertenciasContador 
			from :v_out_AdvertenciasDetalle;
			
			v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_ErroresDetalle
									UNION ALL
									SELECT "Codigo" AS "CodigoMensaje",
									"NombreParametro" as "CodigoDato",
									"ValorParametro" as "ValorDato" 
									FROM :v_out_AdvertenciasDetalle;
									
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
			
			p_out_ErroresDetalle = SELECT 	"errores"."NumeroCalculoCostoFactorAjuste",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."NumeroCalculoCostoFactorAjuste",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo";
										
		END; 
	
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
			DECLARE v_count BIGINT;
			
			v_validaciones = SELECT "NumeroCalculoCostoFactorAjuste",
									"CantidadRescate",
									"CantidadTotalCartera",
									"CostoFactorAjusteTotalCartera",
									CASE WHEN "CantidadRescate" > 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoCantidadRescate",
									CASE WHEN "CantidadTotalCartera" > 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoCantidadTotalCartera",
									CASE WHEN "CostoFactorAjusteTotalCartera" > 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoCostoFactorAjusteTotalCartera"
							 FROM  :p_DatosCartera;
			
			v_out_ErroresDetalle = SELECT "NumeroCalculoCostoFactorAjuste", 'CANTIDAD_RESCATE_MAYOR_CERO' AS "Codigo", CAST("CantidadRescate" AS NVARCHAR(100)) AS "ValorParametro", 'CantidadRescate' AS "NombreParametro"
								   FROM :v_validaciones WHERE "ResultadoCantidadRescate" = 'ERR'
								   UNION ALL   
								   SELECT "NumeroCalculoCostoFactorAjuste", 'CANTIDAD_TOTAL_CARTERA_MAYOR_CERO' AS "Codigo", CAST("CantidadTotalCartera" AS NVARCHAR(100)) AS "ValorParametro", 'CantidadTotalCartera' AS "NombreParametro"
								   FROM :v_validaciones WHERE "ResultadoCantidadTotalCartera" = 'ERR'
								   UNION ALL
								   SELECT "NumeroCalculoCostoFactorAjuste", 'COSTO_FACTOR_REAJUSTE_MAYOR_CERO' AS "Codigo", CAST("CostoFactorAjusteTotalCartera" AS NVARCHAR(100)) AS "ValorParametro", 'CostoFactorAjusteTotalCartera' AS "NombreParametro"
								   FROM :v_validaciones WHERE "ResultadoCostoFactorAjusteTotalCartera" = 'ERR';
								   
			v_carteras_validadas = SELECT "VALIDACIONES"."NumeroCalculoCostoFactorAjuste",
										  "VALIDACIONES"."CantidadRescate",
										  "VALIDACIONES"."CantidadTotalCartera",
										  "VALIDACIONES"."CostoFactorAjusteTotalCartera"
								   FROM :v_validaciones AS "VALIDACIONES"
								   WHERE 'ERR' NOT IN ("ResultadoCantidadRescate","ResultadoCantidadTotalCartera","ResultadoCostoFactorAjusteTotalCartera");
			
			SELECT COUNT(1) into v_count FROM :v_carteras_validadas;
			
			IF v_count = 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
								   
			v_datos_cartera_costo_rescate = SELECT "NumeroCalculoCostoFactorAjuste",
											   "CantidadRescate",
											   "CantidadTotalCartera",
											   "CostoFactorAjusteTotalCartera",
											   TO_DECIMAL(("CantidadRescate" * "CostoFactorAjusteTotalCartera")/("CantidadTotalCartera"),26,8) AS "CostoFactorAjusteRescate"
										FROM :v_carteras_validadas;
		
			p_out_Resultado = SELECT "NumeroCalculoCostoFactorAjuste",
								 "CantidadRescate",
								 "CantidadTotalCartera",
								 "CostoFactorAjusteTotalCartera",
								 "CostoFactorAjusteRescate",
								 TO_DECIMAL("CostoFactorAjusteTotalCartera" - "CostoFactorAjusteRescate",26,8) AS "CostoFactorAjusteRestante"
						  FROM :v_datos_cartera_costo_rescate;
						  
			SIGNAL SALIDA_CONTROLADA;
		END;
	END;
END;
