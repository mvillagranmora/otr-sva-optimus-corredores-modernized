SELECT MvtoCmbte."IdMovimientoComprobante" As "NumeroMvtoCmbte",
       "IdEmpresa",
		CASE 
			WHEN MvtoCmbte."IdTipoProducto" = 'MEME' THEN ConcepFact."IdConcepto"
            WHEN (MvtoCmbte."IdTipoComprobante" = 'FOC' OR MvtoCmbte."IdTipoComprobante" = 'FOE' OR MvtoCmbte."IdTipoComprobante" = 'FOX' OR MvtoCmbte."IdTipoComprobante" = 'FEE' OR MvtoCmbte."IdTipoComprobante" = 'BOE') 
				Then MvtoCmbte."IdConceptoMovimiento" 
			ELSE ConcepFact."IdConcepto"
       END As "IdConcepto",
       '' As "SecuenciaAgrupacion", 
       MvtoCmbte."IdCliente", 
       MvtoCmbte."IdPortafolio", 
       "IdEmpleadoDigitador", 
       "IdSucursalEmpleadoDigitador", 
       "IdEmpleadoEjecutivoPortafolio", 
       "IdSucursalEmpleadoEjecutivoPortafolio",
       MvtoCmbte."IdInstrumento", 
       COALESCE(MvtoCmbte."IdInstrumentoSubyacente", 0) AS "IdInstrumentoSubyacente", 
       MvtoCmbte."IdTipoInstrumento", 
       MvtoCmbte."IdTipoInstrumentoSubyacente",
       MvtoCmbte."IdClasificacionTipoInstrumento", 
       MvtoCmbte."IdTipoProducto", 
       "IdMoneda",
       CASE 
       	 WHEN MvtoCmbte."IdTipoProducto" = 'MEME' THEN 'NEE' --para Spot siempre NEE (IMPMBIOPT-748)
         WHEN (MvtoCmbte."IdTipoComprobante" = 'FAC') THEN 'NCR'
         WHEN (MvtoCmbte."IdTipoComprobante" = 'POL') THEN 'NCR'
         WHEN (MvtoCmbte."IdTipoComprobante" = 'FOC') THEN 'NCO'
         WHEN (MvtoCmbte."IdTipoComprobante" = 'FAE') THEN 'NCE'
         WHEN (MvtoCmbte."IdTipoComprobante" = 'FOX' OR MvtoCmbte."IdTipoComprobante" = 'FEE') Then 'NEE'
         WHEN (MvtoCmbte."IdTipoComprobante" = 'FOE' OR MvtoCmbte."IdTipoComprobante" = 'BOE') Then 'NEO'
       END AS "IdTipoComprobante",
       0 AS "IdComprobante",
       TipoOperacion."IdTipoOperacionAnulacion" AS "IdTipoOperacion",
       "IdMovimientoRelacionado", 
       "IdBoveda", 
       "IdCorredoraContraparte",
       CASE 
         WHEN "FechaLiquidacion" < :idsfechaSistema THEN CAST(:idsfechaSistema AS nvarchar(10)) 
       ELSE CAST("FechaLiquidacion" AS nvarchar(10))
       END AS "FechaLiquidacion",       
       CAST(CASE
         WHEN CAST("FechaVencimiento" As Nvarchar(10)) = '0001-01-01' THEN "FechaVencimiento"
         WHEN "FechaVencimiento" < :idsfechaSistema THEN :idsfechaSistema
       ELSE "FechaVencimiento"
       END AS NVARCHAR(10)) AS "FechaVencimiento",
       "IdBolsa", 
       "IdTipoReajuste", 
       "IdTipoConvenio", 
       "IdFormaCustodia",
       "IdEmisor", 
       'ING' As "IdEstadoFacturacion", 
       "IdOrden", 
       "IdAsignacion",
       "IdTipoCanalIngreso",       
       CASE 
         WHEN Length("FechaOrden") = 0 THEN '0001-01-01'
       ELSE TO_NVARCHAR(COALESCE("FechaOrden",'0001-01-01'),'yyyy-MM-dd')
       END AS "FechaOrden",       
       "IdTransaccion", 
       "FolioTransaccion", 
       CASE 
         WHEN Length("FechaTransaccion") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
       ELSE TO_NVARCHAR(COALESCE("FechaTransaccion",'0001-01-01'),'yyyy-MM-dd')
       END AS "FechaTransaccion",
       CASE 
         WHEN Length("FechaParidad") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
       ELSE TO_NVARCHAR(COALESCE("FechaParidad",'0001-01-01'),'yyyy-MM-dd')
       END AS "FechaParidad",       
       "ValorParidad", 
       "ValorCaptacionMonedaOrigen", 
       0 As "FolioComprobante", 
       "Tasa", 
       "Cantidad", 
       "Precio", 
       "Monto", 
       "MontoDescuento",
       "MontoInteres",
       CASE
         WHEN length(MvtoCmbte."Glosa") > 0 THEN MvtoCmbte."Glosa"
       ELSE ('Generado desde movimiento comprobante ' || MvtoCmbte."IdMovimientoComprobante")
       END As "Glosa",
       "ValorRescate", 
       "TasaAgente", 
       "Spread", 
       "DiasPlazo", 
       "CodigoCanalIngreso", 
       "DocumentoIdentidad", 
       "TipoDocumentoIdentidad", 
       "NombreLargoCliente", 
       "DireccionFacturacion",
       "NombreEmpleadoDigitador", 
       "NombreSucursalEmpleadoDigitador", 
       "NombreEmpleadoEjecutivoPortafolio", 
       "NombreSucursalEmpleadoEjecutivoPortafolio",
       "FolioCartera", 
       "CodigoTipoProducto", 
       "CodigoTipoOperacion", 
       "CodigoBolsa", 
       "NombreTipoInstrumento",
       "NombreTipoInstrumentoSubyacente", 
       "NombreClasificacionTipoInstrumento", 
       "NemotecnicoInstrumento", 
       "NemotecnicoSubyacente", 
       "RazonSocialEmisor",       
       CASE 
         WHEN Length("FechaEmision") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
       ELSE TO_NVARCHAR(COALESCE("FechaEmision",'0001-01-01'),'yyyy-MM-dd')
       END AS "FechaEmision",       
       "TasaEmision",
       "TasaOperacion",
       CURRENT_UTCTIMESTAMP As "FechaIngresoRegistro", 
       :idsfechaSistema AS "FechaSistema", 
       "EsComprobIndividual", 
       "EsCuentaPropia",
       "IndicadorOrdenDirecta", 
       "EsPrecioPrima", 
       "EsEnRueda", 
       "EsRuteo", 
       "EsIngresoAutomatico", 
       "IdUsuario",
       "IdUsuarioBloqueo", 
       "IdCarga", 
       "EsAPlazo", 
       "MontoAPlazo", 
       "PrecioAPlazo", 
       "IdMovimientoOdRelacionado", 
       "IdMonedaSubyacente",
       "IdMonedaLiquidacion", 
       "ValorTipoCambio", 
       MvtoCmbte."EsVentaCorta", 
       MvtoCmbte."IdComprobante" As "IdComprobanteReferencia",
       MvtoCmbte."IdTipoComprobante" As "IdTipoComprobanteReferencia", 
       MvtoCmbte."FolioComprobante" As "FolioComprobanteReferencia",
       MvtoCmbte."FechaIngresoRegistro" As "FechaComprobanteReferencia", 
       "SecMovimientoFacturarMigracion",
       "ActualizarCustodia", 
       "ActualizarCuentaInversion", 
       "ActualizarTesoreria", 
       CAST(:idsfechaSistema AS nvarchar(10)) As "FechaMovimientoComprobante", 
       CASE 
         WHEN Length("FechaTipoCambio") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
       ELSE TO_NVARCHAR(COALESCE("FechaTipoCambio",'0001-01-01'),'yyyy-MM-dd')
       END AS "FechaTipoCambio",       
       "ActualizarCustodiaRelacionado",
       "IdMonedaMovimientoComprobante", 
       "MontoMovimientoComprobante", 
       "MontoLiquidacion", 
       "IdMonedaSpot",
       "EsFungible",
       :idsCrearComprobanteAutomatico AS "CrearComprobanteAutomatico",
       CASE 
         WHEN MvtoCmbte."IdCondicionLiquidacion" = 'OT' THEN MvtoCmbte."IdCondicionLiquidacion"
         WHEN :idsfechaDiaHabilSiguiente = "FechaLiquidacion" THEN 'T1'
         WHEN DAYS_BETWEEN(:idsfechaSistema, "FechaLiquidacion") < 0 THEN 'T0' 
         WHEN DAYS_BETWEEN(:idsfechaSistema, "FechaLiquidacion") = 0 THEN 'T0'
         WHEN DAYS_BETWEEN(:idsfechaSistema, "FechaLiquidacion") = 1 THEN 'T1'      
       ELSE 'T2'
       END AS "IdCondicionLiquidacion",
       "IdInstrumentoRelacionado",
       "NemoSVS",
       MCR."Referencia",
       MC."TipoReferencia"
From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
  Left Join "BSCL.Plataforma.BD::INS.TipoOperacion" TipoOperacion
    On TipoOperacion."IdTipoOperacion" = MvtoCmbte."IdTipoOperacion"
  Left Join "BSCL.Facturacion.BD.Vistas::ConsultarConceptosFacturacion" ConcepFact
    On ConcepFact."IdTipoOperacion" = TipoOperacion."IdTipoOperacionAnulacion"
   And TipoOperacion."IdTipoOperacionAnulacion" <> ''
  Left Join "BSCL.Facturacion.BD::FAC.MovimientoComprobanteReferencia" MCR
  	On MvtoCmbte."IdMovimientoComprobante" = MCR."IdMovimientoComprobante"
  Left Join "BSCL.Facturacion.BD::FAC.ReferenciaDocumento" MC
  	On MCR."TipoReferencia" = MC."TipoReferencia"
WHERE MvtoCmbte."IdComprobante" IN (:idsComprobante);