SELECT 
    "IdComprobante" AS "NumeroReferencia",
    "IdMedioDePago",
    "IdModalidadDePago",
    "IdMoneda",
    "Monto"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago"
WHERE "IdComprobante" IN (:IdComprobante)
GROUP BY 
    "IdComprobante",
    "IdMedioDePago",
    "IdModalidadDePago",
    "IdMoneda",
    "Monto"