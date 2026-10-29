SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosCrearAsignacionOptimizado";
CREATE PROCEDURE
-- 
--  Proposito: VALIDACIONES DEL SERVICIO CREAR ASIGNACION
--  Autor: PROJO - BCS
--  Fecha Creacion: 15/01/2016
--     

"BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosCrearAsignacionOptimizado"(
	-- Parametros entrada
	IN p_IdEmpresa                        	BIGINT,
	
	IN p_Asignaciones	TABLE(
						"IdInterno"			BIGINT,
						"IdAsignacion" 		BIGINT,
						"Cantidad"			DECIMAL(26,8),
						"ValorRescate"		DECIMAL(26,8),
						"IdOrden" 			BIGINT,
						"IdTransaccion"		BIGINT,
						"IdEstadoModulo"	NVARCHAR(1),
						"NombreModulo"		NVARCHAR(100)
						),
	
	IN p_Ordenes	TABLE(
							"IdOrden" 					BIGINT,
							"IdOrdenReal" 				BIGINT,
							"IdTipoOperacion" 			NVARCHAR(6),
							"IdTipoPrecio"			NVARCHAR(1),
							"EsVentaCorta"				NVARCHAR(1),
							"PrecioOrden"				DECIMAL(26,8),
							"Tamano"					DECIMAL(26,8),
							"IdPortafolio"				BIGINT,
							"IdInstrumento" 			BIGINT,
							"IdTipoInstrumento"			NVARCHAR(3),
							"IdClasificacionTipoInstrumento"	NVARCHAR(2),
							"IdInstrumentoSubyacente" 	BIGINT,
							"IdCondicionDeLiquidacion" 	NVARCHAR(2),
							"IdBolsa"					NVARCHAR(3),
							"FechaOperacionOrden"		SECONDDATE,
							"IdEstadoOrden"				NVARCHAR(3),
							"IdLibroOrdenes"			BIGINT,
							"AccionOrden"		NVARCHAR(10),
							"IdTipoCanalIngreso"		NVARCHAR(3)
						),
	IN p_Transacciones	TABLE(
							"IdTransaccion" 			BIGINT,
							"IdTransaccionReal" 		BIGINT,
							"IdTipoOperacion" 			NVARCHAR(6),
							"IdInstrumento" 			BIGINT,
							"IdLibroOrdenes"			BIGINT,
							"IdCondicionDeLiquidacion" 	NVARCHAR(2),
							"IdBolsa"					NVARCHAR(3),
							"FechaOperacionTransaccion"	SECONDDATE,
							"PrecioTransaccion"			DECIMAL(26,8),
							"EsVentaCorta"				NVARCHAR(1),
							"IdEstadoTransaccion"		NVARCHAR(3),
							"EsFungible"				NVARCHAR(1),
							"IdInstrumentoSubyacente" 	BIGINT,
							"TamanoCorte"				DECIMAL(26,8),
							"AccionOrden"		NVARCHAR(10),
							"EsAPlazo"					NVARCHAR(1)
													),
	-- Salidas para mensajes de errores
	OUT p_out_SalidaDetalle		"BSCL.Orden.BD.Tipos::SalidaDetalle",
	OUT p_out_Salida        	"BSCL.Orden.BD.Tipos::SalidaResumen"


) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
	READS SQL DATA
	AS
BEGIN		
	--VALIDACIONES EN POSITIVO --PALABRAS CLAVE: COINCIDE, EXISTE, MAYOR_CERO, CORRECTO, DISPONIBLE
	
	--********************** VALIDACIONES *********************************** 		
	v_validaciones =  	SELECT 	asig."IdInterno",asig."IdAsignacion",
								CASE WHEN COALESCE(oo."IdOrdenReal",-1) > 0 THEN 'OK' ELSE 'RECHAZO' END as "ORDEN_EXISTE", 												--01
								CASE WHEN COALESCE(tt."IdTransaccionReal",-1) > 0 THEN 'OK' ELSE 'RECHAZO' END as "TRANSACCION_EXISTE",										--02
								CASE WHEN tt."EsAPlazo" = 'S' OR oo."IdTipoOperacion" = tt."IdTipoOperacion" THEN 'OK' ELSE 'RECHAZO' END as "TIPO_OPERACION_COINCIDE",		--03
								CASE
									WHEN oo."IdClasificacionTipoInstrumento" = 'IF' and tt."EsFungible" = 'S'  THEN 'OK'
									WHEN asig."Cantidad" > 0 THEN 'OK' 
									ELSE 'RECHAZO' END as "CANTIDAD_MAYOR_CERO",																							--04
								CASE WHEN tt."EsAPlazo" = 'S' OR oo."IdInstrumento" = tt."IdInstrumento" THEN 'OK' ELSE 'RECHAZO' END as "INSTRUMENTO_COINCIDE",			--05
								CASE 
									WHEN oo."IdTipoInstrumento" != 'SIM' THEN 'OK'
									WHEN oo."IdTipoInstrumento" = 'SIM' and oo."IdTipoOperacion" = 'COMSIM' and oo."IdTipoCanalIngreso" != 'TRA' THEN 'OK' --orden va sin nemo
									WHEN oo."IdInstrumentoSubyacente" = tt."IdInstrumentoSubyacente" THEN 'OK' 
									ELSE 'RECHAZO' END as "SUBYACENTE_COINCIDE",																							--06
								CASE WHEN tt."EsAPlazo" = 'S' OR oo."IdCondicionDeLiquidacion" = tt."IdCondicionDeLiquidacion" THEN 'OK' ELSE 'RECHAZO' END as "COND_LIQ_COINCIDE",				--07
								CASE WHEN oo."IdBolsa" = tt."IdBolsa" OR oo."IdBolsa" = '0' THEN 'OK' ELSE 'RECHAZO' END as "BOLSA_COINCIDE",														--08
								CASE 
									WHEN oo."IdClasificacionTipoInstrumento" = 'IF' and tt."EsFungible" = 'S' AND asig."ValorRescate" <= 0 THEN 'RECHAZO' 
									ELSE 'OK' END as "VAL_RESC_MAYOR_CERO",													--09
								CASE WHEN oo."EsVentaCorta" = tt."EsVentaCorta" THEN 'OK' ELSE 'RECHAZO' END as "MARCA_VENTA_CORTA_COINCIDE",								--10
								CASE
									WHEN oo."IdTipoInstrumento" = 'SIM' or oo."IdClasificacionTipoInstrumento" = 'IF' THEN 'OK'
									WHEN oo."IdTipoPrecio" = 'M' THEN 'OK'
								 	WHEN oo."IdTipoPrecio" = 'L' and oo."AccionOrden" = 'COMPRA' and tt."PrecioTransaccion" <= oo."PrecioOrden" THEN 'OK'
								 	WHEN oo."IdTipoPrecio" = 'L' and oo."AccionOrden" = 'VENTA' and tt."PrecioTransaccion" >= oo."PrecioOrden" THEN 'OK'
									ELSE 'RECHAZO'
								END as "PRECIO_VALIDO",																														--11
								CASE WHEN tt."FechaOperacionTransaccion" >= oo."FechaOperacionOrden" THEN 'OK' ELSE 'RECHAZO' END as "FECHA_ORDEN_MENOR_FECHA_PALO",		--12
								CASE WHEN oo."IdEstadoOrden" = 'OFE' or oo."IdTipoCanalIngreso" = 'TRA' THEN 'OK' ELSE 'RECHAZO' END as "ESTADO_ORDEN_CORRECTO",			--13
								CASE WHEN tt."IdEstadoTransaccion" = 'ING' THEN 'OK' ELSE 'RECHAZO' END as "ESTADO_TRANSACCION_CORRECTO",									--14
								CASE WHEN oo."IdLibroOrdenes" = tt."IdLibroOrdenes" THEN 'OK' ELSE 'RECHAZO' END as "LIBRO_ORDENES_COINCIDE",								--15
								CASE
									WHEN oo."IdClasificacionTipoInstrumento" != 'RF' THEN 'OK'
									WHEN oo."IdClasificacionTipoInstrumento" = 'RF' AND MOD(asig."Cantidad",tt."TamanoCorte") != 0 THEN 'RECHAZO' 
									ELSE 'OK' END as "CANTIDAD_MULTIPLO_CORTES",																							--16
								CASE WHEN asig."IdEstadoModulo" = 'C' THEN 'RECHAZO' ELSE 'OK' END as "MODULO_CERRADO"
						
						FROM :p_Asignaciones asig
						INNER JOIN :p_Ordenes oo on asig."IdOrden" = oo."IdOrden"
						INNER JOIN :p_Transacciones tt on asig."IdTransaccion" = tt."IdTransaccion";
						
	v_TablaRequisitos = 
		SELECT "IdInterno","IdAsignacion","Resultado","CodigoValidacion" 
		FROM (
			SELECT * ,
			
			COALESCE(
			"1" ||"ORDEN_EXISTE",			"2" ||"TRANSACCION_EXISTE",				"3" ||"TIPO_OPERACION_COINCIDE",	"4" ||"CANTIDAD_MAYOR_CERO",			"5" ||"INSTRUMENTO_COINCIDE",	"6" ||"SUBYACENTE_COINCIDE",
			"7" ||"COND_LIQ_COINCIDE",		"8" ||"BOLSA_COINCIDE",					"9" ||"VAL_RESC_MAYOR_CERO",		"10" ||"MARCA_VENTA_CORTA_COINCIDE",	"11" ||"PRECIO_VALIDO",			"12" ||"FECHA_ORDEN_MENOR_FECHA_PALO",
			"13" ||"ESTADO_ORDEN_CORRECTO",	"14" ||"ESTADO_TRANSACCION_CORRECTO",	"15" ||"LIBRO_ORDENES_COINCIDE",	"16" ||"CANTIDAD_MULTIPLO_CORTES",		"17" ||"MODULO_CERRADO"
			)  	AS "Resultado",
			
			COALESCE(
			"1" ||'ORDEN_EXISTE',			"2" ||'TRANSACCION_EXISTE',				"3" ||'TIPO_OPERACION_COINCIDE',	"4" ||'CANTIDAD_MAYOR_CERO',			"5" ||'INSTRUMENTO_COINCIDE',	"6" ||'SUBYACENTE_COINCIDE',
			"7" ||'COND_LIQ_COINCIDE',		"8" ||'BOLSA_COINCIDE',					"9" ||'VAL_RESC_MAYOR_CERO',		"10" ||'MARCA_VENTA_CORTA_COINCIDE',	"11" ||'PRECIO_VALIDO',			"12" ||'FECHA_ORDEN_MENOR_FECHA_PALO',
			"13" ||'ESTADO_ORDEN_CORRECTO',	"14" ||'ESTADO_TRANSACCION_CORRECTO',	"15" ||'LIBRO_ORDENES_COINCIDE',	"16" ||'CANTIDAD_MULTIPLO_CORTES',		"17" ||'MODULO_CERRADO'
			)  AS "CodigoValidacion" 
			FROM :v_validaciones,"BSCL.Plataforma.BD::PLA.Identidad"
		) WHERE "Resultado" IS NOT NULL;
	
	
	p_out_SalidaDetalle =	SELECT 
								tb."IdInterno",
								tb."IdAsignacion" as "IdOperacion",
								tb."Resultado",
								CASE "CodigoValidacion" 
									WHEN 'ORDEN_EXISTE' 					THEN 'ORDEN_NO_EXISTE'								--01
									WHEN 'TRANSACCION_EXISTE' 				THEN 'TRANSACCION_NO_EXISTE'						--02
									WHEN 'TIPO_OPERACION_COINCIDE' 			THEN 'TIPO_OPERACION_NO_COINCIDE'					--03
									WHEN 'CANTIDAD_MAYOR_CERO' 				THEN 'CANTIDAD_MENOR_IGUAL_CERO'					--04
									WHEN 'INSTRUMENTO_COINCIDE' 			THEN 'INSTRUMENTO_NO_COINCIDE'						--05
									WHEN 'SUBYACENTE_COINCIDE' 				THEN 'SUBYACENTE_NO_COINCIDE'						--06
									WHEN 'COND_LIQ_COINCIDE' 				THEN 'COND_LIQ_NO_COINCIDE'							--07
									WHEN 'BOLSA_COINCIDE' 					THEN 'BOLSA_NO_COINCIDE'							--08
									WHEN 'VAL_RESC_MAYOR_CERO' 				THEN 'VAL_RESC_MENOR_IGUAL_CERO'					--09
									WHEN 'MARCA_VENTA_CORTA_COINCIDE' 		THEN 'MARCA_VENTA_CORTA_NO_COINCIDE'				--10
									WHEN 'PRECIO_VALIDO' 					THEN 'PRECIO_NO_VALIDO'								--11
									WHEN 'FECHA_ORDEN_MENOR_FECHA_PALO' 	THEN 'FECHA_TRANSACCION_MENOR_FECHA_ORDEN'			--12
									WHEN 'ESTADO_ORDEN_CORRECTO' 			THEN 'ESTADO_ORDEN_INCORRECTO_ASIGNACION'			--13
									WHEN 'ESTADO_TRANSACCION_CORRECTO' 		THEN 'ESTADO_TRANSACCION_INCORRECTO_ASIGNACION'		--14
									WHEN 'LIBRO_ORDENES_COINCIDE' 			THEN 'CLIENTE_NO_PERTENECE_LIBRO_ORDENES'			--15
									WHEN 'CANTIDAD_MULTIPLO_CORTES' 		THEN 'CANTIDAD_NO_ES_MULTIPLO_CORTES'				--16
									WHEN 'MODULO_CERRADO'			 		THEN 'MODULO_CERRADO'								--17
								END AS "Codigo",
								CASE "CodigoValidacion"
									WHEN 'ORDEN_EXISTE' 					THEN 'IdOrden'								--01
									WHEN 'TRANSACCION_EXISTE' 				THEN 'IdTransaccion'						--02
									WHEN 'TIPO_OPERACION_COINCIDE' 			THEN 'IdAsignacion'							--03
									WHEN 'CANTIDAD_MAYOR_CERO' 				THEN 'IdAsignacion'							--04
									WHEN 'INSTRUMENTO_COINCIDE' 			THEN 'IdAsignacion'							--05
									WHEN 'SUBYACENTE_COINCIDE' 				THEN 'IdAsignacion'							--06
									WHEN 'COND_LIQ_COINCIDE' 				THEN 'IdAsignacion'							--07
									WHEN 'BOLSA_COINCIDE' 					THEN 'IdAsignacion'							--08
									WHEN 'VAL_RESC_MAYOR_CERO' 				THEN 'IdAsignacion'							--09
									WHEN 'MARCA_VENTA_CORTA_COINCIDE' 		THEN 'IdAsignacion'							--10
									WHEN 'PRECIO_VALIDO' 					THEN 'IdAsignacion'							--11
									WHEN 'FECHA_ORDEN_MENOR_FECHA_PALO' 	THEN 'IdAsignacion'							--12
									WHEN 'ESTADO_ORDEN_CORRECTO' 			THEN 'IdOrden'								--13
									WHEN 'ESTADO_TRANSACCION_CORRECTO' 		THEN 'IdTransaccion'						--14
									WHEN 'LIBRO_ORDENES_COINCIDE' 			THEN 'IdAsignacion'							--15
									WHEN 'CANTIDAD_MULTIPLO_CORTES' 		THEN 'IdAsignacion'							--16
									WHEN 'MODULO_CERRADO'			 		THEN 'NombreModulo'							--17
								END AS "CodigoDato1",
								CASE "CodigoValidacion"
									WHEN 'ORDEN_EXISTE' 					THEN CAST("IdOrden" AS NVARCHAR(520))							--01
									WHEN 'TRANSACCION_EXISTE' 				THEN CAST("IdTransaccion" AS NVARCHAR(520))						--02
									WHEN 'TIPO_OPERACION_COINCIDE' 			THEN CAST(tb."IdAsignacion" AS NVARCHAR(520))					--03
									WHEN 'CANTIDAD_MAYOR_CERO' 				THEN CAST(tb."IdAsignacion" AS NVARCHAR(520))					--04
									WHEN 'INSTRUMENTO_COINCIDE' 			THEN CAST(tb."IdAsignacion" AS NVARCHAR(520))					--05
									WHEN 'SUBYACENTE_COINCIDE' 				THEN CAST(tb."IdAsignacion" AS NVARCHAR(520))					--06
									WHEN 'COND_LIQ_COINCIDE' 				THEN CAST(tb."IdAsignacion" AS NVARCHAR(520))					--07
									WHEN 'BOLSA_COINCIDE' 					THEN CAST(tb."IdAsignacion" AS NVARCHAR(520))					--08
									WHEN 'VAL_RESC_MAYOR_CERO' 				THEN CAST(tb."IdAsignacion" AS NVARCHAR(520))					--09
									WHEN 'MARCA_VENTA_CORTA_COINCIDE' 		THEN CAST(tb."IdAsignacion" AS NVARCHAR(520))					--10
									WHEN 'PRECIO_VALIDO' 					THEN CAST(tb."IdAsignacion" AS NVARCHAR(520))					--11
									WHEN 'FECHA_ORDEN_MENOR_FECHA_PALO' 	THEN CAST(tb."IdAsignacion" AS NVARCHAR(520))					--12
									WHEN 'ESTADO_ORDEN_CORRECTO' 			THEN CAST("IdOrden" AS NVARCHAR(520))							--13
									WHEN 'ESTADO_TRANSACCION_CORRECTO' 		THEN CAST("IdTransaccion" AS NVARCHAR(520))						--14
									WHEN 'LIBRO_ORDENES_COINCIDE' 			THEN CAST(tb."IdAsignacion" AS NVARCHAR(520))					--15
									WHEN 'CANTIDAD_MULTIPLO_CORTES' 		THEN CAST(tb."IdAsignacion" AS NVARCHAR(520))					--16
									WHEN 'MODULO_CERRADO'			 		THEN CAST("NombreModulo" AS NVARCHAR(520))					--17
								END AS "ValorDato1",
								'' as "CodigoDato2", 
								'' as "ValorDato2"
							FROM :v_TablaRequisitos tb
							INNER JOIN :p_Asignaciones asig on tb."IdInterno" = asig."IdInterno"
							WHERE "Resultado" != 'OK'
							ORDER BY "IdInterno";
	
	p_out_Salida = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:p_out_SalidaDetalle);
	
	
	

END;