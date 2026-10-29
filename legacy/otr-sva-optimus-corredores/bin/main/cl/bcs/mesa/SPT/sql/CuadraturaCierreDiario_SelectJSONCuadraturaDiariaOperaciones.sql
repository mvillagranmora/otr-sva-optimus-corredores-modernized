SELECT 
	"DetalleCuadraturaJSON"
FROM "BSCL.Mesa.BD::SPT.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa;
