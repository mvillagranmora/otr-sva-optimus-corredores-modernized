SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.FondoMutuo.BD.Utilitarios::CalcularSpreadRescate";

CREATE PROCEDURE
--  Proposito: Calcular spread de una operacion de rescate
--  Autor: - Gat-Blac 
--  Fecha Creacion: 15/11/2016
"BSCL.FondoMutuo.BD.Utilitarios::CalcularSpreadRescate" (
	IN p_IdEmpresa				BIGINT,
	IN p_Datos					TABLE(
									"NumeroCalculoSpreadRescate"		BIGINT,
									"Fecha"								DATE,
									"ValorFactorReajuste"				DECIMAL(26,8),
									"CostoFactorReajusteRescate"		DECIMAL(26,8),
									"CantidadRescate"					DECIMAL(26,8),
									"ValorCuota"						DECIMAL(26,8),
									"IdMonedaFondo"						NVARCHAR(3)
								),
	OUT p_out_Resultado	TABLE (
									"NumeroCalculoSpreadRescate"		BIGINT,
									"ValorFactorReajuste"				DECIMAL(26,8),
									"CostoFactorReajusteRescate"		DECIMAL(26,8),
									"CantidadRescate"					DECIMAL(26,8),
									"ValorCuota"						DECIMAL(26,8),
									"Spread"							DECIMAL(26,8)
								),
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("NumeroCalculoSpreadRescate"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("NumeroCalculoSpreadRescate"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("NumeroCalculoSpreadRescate" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("NumeroCalculoSpreadRescate" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "NumeroCalculoSpreadRescate" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "NumeroCalculoSpreadRescate" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
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
			
			p_out_ErroresDetalle = SELECT 	"errores"."NumeroCalculoSpreadRescate",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."NumeroCalculoSpreadRescate",
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
			DECLARE v_error NVARCHAR(500);
			
			v_validaciones = SELECT "NumeroCalculoSpreadRescate",
									"ValorFactorReajuste",
									"CostoFactorReajusteRescate",
									"CantidadRescate",
									"ValorCuota",
									"Fecha",
									"IdMonedaFondo",
									CASE WHEN "ValorFactorReajuste" > 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoValorFactorReajuste",
									CASE WHEN "CostoFactorReajusteRescate" > 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoCostoFactorReajusteRescate",
									CASE WHEN "CantidadRescate" > 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoCantidadRescate",
									CASE WHEN "ValorCuota" > 0 THEN 'OK' ELSE 'ERR' END AS "ResultadoValorCuota"
							 FROM  :p_Datos;
			
			v_out_ErroresDetalle = SELECT "NumeroCalculoSpreadRescate", 'VALOR_FACTOR_MAYOR_CERO' AS "Codigo", CAST("ValorFactorReajuste" AS NVARCHAR(100)) AS "ValorParametro", 'ValorFactorReajuste' AS "NombreParametro"
								   FROM :v_validaciones WHERE "ResultadoValorFactorReajuste" = 'N'
								   UNION ALL   
								   SELECT "NumeroCalculoSpreadRescate", 'COSTO_FACTOR_MAYOR_CERO' AS "Codigo", CAST("CostoFactorReajusteRescate" AS NVARCHAR(100)) AS "ValorParametro", 'CostoFactorReajusteRescate' AS "NombreParametro"
								   FROM :v_validaciones WHERE "ResultadoCostoFactorReajusteRescate" = 'N'
								   UNION ALL
								   SELECT "NumeroCalculoSpreadRescate", 'CANTIDAD_RESCATE_MAYOR_CERO' AS "Codigo", CAST("CantidadRescate" AS NVARCHAR(100)) AS "ValorParametro", 'CantidadRescate' AS "NombreParametro"
								   FROM :v_validaciones WHERE "ResultadoCantidadRescate" = 'N'
								   UNION ALL
								   SELECT "NumeroCalculoSpreadRescate", 'VALOR_CUOTA_MAYOR_CERO' AS "Codigo", CAST("CantidadRescate" AS NVARCHAR(100)) AS "ValorParametro", 'CantidadRescate' AS "NombreParametro"
								   FROM :v_validaciones WHERE "ResultadoValorCuota" = 'N';
								   
			v_datos_validados = SELECT "VALIDACIONES"."NumeroCalculoSpreadRescate",
										  "VALIDACIONES"."ValorFactorReajuste",
										  "VALIDACIONES"."CostoFactorReajusteRescate",
										  "VALIDACIONES"."CantidadRescate",
										  "VALIDACIONES"."ValorCuota",
										  "VALIDACIONES"."Fecha",
										  "VALIDACIONES"."IdMonedaFondo",
										  TO_DECIMAL(ROUND("ValorFactorReajuste" * "CostoFactorReajusteRescate",8),26,8) AS "MontoReajuste"
								   FROM :v_validaciones AS "VALIDACIONES"
								   WHERE 'ERR' NOT IN ("ResultadoValorFactorReajuste","ResultadoCostoFactorReajusteRescate","ResultadoCantidadRescate","ResultadoValorCuota");
			
			SELECT COUNT(1) into v_count FROM :v_datos_validados;
			
			IF v_count = 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			v_tipo_cambio_factor = SELECT  0 AS "IdTrabajo",
										   "datos"."NumeroCalculoSpreadRescate" AS "IdInterno",
										   "datos"."Fecha",
								  		   'CLP' AS "IdMonedaOrigen",
								  		   "datos"."IdMonedaFondo" AS "IdMonedaDestino",
								  		   0 AS "ValorCambioLocalModificado",
								  		   "datos"."MontoReajuste" AS "MontoOrigen",
								  		   '' AS "IdUso"
							FROM :v_datos_validados AS "datos"
							WHERE "datos"."IdMonedaFondo" != 'CLP';
			
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"(
			"IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso"
			)
			SELECT "IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso" FROM :v_tipo_cambio_factor;
			
			CALL "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
				p_IdEmpresa => :p_IdEmpresa,
				p_IdTrabajo	=> 0
			);
									   
			p_out_Resultado = SELECT 	"DATOS"."NumeroCalculoSpreadRescate",
								   		"DATOS"."ValorFactorReajuste",
								   		"DATOS"."CostoFactorReajusteRescate",
								   		"DATOS"."CantidadRescate",
								   		"DATOS"."ValorCuota",
								   		CASE "DATOS"."IdMonedaFondo" WHEN 'CLP' THEN TO_DECIMAL(ROUND(("DATOS"."CantidadRescate" * "DATOS"."ValorCuota")-("DATOS"."MontoReajuste"),8),26,8)
								   		ELSE  TO_DECIMAL(ROUND(("DATOS"."CantidadRescate" * "DATOS"."ValorCuota")-(COALESCE("TipoCambio"."MontoDestino",0)),8),26,8) END AS "Spread"
									FROM :v_datos_validados AS "DATOS"
									LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" AS "TipoCambio" ON ("TipoCambio"."IdInterno" = "DATOS"."NumeroCalculoSpreadRescate" AND "TipoCambio"."IdTrabajo" = 0);

			SIGNAL SALIDA_CONTROLADA;
		END;
	END;
END;
