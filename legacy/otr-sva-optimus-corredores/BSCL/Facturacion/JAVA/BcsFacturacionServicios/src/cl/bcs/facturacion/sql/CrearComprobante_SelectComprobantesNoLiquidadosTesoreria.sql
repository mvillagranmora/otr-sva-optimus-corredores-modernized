SELECT DISTINCT
    "IdComprobante",
    MAX(TO_VARCHAR("FechaComprobanteOrigen", 'YYYY-MM-DD')) AS "FechaComprobanteOrigen"
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
WHERE "IdEstado" NOT IN ('LIQ','ANU')
AND "IdComprobanteOrigen" IN (:idsComprobanteOrigen)
AND "IdComprobanteOrigen" <> 0
GROUP BY "IdComprobante";