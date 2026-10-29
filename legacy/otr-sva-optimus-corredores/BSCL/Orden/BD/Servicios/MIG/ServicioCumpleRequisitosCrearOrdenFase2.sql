SET SCHEMA "BSCL";
   
DROP PROCEDURE  "BSCL.Orden.BD.Servicios::MIG.ServicioCumpleRequisitosCrearOrdenFase2";

CREATE PROCEDURE
-- 
--  Proposito: Validar datos de la orden despues de realizar calculos (cobros, montos, paridades)
--  Autor: BCS
--  Fecha Creacion: 01/07/2015  
--   
"BSCL.Orden.BD.Servicios::MIG.ServicioCumpleRequisitosCrearOrdenFase2"(
									
	OUT p_out_SalidaDetalle		"BSCL.Orden.BD.Tipos::SalidaDetalle"
																						
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
READS SQL DATA AS
BEGIN
	
	--VALIDACIONES
	v_val1 = SELECT ord."IdInterno",ord."IdOrden",
				CASE WHEN ord."MontoTotalOrden" < 0 THEN 'RECHAZO' ELSE 'OK' END as "MONTO_ORDEN_VALIDO", 
				CASE
					WHEN ord."MontoMaximoDistribucionOrden" = 0 THEN 'OK'
					WHEN ord."MontoOperacionMonedaLocal" <= ord."MontoMaximoDistribucionOrden" THEN 'OK' 
					ELSE "CO_MontoSuperaMaximoPermitidoCanalDistribucion" END as "MONTO_DISTRIBUCION_VALIDO",
				CASE WHEN ord."ValidaSaldoCaja" = 'S' AND ord."PortafolioConFlujoCaja" = 'N' THEN 'RECHAZO' ELSE 'OK' END as "PORTAFOLIO_CON_FLUJO_CAJA",
				CASE WHEN ord."ValidaSaldoCustodia" = 'S' AND ord."PortafolioConSaldoCustodia" = 'N' THEN "CO_OperacionSinFlujoCustodia" ELSE 'OK' END as "PORTAFOLIO_CON_SALDO_CUSTODIA",
				CASE WHEN ord."PlantillaEmpleadoMontoMaximoOperacion" > 0 AND "MontoOperacionMonedaLocal" > "PlantillaEmpleadoMontoMaximoOperacion" THEN "CO_MontoOperacionSuperaPermitidoPlantilla"
					ELSE 'OK' END AS "MONTO_OPERACION_ORDEN_VALIDO"
			FROM "BSCL.Orden.BD::MIG.RequisitosCrearOrdenesFase2_Entrada_Ordenes" ord;
	
	v_val2 = SELECT DISTINCT ord."IdInterno",ord."IdOrden",
				CASE
					WHEN cob."CodigoCobroClasificacion" = 'COMISION' AND cob."FormaCobro" = 'V' AND cob."ValorCobroPerfil" - cob."ValorCobroAplicado" > ord."PlantillaEmpleadoMaximoDescuentoComision" THEN "CO_DescuentoComisionSuperaPermitidoPlantilla" 
					ELSE 'OK' END as "DESCUENTO_COMISION_VALIDO",
				CASE
					WHEN cob."CodigoCobroClasificacion" = 'COMISION' AND cob."FormaCobro" = 'V' AND cob."ValorCobroAplicado" > ord."PorcentajeMaximoComision" THEN "CO_ComisionSuperaMaximoPermitido"
					ELSE 'OK' END AS "PORCENTAJE_COMISION_BAJO_MAXIMO",
				CASE
					WHEN cob."CodigoCobroClasificacion" = 'COMISION' AND cob."FormaCobro" = 'V' AND cob."ValorCobroAplicado" < ord."PorcentajeMinimoComision" THEN "CO_ComisionBajoMinimoPermitido"
					ELSE 'OK' END AS "PORCENTAJE_COMISION_SOBRE_MINIMO"
				FROM "BSCL.Orden.BD::MIG.RequisitosCrearOrdenesFase2_Entrada_Cobros" cob
				INNER JOIN "BSCL.Orden.BD::MIG.RequisitosCrearOrdenesFase2_Entrada_Ordenes" ord on cob."IdInterno" = ord."IdInterno";
	
	v_val = SELECT v1.*,v2."DESCUENTO_COMISION_VALIDO",v2."PORCENTAJE_COMISION_BAJO_MAXIMO",v2."PORCENTAJE_COMISION_SOBRE_MINIMO"
			FROM :v_val1 v1
			INNER JOIN :v_val2 v2 on v1."IdInterno" = v2."IdInterno";
	
	--SELECT * FROM :v_val;
	--SELECT * FROM :p_Ordenes;
	--TRANSPOSICION DE MATRIZ
	v_TablaRequisitos = 
		SELECT "IdOrden","IdInterno","Resultado","CodigoValidacion"
		FROM (
			SELECT * ,
			
			COALESCE(
			"1" ||"MONTO_ORDEN_VALIDO",	"2" ||"MONTO_DISTRIBUCION_VALIDO",	"3" ||"PORTAFOLIO_CON_FLUJO_CAJA",	"4" ||"DESCUENTO_COMISION_VALIDO",
			"5" ||"PORTAFOLIO_CON_SALDO_CUSTODIA","6" ||"PORCENTAJE_COMISION_BAJO_MAXIMO","7" ||"PORCENTAJE_COMISION_SOBRE_MINIMO","8" ||"MONTO_OPERACION_ORDEN_VALIDO"
			)  	AS "Resultado",
			
			COALESCE(
			"1" ||'MONTO_ORDEN_VALIDO',	"2" ||'MONTO_DISTRIBUCION_VALIDO',	"3" ||'PORTAFOLIO_CON_FLUJO_CAJA',	"4" ||'DESCUENTO_COMISION_VALIDO',
			"5" ||'PORTAFOLIO_CON_SALDO_CUSTODIA',"6" ||'PORCENTAJE_COMISION_BAJO_MAXIMO',"7" ||'PORCENTAJE_COMISION_SOBRE_MINIMO',"8" ||'MONTO_OPERACION_ORDEN_VALIDO'
			)  AS "CodigoValidacion" 
			FROM :v_val,"BSCL.Plataforma.BD::PLA.Identidad"
		) WHERE "Resultado" IS NOT NULL;
	
	p_out_SalidaDetalle =	SELECT 
								tb."IdInterno", tb."IdOrden" as "IdOperacion",
								CASE WHEN "Resultado" = 'AUTORIZACION' AND ord."IdTipoCanalIngreso"= 'TRA' THEN 'RECHAZO' ELSE "Resultado" END AS "Resultado" ,
								CASE "CodigoValidacion" 
									WHEN 'MONTO_ORDEN_VALIDO' 					THEN 'MONTO_ORDEN_NO_VALIDO'					--1
									WHEN 'MONTO_DISTRIBUCION_VALIDO' 			THEN 'MONTO_DISTRIBUCION_SUPERA_PERMITIDO'		--2
									WHEN 'PORTAFOLIO_CON_FLUJO_CAJA' 			THEN 'FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO'		--3
									WHEN 'DESCUENTO_COMISION_VALIDO' 			THEN 'DESCUENTO_COMISION_EXCEDE_PERMITIDO'		--4
									WHEN 'PORTAFOLIO_CON_SALDO_CUSTODIA' 		THEN 'FLUJO_CUSTODIA_PORTAFOLIO_INSUFICIENTE'	--5
									WHEN 'PORCENTAJE_COMISION_BAJO_MAXIMO' 		THEN 'PORCENTAJE_COMISION_SOBRE_MAXIMO_PERMITIDO_CORREDORA'			--6
									WHEN 'PORCENTAJE_COMISION_SOBRE_MINIMO' 	THEN 'PORCENTAJE_COMISION_BAJO_MINIMO_PERMITIDO_CORREDORA'			--7
									WHEN 'MONTO_OPERACION_ORDEN_VALIDO' 		THEN 'MONTO_OPERACION_SUPERA_MAXIMO_PERMITIDO_PLANTILLA_EJECUTIVO'	--8

								END AS "Codigo",
								CASE "CodigoValidacion" 
									WHEN 'MONTO_ORDEN_VALIDO' 					THEN 'IdOrden'						--01
									WHEN 'MONTO_DISTRIBUCION_VALIDO' 			THEN 'IdOrden'						--02
									WHEN 'PORTAFOLIO_CON_FLUJO_CAJA' 			THEN 'IdMonedaLiquidacionOrden'		--03
									WHEN 'DESCUENTO_COMISION_VALIDO' 			THEN 'IdOrden'						--04
									WHEN 'PORTAFOLIO_CON_SALDO_CUSTODIA' 		THEN 'FlujoCustodiaPortafolio'		--05
									WHEN 'PORCENTAJE_COMISION_BAJO_MAXIMO' 		THEN 'IdOrden'						--06
									WHEN 'PORCENTAJE_COMISION_SOBRE_MINIMO' 	THEN 'IdOrden'						--07
									WHEN 'MONTO_OPERACION_ORDEN_VALIDO' 		THEN 'IdOrden'						--08
								END AS "CodigoDato1",
								CASE "CodigoValidacion" 
									WHEN 'MONTO_ORDEN_VALIDO' 					THEN CAST(ord."IdOrden" AS NVARCHAR(100))		--01
									WHEN 'MONTO_DISTRIBUCION_VALIDO' 			THEN CAST(ord."IdOrden" AS NVARCHAR(100))		--02
									WHEN 'PORTAFOLIO_CON_FLUJO_CAJA' 			THEN CAST(ord."IdMonedaLiquidacionOrden" AS NVARCHAR(100))		--03
									WHEN 'DESCUENTO_COMISION_VALIDO' 			THEN CAST(ord."IdOrden" AS NVARCHAR(100))		--04
									WHEN 'PORTAFOLIO_CON_SALDO_CUSTODIA' 		THEN CAST(ord."FlujoCustodiaPortafolio" AS NVARCHAR(100))		--05
									WHEN 'PORCENTAJE_COMISION_BAJO_MAXIMO' 		THEN CAST(ord."IdOrden" AS NVARCHAR(100))		--06
									WHEN 'PORCENTAJE_COMISION_SOBRE_MINIMO' 	THEN CAST(ord."IdOrden" AS NVARCHAR(100))		--07
									WHEN 'MONTO_OPERACION_ORDEN_VALIDO' 		THEN CAST(ord."IdOrden" AS NVARCHAR(100))		--8
								END AS "ValorDato1",
								CASE "CodigoValidacion"
									WHEN 'PORTAFOLIO_CON_FLUJO_CAJA'			THEN 'FlujoCajaPortafolio'				--03
									WHEN 'PORTAFOLIO_CON_SALDO_CUSTODIA'		THEN 'FlujoCustodiaPortafolio'			--05
									ELSE '' END as "CodigoDato2",
								CASE "CodigoValidacion"
									WHEN 'PORTAFOLIO_CON_FLUJO_CAJA'			THEN CAST(ord."FlujoCajaPortafolio" AS NVARCHAR(100))		--03
									ELSE '' END as "ValorDato2"
									
								
								
							FROM :v_TablaRequisitos tb
							INNER JOIN "BSCL.Orden.BD::MIG.RequisitosCrearOrdenesFase2_Entrada_Ordenes" ord on tb."IdInterno" = ord."IdInterno"
							WHERE "Resultado" != 'OK'
							ORDER BY "IdInterno";	
		
	
END;