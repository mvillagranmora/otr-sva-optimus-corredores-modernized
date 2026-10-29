UPDATE "BSCL.Orden.BD::ORD.Transaccion"
SET
	"Monto" 							= CASE WHEN "IdClasificacionTipoInstrumento" = 'RV' THEN COALESCE(:Precio,"Precio") * COALESCE(:Cantidad,"Cantidad") ELSE COALESCE(:Monto,"Monto") END,
	"Precio" 							= COALESCE(:Precio,"Precio"),
	"FechaEmision" 						= COALESCE(:FechaEmision,"FechaEmision"),
	"Cantidad"				 			= CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN "Cantidad"  ELSE COALESCE(:Cantidad,"Cantidad") END,
	"ValorRescate"						= CASE WHEN "IdClasificacionTipoInstrumento" = 'IF' THEN COALESCE(:Cantidad,"Cantidad") ELSE "ValorRescate" END,
	"IdLibroOrdenes" 					= COALESCE(:IdLibroOrdenes,"IdLibroOrdenes"),
	"TipoCambioCorresponsal"			= COALESCE(:TipoCambioCorresponsal,"TipoCambioCorresponsal"),
	"IdCorredorCorresponsal"			= COALESCE(:IdCorredorCorresponsal,"IdCorredorCorresponsal"),
	"MontoLiquidacionCorresponsal"		= COALESCE(:MontoLiquidacionCorresponsal,"MontoLiquidacionCorresponsal"),
	"GastosCorresponsal"				= COALESCE(:GastosCorresponsal,"GastosCorresponsal"),
	"IdMonedaLiquidacionCorresponsal"	= COALESCE(:IdMonedaLiquidacionCorresponsal,"IdMonedaLiquidacionCorresponsal"),
	"MarcaNegociacion"					= COALESCE(:MarcaNegociacion,"MarcaNegociacion"),
	"Tasa"								= COALESCE(:Tasa,"Tasa"),
	"EsBilateral"						= COALESCE(:EsBilateral,"EsBilateral"),
	"BloqueoLiquidacion"				= COALESCE(:BloqueoLiquidacion,"BloqueoLiquidacion"),			
	"FechaLiquidacion"					= COALESCE(:FechaLiquidacion,"FechaLiquidacion"),
	"MontoRetencion"					= COALESCE(:MontoRetencion,"MontoRetencion"),
	"IdCamara"							= COALESCE(:IdCamara,"IdCamara"),
	"IdCondicionDeLiquidacion"			= COALESCE(:IdCondicionDeLiquidacion,"IdCondicionDeLiquidacion"),
	"Plazo"								= COALESCE(:Plazo,"Plazo")
WHERE "IdTransaccion" = :IdTransaccion;