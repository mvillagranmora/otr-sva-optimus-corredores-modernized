SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", 
"IdComprobante" AS "IdMovimiento",
'MONTO_OPERACION' AS "IdEvento", 
'Monto Operación' AS "Nombre", 
IFNULL("MontoComprobante",0) AS "MontoEvento", 
"IdMonedaComprobante" AS  "CodigoMoneda"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"
WHERE "FechaSistema" = :FechaContable AND 
"DocumentoIdentidad" != :DocumentoIdentidad AND 
"IdEstadoComprobante" <> 'ANU' AND
"EsVentaCorta" = 'S' AND "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
UNION  ALL
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", 
"IdComprobante" AS "IdMovimiento",
'COMISION' AS "IdEvento", 
'Comisión' AS "Nombre", 
IFNULL("TotalComisCmbte" ,0) AS "MontoEvento", 
"IdMonedaComprobante" AS  "CodigoMoneda"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"
WHERE "FechaSistema" = :FechaContable AND 
"DocumentoIdentidad" != :DocumentoIdentidad AND 
"IdEstadoComprobante" <> 'ANU' AND
"EsVentaCorta" = 'S' AND "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
UNION ALL
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", 
"IdComprobante" AS "IdMovimiento",
'IMPUESTO' AS "IdEvento", 
'Impuesto' AS "Nombre", 
IFNULL("TotalImptoCmbte" ,0) AS "MontoEvento", 
"IdMonedaComprobante" AS  "CodigoMoneda"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"
WHERE "FechaSistema" = :FechaContable AND 
"DocumentoIdentidad" != :DocumentoIdentidad AND 
"IdEstadoComprobante" <> 'ANU' AND
"EsVentaCorta" = 'S' AND "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
UNION ALL
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", 
"IdComprobante" AS "IdMovimiento",
'MONTO TOTAL' AS "IdEvento", 
'Monto Total' AS "Nombre", 
IFNULL("MontoLiquidacionFinal" ,0) AS "MontoEvento", 
"IdMonedaLiquidacion"  AS  "CodigoMoneda"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"
WHERE "FechaSistema" = :FechaContable AND 
"DocumentoIdentidad" != :DocumentoIdentidad AND 
"IdEstadoComprobante" <> 'ANU' AND
"EsVentaCorta" = 'S' AND "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento