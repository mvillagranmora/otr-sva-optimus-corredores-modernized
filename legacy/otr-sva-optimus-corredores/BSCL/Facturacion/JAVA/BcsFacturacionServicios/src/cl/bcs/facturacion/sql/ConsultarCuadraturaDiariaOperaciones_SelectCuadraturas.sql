SELECT 
        CDODET."IdModuloCuadratura"
	   ,CDODET."IdCuadraturaDiariaOperaciones"
	   ,CDODET."IdEmpresa"
	   ,CDODET."OperacionCuadraturaDiariaOperaciones"
	   ,TO_VARCHAR(CDOULT."FechaEjecucion",'YYYY-MM-DD') AS "FechaEjecucion"
	   ,CDOULT."TotalOperaciones"
	   ,CDOULT."OperacionesConProblemas"
	   ,CDOULT."TipoOperacion"
	   ,CDOULT."Resultado"
FROM(
       SELECT 
              TCO."IdModuloCuadratura"
	         ,TCO."IdCuadraturaDiariaOperaciones"
	         ,TCO."IdEmpresa"
	         ,TCO."OperacionCuadraturaDiariaOperaciones"
	         ,MAX(TO_VARCHAR(CDO."FechaEjecucion",'YYYY-MM-DD')) AS "FechaEjecucion"	  
       FROM "BSCL.Plataforma.BD::PLA.TipoCuadraturaOperaciones" TCO
         LEFT JOIN "BSCL.Facturacion.BD::FAC.CuadraturaDiariaOperaciones" CDO 
           ON TCO."IdCuadraturaDiariaOperaciones" = CDO."IdCuadraturaDiariaOperaciones" 
          AND TCO."IdEmpresa" = CDO."IdEmpresa"
          AND CDO."FechaEjecucion" <= :FechaEjecucion
       WHERE TCO."IdEmpresa" = :IdEmpresa
         AND TCO."IdModuloCuadratura" = :IdModuloCuadratura
      GROUP BY TCO."IdModuloCuadratura", TCO."IdCuadraturaDiariaOperaciones", TCO."IdEmpresa", TCO."OperacionCuadraturaDiariaOperaciones") AS CDODET
  LEFT JOIN "BSCL.Facturacion.BD::FAC.CuadraturaDiariaOperaciones" AS CDOULT
     ON CDOULT."IdCuadraturaDiariaOperaciones" = CDODET."IdCuadraturaDiariaOperaciones"
    AND CDOULT."FechaEjecucion" = CDODET."FechaEjecucion";