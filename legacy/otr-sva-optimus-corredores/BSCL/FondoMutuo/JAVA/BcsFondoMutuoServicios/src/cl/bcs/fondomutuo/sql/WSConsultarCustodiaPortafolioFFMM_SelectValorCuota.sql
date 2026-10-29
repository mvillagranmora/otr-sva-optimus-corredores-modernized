SELECT
 "IdSerieFondo"
,TO_CHAR("FechaCierre") AS "FechaCierre"
,"ValorCuota"
FROM  "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuota"
WHERE "IdSerieFondo" IN (:IdSerieFondo);