/********************************************************** 
Proposito	: Script para la actualización de montos valorizados en la tabla Transito
autor		: NRM
fecha		: 20/05/2017
***********************************************************/


UPDATE "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
SET
	"FechaFactorMonedaAdicional" 		= COALESCE(:FechaFactorMonedaAdicional,"FechaFactorMonedaAdicional"),
	"ValorFactorMonedaAdicional" 		= COALESCE(:ValorFactorMonedaAdicional,"ValorFactorMonedaAdicional"),
	"FechaFactorMonedaInstrumento" 		= COALESCE(:FechaFactorMonedaInstrumento,"FechaFactorMonedaInstrumento"),
	"ValorFactorMonedaInstrumento" 		= COALESCE(:ValorFactorMonedaInstrumento,"ValorFactorMonedaInstrumento"),
	"MontoCompra" 						= COALESCE(:MontoCompra,"MontoCompra"),
	"MontoCompraMonedaInstrumento"		= COALESCE(:MontoCompraMonedaInstrumento,"MontoCompraMonedaInstrumento"),
	"MontoCompraMonedaAdicional" 		= COALESCE(:MontoCompraMonedaAdicional,"MontoCompraMonedaAdicional")
WHERE "IdMovimientoCarteraEnTransito" = :IdMovimientoCarteraEnTransito;
