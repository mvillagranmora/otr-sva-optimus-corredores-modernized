SELECT 
	"DetalleCuadraturaJSON"
FROM "BSCL.Contabilidad.BD::ITZ.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa;
