WITH RegistrosCuadratura AS (
    SELECT 
        "IdCuadraturaDiariaOperaciones", 
        "IdEmpresa",
        "OperacionCuadraturaDiariaOperaciones", 
        "FechaEjecucion", 
        "TotalOperaciones", 
        "OperacionesConProblemas", 
        "TipoOperacion", 
        "Resultado", 
        "Orden", 
        "DetalleCuadraturaJSON",
        ROW_NUMBER() OVER (PARTITION BY "IdCuadraturaDiariaOperaciones" ORDER BY "FechaEjecucion" DESC) AS rn
    FROM "BSCL.Custodia.BD::CUS.CuadraturaDiariaOperaciones"
    WHERE "IdCuadraturaDiariaOperaciones" IN (:IdCuadraturaDiariaOperaciones)
      AND "IdEmpresa" = :IdEmpresa
)
SELECT 
    *
FROM RegistrosCuadratura
WHERE rn = 1