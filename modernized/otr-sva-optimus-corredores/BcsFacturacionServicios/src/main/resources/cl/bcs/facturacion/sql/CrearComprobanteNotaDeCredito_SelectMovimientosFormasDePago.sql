SELECT "IdMovimientoComprobanteFormaDePago" AS "IdMovimientoComprobanteFormaDePagoOld",
       ConsMvtoCmbteFDPag."IdMovimientoComprobante" AS "NumeroMvtoCmbte",
       ConsMvtoCmbteFDPag."IdMovimientoComprobanteFormaDePago" AS "NumeroMvtoCmbteFormaDePago",
       ConsMvtoCmbteFDPag."IdTipoMovimiento",
       ConsMvtoCmbteFDPag."IdMedioDePago", 
       ConsMvtoCmbteFDPag."NombreMedioDePago", 
       ConsMvtoCmbteFDPag."NumeroDocumento",
       ConsMvtoCmbteFDPag."IdCuentaBancaria", 
       ConsMvtoCmbteFDPag."NombreCuentaBancaria", 
       ConsMvtoCmbteFDPag."IdTipoCuentaBancaria",
       ConsMvtoCmbteFDPag."NombreTipoCuentaBancaria", 
       ConsMvtoCmbteFDPag."NumeroCuentaBancaria", 
       ConsMvtoCmbteFDPag."NumeroSerie", 
       ConsMvtoCmbteFDPag."IdTarjetaBancaria",
       ConsMvtoCmbteFDPag."IdTipoTarjetaBancaria", 
       ConsMvtoCmbteFDPag."NombreTipoTarjetaBancaria", 
       ConsMvtoCmbteFDPag."NumeroTarjetaBancaria",
       ConsMvtoCmbteFDPag."IdModalidadDePago", 
       ConsMvtoCmbteFDPag."NombreModalidadDePago", 
       ConsMvtoCmbteFDPag."IdBanco", 
       ConsMvtoCmbteFDPag."CodigoBanco",
       ConsMvtoCmbteFDPag."NombreBanco", 
       CASE 
         WHEN Length("FechaDocumentoPago") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
       ELSE TO_NVARCHAR(COALESCE("FechaDocumentoPago",'0001-01-01'),'yyyy-MM-dd')
       END AS "FechaDocumentoPago", 
       ConsMvtoCmbteFDPag."MontoLiquidacionFormaDePago", 
       ConsMvtoCmbteFDPag."IdEstado",
       ConsMvtoCmbteFDPag."IdMoneda",
       CASE 
         WHEN ConsMvtoCmbteFDPag."IdTipoMovimientoSpot" = 'MOVEGR' THEN 'MOVING'
         WHEN ConsMvtoCmbteFDPag."IdTipoMovimientoSpot" = 'MOVING' THEN 'MOVEGR'
       ELSE ''			
       END AS "IdTipoMovimientoSpot",       
       CASE 
         WHEN Length("FechaVctoTarjetaBancaria") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
       ELSE TO_NVARCHAR(COALESCE("FechaVctoTarjetaBancaria",'0001-01-01'),'yyyy-MM-dd')
       END AS "FechaVctoTarjetaBancaria",
       CASE 
         WHEN ConsMvtoCmbteFDPag."AfectarTesoreria" = NULL THEN 'S'
         WHEN ConsMvtoCmbteFDPag."AfectarTesoreria" = '' THEN 'S'
       ELSE ConsMvtoCmbteFDPag."AfectarTesoreria"
       END AS "AfectarTesoreria",
       CASE 
         WHEN ConsMvtoCmbteFDPag."AfectarCuentaInversion" = NULL THEN 'S'
         WHEN ConsMvtoCmbteFDPag."AfectarCuentaInversion" = '' THEN 'S'
       ELSE ConsMvtoCmbteFDPag."AfectarCuentaInversion"
       END AS "AfectarCuentaInversion"
       
FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFormaDePago" AS ConsMvtoCmbteFDPag
WHERE ConsMvtoCmbteFDPag."IdMovimientoComprobante" IN (:idsMovimientoComprobanteOld)
  AND ConsMvtoCmbteFDPag."IdEstado" = 'VIG';
