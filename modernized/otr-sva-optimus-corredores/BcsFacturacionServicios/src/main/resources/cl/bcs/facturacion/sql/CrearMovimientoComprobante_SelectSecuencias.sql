SELECT "IdCliente","IdPortafolio","SecuenciaAgrupacion",COUNT(1) AS "Cantidad"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MVTO
WHERE "SecuenciaAgrupacion" IN (:secuenciasAgrupacion) AND "SecuenciaAgrupacion" != '0' AND "SecuenciaAgrupacion" != ''
AND "IdEstadoFacturacion" = 'FAC'
GROUP BY "IdCliente" , "IdPortafolio","SecuenciaAgrupacion"