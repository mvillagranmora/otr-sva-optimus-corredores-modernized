SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosCrearTransaccionMM";
CREATE PROCEDURE
-- 
--  Proposito: VALIDACIONES DEL SERVICIO CREAR TRANSACCION
--  Autor: TEAM ORDENES
--  Fecha Creacion: 15/01/2016
--     

"BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosCrearTransaccionMM"(
	-- Parametros entrada
	IN p_Transacciones	TABLE(
							"IdInterno" 						BIGINT,
							"IdTransaccion"						BIGINT,
							"FolioTransaccion" 					NVARCHAR(40),
							"FolioTransaccionPlazo"				NVARCHAR(40),
							"IdClasificacionTipoInstrumento"	NVARCHAR(3),
							"IdTipoInstrumento"					NVARCHAR(3),
							"Cantidad"							DECIMAL(26,8),
							"CorteMinimo"						DECIMAL(26,8),
							"Plazo"								DECIMAL(26,8),
							"IdPaisBolsa"						BIGINT,
							"IdPaisSistema"						BIGINT,
							"IdCorredorCorresponsal"			BIGINT,
							"IdEstadoModulo"					NVARCHAR(3),
							"NombreModulo"						NVARCHAR(100),
							"ExisteFolioContado"				NVARCHAR(1),
							"ExisteFolioPlazo"					NVARCHAR(1),
							"EsFungible"						NVARCHAR(1),
							"ValorRescate"						DECIMAL(26,8),
							"EsVentaCorta"						NVARCHAR(1),
							"InstrumentoPermiteVentaCorta"		NVARCHAR(1),
							"IdEstadoLibroOrdenes"				NVARCHAR(3)
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
	
	v_validaciones = SELECT tran."IdInterno",tran."IdTransaccion",
							CASE WHEN COALESCE(tran."FolioTransaccion",'') != '' THEN 'OK' ELSE 'RECHAZO' END AS "FOLIO_TRANSACCION_PRESENTE",
							CASE WHEN "ExisteFolioContado" = 'S' THEN 'RECHAZO' ELSE 'OK' END AS "FOLIO_TRANSACCION_LIBRE",
							CASE 
								WHEN "IdClasificacionTipoInstrumento" = 'RV' AND "IdTipoInstrumento"  = 'SIM' AND "ExisteFolioPlazo" = 'S' THEN 'RECHAZO' 
								WHEN "IdClasificacionTipoInstrumento" = 'RE' AND "ExisteFolioPlazo" = 'S' THEN 'RECHAZO' 
								ELSE 'OK' 
								END AS "FOLIO_TRANSACCION_PLAZO_LIBRE",
							CASE
								WHEN tran."IdPaisBolsa" != tran."IdPaisSistema" AND COALESCE(tran."IdCorredorCorresponsal",0) = 0 THEN 'RECHAZO' 
								ELSE 'OK' 
								END AS "CORREDOR_CORRESPONSAL_VALIDO",
							CASE 
								WHEN "IdClasificacionTipoInstrumento" = 'RV' and "IdTipoInstrumento"  = 'SIM' AND tran."FolioTransaccionPlazo" = tran."FolioTransaccion" THEN 'RECHAZO' 
								WHEN "IdClasificacionTipoInstrumento" = 'RE' AND tran."FolioTransaccionPlazo" = tran."FolioTransaccion" THEN 'RECHAZO' 
								ELSE 'OK' 
								END AS "FOLIO_TRANSACCION_DIFERENTE_FOLIO_TRANSACCION_PLAZO",
							CASE WHEN tran."Cantidad" > 0 THEN 'OK' ELSE 'RECHAZO' END AS "CANTIDAD_MAYOR_CERO",
							CASE
								WHEN "IdClasificacionTipoInstrumento" != 'RF' THEN 'OK'
								WHEN "IdClasificacionTipoInstrumento" = 'RF' AND tran."CorteMinimo" != 0 AND MOD(tran."Cantidad", tran."CorteMinimo") != 0 THEN 'RECHAZO'
								ELSE 'OK'
								END AS "CORTE_MINIMO_VALIDO",
							CASE 
								WHEN "IdClasificacionTipoInstrumento" = 'RV' and "IdTipoInstrumento"  = 'SIM' AND tran."Plazo" <= 0 THEN 'RECHAZO'
								WHEN "IdClasificacionTipoInstrumento" = 'RE' AND tran."Plazo" <= 0 THEN 'RECHAZO'
								ELSE 'OK'
								END AS "PLAZO_VALIDO",
							CASE "IdEstadoModulo" WHEN 'C' THEN 'RECHAZO' ELSE 'OK' END AS "MODULO_CERRADO",
							CASE WHEN "EsFungible" = 'S' AND "ValorRescate" <= 0 THEN 'RECHAZO' ELSE 'OK' END AS "VALOR_RESCATE_MENOR_CERO",
							CASE WHEN "EsVentaCorta" = 'S' AND "InstrumentoPermiteVentaCorta" = 'N' THEN 'RECHAZO' ELSE 'OK' END AS "INSTRUMENTO_VENTA_CORTA",
							CASE WHEN "IdEstadoLibroOrdenes" = 'CER' THEN 'RECHAZO' ELSE 'OK' END AS "LIBRO_ORDENES_CERRADO"
						FROM :p_Transacciones tran;
	
	v_TablaRequisitos = 
			SELECT "IdTransaccion","IdInterno","Resultado","CodigoValidacion"
			FROM (
				SELECT * ,
				
				COALESCE(
				"1" ||"FOLIO_TRANSACCION_PRESENTE",	"2" ||"FOLIO_TRANSACCION_LIBRE",	"3" ||"FOLIO_TRANSACCION_PLAZO_LIBRE",	"4" ||"CORREDOR_CORRESPONSAL_VALIDO",
				"5" ||"FOLIO_TRANSACCION_DIFERENTE_FOLIO_TRANSACCION_PLAZO",			"6" ||"CANTIDAD_MAYOR_CERO",			"7" ||"CORTE_MINIMO_VALIDO",
				"8" ||"PLAZO_VALIDO",				"9" ||"MODULO_CERRADO",				"10" ||"VALOR_RESCATE_MENOR_CERO",		"11" ||"INSTRUMENTO_VENTA_CORTA",
				"12" ||"LIBRO_ORDENES_CERRADO"
				)  	AS "Resultado",
				
				COALESCE(
				"1" ||'FOLIO_TRANSACCION_PRESENTE',	"2" ||'FOLIO_TRANSACCION_LIBRE',	"3" ||'FOLIO_TRANSACCION_PLAZO_LIBRE',	"4" ||'CORREDOR_CORRESPONSAL_VALIDO',
				"5" ||'FOLIO_TRANSACCION_DIFERENTE_FOLIO_TRANSACCION_PLAZO',			"6" ||'CANTIDAD_MAYOR_CERO',			"7" ||'CORTE_MINIMO_VALIDO',
				"8" ||'PLAZO_VALIDO',				"9" ||'MODULO_CERRADO',				"10" ||'VALOR_RESCATE_MENOR_CERO',		"11" ||'INSTRUMENTO_VENTA_CORTA',
				"12" ||'LIBRO_ORDENES_CERRADO'
				)  AS "CodigoValidacion" 
				FROM :v_validaciones,"BSCL.Plataforma.BD::PLA.Identidad"
			) WHERE "Resultado" IS NOT NULL;

	select 'v_TablaRequisitosasdfasdf',* from :v_TablaRequisitos;
	
	p_out_SalidaDetalle =	SELECT 
								tb."IdInterno", tb."IdTransaccion" as "IdOperacion","Resultado" ,
								CASE "CodigoValidacion" 
									WHEN 'FOLIO_TRANSACCION_PRESENTE' 							THEN 'FOLIO_TRANSACCION_VACIO'							--01
									WHEN 'FOLIO_TRANSACCION_LIBRE' 								THEN 'FOLIO_TRANSACCION_NO_DISPONIBLE'					--02
									WHEN 'FOLIO_TRANSACCION_PLAZO_LIBRE' 						THEN 'FOLIO_TRANSACCION_PLAZO_NO_DISPONIBLE'			--03
									WHEN 'CORREDOR_CORRESPONSAL_VALIDO' 						THEN 'CORREDOR_CORRESPONSAL_NO_VALIDO'					--04
									WHEN 'FOLIO_TRANSACCION_DIFERENTE_FOLIO_TRANSACCION_PLAZO' 	THEN 'FOLIO_TRANSACCION_IGUAL_FOLIO_TRANSACCION_PLAZO'	--05
									WHEN 'CANTIDAD_MAYOR_CERO' 									THEN 'CANTIDAD_MENOR_CERO'								--06
									WHEN 'CORTE_MINIMO_VALIDO' 									THEN 'CORTE_MINIMO_INVALIDO'							--07
									WHEN 'PLAZO_VALIDO' 										THEN 'PLAZO_INVALIDO'									--08
									WHEN 'MODULO_CERRADO' 										THEN 'MODULO_CERRADO'									--09
									WHEN 'VALOR_RESCATE_MENOR_CERO' 							THEN 'VALOR_RESCATE_MENOR_CERO'							--10
									WHEN 'INSTRUMENTO_VENTA_CORTA' 								THEN 'INSTRUMENTO_NO_PERMITE_VENTA_CORTA'				--11
									WHEN 'LIBRO_ORDENES_CERRADO' 								THEN 'LIBRO_ORDENES_CERRADO'							--12
								END AS "Codigo",
								CASE "CodigoValidacion" 
									WHEN 'FOLIO_TRANSACCION_PRESENTE' 							THEN 'IdInterno'										--01
									WHEN 'FOLIO_TRANSACCION_LIBRE' 								THEN 'IdInterno'										--02
									WHEN 'FOLIO_TRANSACCION_PLAZO_LIBRE' 						THEN 'IdInterno'										--03
									WHEN 'CORREDOR_CORRESPONSAL_VALIDO' 						THEN 'IdInterno'										--04
									WHEN 'FOLIO_TRANSACCION_DIFERENTE_FOLIO_TRANSACCION_PLAZO' 	THEN 'IdInterno'										--05
									WHEN 'CANTIDAD_MAYOR_CERO' 									THEN 'IdInterno'										--06
									WHEN 'CORTE_MINIMO_VALIDO' 									THEN 'IdInterno'										--07
									WHEN 'PLAZO_VALIDO' 										THEN 'IdInterno'										--08
									WHEN 'MODULO_CERRADO' 										THEN 'NombreModulo'										--09
									WHEN 'VALOR_RESCATE_MENOR_CERO' 							THEN 'IdInterno'										--10
									WHEN 'INSTRUMENTO_VENTA_CORTA' 								THEN 'IdInterno'										--11
									WHEN 'LIBRO_ORDENES_CERRADO' 								THEN 'IdInterno'										--12
								END AS "CodigoDato1",
								CASE "CodigoValidacion" 
									WHEN 'FOLIO_TRANSACCION_PRESENTE' 							THEN CAST(tran."IdInterno" AS NVARCHAR(520))			--01
									WHEN 'FOLIO_TRANSACCION_LIBRE' 								THEN CAST(tran."IdInterno" AS NVARCHAR(520))			--02
									WHEN 'FOLIO_TRANSACCION_PLAZO_LIBRE' 						THEN CAST(tran."IdInterno" AS NVARCHAR(520))			--03
									WHEN 'CORREDOR_CORRESPONSAL_VALIDO' 						THEN CAST(tran."IdInterno" AS NVARCHAR(520))			--04
									WHEN 'FOLIO_TRANSACCION_DIFERENTE_FOLIO_TRANSACCION_PLAZO' 	THEN CAST(tran."IdInterno" AS NVARCHAR(520))			--05
									WHEN 'CANTIDAD_MAYOR_CERO' 									THEN CAST(tran."IdInterno" AS NVARCHAR(520))			--06
									WHEN 'CORTE_MINIMO_VALIDO' 									THEN CAST(tran."IdInterno" AS NVARCHAR(520))			--07
									WHEN 'PLAZO_VALIDO' 										THEN CAST(tran."IdInterno" AS NVARCHAR(520))			--08
									WHEN 'MODULO_CERRADO' 										THEN CAST(tran."NombreModulo" AS NVARCHAR(520))   		--09
									WHEN 'VALOR_RESCATE_MENOR_CERO' 							THEN CAST(tran."IdInterno" AS NVARCHAR(520))			--10
									WHEN 'INSTRUMENTO_VENTA_CORTA' 								THEN CAST(tran."IdInterno" AS NVARCHAR(520))			--11
									WHEN 'LIBRO_ORDENES_CERRADO' 								THEN CAST(tran."IdInterno" AS NVARCHAR(520))			--12
								END AS "ValorDato1",
								'' as "CodigoDato2",
								'' as "ValorDato2"
							FROM :v_TablaRequisitos tb
							INNER JOIN :p_Transacciones tran on tb."IdInterno" = tran."IdInterno"
							--WHERE "Resultado" != 'OK'
							ORDER BY "IdInterno";
	
	select 'p_out_SalidaDetalle',* from :p_out_SalidaDetalle;
							
	p_out_Salida = SELECT * FROM "BSCL.Orden.BD.Utilitarios::ObtenerSalidaResumen"(:p_out_SalidaDetalle);

END;