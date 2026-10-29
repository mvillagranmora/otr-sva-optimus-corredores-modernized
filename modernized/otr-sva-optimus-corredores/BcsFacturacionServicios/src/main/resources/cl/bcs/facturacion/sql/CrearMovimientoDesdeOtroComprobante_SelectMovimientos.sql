SELECT  Mov."IdMovimientoComprobante" As "NumeroMvtoCmbte",
        Mov."IdEmpresa",
        Mov."IdConceptoMovimiento" AS "IdConcepto", 
        '' AS "SecuenciaAgrupacion", 
        To_Bigint(:idCliente) AS "IdCliente", 
        To_Bigint(:idPortafolio) AS "IdPortafolio", 
        Mov."ModPrefFact",
        Mov."IdEmpleadoDigitador", 
        Mov."IdInstrumento", 
        Mov."NemotecnicoInstrumento", 
        Mov."IdInstrumentoRelacionado", 
        Mov."IdInstrumentoSubyacente",
        Mov."IdTipoProducto", 
        Mov."IdMoneda", 
        Mov."IdMonedaSubyacente", 
        Mov."IdMonedaLiquidacion", 
        Mov."IdMonedaSpot", 
        Mov."IdTipoComprobante", 
        Mov."IdTipoOperacion",
        Mov."IdBoveda",
        Mov."IdCorredoraContraparte", 
        Mov."IdCondicionLiquidacion", 
        CASE 
          WHEN Mov."FechaLiquidacion" < :fechaSistema THEN CAST(:fechaSistema AS nvarchar(10)) 
        ELSE CAST(Mov."FechaLiquidacion" AS nvarchar(10))
        END AS "FechaLiquidacion", 
        Mov."IdBolsa", 
        Mov."IdTipoReajuste", 
        Mov."IdTipoConvenio",
        Mov."IdFormaCustodia", 
        Mov."IdEmisor", 
        Mov."IdOrden", 
        Mov."IdAsignacion", 
        Mov."IdAsignacionRef",
        Mov."IdTipoCanalIngreso",        
        CASE 
         WHEN Length(Mov."FechaOrden") = 0 THEN '0001-01-01'
        ELSE TO_NVARCHAR(COALESCE(Mov."FechaOrden",'0001-01-01'),'yyyy-MM-dd')
        END AS "FechaOrden",
        Mov."IdTransaccion", 
        Mov."FolioTransaccion",        
        CASE 
          WHEN Length(Mov."FechaTransaccion") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
        ELSE TO_NVARCHAR(COALESCE(Mov."FechaTransaccion",'0001-01-01'),'yyyy-MM-dd')
        END AS "FechaTransaccion",
        CAST(CASE
               WHEN CAST("FechaVencimiento" As Nvarchar(10)) = '0001-01-01' THEN "FechaVencimiento"
               WHEN "FechaVencimiento" < :fechaSistema THEN :fechaSistema
             ELSE "FechaVencimiento"
             END AS NVARCHAR(10)) AS "FechaVencimiento",
        Mov."PrecioAPlazo", 
        Mov."MontoAPlazo",
        CASE 
           WHEN Length("FechaParidad") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
        ELSE TO_NVARCHAR(COALESCE("FechaParidad",'0001-01-01'),'yyyy-MM-dd')
        END AS "FechaParidad",
        Mov."ValorParidad", 
        CASE 
           WHEN Length("FechaTipoCambio") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
        ELSE TO_NVARCHAR(COALESCE("FechaTipoCambio",'0001-01-01'),'yyyy-MM-dd')
        END AS "FechaTipoCambio", 
        Mov."ValorTipoCambio", 
        Mov."ValorCaptacionMonedaOrigen", 
        Mov."Tasa", 
        Mov."Cantidad", 
        Mov."Precio",
        Mov."Monto", 
        Mov."MontoMovimientoComprobante", 
        Mov."MontoLiquidacion", 
        Mov."MontoDescuento", 
        Mov."MontoInteres", 
        Mov."Glosa", 
        Mov."ValorRescate",
        Mov."TasaAgente", 
        Mov."Spread", 
        Mov."DiasPlazo", 
        :fechaSistema AS "FechaSistema", 
        Mov."IdMesa", 
        Mov."FolioCartera", 
        CASE 
           WHEN Length("FechaEmision") = 0 THEN TO_NVARCHAR('0001-01-01','yyyy-MM-dd')
        ELSE TO_NVARCHAR(COALESCE("FechaEmision",'0001-01-01'),'yyyy-MM-dd')
        END AS "FechaEmision",
        Mov."TasaEmision", 
        Mov."EsComprobIndividual", 
        Mov."EsCuentaPropia", 
        Mov."IndicadorOrdenDirecta", 
        Mov."EsVentaCorta", 
        Mov."EsPrecioPrima",
        Mov."EsEnRueda", 
        Mov."EsRuteo", 
        Mov."EsAPlazo", 
        Mov."EsFungible", 
        Mov."ActualizarCustodia", 
        Mov."ActualizarCuentaInversion", 
        Mov."ActualizarTesoreria",
        Mov."ActualizarCustodiaRelacionado", 
        :idsCrearComprobanteAutomatico AS "CrearComprobanteAutomatico", 
        Mov."NemoSVS", 
        To_Bigint(:idUsuario) AS "IdUsuario",
        CAST(:fechaSistema AS nvarchar(10)) As "FechaMovimientoComprobante",
        Mov."IdComprobante" As "IdComprobanteReferencia",
        Mov."IdTipoComprobante" As "IdTipoComprobanteReferencia"
FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" CompDet
  INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" MovCompDet 
     ON MovCompDet."IdComprobanteDetalle" = CompDet."IdComprobanteDetalle"
  INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" Mov 
     ON Mov."IdMovimientoComprobante" = MovCompDet."IdMovimientoComprobante"
WHERE CompDet."IdComprobante" IN (:idsComprobante);