SELECT
  COB."IdComprobante",
  COB."NombreCobro",
  COB."FormaCobro",
  COB."Porcentaje",
  COB."CodigoMonedaLocalCobro",
  COB."MontoLocalCobro",
  COB."CodigoMonedaLiquidacionCobro",
  COB."MontoLiquidacionCobro"
FROM
  "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobros" AS COB
  INNER JOIN #WsConsultaComprobantes_IdComprobantes AS COM ON COM."IdComprobante" = COB."IdComprobante"
ORDER BY
  COB."IdComprobante" ASC
