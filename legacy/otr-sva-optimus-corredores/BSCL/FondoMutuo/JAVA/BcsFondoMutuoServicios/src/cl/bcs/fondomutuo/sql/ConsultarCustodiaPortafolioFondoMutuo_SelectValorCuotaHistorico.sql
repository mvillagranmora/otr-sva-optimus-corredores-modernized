SELECT 
"IdSerieFondo"
,"ValorCuota"
,TO_VARCHAR("FechaValorCuota", 'YYYY-MM-DD') AS "FechaValorCuota"
FROM (SELECT 
RANK() over (partition by  VCH."IdSerieFondo" order by VCH."IdValorCuotaHistorico" desc) as "RANK",
VCH."IdSerieFondo",
VCH."ValorCuota" AS "ValorCuota",
"FechaCierre" AS "FechaValorCuota"
FROM "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" VCH
WHERE VCH."FechaCierre" = :FechaConsulta
AND VCH."IdEmpresa" = :IdEmpresa
AND VCH."IdSerieFondo" IN (:IdSerieFondo)
)
WHERE "RANK" = 1;