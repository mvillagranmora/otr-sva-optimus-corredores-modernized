--revisar el caso de nominales
SELECT 
     ope."IdOperacion"
    ,ope."IdTipoOperacion"
    ,ope."IdCliente"
    ,ope."IdPortafolio"
    ,ope."IdClienteCorredor"
    ,ope."IdPortafolioCorredor"
    ,ope."FolioCartera" AS "FolioCarteraPacto"
    ,ope."IdTipoProducto"
    ,car."IdCartera" AS "IdCarteraPacto"
    ,ope."IdInstrumento" AS "IdInstrumentoPacto"
    ,car."INGValorRescate" AS "ValorRescatePacto"
    ,car."FechaLiquidacion"
    ,car."EsFungible"
    ,car."IdEmisor"
    ,car."IdMonedaEmision"
    ,car."IdTipoReajuste"
    ,car."IdMonedaInstrumento"
    ,car."INGValorRescate"
    ,car."INGCantidad"
    ,car."IdOperacion"
    ,OPE."Tasa" AS "TasaOperacion"
    ,ope."MontoOperacionInicial" AS "MontoInicial"
    ,ope."MontoOperacionFinal" AS "MontoFinal"
    ,ope."MontoOperacionFinalMoneda" AS "MontoFinalMoneda"
    ,ope."MontoOperacionInicialMoneda" AS "MontoInicialMoneda"
FROM  "BSCL.Mesa.BD::MES.Operacion" ope
    INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" car ON (ope."FolioCartera" = car."FolioCartera" AND ope."IdOperacion" = car."IdOperacion")
WHERE ope."IdOperacion" IN (:IdOperacion) AND ope."IdEstado"='VIG'
AND ope."IdEstado"='VIG' 
AND ope."FechaVencimiento"<=(:FechaVencimiento);
