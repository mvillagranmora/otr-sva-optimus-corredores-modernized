SELECT 
    SPT."IdOperacion"
    ,CASE WHEN COMTES."IdComprobante" IS NULL 
    	THEN TO_INT(IE."IdIngresoEgreso")
    	ELSE TO_INT(COMTES."IdComprobante") END AS "FolioOperacion"
    ,SPT."NombreCliente" AS "RazonSocial"
    ,CASE WHEN COMTES."IdComprobante" IS NULL 
    	THEN IE."Monto"
    	ELSE COMTES."Monto" END AS "MontoOperacion"
    ,SPT."MontoMonedaSecundaria" AS "MontoOperacionMoneda"
    ,SPT."IdMonedaPrincipal" AS "IdMoneda"
    ,SPT."IdMonedaSecundaria" AS "IdMonedaLiquidacion"
    ,SPT."IdMesa" AS "IdMesaOrigen"
    ,CASE WHEN COMTES."IdEstado" IS NULL 
    	THEN IE."IdEstado"
    	ELSE COMTES."IdEstado" END AS "IdEstado"
    ,MFAC."IdEstadoFacturacion"
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarMovimientosSpot" SPT
INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" MFAC
ON MFAC."IdTransaccion" = SPT."IdOperacion"
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COMFAC
ON COMFAC."IdComprobante" = MFAC."IdComprobante"
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" IE
ON IE."IdComprobanteOrigen" = COMFAC."IdComprobante"
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" CDO 
ON CDO."IdIngresoEgreso" = IE."IdIngresoEgreso"
LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" CD 
ON CD."IdComprobanteDetalle" = CDO."IdComprobanteDetalle"
LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" COMTES
ON COMTES."IdComprobante" = CD."IdComprobante"
AND COMTES."IdEstado" <> 'ANU'
WHERE "OperacionNoFacturable" = 'N' AND CASE WHEN COMTES."IdComprobante" IS NULL 
    	THEN TO_VARCHAR(IE."FechaLiquidacion",'YYYY-MM-DD')
    	ELSE TO_VARCHAR(COMTES."FechaLiquidacion",'YYYY-MM-DD') END = :Fecha
AND SPT."IdEmpresa" = :IdEmpresa
AND SPT."EstadoConfirmacion" = 'CNF';