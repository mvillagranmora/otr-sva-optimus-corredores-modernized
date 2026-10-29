SELECT
      CM."IdCargoAbonoMovimiento"
     ,CM."FechaSistema"
     ,CM."FechaIngresoRegistro"
     ,CM."IdCuentaInversion"
     ,CM."CodigoCuentaInversion"
     ,CM."NombreCuentaInversion"
     ,CM."IdCliente"
     ,CM."NombreCliente"
     ,CM."IdPortafolio"
     ,CM."IdMoneda"
     ,CM."CodigoMoneda"
     ,CM."NombreMoneda"
     ,CM."SimboloMoneda"
     ,CM."DecimalesMoneda"
     ,CM."IdTipoMovimiento"
     ,CM."TipoMovimiento"
     ,CM."NombreTipoMovimiento"
     ,CM."IdConcepto"
     ,CM."NombreConcepto"
     ,CM."OrigenConcepto"
     ,CM."Descripcion"
     ,CM."IdCondicionDeLiquidacion"
     ,CM."CodigoCondicionDeLiquidacion"
     ,CM."NombreCondicionDeLiquidacion"
     ,CM."PlazoCondicionDeLiquidacion"
     ,CM."FechaEfectivaMovimiento"
     ,CM."Monto"
     ,CM."SaldoPorFechaEfectiva"
     ,CM."SaldoPorFechaMovimiento"
     ,CM."IdModulo"
     ,CM."IdTipoProducto"
     ,CM."CodigoTipoProducto"
     ,CM."NombreTipoProducto"
     ,CM."IdTipoOperacion"
     ,CM."CodigoTipoOperacion"
     ,CM."NombreTipoOperacion"
     ,CM."AccionTipoOperacion"
     ,CM."IdOperacion"
     ,CM."FechaOperacion"
     ,CM."FechaLiquidacionOperacion"
     ,CM."FechaVencimientoOperacion"
     ,CM."IdMonedaOperacion"
     ,CM."MontoOperacion"
     ,CM."TipoCambioOperacion"
     ,CM."IdTipoInstrumento"
     ,CM."NombreTipoInstrumento"
     ,CM."IdClasificacionTipoInstrumento"
     ,CM."NombreClasificacionTipoInstrumento"
     ,CM."IdInstrumento"
     ,CM."Cantidad"
     ,CM."NemotecnicoOriginal"
     ,CM."EsCargoAbonoPorTraspaso"
     ,CM."EsCargoAbonoPorTraspasoTraducido"
     ,CM."IdCargoAbonoRelacionadoTraspaso"
     ,CM."NombreClienteRelacionado"
     ,CM."CodigoPortafolioRelacionado"
     ,CM."IdTipoComprobante"
     ,CM."CodigoTipoComprobante"
     ,CM."NombreTipoComprobante"
     ,CM."IdComprobante"
     ,CM."FolioComprobante"
     ,CM."EsVentaCorta"
     ,CM."FechaComprobante"
     ,CM."IdComprobanteDetalle"
     ,CM."IdPersonaPagoCobro"
     ,CM."EsCertificadoAutomatico"
     ,CM."IdUsuarioCertificador"
     ,CM."FechaCertificacion"
     ,CM."IdContraparte"
     ,CM."EsIndicadorBilateral"
     ,CM."IdEmpresa"
     ,CM."CodigoEmpresa"
     ,CM."NombreEmpresa"
     ,CM."EsIngresoManual"
     ,CM."IdEvento"
     ,CM."IdUsuario"
     ,CM."NombreUsuario"
     ,CM."IdEstado"
     ,CM."NombreEstado"
     ,CM."IdCarga"
     ,CM."NombreUsuarioCert"
     ,CASE
          WHEN CM."IdTipoMovimiento" = 'MOVCAR' THEN CM."Monto"
          WHEN CM."IdTipoMovimiento" = 'MOVABN' THEN 0
      END AS "Cargo"
     ,CASE
          WHEN CM."IdTipoMovimiento" = 'MOVCAR' THEN 0
          WHEN CM."IdTipoMovimiento" = 'MOVABN' THEN CM."Monto"
      END AS "Abono"
     ,CAST(
          COALESCE(CMP."SaldoInicial", 0) +
          SUM(
              CASE
                  WHEN CM."IdEstado" = 'ANU' THEN 0
                  ELSE
                      CASE
                          WHEN CM."IdTipoMovimiento" = 'MOVCAR' THEN (-1 * CM."Monto")
                          WHEN CM."IdTipoMovimiento" = 'MOVABN' THEN CM."Monto"
                      END
              END
          ) OVER (
              PARTITION BY CM."IdCuentaInversion"
              ORDER BY
                  (CASE WHEN CMP."SaldoPorFechaEfectiva" = 'S' THEN CM."FechaEfectivaMovimiento" ELSE CM."FechaSistema" END),
                  (CASE WHEN CMP."SaldoPorFechaEfectiva" = 'S' THEN CM."FechaCertificacion" ELSE CM."FechaIngresoRegistro" END),
                  CM."IdCargoAbonoMovimiento"
              ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
          ) AS DECIMAL(26,8)
      ) AS "SaldoSimulado"
     ,CASE
          WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV", 0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV", 0)
          ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV", 0)
      END AS "IdMatrizAgrupador"
     ,CASE
          WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV", 0) != 0 THEN COALESCE(MAT."EsADC", 'N')
          ELSE COALESCE(MAT_NOAPV."EsADC", 'N')
      END AS "EsADC"
     ,CM."IdEstadoSII"
     ,CM."IdRespuestaDTE"
     ,COALESCE(ComprobanteFAC."MayorMenorValor", 0) AS "MayorMenorValor"
     ,COALESCE(ComprobanteFAC."MontoRetencionGananciaCap", 0) AS "MontoRetencionGananciaCap"

FROM "BSCL.CuentaInversion.BD::CTA.ConsultarMovimientoCuentaInversion_Proceso" AS CMP

INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" AS CM
    ON CM."IdPortafolio" = CMP."IdPortafolio"
   AND CM."IdMoneda" = CMP."IdMoneda"

LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
    ON Portafolio."IdPortafolio" = CMP."IdPortafolio"

LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MAT
    ON MAT."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"

LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" AS MAT_NOAPV
    ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio"
   AND Portafolio."IdMatrizAgrupadorAPV" = 0

LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS ComprobanteFAC
    ON ComprobanteFAC."IdComprobante" = CM."IdComprobante"

WHERE
    (CASE WHEN CMP."SaldoPorFechaEfectiva" = 'S' THEN CM."FechaEfectivaMovimiento" ELSE CM."FechaSistema" END)
        BETWEEN CMP."FechaDesde" AND CMP."FechaHasta"

    AND CASE
            WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV", 0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV", 0)
            ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV", 0)
        END
        =
        CASE
            WHEN CMP."IdMatrizAgrupador" IS NOT NULL THEN CMP."IdMatrizAgrupador"
            ELSE (
                CASE
                    WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV", 0) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV", 0)
                    ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV", 0)
                END
            )
        END

    AND CASE
            WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV", 0) != 0 THEN COALESCE(MAT."EsADC", 'N')
            ELSE COALESCE(MAT_NOAPV."EsADC", 'N')
        END
        =
        CASE
            WHEN CMP."EsADC" IS NOT NULL THEN CMP."EsADC"
            ELSE CASE
		          WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV", 0) != 0 THEN COALESCE(MAT."EsADC", 'N')
		          ELSE COALESCE(MAT_NOAPV."EsADC", 'N')
		      END
        END

    AND CM."IdEstado" = COALESCE(CMP."IdEstado", CM."IdEstado")

    AND CM."IdConcepto" NOT IN ('ABMIGI', 'CAMIGI', 'ABMIGR', 'ABAJUS', 'CAAJUS')

ORDER BY
    -- El orden depende de si la consulta es por Fecha Adquisicion o por Fecha Movimiento
    (CASE WHEN CMP."SaldoPorFechaEfectiva" = 'S' THEN CM."FechaEfectivaMovimiento" ELSE CM."FechaSistema" END),
    (CASE WHEN CMP."SaldoPorFechaEfectiva" = 'S' THEN CM."FechaCertificacion" ELSE CM."FechaSistema" END),
    "IdCargoAbonoMovimiento" ASC;