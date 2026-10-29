--revisar el caso de nominales
SELECT 
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
    res."FolioCarteraOrigen" AS "FolioCarteraOrigenDetalle",
    res."IdCarteraOrigen" AS "IdCarteraOrigenDetalle",
    car."IdCartera" AS "IdCarteraDestinoDetalle",
    car."FolioCartera" AS "FolioCarteraDestinoDetalle",
    car."INGCantidad" AS "CantidadCarteraDestinoDetalle",
    car."INGValorRescate" AS "ValorRescateDestinoDetalle",
    car."FechaLiquidacion",
    car."EsFungible",
    car."IdEmisor",
    CAR."IdMonedaEmision",
    CAR."IdTipoReajuste",
    CAR."IdMonedaInstrumento",
    CAR."FechaVencimientoInstrumento"
FROM  "BSCL.Mesa.BD::MES.Operacion" ope
    INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" res ON (ope."IdOperacion"= res."IdOperacion" AND res."IdEstado" IN ('VIG','LIQ')) --res."FolioCarteraOrigen" = car."FolioCarteraRelacionada"
    --INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" car ON ( res."IdCarteraOrigen"=car."IdCartera" AND car."EGRIdOperacion"=ope."IdOperacion")
    INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" car ON ( ope."IdOperacion" = car."FolioComprobante"/* AND res."IdInstrumento"= car."IdInstrumento"*/ AND res."FolioCarteraCustodia" = car."FolioCartera" AND car."IdTipoInstrumento" != 'PAC' AND car."IdEstado" = 'VIG')
WHERE ope."IdOperacion" IN (:IdOperacion) AND ope."IdEstado"='VIG'--(1269)


--(1243)(1268)

 /*
SELECT * FROM "BSCL.Custodia.BD::CUS.Subyacente" WHERE "FolioCarteraRelacionada"=20683831
SELECT * FROM "BSCL.Custodia.BD::CUS.Cartera" WHERE "FolioCartera"=20683880
20683452
SELECT * FROM "BSCL.Mesa.BD::MES.Operacion"*/
