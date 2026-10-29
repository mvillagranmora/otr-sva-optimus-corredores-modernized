SELECT fpag."IdMovimientoComprobanteFormaDePago" AS "IdMovimientoComprobanteFormaDePagoOld",
       fpag."IdMovimientoComprobante" AS "NumeroMvtoCmbte",
       fpag."IdMovimientoComprobanteFormaDePago" AS "NumeroMvtoCmbteFormaDePago",
       fpag."IdTipoMovimiento",
       fpag."IdMedioDePago", 
       fpag."NumeroSerie",
       fpag."NumeroDocumento", 
       fpag."IdCuentaBancaria", 
       fpag."IdTipoCuentaBancaria", 
       fpag."NumeroCuentaBancaria", 
       fpag."IdTarjetaBancaria",
	   fpag."IdTipoTarjetaBancaria", 
	   fpag."NumeroTarjetaBancaria", 
	   fpag."IdModalidadDePago", 
	   fpag."IdBanco", 
	   fpag."IdMoneda",
	   CASE 
         WHEN Length(fpag."FechaDocumentoPago") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
       ELSE TO_NVARCHAR(COALESCE(fpag."FechaDocumentoPago",'0001-01-01'),'yyyy-MM-dd')
       END AS "FechaDocumentoPago",
	   fpag."MontoLiquidacionFormaDePago", 
	   fpag."IdTipoMovimientoSpot", 
	   fpag."AfectarTesoreria",
	   fpag."AfectarCuentaInversion",
	   fpag."IdMovimientoComprobanteFormaDePago" AS "IdMovimientoComprobanteFormaDePagoOld",
       fpag."IdMovimientoComprobante" AS "NumeroMvtoCmbte",
       fpag."IdMovimientoComprobanteFormaDePago" AS "NumeroMvtoCmbteFormaDePago",
       fpag."IdTipoMovimiento",
       fpag."IdMedioDePago", 
       fpag."NombreMedioDePago", 
       fpag."NumeroDocumento",
       fpag."IdCuentaBancaria", 
       fpag."NombreCuentaBancaria", 
       fpag."IdTipoCuentaBancaria",
       fpag."NombreTipoCuentaBancaria", 
       fpag."NumeroCuentaBancaria", 
       fpag."IdTarjetaBancaria",
       fpag."IdTipoTarjetaBancaria", 
       fpag."NombreTipoTarjetaBancaria", 
       fpag."NumeroTarjetaBancaria",
       fpag."IdModalidadDePago", 
       fpag."NombreModalidadDePago", 
       fpag."IdBanco", 
       fpag."CodigoBanco",
       fpag."NombreBanco", 
       CASE 
         WHEN Length(fpag."FechaDocumentoPago") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
       ELSE TO_NVARCHAR(COALESCE(fpag."FechaDocumentoPago",'0001-01-01'),'yyyy-MM-dd')
       END AS "FechaDocumentoPago", 
       fpag."MontoLiquidacionFormaDePago", 
       fpag."IdEstado",
       fpag."IdMoneda",
       CASE 
         WHEN Length("FechaVctoTarjetaBancaria") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
       ELSE TO_NVARCHAR(COALESCE("FechaVctoTarjetaBancaria",'0001-01-01'),'yyyy-MM-dd')
       END AS "FechaVctoTarjetaBancaria"
	   
FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFormaDePago" AS fpag
WHERE fpag."IdMovimientoComprobante" IN (:idsMovimientoComprobanteOld)
  AND fpag."IdEstado" = 'VIG';