SELECT 
"IdSerieFondo"
,"FechaVigencia"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaVigente"
WHERE "IdSerieFondo" IN (:IdSerieFondo)
AND "FechaVigencia" IN (:FechaVigencia);