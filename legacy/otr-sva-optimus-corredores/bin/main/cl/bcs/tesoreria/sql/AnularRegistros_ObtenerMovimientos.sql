SELECT CDO."IdIngresoEgreso" AS "Id", 'TES.IngresoEgreso' AS "Entidad"
FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS COM
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD ON (CD."IdComprobante" = COM."IdComprobante")
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO ON (CDO."IdComprobanteDetalle" = CD."IdComprobanteDetalle")
INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON (IE."IdIngresoEgreso" = CDO."IdIngresoEgreso")
WHERE COM."IdComprobante" IN(:p_comprobante)