
UPDATE "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" 						
SET "PrecioCompra" = :PrecioCompraNuevo,					
	"MontoCompra" = :MontoCompra,			
	"MontoCompraMonedaInstrumento" =  :MontoCompraMonedaInstrumento, 					
	"MontoCompraMonedaAdicional" = 	:MontoCompraMonedaAdicional
WHERE "IdMovimientoCarteraEnTransito" = :IdMovimientoCarteraEnTransito;