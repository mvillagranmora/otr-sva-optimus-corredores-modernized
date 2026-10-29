UPDATE "BSCL.Orden.BD::ORD.Transaccion"
SET
	"FechaEmision" 						= COALESCE(:FechaEmision,"FechaEmision"),
	"Cantidad"				 			= COALESCE(:Cantidad,"Cantidad"),
	"IdLibroOrdenes" 					= COALESCE(:IdLibroOrdenes,"IdLibroOrdenes"),
	"EsBilateral"						= COALESCE(:EsBilateral,"EsBilateral"),
	"BloqueoLiquidacion"				= COALESCE(:BloqueoLiquidacion,"BloqueoLiquidacion"),			
	"FechaLiquidacion"					= COALESCE(:FechaLiquidacion,"FechaLiquidacion"),				
	"MontoRetencion"					= COALESCE(:MontoRetencion,"MontoRetencion"),
	"Tasa"								= COALESCE(:Tasa,"Tasa"),
	"TipoCambioCorresponsal"			= COALESCE(:TipoCambioCorresponsal,"TipoCambioCorresponsal"),
	"IdCorredorCorresponsal"			= COALESCE(:IdCorredorCorresponsal,"IdCorredorCorresponsal"),
	"MontoLiquidacionCorresponsal"		= COALESCE(:MontoLiquidacionCorresponsal,"MontoLiquidacionCorresponsal"),
	"GastosCorresponsal"				= COALESCE(:GastosCorresponsal,"GastosCorresponsal"),
	"IdMonedaLiquidacionCorresponsal"	= COALESCE(:IdMonedaLiquidacionCorresponsal,"IdMonedaLiquidacionCorresponsal"),
	"Monto" 							= CASE WHEN "IdClasificacionTipoInstrumento" = 'RV' THEN COALESCE(:PrecioAPlazo,"Precio") * COALESCE(:Cantidad,"Cantidad") ELSE COALESCE(:MontoAPlazo,"Monto") END,
	"Precio" 							= COALESCE(:Precio,"Precio"),
	"IdCamara"							= COALESCE(:IdCamara,"IdCamara"),
	"Plazo"								= COALESCE(:Plazo,"Plazo")
WHERE "IdTransaccion" = :IdTransaccion