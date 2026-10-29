SELECT 
	"DetalleCuadraturaJSON"
FROM "BSCL.Orden.BD::ORD.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa;
