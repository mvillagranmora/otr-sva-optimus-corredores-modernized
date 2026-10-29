SELECT "IdOrdenAsociada","IdOrdenM" AS "IdOrdenReportante",RTDO."IdOrden" AS "IdOrdenReportado",RTDO."TasaPromedioReporte"
FROM "BSCL.Orden.BD::ORD.OrdenAsociada" ASOC
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" RTDO ON ASOC."IdOrdenN" = RTDO."IdOrden"
WHERE "IdOrdenM" IN (:idsOrdenAsociada);