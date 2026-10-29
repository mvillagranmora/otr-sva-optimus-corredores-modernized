UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET 
	"INGMontoATasaMercadoActual" = COALESCE(:INGMontoATasaMercadoActual,"INGMontoATasaMercadoActual")  ------ ****************
	,"INGMontoMercadoActualMonedaInstrumento" = COALESCE(:INGMontoMercadoActualMonedaInstrumento,"INGMontoMercadoActualMonedaInstrumento")
	,"INGMontoMercadoActualMonedaAdicional" = COALESCE(:INGMontoMercadoActualMonedaAdicional,"INGMontoMercadoActualMonedaAdicional")
	,"INGPrecioMercadoActual" = COALESCE(:INGPrecioMercadoActual,"INGPrecioMercadoActual")
	,"INGFechaPrecioTasaMercadoActual" = COALESCE(:INGFechaPrecioTasaMercadoActual,"INGFechaPrecioTasaMercadoActual")
	,"INGFuenteTasaPrecioMercadoActual" = COALESCE(:INGFuenteTasaPrecioMercadoActual,"INGFuenteTasaPrecioMercadoActual") 
	,"INGMontoATasaCompraActual" = COALESCE(:INGMontoATasaCompraActual,"INGMontoATasaCompraActual")
	,"INGMontoCompraActualMonedaInstrumento" = COALESCE(:INGMontoCompraActualMonedaInstrumento,"INGMontoCompraActualMonedaInstrumento")
	,"EGRMontoATasaMercado" = COALESCE(:EGRMontoATasaMercado,"EGRMontoATasaMercado")
	,"EGRMontoMercadoMonedaInstrumento" = COALESCE(:EGRMontoMercadoMonedaInstrumento,"EGRMontoMercadoMonedaInstrumento")
	,"EGRMontoMercadoMonedaAdicional" = COALESCE(:EGRMontoMercadoMonedaAdicional,"EGRMontoMercadoMonedaAdicional")
	,"EGRPrecioMercado" = COALESCE(:EGRPrecioMercado,"EGRPrecioMercado")
	,"EGRFechaPrecioTasaMercado" = COALESCE(:EGRFechaPrecioTasaMercado,"EGRFechaPrecioTasaMercado")
	,"EGRFuenteTasaPrecioMercado" = COALESCE(:EGRFuenteTasaPrecioMercado,"EGRFuenteTasaPrecioMercado")
	,"EGRMonto" = COALESCE(:EGRMonto,"EGRMonto")
	,"EGRMontoMonedaInstrumento" = COALESCE(:EGRMontoMonedaInstrumento,"EGRMontoMonedaInstrumento")
	,"EGRMontoMonedaAdicional" = COALESCE(:EGRMontoMonedaAdicional,"EGRMontoMonedaAdicional")
WHERE "IdCartera" = :IdCartera;