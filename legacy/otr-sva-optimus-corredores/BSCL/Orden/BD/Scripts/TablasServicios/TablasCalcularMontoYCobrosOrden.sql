SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_Ordenes";
DROP TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_CobrosModificados";
DROP TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_Ordenes";
DROP TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_Cobros";
DROP TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_AdvertenciasDetalle";
DROP TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_ErroresDetalle";
DROP TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_Ordenes";
DROP TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos";


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_Ordenes"(
	"IdInterno"						BIGINT,
	"IdPortafolio"					BIGINT,
	"IdTipoProducto"				NVARCHAR(4),
	"IdTipoOperacion"				NVARCHAR(6),
	"IdTipoCanalIngreso" 			NVARCHAR(4),
	"IdBolsa" 						NVARCHAR(3),
	"IdCorredorCorresponsal"		BIGINT,
	"IdInstrumento"					BIGINT,
	"DiasPlazo"						BIGINT,
	"CantidadOrden"					DECIMAL(26,8),
	"MontoOrden"					DECIMAL(26,8),
	"ValorRescateOrden"				DECIMAL(26,8),
	"EsFungible"					NVARCHAR(1),
	"TasaSimultanea"				DECIMAL(26,8),
	"TasaRFIF"						DECIMAL(26,8),
	"TasaVentaCorta"				DECIMAL(26,8),
	"IdMonedaLiquidacionOrden"		NVARCHAR(3),
	"PrecioOrden"					DECIMAL(26,8),
	"TipoCambio"					DECIMAL(26,8),
	"MontoTotal"					DECIMAL(26,8),
	"EsPorMonto"					NVARCHAR(1)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Entrada_CobrosModificados"(
	"IdInterno"				BIGINT,
	"IdCobro"				NVARCHAR(6),
	"ValorCobroAplicado"	DECIMAL(26,8)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_Ordenes"(
	"IdInterno"						BIGINT,
	"IdPortafolio"					BIGINT,
	"IdTipoProducto"				NVARCHAR(4),
	"IdTipoOperacion"				NVARCHAR(6),
	"IdTipoCanalIngreso" 			NVARCHAR(4),
	"IdBolsa" 						NVARCHAR(3),
	"IdInstrumento"					BIGINT,
	"DiasPlazo"						BIGINT,
	"CantidadOrden"					DECIMAL(26,8),
	"MontoOrden"					DECIMAL(26,8),
	"ValorRescateOrden"				DECIMAL(26,8),
	"EsFungible"					NVARCHAR(1),
	"TasaSimultanea"				DECIMAL(26,8),
	"TasaRFIF"						DECIMAL(26,8),
	"TasaVentaCorta"				DECIMAL(26,8),
	"IdMonedaLiquidacionOrden"		NVARCHAR(3),
	"PrecioOrden"					DECIMAL(26,8),
	"TipoCambio"					DECIMAL(26,8),
	"MontoTotal"					DECIMAL(26,8),
	"EsPorMonto"					NVARCHAR(1),
	
	--SALIDA
	"IdMonedaOrden"						NVARCHAR(1),
	"MontoTotalLiquidacion"				DECIMAL(26,8), -- MONTO OPERACION LIQUIDACION + TOTAL COBROS LIQUIDACION
	"MontoOperacion"					DECIMAL(26,8), -- P X Q MONEDA DEL INSTRUMENTO
	"CantidadOperacion"					DECIMAL(26,8), -- Q
	"ValorRescateOperacion"				DECIMAL(26,8),
	"MontoOperacionLocal"				DECIMAL(26,8), -- P X Q MONEDA LOCAL
	"MontoOperacionLiquidacion"			DECIMAL(26,8), -- P X Q MONEDA DE LIQUIDACION
	"TotalCobrosLocal"					DECIMAL(26,8),
	"TotalCobrosLiquidacion"			DECIMAL(26,8)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_Cobros"(
	"IdInterno"							BIGINT,
	"IdCobro" 							NVARCHAR(40),
	"CodigoCobroClasificacion" 			NVARCHAR(40),
	"IdMonedaCobro" 					NVARCHAR(40),
	"ValorCobroPerfil" 					DECIMAL(26,8),
	"PermiteCambiarValor"				NVARCHAR(1),
	"FormaCobro" 						NVARCHAR(1),
	"MontoMinimo" 						DECIMAL(26,8),
	"ValorCobroAplicado" 				DECIMAL(26,8),
	"MontoCobroCalculado" 				DECIMAL(26,8),
	"MontoCobroCalculadoLocal" 			DECIMAL(26,8),
	"MontoCobroCalculadoLiquidacion" 	DECIMAL(26,8)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_AdvertenciasDetalle"(
"IdInterno"				BIGINT,
"Codigo" 				NVARCHAR(100),
"RequiereAutorizacion" 	NVARCHAR(100),
"Descripcion" 			NVARCHAR(5000)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Salida_ErroresDetalle"(
"IdInterno"		BIGINT,
"Codigo" 		NVARCHAR(100),
"Descripcion" 	NVARCHAR(5000)
) ON COMMIT DELETE ROWS;



CREATE GLOBAL TEMPORARY TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_Ordenes" (
	"IdInterno"						BIGINT,
		"IdPortafolio"					BIGINT,
		"IdTipoProducto"				NVARCHAR(4),
		"IdTipoOperacion"				NVARCHAR(6),
		"IdTipoCanalIngreso" 			NVARCHAR(4),
		"IdBolsa" 						NVARCHAR(3),
		"IdCorredorCorresponsal"		BIGINT,
		
		"IdInstrumento"					BIGINT,
		"AccionOrden"					NVARCHAR(40),
		"DiasPlazo"						BIGINT,
		"CantidadOrden"					DECIMAL(26,8),
		"MontoOrden"					DECIMAL(26,8),
		"ValorRescateOrden"				DECIMAL(26,8),
		"EsFungible"					NVARCHAR(1),
		"TasaSimultanea"				DECIMAL(26,8),
		"TasaRFIF"						DECIMAL(26,8),
		"TasaVentaCorta"				DECIMAL(26,8),
		"IdMonedaLiquidacionOrden"		NVARCHAR(3),
		"PrecioOrden"					DECIMAL(26,8),
		"PrecioOrdenMonedaLiquidacion"	DECIMAL(26,8),
		"IdMonedaInstrumento"			NVARCHAR(3),
		"TipoCambio"					DECIMAL(26,8),
		"MontoTotal"					DECIMAL(26,8),	--EN MONEDA DE LIQUIDACION
		"EsPorMonto"					NVARCHAR(1)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY TABLE "BSCL.Orden.BD::ORD.CalcularMontoYCobrosOrden_Proceso_CobrosEfectivos" (
	"IdInterno"							BIGINT,
	"IdInternoCobro"					BIGINT,
	"IdCobro" 							NVARCHAR(40),
	"IdMonedaCobro" 					NVARCHAR(3),
	"IdMonedaLiquidacion"				NVARCHAR(3),
	"ValorCobro" 						DECIMAL(26,8),
	"ValorCobroMonedaLiquidacion"		DECIMAL(26,8),
	"FormaCobro" 						NVARCHAR(1),
	"TipoCambio"						DECIMAL(26,8),
	"ValorTopeMinimoOrden"				DECIMAL(26,8),
	"ValorTopeMinimoOrdenMonedaLiquidacion"	DECIMAL(26,8)
) ON COMMIT DELETE ROWS;

commit

 
 

 

 

 





 






 

 
 

