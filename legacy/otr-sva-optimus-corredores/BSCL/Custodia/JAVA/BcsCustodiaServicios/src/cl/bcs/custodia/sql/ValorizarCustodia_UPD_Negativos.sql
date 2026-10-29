UPDATE "BSCL.Custodia.BD::CUS.CarteraNegativa"
SET
	"FechaValorizacion" 						= COALESCE(:INGFechaValorizacionActual,"FechaValorizacion"),
	"PrecioMercado"								= COALESCE(:INGPrecioMercadoActual,"PrecioMercado"),
	"TasaMercado" 								= COALESCE(:INGTasaMercadoActual,"TasaMercado"),
	"MontoMercadoMonedaSistema" 				= COALESCE(:INGMontoATasaMercadoActual,"MontoMercadoMonedaSistema"),
	"MontoMercadoMonedaInstrumento" 			= COALESCE(:INGMontoMercadoActualMonedaInstrumento,"MontoMercadoMonedaInstrumento"),
	"FechaPrecioTasa"				 			= COALESCE(:INGFechaPrecioTasaMercadoActual,"FechaPrecioTasa"),
	"FuentePrecioTasa"							= COALESCE(:INGFuenteTasaPrecioMercadoActual,"FuentePrecioTasa"),
	"MontoCorteCupon"							= COALESCE(:INGMontoCorteCupon,"MontoCorteCupon"),
	"MontoCorteCuponMonedaInstrumento"			= COALESCE(:INGMontoCorteCuponMonedaInstrumento,"MontoCorteCuponMonedaInstrumento"),
	"IdMonedaPagoCupon" 						= COALESCE(:INGIdMonedaPagoCupon,"IdMonedaPagoCupon"),
	"FechaCorteCupon" 							= COALESCE(:INGFechaCorteCupon,"FechaCorteCupon"),
	"NumeroCupon" 								= COALESCE(:INGNumeroCupon,"NumeroCupon"),
 	"MontoCapitalCorteCupon"					= COALESCE(:INGMontoCapitalCorteCupon,"MontoCapitalCorteCupon"),
 	"MontoInteresCorteCupon" 					= COALESCE(:INGMontoInteresCorteCupon,"MontoInteresCorteCupon"),
 	"InteresesPagoCorteCupon" 					= COALESCE(:INGInteresesPagoCorteCupon,"InteresesPagoCorteCupon"),
	"MontoCapitalCorteCuponMonedaInstrumento"	= COALESCE(:INGMontoCapitalCorteCuponMonedaInstrumento,"MontoCapitalCorteCuponMonedaInstrumento"),
 	"MontoReajusteCorteCupon"					= COALESCE(:INGMontoReajusteCorteCupon,"MontoReajusteCorteCupon"),
 	"FechaPagoCorteCupon"						= COALESCE(:INGFechaPagoCorteCupon,"FechaPagoCorteCupon")
	 
WHERE "IdCarteraNegativa" = :IdCarteraNegativa;