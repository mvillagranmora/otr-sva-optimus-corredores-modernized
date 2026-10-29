SELECT 
       COUNT(*) AS "Validacion", 
       "Comprobante"."IdComprobante"
FROM "BSCL.Orden.BD::ORD.Transaccion" AS "Transaccion"
  LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS "ComprobanteDetalle"
    ON "Transaccion"."Folio" = "ComprobanteDetalle"."FolioTransaccion"
  LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS "Comprobante"
    ON "Comprobante"."IdComprobante" = "ComprobanteDetalle"."IdComprobante"
  LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS "Portafolio"
    ON "Portafolio"."IdPortafolio" = "Comprobante"."IdPortafolio"
  LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS "Indicador"
    ON "Indicador"."IdEntidad" = "Portafolio"."IdPortafolio"
WHERE "Comprobante"."IdComprobante" IN (:IdComprobante)
  AND "Indicador"."IdTipoIndicador" = 21
  AND "Indicador"."ValorIndicador" = '2'
  AND ("Transaccion"."IndicadorOrdenDirecta" = 'OE' OR "Transaccion"."IndicadorOrdenDirecta" = 'OD')
GROUP BY "Comprobante"."IdComprobante"; 