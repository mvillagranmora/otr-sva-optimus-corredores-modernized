SELECT 
	"DetalleCuadraturaJSON"
FROM "BSCL.Mesa.BD::MES.CuadraturaDiariaOperacionesFWD"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa;
