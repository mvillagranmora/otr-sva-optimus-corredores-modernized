SELECT
    CAR."IdCliente" || '_' || CAR."IdEmisor" || '_' || CAR."IdMonedaInstrumento" AS "Id",
    CAR."IdCliente" || '_' || CAR."IdEmisor" AS "IdClienteEmisor",
    CAR."IdCliente",
    CAR."IdEmisor",
    CAR."IdMonedaInstrumento",
    COALESCE(SUM(VC."ValorCuota" * CAR."INGCantidad"), SUM(CAR."INGMontoMercadoActualMonedaInstrumento")) AS "Monto",
    SUM(CAR."INGCantidad") AS "Cantidad"
FROM "BSCL.Orden.BD::GBL.ValidaPatrimonioEmisor_Proceso_Paso" POR
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR
    ON POR."IdCliente" = CAR."IdCliente"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SER
    ON CAR."IdInstrumento" = SER."IdInstrumento"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" VC
    ON VC."IdSerieFondo" = CAR."Nemotecnico"
WHERE "FechaLiquidacion" <= :Fecha
    AND CAR."IdEstado" = 'VIG'
GROUP BY CAR."IdCliente", CAR."IdEmisor", CAR."IdMonedaInstrumento";
