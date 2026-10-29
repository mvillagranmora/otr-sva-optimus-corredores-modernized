SELECT INSMON."IdInstrumentoMoneda"
FROM "BSCL.Mesa.BD::MES.InstrumentoMoneda" as INSMON
WHERE :IdInstrumento="IdInstrumento" AND :IdMonedaPrincipal='USD' AND :IdMonedaSecundaria='CLP' AND "IdEstado"='ACT' AND "FeriadoMonedaPrincipal" = 'S'; 