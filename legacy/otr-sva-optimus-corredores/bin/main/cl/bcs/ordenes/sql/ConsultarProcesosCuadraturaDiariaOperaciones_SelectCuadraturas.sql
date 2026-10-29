SELECT 
"IdCuadraturaDiariaOperaciones"
,"OperacionCuadraturaDiariaOperaciones"
,TO_VARCHAR("FechaEjecucion",'YYYY-MM-DD') AS "FechaEjecucion"
,"TotalOperaciones"
,"OperacionesConProblemas"
,"TipoOperacion"
,"Resultado"
FROM "BSCL.Orden.BD::ORD.CuadraturaDiariaOperaciones"
WHERE "IdEmpresa" = :IdEmpresa AND "FechaEjecucion" <= :FechaInicio
ORDER BY "Orden" ASC;