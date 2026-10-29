SELECT "FolioCartera" AS "Valor5",
"IdClasificacionTipoInstrumento" AS "Valor1",
"IdTipoComprobante" AS "Valor2"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "EGRMonto" = 0
AND "EGRFechaOperacion" =  :p_FechaSistema;