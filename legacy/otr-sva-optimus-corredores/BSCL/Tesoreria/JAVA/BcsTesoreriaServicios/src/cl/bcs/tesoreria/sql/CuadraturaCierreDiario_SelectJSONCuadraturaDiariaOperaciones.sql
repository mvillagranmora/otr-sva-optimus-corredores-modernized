SELECT 
	"DetalleCuadraturaJSON"
FROM "BSCL.Tesoreria.BD::TES.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa;
