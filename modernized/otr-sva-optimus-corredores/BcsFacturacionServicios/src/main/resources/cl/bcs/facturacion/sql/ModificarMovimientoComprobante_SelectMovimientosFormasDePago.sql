SELECT PAG."IdMovimientoComprobanteFormaDePago", 
       PAG."IdMovimientoComprobante", 
       PAG."IdTipoMovimiento", 
       PAG."IdMedioDePago", 
       PAG."NombreMedioDePago", 
       COALESCE(PAG."IdCuentaBancaria",TO_BIGINT(0)) AS "IdCuentaBancaria",
       PAG."NombreCuentaBancaria", 
       PAG."IdTipoCuentaBancaria", 
       COALESCE(PAG."NombreTipoCuentaBancaria",'') AS "NombreTipoCuentaBancaria", 
       PAG."NumeroCuentaBancaria", 
       PAG."IdTarjetaBancaria", 
       PAG."IdTipoTarjetaBancaria", 
       COALESCE(PAG."NombreTipoTarjetaBancaria",'') AS "NombreTipoTarjetaBancaria", 
       PAG."NumeroTarjetaBancaria", 
       PAG."FechaVctoTarjetaBancaria", 
       PAG."NumeroSerie", 
       PAG."NumeroDocumento", 
       PAG."FechaDocumentoPago", 
       PAG."IdModalidadDePago", 
       PAG."NombreModalidadDePago", 
       PAG."IdBanco", 
       PAG."CodigoBanco", 
       PAG."NombreBanco", 
       PAG."IdMoneda", 
       PAG."MontoLiquidacionFormaDePago",       
       PAG."IdEstado", PAG."FechaIngresoRegistro", 
       PAG."IdTipoMovimientoSpot", 
       PAG."AfectarTesoreria", 
       'S' AS "AfectarCuentaInversion",
       COALESCE(SolApoRes."MontoComision",0) AS "MontoComision",
       COALESCE(OpeSpot."TipoCambioCierre",0) AS "TipoCambioCierre",
       COALESCE(MvtoCobro."MontoMovimientoComprobanteCobro", 0) AS "MontoCobro"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago" PAG
Left Join "BSCL.Plataforma.BD.Vistas::PAG.ConsultarMedioDePago" As ConsMedioPago On ConsMedioPago."IdMedioDePago" = PAG."IdMedioDePago"          
Left Join "BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago" As ConsModPag On ConsModPag."IdModalidadDePago" = PAG."IdModalidadDePago" And ConsModPag."IdMedioDePago" = PAG."IdMedioDePago"
Left Join "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco" As ConsBan On ConsBan."IdBanco" = PAG."IdBanco"
Inner Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" Mvto
   On Mvto."IdMovimientoComprobante" = PAG."IdMovimientoComprobante"
Left Join "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" MvtoCobro
   On MvtoCobro."IdMovimientoComprobante" = Mvto."IdMovimientoComprobante"
  And MvtoCobro."IdCobro" = 'IVA'   
Left Join "BSCL.Mesa.BD::SPT.OperacionSpot" OpeSpot
   On CAST (OpeSpot."IdOperacionSpotOrigen" AS varchar) = Mvto."FolioTransaccion"
  And OpeSpot."Estado" = 'VIG'
Left Join "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SolApoRes
   On SolApoRes."IdOperacionSpot" = CAST(OpeSpot."IdOperacionSpotOrigen" AS BIGINT)
  And SolApoRes."Estado" = 'CRT'  
WHERE PAG."IdMovimientoComprobante" IN (:idsMovimientoComprobante);