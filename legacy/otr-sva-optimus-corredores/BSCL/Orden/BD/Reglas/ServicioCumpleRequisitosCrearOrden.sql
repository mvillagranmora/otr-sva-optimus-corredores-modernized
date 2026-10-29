SET SCHEMA "BSCL";
   
DROP PROCEDURE "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosCrearOrden";
	

CREATE PROCEDURE
-- 
--  Proposito: Validar datos del cliente para ingresar ordeon.
--  Autor: BCS
--  Fecha Creacion: 01/07/2015  
--   
"BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosCrearOrden"(
	IN  p_Ordenes				TABLE(
									"IdInterno"								BIGINT,
									"IdOrden"								BIGINT,
									"Cantidad"								DECIMAL(26,8),
									"MontoNeto"								DECIMAL(26,8),
									"ValorRescate"							DECIMAL(26,8),
									"IdPortafolio"							BIGINT,
									"IdTipoLibroOrden"						NVARCHAR(40),
									"IdInstrumento"							BIGINT,
									"IdInstrumentoReal"						BIGINT,
									"IdEstadoInstrumento"					NVARCHAR(3),
									"IdTipoOperacion"						NVARCHAR(6),
									"IdTipoOperacionReal"					NVARCHAR(6),
									"IdTipoInstrumento"						NVARCHAR(3),
									"IdClasificacionTipoInstrumento"		NVARCHAR(2),
									"IdBolsa"								NVARCHAR(3),
									"IdBolsaReal"							NVARCHAR(3),
									"EspecificaBolsa"						NVARCHAR(1),
									"IdCondicionDeLiquidacionContado"		NVARCHAR(2),
									"IdCondicionDeLiquidacionContadoReal"	NVARCHAR(2),
									"IdCondicionDeLiquidacionPlazo"			NVARCHAR(2),
									"IdCondicionDeLiquidacionPlazoReal"		NVARCHAR(2),
									"InstrumentoPermiteVentaCorta"			VARCHAR(1),
									"IdTipoVigencia"						NVARCHAR(2),
									"IdTipoVigenciaReal"					NVARCHAR(2),
									"IdBoveda"								NVARCHAR(3),
									"IdBovedaReal"							NVARCHAR(3),
									"IdFormaCustodia"						NVARCHAR(3),
									"IdFormaCustodiaReal"					NVARCHAR(3), --CUS, NOM
									"IdPorcentajeVisibilidad"				BIGINT,
									"IdPorcentajeVisibilidadReal"			BIGINT,
									"TasaVentaCorta"						DECIMAL(26,8),
									"TasaSimultanea"						DECIMAL(26,8),
									"PlazoVentaCorta"						BIGINT,
									"PlazoSimultanea"						BIGINT,
									"PrecioLimite"							DECIMAL(26,8),
									"PrecioMercado"							DECIMAL(26,8),
									"IdTipoPrecio"							NVARCHAR(1),
									"IdTipoPrecioReal"						NVARCHAR(1),
									"IdTipoCanalIngreso"					NVARCHAR(3),
									"IdTipoCanalIngresoReal"				NVARCHAR(3),
									"IdTipoCanalDistribucion"				NVARCHAR(3),
									"IdTipoCanalDistribucionReal"			NVARCHAR(3),
									"IdTipoReajuste"						NVARCHAR(3),
									"IdTipoReajusteReal"					NVARCHAR(3),
									"TamanoCorte"							DECIMAL(26,8),
									"EsVentaCorta"							NVARCHAR(1),
									"EsFungible"							NVARCHAR(1),
									"AccionOrden"							NVARCHAR(10),
									"IdEmpleadoDigitador"					BIGINT,
									"IdEstadoEmpleadoDigitador"				NVARCHAR(3),
									"PlantillaEmpleadoProductoHabilitado"	NVARCHAR(1),
									"PlantillaEmpleadoPermiteRuteo"			NVARCHAR(1),
									"CantidadCobros"						BIGINT,
									"IdPortafolioReal"						BIGINT,
									"IdEstadoPortafolio" 					NVARCHAR(3),
									"IdCliente"								BIGINT,
									"IdClienteReal"							BIGINT,
									"IdEstadoCliente" 						NVARCHAR(3),
									"IdLibroOrdenes"						BIGINT,
									"DocumentoIdentidad" 					NVARCHAR(40),
									"IdPerfilCobros"						NVARCHAR(40),
									"IdEmpleadoEjecutivoPortafolio"			BIGINT,
									"CumpleHorario"							NVARCHAR(1),
									"IdEstadoModulo"						NVARCHAR(1),
									"NombreModulo"							NVARCHAR(100),
									"AplazaOrdenPorHorarioNegociacion"		NVARCHAR(1),
									"AplazaOrdenPorModuloCerrado"			NVARCHAR(1),
									"VentaCortaPlazoMinimo"					BIGINT,
									"VentaCortaPlazoMaximo"					BIGINT,
																		
									"CO_ContratoSimultanea" 	NVARCHAR(40),
									"CO_ContratoCustodia" 		NVARCHAR(40),
									"CO_PortafolioBloqueado" 	NVARCHAR(40),
									"CO_ContratoFuturos" 		NVARCHAR(40),
									"CO_ContratoVentaCorta" 	NVARCHAR(40),
									"CO_ContratoMercantil" 		NVARCHAR(40),
									"CO_CompraNominalRV" 		NVARCHAR(40),
									"CO_CompraNominalRFIF" 		NVARCHAR(40),
									"CO_VentaNominalRV" 		NVARCHAR(40),
									"CO_VentaNominalRFIF" 		NVARCHAR(40),
									"CO_ConfirmacionOrdenVerbalEscrita" NVARCHAR(40),
									"CO_ContratoCondicionesGenerales" NVARCHAR(40),
									
									"CO_ClienteBloqueado" 				NVARCHAR(40),
									"CO_PersonaBloqueada" 				NVARCHAR(40),
									"CO_EjecutivoSinProductoPlantilla" 	NVARCHAR(40),
									"CO_EjecutivoSinRuteoPlantilla" 	NVARCHAR(40),
									"CO_EjecutivoPortafolioBloqueado" 	NVARCHAR(40),
									"CO_OrdenRequiereAutorizacion" 		NVARCHAR(40),
									"CO_FotocopiaCarnet" 				NVARCHAR(40),
									"CO_AntecedentesLegales" 			NVARCHAR(40),
									"CO_DiferenciaPrecioOrden"			NVARCHAR(40),
									
									
									"ContratoSimultanea" 		NVARCHAR(40),
									"ContratoFuturos" 			NVARCHAR(40),
									"ContratoVentaCorta" 		NVARCHAR(40),
									"ContratoCustodia" 			NVARCHAR(40),
									"ContratoMercantil" 		NVARCHAR(40),
									"ClaseOrden"		 		NVARCHAR(40),
									"ContratoCondicionesGenerales" NVARCHAR(40),
									
									"OperaCuentaInversion" 		NVARCHAR(40),
								    "OperaLineaSobregiro" 		NVARCHAR(40),
								    "OperaCustodiaRV" 			NVARCHAR(40),
								    "OperaCustodiaRFIF" 		NVARCHAR(40),
								    "PermiteVenderNominalRV" 	NVARCHAR(40),
								    "PermiteVenderNominalRFIF"	NVARCHAR(40),
									
									"IdEstadoPersona"			NVARCHAR(3),
									"IdEstadoEmpleadoEjecutivoPortafolio"	NVARCHAR(3),
									"FotocopiaCarnet"			NVARCHAR(40),
									"AntecedentesLegales"		NVARCHAR(40),
									"PorcentajeDiferenciaPrecio"	DECIMAL(26,8),
									"IdEstadoLibroOrdenes"		NVARCHAR(3)
									),

    
	--OUT p_out_SalidaDetalle     TABLE ("IdInterno" BIGINT, "IdOrden" BIGINT, "Resultado" NVARCHAR(40), "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(100)),
	OUT p_out_SalidaDetalle		"BSCL.Orden.BD.Tipos::SalidaDetalle"
																						
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
READS SQL DATA AS
BEGIN
	
	--VALIDACIONES
	v_val = select ord."IdInterno",ord."IdOrden",
				CASE WHEN COALESCE(ord."IdClienteReal",-1) > 0 THEN 'OK' ELSE 'RECHAZO' END as "CLIENTE_EXISTE", 
				CASE WHEN ord."IdEstadoCliente" = 'ACT' THEN 'OK' ELSE "CO_ClienteBloqueado" END as "CLIENTE_HABILITADO",
				CASE WHEN COALESCE(ord."IdPortafolioReal",-1) > 0 THEN 'OK' ELSE 'RECHAZO' END as "PORTAFOLIO_EXISTE",
				CASE WHEN COALESCE(ord."IdInstrumentoReal",-1) > 0 THEN 'OK' ELSE 'RECHAZO' END as "INSTRUMENTO_EXISTE",
				CASE WHEN ord."IdEstadoInstrumento" = 'ACT' THEN 'OK' ELSE 'RECHAZO' END as "INSTRUMENTO_VIGENTE",
				CASE WHEN ord."EspecificaBolsa" = 'N' or COALESCE(ord."IdBolsaReal",'') != '' THEN 'OK' ELSE 'RECHAZO' END as "BOLSA_VALIDA",
				CASE WHEN COALESCE(ord."IdTipoOperacionReal",'') != '' THEN 'OK' ELSE 'RECHAZO' END as "TIPO_OPERACION_VALIDA",
				CASE WHEN COALESCE(ord."IdCondicionDeLiquidacionContadoReal",'') != '' THEN 'OK' ELSE 'RECHAZO' END as "CONDICION_LIQUIDACION_CONTADO_VALIDA", 
				CASE WHEN ord."IdTipoInstrumento" != 'SIM' OR ( ord."IdTipoInstrumento" = 'SIM' AND COALESCE(ord."IdCondicionDeLiquidacionPlazoReal",'') != '') THEN 'OK' ELSE 'RECHAZO' END as "CONDICION_LIQUIDACION_PLAZO_VALIDA",
				CASE WHEN COALESCE(ord."IdPorcentajeVisibilidadReal",-1) > 0 THEN 'OK' ELSE 'RECHAZO' END as "PORCENTAJE_VISIBILIDAD_VALIDO",
				CASE WHEN COALESCE(ord."IdTipoVigenciaReal",'') != '' THEN 'OK' ELSE 'RECHAZO' END as "TIPO_VIGENCIA_VALIDA",
				CASE WHEN ord."IdBoveda" IS NULL OR COALESCE(ord."IdBovedaReal",'') != '' THEN 'OK' ELSE 'RECHAZO' END as "BOVEDA_VALIDA",
				CASE WHEN COALESCE(ord."IdFormaCustodiaReal",'') != '' THEN 'OK' ELSE 'RECHAZO' END as "FORMA_CUSTODIA_VALIDA",
				CASE WHEN COALESCE(ord."IdTipoPrecioReal",'') != '' THEN 'OK' ELSE 'RECHAZO' END as "TIPO_PRECIO_VALIDO",
				CASE WHEN COALESCE(ord."IdEmpleadoDigitador",-1) > 0 THEN 'OK' ELSE 'RECHAZO' END as "EMPLEADO_DIGITADOR_VALIDO",
				CASE WHEN ord."EsVentaCorta" = 'N' OR (ord."EsVentaCorta" = 'S' AND ord."PlazoVentaCorta" > 0) THEN 'OK' ELSE 'RECHAZO' END as "PLAZO_VENTA_CORTA_VALIDO",
				CASE WHEN ord."IdTipoInstrumento" != 'SIM' OR (ord."IdTipoInstrumento" = 'SIM' AND ord."PlazoSimultanea" > 0) THEN 'OK' ELSE 'RECHAZO' END as "PLAZO_SIMULTANEA_VALIDO",
				CASE WHEN ord."EsVentaCorta" = 'N' OR (ord."EsVentaCorta" = 'S' AND ord."InstrumentoPermiteVentaCorta" = 'S') THEN 'OK' ELSE 'RECHAZO' END as "INSTRUMENTO_PERMITE_VENTA_CORTA",
				CASE WHEN ord."EsVentaCorta" = 'N' OR (ord."EsVentaCorta" = 'S' AND ord."TasaVentaCorta" >= 0) THEN 'OK' ELSE 'RECHAZO' END as "TASA_VENTA_CORTA_VALIDA",
				CASE 
					WHEN ord."IdTipoInstrumento" != 'SIM' OR (ord."IdTipoInstrumento" = 'SIM' AND ord."TasaSimultanea" >= 0) THEN 'OK' 
					ELSE 'RECHAZO' END as "TASA_SIMULTANEA_VALIDA",
				CASE
					WHEN ord."IdClasificacionTipoInstrumento" = 'RV' AND ord."IdTipoOperacion" = 'COMSIM' THEN 'OK'
					WHEN ord."IdClasificacionTipoInstrumento" = 'RE' AND ord."IdTipoOperacion" = 'COMRRV' THEN 'OK'
					WHEN ord."IdClasificacionTipoInstrumento" = 'IF' AND ord."EsFungible" = 'S' THEN 'OK'
					WHEN ord."Cantidad" > 0 THEN 'OK' 
					ELSE 'RECHAZO' END as "CANTIDAD_MAYOR_CERO",
				CASE
					WHEN ord."IdClasificacionTipoInstrumento" = 'RV' AND ord."IdTipoInstrumento" = 'SIM' THEN 'OK'
					WHEN ord."IdClasificacionTipoInstrumento" = 'IF' THEN 'OK'
					WHEN ord."IdClasificacionTipoInstrumento" = 'RF' THEN 'OK'
					WHEN ord."PrecioLimite" > 0 THEN 'OK'
					WHEN ord."IdTipoPrecio" = 'M' THEN 'OK' 
					ELSE 'RECHAZO' END as "PRECIO_MAYOR_CERO",
				CASE 
					WHEN COALESCE(ord."IdEmpleadoEjecutivoPortafolio",0) != 0 THEN 'OK' 
					ELSE 'RECHAZO' END as "EJECUTIVO_PORTAFOLIO_CONFIGURADO",
				CASE 
					WHEN ord."IdTipoOperacion" = 'COMSIM' and ord."IdTipoCanalIngreso" != 'TRA' AND ord."MontoNeto" < 0 THEN 'RECHAZO' --SOLO CUANDO NO ES POSTRERA VIENE POR MONTO
					ELSE 'OK' END as "MONTO_MAYOR_CERO",
				CASE 
					WHEN ord."IdClasificacionTipoInstrumento" = 'IF' AND "EsFungible" = 'S' AND ord."ValorRescate" <= 0 THEN 'RECHAZO'
					ELSE 'OK' END as "VALOR_RESCATE_MAYOR_CERO", 
				CASE 
					WHEN ord."IdTipoInstrumento" = 'SIM' AND ord."ContratoSimultanea" = 'N' THEN "CO_ContratoSimultanea"
					ELSE 'OK' END as "CONTRATO_SIMULTANEA", 
				CASE
					WHEN ord."ContratoMercantil" = 'S' THEN 'OK'
					WHEN ord."ContratoMercantil" = 'N' AND ord."OperaCuentaInversion" = 'S' THEN "CO_ContratoMercantil"
					ELSE 'OK' END as "CONTRATO_MERCANTIL",
				CASE
					WHEN ord."ContratoVentaCorta" = 'N' AND ord."EsVentaCorta" = 'S' THEN "CO_ContratoVentaCorta"
					ELSE 'OK' END as "CONTRATO_VENTA_CORTA", 
				CASE
					WHEN ord."IdFormaCustodiaReal" = 'CUS' AND ord."ContratoCustodia" = 'N' THEN "CO_ContratoCustodia"
					ELSE 'OK' END as "CONTRATO_CUSTODIA", 
				CASE
					WHEN ord."IdTipoOperacion" = 'COMFUT' OR ord."IdTipoOperacion" = 'VENFUT' AND ord."ContratoFuturos" = 'N' THEN "CO_ContratoFuturos"
					ELSE 'OK' END as "CONTRATO_FUTUROS",
				CASE 
					WHEN ord."IdEstadoPortafolio" != 'ACT' THEN "CO_PortafolioBloqueado" 
					ELSE 'OK' END as "PORTAFOLIO_HABILITADO", 
				CASE 
					WHEN ord."IdClasificacionTipoInstrumento" = 'RV' AND ord."OperaCustodiaRV" = 'S' AND ord."AccionOrden" = 'COMPRA' AND ord."IdFormaCustodiaReal" = 'NOM' THEN "CO_CompraNominalRV" 
					ELSE 'OK' END as "OPERA_CUSTODIA_ORDEN_NOMINAL_COMPRA_RV",
				CASE 
					WHEN (ord."IdClasificacionTipoInstrumento" = 'RF' OR ord."IdClasificacionTipoInstrumento" = 'IF') AND ord."OperaCustodiaRFIF" = 'S' AND ord."AccionOrden" = 'COMPRA' AND ord."IdFormaCustodiaReal" = 'NOM' THEN "CO_CompraNominalRFIF" 
					ELSE 'OK' END as "OPERA_CUSTODIA_ORDEN_NOMINAL_COMPRA_RFIF",
				CASE
					WHEN ord."IdClasificacionTipoInstrumento" = 'RV' AND ord."EsVentaCorta" = 'S' THEN 'OK'
					WHEN ord."IdClasificacionTipoInstrumento" = 'RV' AND ord."OperaCustodiaRV" = 'S' AND ord."PermiteVenderNominalRV" = 'N' AND ord."AccionOrden" = 'VENTA' AND ord."IdFormaCustodiaReal" = 'NOM' THEN 'RECHAZO'
					WHEN ord."IdClasificacionTipoInstrumento" = 'RV' AND ord."OperaCustodiaRV" = 'S' AND ord."PermiteVenderNominalRV" = 'S' AND ord."AccionOrden" = 'VENTA' AND ord."IdFormaCustodiaReal" = 'NOM' THEN "CO_VentaNominalRV" 
					ELSE 'OK' END as "OPERA_CUSTODIA_ORDEN_NOMINAL_VENTA_RV",
				CASE 
					WHEN (ord."IdClasificacionTipoInstrumento" = 'RF' OR ord."IdClasificacionTipoInstrumento" = 'IF') AND ord."OperaCustodiaRFIF" = 'S' AND ord."PermiteVenderNominalRFIF" = 'N' AND ord."AccionOrden" = 'VENTA' AND ord."IdFormaCustodiaReal" = 'NOM' THEN 'RECHAZO'
					WHEN (ord."IdClasificacionTipoInstrumento" = 'RF' OR ord."IdClasificacionTipoInstrumento" = 'IF') AND ord."OperaCustodiaRFIF" = 'S' AND ord."PermiteVenderNominalRFIF" = 'S' AND ord."AccionOrden" = 'VENTA' AND ord."IdFormaCustodiaReal" = 'NOM' THEN "CO_VentaNominalRFIF" 
					ELSE 'OK' END as "OPERA_CUSTODIA_ORDEN_NOMINAL_VENTA_RFIF",
				CASE 
					WHEN ord."ClaseOrden" IN ('OE','OV') THEN "CO_ConfirmacionOrdenVerbalEscrita" 
					ELSE 'OK' END AS "PERMITE_INGRESO_ORDEN_VERBAL_ESCRITA",
				CASE 
					WHEN COALESCE(ord."IdTipoCanalDistribucionReal",'') = '' THEN 'RECHAZO'
					ELSE 'OK' END as "CANAL_DISTRIBUCION_VALIDO",
				CASE 
					WHEN ord."PlantillaEmpleadoProductoHabilitado" = 'N' THEN "CO_EjecutivoSinProductoPlantilla"
					ELSE 'OK' END as "PLANTILLA_EMPLEADO_PRODUCTO_HABILITADO",
				CASE 
					WHEN ord."PlantillaEmpleadoPermiteRuteo" = 'N' AND ord."IdTipoCanalDistribucion" = 'RUA' AND ord."IdTipoCanalIngreso" = 'AGE' THEN "CO_EjecutivoSinRuteoPlantilla" 
					ELSE 'OK' END as "PLANTILLA_EMPLEADO_PERMITE_RUTEO",
				CASE 
					WHEN ord."CantidadCobros" = 0 THEN 'RECHAZO' 
					ELSE 'OK' END as "PORTAFOLIO_COBROS_CONFIGURADOS",
				CASE 
					WHEN ord."IdEstadoEmpleadoDigitador" != 'ACT' THEN 'RECHAZO' 
					ELSE 'OK' END as "EMPLEADO_HABILITADO",
				CASE
					WHEN ord."CumpleHorario" = 'N' AND ord."AplazaOrdenPorHorarioNegociacion" = 'N' THEN 'RECHAZO'
					ELSE 'OK' END as "ORDEN_EN_HORARIO",
				CASE
					WHEN (ord."CumpleHorario" = 'N' AND ord."AplazaOrdenPorHorarioNegociacion" = 'S') OR "AplazaOrdenPorModuloCerrado" = 'S' THEN 'ADVERTENCIA'
					ELSE 'OK' END as "ORDEN_APLAZADA",
				CASE
					WHEN COALESCE(ord."IdLibroOrdenes",-1) > 0 THEN 'OK' ELSE 'RECHAZO' 
					END as "CLIENTE_LIBRO_ORDENES_CONFIGURADO",
				CASE
					WHEN ord."EsVentaCorta" = 'N' THEN 'OK'
					WHEN ord."EsVentaCorta" = 'S' AND ord."PlazoVentaCorta" <= ord."VentaCortaPlazoMaximo" THEN 'OK' 
					ELSE 'RECHAZO' 
					END as "PLAZO_VENTA_CORTA_SUPERA_MAXIMO_PERMITIDO",
				CASE	
					WHEN ord."EsVentaCorta" = 'N' THEN 'OK'
					WHEN ord."EsVentaCorta" = 'S' AND (ord."PlazoVentaCorta" = 0 OR ord."PlazoVentaCorta" >= ord."VentaCortaPlazoMinimo") THEN 'OK' 
					ELSE 'RECHAZO' 
					END as "PLAZO_VENTA_CORTA_BAJO_MINIMO_PERMITIDO",
				CASE	
					WHEN ord."ContratoCondicionesGenerales" = 'N' THEN "CO_ContratoCondicionesGenerales"
					ELSE 'OK' 
					END as "CONTRATO_CONDICIONES_GENERALES",
					
				CASE WHEN ord."IdEstadoPersona" = 'ACT' THEN 'OK' ELSE "CO_PersonaBloqueada" END as "PERSONA_HABILITADA",
				CASE WHEN ord."IdEstadoEmpleadoEjecutivoPortafolio" = 'ACT' THEN 'OK' ELSE "CO_EjecutivoPortafolioBloqueado" END as "EJECUTIVO_PORTAFOLIO_HABILITADO",
				CASE WHEN ord."CO_OrdenRequiereAutorizacion" = 'OK' THEN 'OK' ELSE "CO_OrdenRequiereAutorizacion" END as "ORDEN_REQUIERE_AUTORIZACION",
				CASE WHEN ord."FotocopiaCarnet" = 'S' THEN 'OK' ELSE "CO_FotocopiaCarnet" END as "FOTOCOPIA_DOCUMENTO_IDENTIDAD",
				CASE WHEN ord."AntecedentesLegales" = 'S' THEN 'OK' ELSE "CO_AntecedentesLegales" END as "ANTECEDENTES_LEGALES",
				CASE
					WHEN ord."PrecioMercado" = 0 or ord."IdTipoPrecio" = 'M' THEN 'OK'
					WHEN ord."PrecioMercado" > 0 and ord."IdTipoPrecio" = 'L' and 100.0 * ABS(ord."PrecioMercado"-ord."PrecioLimite")/ord."PrecioMercado" <= ord."PorcentajeDiferenciaPrecio" THEN 'OK' 
					ELSE "CO_DiferenciaPrecioOrden" END as "DIFERENCIA_PRECIO_ORDEN",
				CASE WHEN ord."IdEstadoLibroOrdenes" = 'ABT' THEN 'OK' ELSE 'RECHAZO' END as "ESTADO_LIBRO"
			FROM :p_Ordenes ord;
	
	--SELECT 'v_val',* FROM :v_val;
	
	--TRANSPOSICION DE MATRIZ
	v_TablaRequisitos = 
		SELECT "IdOrden","IdInterno","Resultado","CodigoValidacion"
		FROM (
			SELECT * ,
			
			COALESCE(
			"1" ||"CLIENTE_EXISTE",				"2" ||"CLIENTE_HABILITADO",		"3" ||"PORTAFOLIO_EXISTE",						"4" ||"INSTRUMENTO_EXISTE",					"5" ||"INSTRUMENTO_VIGENTE",					
			"6" ||"BOLSA_VALIDA",				"7" ||"TIPO_OPERACION_VALIDA",	"8" ||"CONDICION_LIQUIDACION_CONTADO_VALIDA",	"9" ||"CONDICION_LIQUIDACION_PLAZO_VALIDA",	"10"||"PORCENTAJE_VISIBILIDAD_VALIDO",
			"11"||"TIPO_VIGENCIA_VALIDA",		"12"||"BOVEDA_VALIDA",			"13"||"FORMA_CUSTODIA_VALIDA",					"14"||"TIPO_PRECIO_VALIDO",					"15"||"EMPLEADO_DIGITADOR_VALIDO",
			"16"||"PLAZO_VENTA_CORTA_VALIDO",	"17"||"PLAZO_SIMULTANEA_VALIDO","18"||"INSTRUMENTO_PERMITE_VENTA_CORTA",		"19"||"TASA_VENTA_CORTA_VALIDA",			"20"||"TASA_SIMULTANEA_VALIDA",
			"21"||"CANTIDAD_MAYOR_CERO",		"22"||"PRECIO_MAYOR_CERO",		"23"||"EJECUTIVO_PORTAFOLIO_CONFIGURADO",		"24"||"MONTO_MAYOR_CERO",					"25"||"VALOR_RESCATE_MAYOR_CERO",
			"26"||"CONTRATO_SIMULTANEA",		"27"||"CONTRATO_MERCANTIL",		"28"||"CONTRATO_VENTA_CORTA",					"29"||"CONTRATO_CUSTODIA",					"30"||"CONTRATO_FUTUROS",
			"31"||"PORTAFOLIO_HABILITADO",		"32"||"OPERA_CUSTODIA_ORDEN_NOMINAL_COMPRA_RV",									"33"||"OPERA_CUSTODIA_ORDEN_NOMINAL_COMPRA_RFIF",
			"34"||"OPERA_CUSTODIA_ORDEN_NOMINAL_VENTA_RV",						"35"||"OPERA_CUSTODIA_ORDEN_NOMINAL_VENTA_RFIF","36"||"PERMITE_INGRESO_ORDEN_VERBAL_ESCRITA",
			"37"||"CANAL_DISTRIBUCION_VALIDO",	"38"||"PLANTILLA_EMPLEADO_PRODUCTO_HABILITADO",									"39"||"PLANTILLA_EMPLEADO_PERMITE_RUTEO",	"40"||"PORTAFOLIO_COBROS_CONFIGURADOS",
			"41"||"EMPLEADO_HABILITADO",		"42"||"ORDEN_EN_HORARIO",		"43"||"ORDEN_APLAZADA",							"44"||"CLIENTE_LIBRO_ORDENES_CONFIGURADO",	"45"||"PLAZO_VENTA_CORTA_SUPERA_MAXIMO_PERMITIDO",
			"46"||"PLAZO_VENTA_CORTA_BAJO_MINIMO_PERMITIDO", "47"||"CONTRATO_CONDICIONES_GENERALES"  ,
			
			"48"||"PERSONA_HABILITADA","49"||"EJECUTIVO_PORTAFOLIO_HABILITADO","50"||"ORDEN_REQUIERE_AUTORIZACION",
			"51"||"FOTOCOPIA_DOCUMENTO_IDENTIDAD","52"||"ANTECEDENTES_LEGALES","53"||"DIFERENCIA_PRECIO_ORDEN","54"||"ESTADO_LIBRO"
			)  	AS "Resultado",
			
			COALESCE(
			"1" ||'CLIENTE_EXISTE',				"2" ||'CLIENTE_HABILITADO',		"3" ||'PORTAFOLIO_EXISTE',						"4" ||'INSTRUMENTO_EXISTE',					"5" ||'INSTRUMENTO_VIGENTE',
			"6" ||'BOLSA_VALIDA',				"7" ||'TIPO_OPERACION_VALIDA',	"8" ||'CONDICION_LIQUIDACION_CONTADO_VALIDA',	"9" ||'CONDICION_LIQUIDACION_PLAZO_VALIDA',	"10"||'PORCENTAJE_VISIBILIDAD_VALIDO',
			"11"||'TIPO_VIGENCIA_VALIDA',		"12"||'BOVEDA_VALIDA',			"13"||'FORMA_CUSTODIA_VALIDA',					"14"||'TIPO_PRECIO_VALIDO',					"15"||'EMPLEADO_DIGITADOR_VALIDO',
			"16"||'PLAZO_VENTA_CORTA_VALIDO',	"17"||'PLAZO_SIMULTANEA_VALIDO',"18"||'INSTRUMENTO_PERMITE_VENTA_CORTA',		"19"||'TASA_VENTA_CORTA_VALIDA',			"20"||'TASA_SIMULTANEA_VALIDA',
			"21"||'CANTIDAD_MAYOR_CERO',		"22"||'PRECIO_MAYOR_CERO',		"23"||'EJECUTIVO_PORTAFOLIO_CONFIGURADO',		"24"||'MONTO_MAYOR_CERO',					"25"||'VALOR_RESCATE_MAYOR_CERO',
			"26"||'CONTRATO_SIMULTANEA',		"27"||'CONTRATO_MERCANTIL',		"28"||'CONTRATO_VENTA_CORTA',					"29"||'CONTRATO_CUSTODIA',					"30"||'CONTRATO_FUTUROS',
			"31"||'PORTAFOLIO_HABILITADO',		"32"||'OPERA_CUSTODIA_ORDEN_NOMINAL_COMPRA_RV',									"33"||'OPERA_CUSTODIA_ORDEN_NOMINAL_COMPRA_RFIF',
			"34"||'OPERA_CUSTODIA_ORDEN_NOMINAL_VENTA_RV',						"35"||'OPERA_CUSTODIA_ORDEN_NOMINAL_VENTA_RFIF',"36"||'PERMITE_INGRESO_ORDEN_VERBAL_ESCRITA',
			"37"||'CANAL_DISTRIBUCION_VALIDO',	"38"||'PLANTILLA_EMPLEADO_PRODUCTO_HABILITADO',									"39"||'PLANTILLA_EMPLEADO_PERMITE_RUTEO',	"40"||'PORTAFOLIO_COBROS_CONFIGURADOS',
			"41"||'EMPLEADO_HABILITADO',		"42"||'ORDEN_EN_HORARIO',		"43"||'ORDEN_APLAZADA',							"44"||'CLIENTE_LIBRO_ORDENES_CONFIGURADO',	"45"||'PLAZO_VENTA_CORTA_SUPERA_MAXIMO_PERMITIDO',
			"46"||'PLAZO_VENTA_CORTA_BAJO_MINIMO_PERMITIDO'  , "47"||'CONTRATO_CONDICIONES_GENERALES',

			"48"||'PERSONA_HABILITADA',"49"||'EJECUTIVO_PORTAFOLIO_HABILITADO',"50"||'ORDEN_REQUIERE_AUTORIZACION',
			"51"||'FOTOCOPIA_DOCUMENTO_IDENTIDAD',"52"||'ANTECEDENTES_LEGALES',"53"||'DIFERENCIA_PRECIO_ORDEN',"54"||'ESTADO_LIBRO'
			)  AS "CodigoValidacion" 
			FROM :v_val,"BSCL.Plataforma.BD::PLA.Identidad"
		) WHERE "Resultado" IS NOT NULL;
	
	--SELECT 'v_TablaRequisitos',* FROM :v_TablaRequisitos;
	
	p_out_SalidaDetalle =	SELECT 
								tb."IdInterno", tb."IdOrden" as "IdOperacion",
								CASE WHEN "Resultado" = 'AUTORIZACION' AND ord."IdTipoCanalIngreso"= 'TRA' THEN 'RECHAZO' ELSE "Resultado" END AS "Resultado" ,
								CASE "CodigoValidacion" 
									WHEN 'CLIENTE_EXISTE' 					THEN 'CLIENTE_NO_EXISTE'					--1
									WHEN 'CLIENTE_HABILITADO' 				THEN 'CLIENTE_BLOQUEADO'
									WHEN 'PORTAFOLIO_EXISTE' 				THEN 'PORTAFOLIO_NO_EXISTE'
									WHEN 'INSTRUMENTO_EXISTE' 				THEN 'INSTRUMENTO_NO_EXISTE'
									WHEN 'INSTRUMENTO_VIGENTE' 				THEN 'INSTRUMENTO_NO_VIGENTE'
									WHEN 'BOLSA_VALIDA' 					THEN 'BOLSA_NO_VALIDA'
									WHEN 'TIPO_OPERACION_VALIDA' 			THEN 'TIPO_OPERACION_NO_VALIDA'
									WHEN 'CONDICION_LIQUIDACION_CONTADO_VALIDA' THEN 'CONDICION_LIQUIDACION_CONTADO_NO_VALIDA'
									WHEN 'CONDICION_LIQUIDACION_PLAZO_VALIDA' THEN 'CONDICION_LIQUIDACION_PLAZO_NO_VALIDA'
									WHEN 'PORCENTAJE_VISIBILIDAD_VALIDO' 	THEN 'PORCENTAJE_VISIBILIDAD_NO_VALIDO'		--10
									WHEN 'TIPO_VIGENCIA_VALIDA' 			THEN 'TIPO_VIGENCIA_NO_VALIDA'
									WHEN 'BOVEDA_VALIDA' 					THEN 'BOVEDA_NO_VALIDA'
									WHEN 'FORMA_CUSTODIA_VALIDA' 			THEN 'FORMA_CUSTODIA_NO_VALIDA'
									WHEN 'TIPO_PRECIO_VALIDO' 				THEN 'TIPO_PRECIO_NO_VALIDO'
									WHEN 'EMPLEADO_DIGITADOR_VALIDO'		THEN 'EMPLEADO_DIGITADOR_NO_VALIDO'
									WHEN 'PLAZO_VENTA_CORTA_VALIDO' 		THEN 'PLAZO_VENTA_CORTA_NO_VALIDO'
									WHEN 'PLAZO_SIMULTANEA_VALIDO'			THEN 'PLAZO_SIMULTANEA_NO_VALIDO'
									WHEN 'INSTRUMENTO_PERMITE_VENTA_CORTA' 	THEN 'INSTRUMENTO_NO_PERMITE_VENTA_CORTA'
									WHEN 'TASA_VENTA_CORTA_VALIDA' 			THEN 'TASA_VENTA_CORTA_NO_VALIDA'
									WHEN 'TASA_SIMULTANEA_VALIDA' 			THEN 'TASA_SIMULTANEA_NO_VALIDA'			--20
									WHEN 'CANTIDAD_MAYOR_CERO' 				THEN 'CANTIDAD_MENOR_IGUAL_CERO'
									WHEN 'PRECIO_MAYOR_CERO' 				THEN 'PRECIO_MENOR_IGUAL_CERO'
									WHEN 'EJECUTIVO_PORTAFOLIO_CONFIGURADO' THEN 'EJECUTIVO_PORTAFOLIO_NO_CONFIGURADO'
									WHEN 'MONTO_MAYOR_CERO' 				THEN 'MONTO_MENOR_IGUAL_CERO'
									WHEN 'VALOR_RESCATE_MAYOR_CERO' 		THEN 'VALOR_RESCATE_MENOR_IGUAL_CERO'
									WHEN 'CONTRATO_SIMULTANEA' 				THEN 'CLIENTE_SIN_CONTRATO_SIMULTANEA'
									WHEN 'CONTRATO_MERCANTIL' 				THEN 'CLIENTE_SIN_CONTRATO_MERCANTIL_PARA_OPERAR_CUENTA_INVERSION'
									WHEN 'CONTRATO_VENTA_CORTA'				THEN 'CLIENTE_SIN_CONTRATO_VENTA_CORTA'
									WHEN 'CONTRATO_CUSTODIA' 				THEN 'CLIENTE_SIN_CONTRATO_CUSTODIA'
									WHEN 'CONTRATO_FUTUROS'					THEN 'CLIENTE_SIN_CONTRATO_FUTUROS'			--30
									WHEN 'PORTAFOLIO_HABILITADO' 			THEN 'PORTAFOLIO_BLOQUEADO'
									WHEN 'OPERA_CUSTODIA_ORDEN_NOMINAL_COMPRA_RV' 	THEN 'CLIENTE_OPERA_CUSTODIA_ORDEN_NOMINAL'
									WHEN 'OPERA_CUSTODIA_ORDEN_NOMINAL_COMPRA_RFIF' THEN 'CLIENTE_OPERA_CUSTODIA_ORDEN_NOMINAL'
									WHEN 'OPERA_CUSTODIA_ORDEN_NOMINAL_VENTA_RV' 	THEN 'CLIENTE_OPERA_CUSTODIA_ORDEN_VENTA_NOMINAL'
									WHEN 'OPERA_CUSTODIA_ORDEN_NOMINAL_VENTA_RFIF' 	THEN 'CLIENTE_OPERA_CUSTODIA_ORDEN_VENTA_NOMINAL'
									WHEN 'PERMITE_INGRESO_ORDEN_VERBAL_ESCRITA' 	THEN 'ORDEN_ES_VERBAL_O_ESCRITA'
									WHEN 'CANAL_DISTRIBUCION_VALIDO' 				THEN 'CANAL_DISTRIBUCION_NO_CONFIGURADO'	--37
									WHEN 'PLANTILLA_EMPLEADO_PRODUCTO_HABILITADO' 	THEN 'PRODUCTO_NO_HABILITADO_EMPLEADO'	--38
									WHEN 'PLANTILLA_EMPLEADO_PERMITE_RUTEO' 		THEN 'RUTEO_NO_HABILITADO_EMPLEADO'		--39
									WHEN 'PORTAFOLIO_COBROS_CONFIGURADOS' 			THEN 'PORTAFOLIO_SIN_MATRIZ_OPERACIONAL_CONFIGURADA'--40
									WHEN 'EMPLEADO_HABILITADO' 						THEN 'EMPLEADO_NO_HABILITADO'				--41
									WHEN 'ORDEN_EN_HORARIO' 						THEN 'ORDEN_FUERA_HORARIO'					--42
									WHEN 'ORDEN_APLAZADA' 							THEN 'ORDEN_APLAZADA_DIA_SIGUIENTE'			--43
									WHEN 'CLIENTE_LIBRO_ORDENES_CONFIGURADO' 		THEN 'CLIENTE_SIN_LIBRO_ORDENES_CONFIGURADO'	--44
									WHEN 'PLAZO_VENTA_CORTA_SUPERA_MAXIMO_PERMITIDO' THEN 'PLAZO_VENTA_CORTA_SUPERA_MAXIMO_PERMITIDO'	--45
									WHEN 'PLAZO_VENTA_CORTA_BAJO_MINIMO_PERMITIDO'	THEN 'PLAZO_VENTA_CORTA_BAJO_MINIMO_PERMITIDO'		--46
									WHEN 'CONTRATO_CONDICIONES_GENERALES'			THEN 'CLIENTE_SIN_CONTRATO_CONDICIONES_GENERALES'	--47
									
									WHEN 'PERSONA_HABILITADA'						THEN 'PERSONA_BLOQUEADA'							--48
									WHEN 'EJECUTIVO_PORTAFOLIO_HABILITADO'			THEN 'EJECUTIVO_PORTAFOLIO_BLOQUEADO'				--49
									WHEN 'ORDEN_REQUIERE_AUTORIZACION'				THEN 'ORDEN_REQUIERE_AUTORIZACION'					--50
									WHEN 'FOTOCOPIA_DOCUMENTO_IDENTIDAD'			THEN 'CLIENTE_SIN_FOTOCOPIA_DOCUMENTO_IDENTIDAD'	--51
									WHEN 'ANTECEDENTES_LEGALES'						THEN 'CLIENTE_SIN_ANTECEDENTES_LEGALES'				--52
									WHEN 'DIFERENCIA_PRECIO_ORDEN'					THEN 'ORDEN_DIFIERE_PORCENTAJE_PERMITIDO_PRECIO_MERCADO'		--53
									WHEN 'ESTADO_LIBRO'								THEN 'LIBRO_CERRADO'									--54

								END AS "Codigo",
								CASE "CodigoValidacion" 
									WHEN 'CLIENTE_EXISTE' 						THEN 'IdCliente'				--01
									WHEN 'CLIENTE_HABILITADO' 					THEN 'IdCliente'
									WHEN 'PORTAFOLIO_EXISTE'					THEN 'IdPortafolio'
									WHEN 'INSTRUMENTO_EXISTE' 					THEN 'IdInstrumento'
									WHEN 'INSTRUMENTO_VIGENTE' 					THEN 'IdInstrumento'
									WHEN 'BOLSA_VALIDA' 						THEN 'IdBolsa'
									WHEN 'TIPO_OPERACION_VALIDA' 				THEN 'IdTipoOperacion'
									WHEN 'CONDICION_LIQUIDACION_CONTADO_VALIDA' THEN 'IdCondicionDeLiquidacionContado'
									WHEN 'CONDICION_LIQUIDACION_PLAZO_VALIDA' 	THEN 'IdCondicionDeLiquidacionPlazo'
									WHEN 'PORCENTAJE_VISIBILIDAD_VALIDO' 		THEN 'IdPorcentajeVisibilidad'	--10
									WHEN 'TIPO_VIGENCIA_VALIDA' 				THEN 'IdTipoVigencia'
									WHEN 'BOVEDA_VALIDA' 						THEN 'IdBoveda'
									WHEN 'FORMA_CUSTODIA_VALIDA' 				THEN 'IdFormaCustodia'
									WHEN 'TIPO_PRECIO_VALIDO' 					THEN 'IdTipoPrecio'
									WHEN 'EMPLEADO_DIGITADOR_VALIDO' 			THEN 'IdInterno'
									WHEN 'PLAZO_VENTA_CORTA_VALIDO' 			THEN 'PlazoVentaCorta'
									WHEN 'PLAZO_SIMULTANEA_VALIDO' 				THEN 'PlazoSimultanea'
									WHEN 'INSTRUMENTO_PERMITE_VENTA_CORTA' 		THEN 'IdInstrumento'
									WHEN 'TASA_VENTA_CORTA_VALIDA' 				THEN 'TasaVentaCorta'
									WHEN 'TASA_SIMULTANEA_VALIDA' 				THEN 'TasaSimultanea'			--20
									WHEN 'CANTIDAD_MAYOR_CERO' 					THEN 'IdInterno'
									WHEN 'PRECIO_MAYOR_CERO' 					THEN 'IdInterno'
									WHEN 'EJECUTIVO_PORTAFOLIO_CONFIGURADO' 	THEN 'IdPortafolio'
									WHEN 'MONTO_MAYOR_CERO' 					THEN 'IdInterno'
									WHEN 'VALOR_RESCATE_MAYOR_CERO' 			THEN 'IdInterno'
									WHEN 'CONTRATO_SIMULTANEA' 					THEN 'IdInterno'
									WHEN 'CONTRATO_MERCANTIL' 					THEN 'IdInterno'
									WHEN 'CONTRATO_VENTA_CORTA' 				THEN 'IdInterno'
									WHEN 'CONTRATO_CUSTODIA' 					THEN 'DocumentoIdentidad'
									WHEN 'CONTRATO_FUTUROS' 					THEN 'IdInterno'				--30
									WHEN 'PORTAFOLIO_HABILITADO' 				THEN 'IdPortafolio'
									WHEN 'OPERA_CUSTODIA_ORDEN_NOMINAL_COMPRA_RV' 	THEN 'IdInterno'
									WHEN 'OPERA_CUSTODIA_ORDEN_NOMINAL_COMPRA_RFIF' THEN 'IdInterno'
									WHEN 'OPERA_CUSTODIA_ORDEN_NOMINAL_VENTA_RV' 	THEN 'IdInterno'
									WHEN 'OPERA_CUSTODIA_ORDEN_NOMINAL_VENTA_RFIF' 	THEN 'IdInterno'
									WHEN 'PERMITE_INGRESO_ORDEN_VERBAL_ESCRITA' 	THEN 'IdInterno'
									WHEN 'CANAL_DISTRIBUCION_VALIDO' 				THEN 'CombinacionCanalDistribucion'			--37
									WHEN 'PLANTILLA_EMPLEADO_PRODUCTO_HABILITADO'	THEN 'IdInterno'			--38
									WHEN 'PLANTILLA_EMPLEADO_PERMITE_RUTEO' 		THEN 'IdInterno'			--39
									WHEN 'PORTAFOLIO_COBROS_CONFIGURADOS' 			THEN 'IdPortafolio'			--40
									WHEN 'EMPLEADO_HABILITADO' 						THEN 'IdEmpleadoDigitador'	--41
									WHEN 'ORDEN_EN_HORARIO' 						THEN 'IdInterno'			--42
									WHEN 'ORDEN_APLAZADA' 							THEN 'IdInterno'			--43
									WHEN 'CLIENTE_LIBRO_ORDENES_CONFIGURADO' 		THEN 'IdInterno'			--44
									WHEN 'PLAZO_VENTA_CORTA_SUPERA_MAXIMO_PERMITIDO' THEN 'IdInterno'			--45
									WHEN 'PLAZO_VENTA_CORTA_BAJO_MINIMO_PERMITIDO'  THEN 'IdInterno'			--46
									WHEN 'CONTRATO_CONDICIONES_GENERALES'			THEN 'IdInterno'			--47
									
									WHEN 'PERSONA_HABILITADA'						THEN 'IdInterno'				--48
									WHEN 'EJECUTIVO_PORTAFOLIO_HABILITADO'			THEN 'IdInterno'				--49
									WHEN 'ORDEN_REQUIERE_AUTORIZACION'				THEN 'IdInterno'				--50
									WHEN 'FOTOCOPIA_DOCUMENTO_IDENTIDAD'			THEN 'IdInterno'				--51
									WHEN 'ANTECEDENTES_LEGALES'						THEN 'IdInterno'				--52
									WHEN 'DIFERENCIA_PRECIO_ORDEN'					THEN 'IdInterno'				--53
									WHEN 'ESTADO_LIBRO'								THEN 'IdInterno'				--53
								
								END AS "CodigoDato1",
								CASE "CodigoValidacion" 
									WHEN 'CLIENTE_EXISTE' 						THEN CAST(ord."IdCliente" AS NVARCHAR(100))					--01
									WHEN 'CLIENTE_HABILITADO' 					THEN CAST(ord."IdCliente" AS NVARCHAR(100))
									WHEN 'PORTAFOLIO_EXISTE' 					THEN CAST(ord."IdPortafolio" AS NVARCHAR(100))
									WHEN 'INSTRUMENTO_EXISTE' 					THEN CAST(ord."IdInstrumento" AS NVARCHAR(100))
									WHEN 'INSTRUMENTO_VIGENTE' 					THEN CAST(ord."IdInstrumento" AS NVARCHAR(100))
									WHEN 'BOLSA_VALIDA' 						THEN CAST(ord."IdBolsa" AS NVARCHAR(100))
									WHEN 'TIPO_OPERACION_VALIDA' 				THEN CAST(ord."IdTipoOperacion" AS NVARCHAR(100))
									WHEN 'CONDICION_LIQUIDACION_CONTADO_VALIDA'	THEN CAST(ord."IdCondicionDeLiquidacionContado" AS NVARCHAR(100))
									WHEN 'CONDICION_LIQUIDACION_PLAZO_VALIDA' 	THEN CAST(ord."IdCondicionDeLiquidacionPlazo" AS NVARCHAR(100))
									WHEN 'PORCENTAJE_VISIBILIDAD_VALIDO' 		THEN CAST(ord."IdPorcentajeVisibilidad" AS NVARCHAR(100))	--10
									WHEN 'TIPO_VIGENCIA_VALIDA' 				THEN CAST(ord."IdTipoVigencia" AS NVARCHAR(100))
									WHEN 'BOVEDA_VALIDA' 						THEN CAST(ord."IdBoveda" AS NVARCHAR(100))
									WHEN 'FORMA_CUSTODIA_VALIDA' 				THEN CAST(ord."IdFormaCustodia" AS NVARCHAR(100))
									WHEN 'TIPO_PRECIO_VALIDO' 					THEN CAST(ord."IdTipoPrecio" AS NVARCHAR(100))
									WHEN 'EMPLEADO_DIGITADOR_VALIDO' 			THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'PLAZO_VENTA_CORTA_VALIDO' 			THEN CAST(ord."PlazoVentaCorta" AS NVARCHAR(100))
									WHEN 'PLAZO_SIMULTANEA_VALIDO' 				THEN CAST(ord."PlazoSimultanea" AS NVARCHAR(100))
									WHEN 'INSTRUMENTO_PERMITE_VENTA_CORTA' 		THEN CAST(ord."IdInstrumento" AS NVARCHAR(100))
									WHEN 'TASA_VENTA_CORTA_VALIDA' 				THEN CAST(ord."TasaVentaCorta" AS NVARCHAR(100))
									WHEN 'TASA_SIMULTANEA_VALIDA' 				THEN CAST(ord."TasaSimultanea" AS NVARCHAR(100))			--20
									WHEN 'CANTIDAD_MAYOR_CERO' 					THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'PRECIO_MAYOR_CERO' 					THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'EJECUTIVO_PORTAFOLIO_CONFIGURADO' 	THEN CAST(ord."IdPortafolio" AS NVARCHAR(100))
									WHEN 'MONTO_MAYOR_CERO' 					THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'VALOR_RESCATE_MAYOR_CERO' 			THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'CONTRATO_SIMULTANEA'					THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'CONTRATO_MERCANTIL' 					THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'CONTRATO_VENTA_CORTA' 				THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'CONTRATO_CUSTODIA' 					THEN CAST(ord."DocumentoIdentidad" AS NVARCHAR(100))
									WHEN 'CONTRATO_FUTUROS' 					THEN CAST(ord."IdInterno" AS NVARCHAR(100))					--30
									WHEN 'PORTAFOLIO_HABILITADO' 				THEN CAST(ord."IdPortafolio" AS NVARCHAR(100))
									WHEN 'OPERA_CUSTODIA_ORDEN_NOMINAL_COMPRA_RV' 	THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'OPERA_CUSTODIA_ORDEN_NOMINAL_COMPRA_RFIF' THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'OPERA_CUSTODIA_ORDEN_NOMINAL_VENTA_RV' 	THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'OPERA_CUSTODIA_ORDEN_NOMINAL_VENTA_RFIF'	THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'PERMITE_INGRESO_ORDEN_VERBAL_ESCRITA' 	THEN CAST(ord."IdInterno" AS NVARCHAR(100))
									WHEN 'CANAL_DISTRIBUCION_VALIDO' 				THEN CAST(ord."IdTipoCanalDistribucion" || '-' || ord."IdBolsa" || '-' || ord."IdTipoInstrumento" AS NVARCHAR(100))				--37
									WHEN 'PLANTILLA_EMPLEADO_PRODUCTO_HABILITADO'	THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--38
									WHEN 'PLANTILLA_EMPLEADO_PERMITE_RUTEO' 		THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--39
									WHEN 'PORTAFOLIO_COBROS_CONFIGURADOS' 			THEN CAST(ord."IdPortafolio" AS NVARCHAR(100))			--40
									WHEN 'EMPLEADO_HABILITADO' 						THEN CAST(ord."IdEmpleadoDigitador" AS NVARCHAR(100))	--41
									WHEN 'ORDEN_EN_HORARIO' 						THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--42
									WHEN 'ORDEN_APLAZADA' 							THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--43
									WHEN 'CLIENTE_LIBRO_ORDENES_CONFIGURADO' 		THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--44
									WHEN 'PLAZO_VENTA_CORTA_SUPERA_MAXIMO_PERMITIDO' THEN CAST(ord."IdInterno" AS NVARCHAR(100))			--45
									WHEN 'PLAZO_VENTA_CORTA_BAJO_MINIMO_PERMITIDO'	THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--46	
									WHEN 'CONTRATO_CONDICIONES_GENERALES'			THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--47
									
									WHEN 'PERSONA_HABILITADA'						THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--48
									WHEN 'EJECUTIVO_PORTAFOLIO_HABILITADO'			THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--49
									WHEN 'ORDEN_REQUIERE_AUTORIZACION'				THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--50
									WHEN 'FOTOCOPIA_DOCUMENTO_IDENTIDAD'			THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--51
									WHEN 'ANTECEDENTES_LEGALES'						THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--52
									WHEN 'DIFERENCIA_PRECIO_ORDEN'					THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--53
									WHEN 'ESTADO_LIBRO'								THEN CAST(ord."IdInterno" AS NVARCHAR(100))				--53
								END AS "ValorDato1",
								'' as "CodigoDato2",
								'' as "ValorDato2"
								
								
							FROM :v_TablaRequisitos tb
							INNER JOIN :p_Ordenes ord on tb."IdInterno" = ord."IdInterno"
							WHERE "Resultado" != 'OK'
							ORDER BY "IdInterno";			
	
END;
