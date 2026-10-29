SELECT 
	X."IdCuadraturaDiariaOperaciones", 
	X."IdEmpresa",
	X."OperacionCuadraturaDiariaOperaciones", 
	X."FechaEjecucion", 
	X."TotalOperaciones", 
	X."OperacionesConProblemas", 
	X."TipoOperacion", 
	X."Resultado", 
	X."Orden", 
	X."DetalleCuadraturaJSON"
FROM (
    SELECT 
        TCO."IdCuadraturaDiariaOperaciones", 
        TCO."IdEmpresa",
        TCO."OperacionCuadraturaDiariaOperaciones", 
        CDO."FechaEjecucion", 
        CDO."TotalOperaciones", 
        CDO."OperacionesConProblemas", 
        TCO."TipoOperacion", 
        CDO."Resultado", 
        CDO."Orden", 
        CDO."DetalleCuadraturaJSON",
        ROW_NUMBER() OVER (
            PARTITION BY TCO."IdCuadraturaDiariaOperaciones", TCO."IdEmpresa"
            ORDER BY CDO."FechaEjecucion" DESC
        ) AS RN
    FROM "BSCL.Plataforma.BD::PLA.TipoCuadraturaOperaciones" TCO
    LEFT JOIN "BSCL.Facturacion.BD::FAC.CuadraturaDiariaOperaciones" CDO 
        ON TCO."IdCuadraturaDiariaOperaciones" = CDO."IdCuadraturaDiariaOperaciones" 
       AND TCO."IdEmpresa" = CDO."IdEmpresa"
    WHERE TCO."IdCuadraturaDiariaOperaciones" IN (:IdCuadraturaDiariaOperaciones)
      AND TCO."IdEmpresa" = :IdEmpresa
) X
WHERE X.RN = 1;



