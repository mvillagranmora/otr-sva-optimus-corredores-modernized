SELECT 
	"DetalleCuadraturaJSON"
FROM "BSCL.FondoMutuo.BD::FMM.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa;
