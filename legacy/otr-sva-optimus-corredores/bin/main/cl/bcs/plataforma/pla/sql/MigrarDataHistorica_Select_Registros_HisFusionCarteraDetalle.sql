SELECT "IdCartera", detalle."IdFusion", "Tasa", "Cantidad", "ValorRescate", "CorteMinimo", "EsFungible"
FROM  "BSCL.Custodia.BD::HIS.FusionCarteraDetalle" detalle
JOIN "BSCL.Custodia.BD::HIS.FusionCartera" fuscar ON detalle."IdFusion" = fuscar."IdFusion"
WHERE CAST(fuscar."FechaFusion" AS DATE) BETWEEN :p_Desde AND :p_Hasta
ORDER BY "IdFusion", fuscar."FechaFusion" DESC
LIMIT :p_Limit OFFSET :p_Offset;