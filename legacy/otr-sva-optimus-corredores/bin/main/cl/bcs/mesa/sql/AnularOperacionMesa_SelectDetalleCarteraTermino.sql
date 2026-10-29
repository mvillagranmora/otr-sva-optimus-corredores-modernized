
SELECT

    car."IdEstado" AS "IdEstadoCartera",
    res."FolioCarteraOrigen" AS "FolioCarteraOrigenDetalle",
    res."IdCarteraOrigen" AS "IdCarteraOrigenDetalle",
	car."INGTasaCompra" as "TasaCompra",
	car."PrecioCompra",  
    ope."IdOperacion",
    ope."IdTipoOperacion",
    ope."IdCliente",
    ope."IdPortafolio",
    ope."IdClienteCorredor",
    ope."IdPortafolioCorredor",
    ope."FolioCartera" AS "FolioCarteraPacto",
    ope."IdInstrumento" AS "IdInstrumentoPacto",
    ope."IdTipoProducto",
    res."IdReserva",
    res."IdInstrumento" AS "IdInstrumentoDetalle" ,
    res."IngCantidad" AS "CantidadDetalle",
    res."IngValorRescate" AS "ValorRescateDetalle", 
    res."IngMontoValorizado" AS "MontoValorizadoDetalle",   
    car."IdCartera" AS "IdCarteraDestinoDetalle",
    car."FolioCartera" AS "FolioCarteraDestinoDetalle",
    car."INGCantidad" AS "CantidadCarteraDestinoDetalle",
    car."INGValorRescate" AS "ValorRescateDestinoDetalle",
    car."FechaLiquidacion",
    car."EsFungible",
    car."IdEmisor",
    car."IdMonedaEmision",
    car."IdTipoReajuste",
    car."IdMonedaInstrumento",
    car."FechaVencimientoInstrumento",
    car."IdClasificacionTipoInstrumento"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS car 
LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva"AS res ON car."FolioCartera" = res."FolioCarteraOrigen"
LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS ope ON ope."IdOperacion"= res."IdOperacion"
WHERE car."FolioCartera" IN (:FolioCartera) AND car."IdEstado"='VIG' AND ope."IdEstado"='VIG'