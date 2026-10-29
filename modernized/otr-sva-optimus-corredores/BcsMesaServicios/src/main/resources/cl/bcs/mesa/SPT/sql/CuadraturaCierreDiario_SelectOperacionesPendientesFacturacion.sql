SELECT 
    SPT."IdOperacion"
    ,SPT."FolioOperacion"
    ,SPT."NombreCliente" AS "RazonSocial"
    ,SPT."MontoMonedaPrincipal" AS "MontoOperacion"
    ,SPT."MontoMonedaSecundaria" AS "MontoOperacionMoneda"
    ,SPT."IdMonedaPrincipal" AS "IdMoneda"
    ,SPT."IdMonedaSecundaria" AS "IdMonedaLiquidacion"
    ,SPT."IdMesa" AS "IdMesaOrigen"
    ,SPT."Estado" AS "IdEstado"
    ,MFAC."IdEstadoFacturacion"
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" SPT
INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" MFAC
ON MFAC."IdMovimientoComprobante" = SPT."IdComprobanteFacturacion"
WHERE TO_VARCHAR(SPT."FechaOperacion",'YYYY-MM-DD') = :Fecha AND SPT."IdEmpresa" = :IdEmpresa
AND "EstadoConfirmacion" = 'CNF' AND SPT."Estado" <> 'ANU' AND SPT."OperacionNoFacturable" = 'N';
