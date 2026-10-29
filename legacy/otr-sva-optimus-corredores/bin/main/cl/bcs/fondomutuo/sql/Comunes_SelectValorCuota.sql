SELECT * FROM (
SELECT 
	"IdSerieFondo",
    TO_CHAR("FechaCierre",'YYYY-MM-DD') AS "FechaCierre",
    "ValorCuota",
    "IdMoneda",
    "Observacion",   
    TO_CHAR("FechaCarga",'YYYY-MM-DD') AS "FechaCarga",
    "FechaIngresoRegistro",
    "IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.ValorCuota"
WHERE "IdEmpresa" = :IdEmpresa AND "FechaCierre" = :FechaCuadratura
UNION ALL
SELECT 
	"IdSerieFondo",
    TO_CHAR("FechaCierre",'YYYY-MM-DD') AS "FechaCierre",
    "ValorCuota",
    "IdMoneda",
    "Observacion",   
    TO_CHAR("FechaCarga",'YYYY-MM-DD') AS "FechaCarga",
    "FechaIngresoRegistro",
    "IdEmpresa"
FROM "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico"
WHERE "IdEmpresa" = :IdEmpresa AND "FechaCierre" = :FechaCuadratura
) T 
ORDER BY "FechaIngresoRegistro" DESC
