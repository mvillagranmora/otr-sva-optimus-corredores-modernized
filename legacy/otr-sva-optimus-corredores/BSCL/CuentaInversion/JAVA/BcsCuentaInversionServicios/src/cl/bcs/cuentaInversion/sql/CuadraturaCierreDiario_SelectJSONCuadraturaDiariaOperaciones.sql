SELECT 
	"DetalleCuadraturaJSON"
FROM "BSCL.CuentaInversion.BD::CTA.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa;
