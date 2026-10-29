UPDATE "BSCL.Mesa.BD::MES.InstrumentoMoneda"
SET "IdInstrumento" = :IdInstrumento, "Nemotecnico" = :Nemotecnico, "IdTipoProducto" = :IdTipoProducto, "IdTipoInstrumento" = :IdTipoInstrumento, 
"IdMonedaPrincipal" = :IdMonedaPrincipal, "IdMonedaSecundaria" = :IdMonedaSecundaria, "IdEstado" = :IdEstado, "FeriadoMonedaPrincipal" = :FeriadoMonedaPrincipal
WHERE "IdInstrumentoMoneda" = :IdInstrumentoMoneda;