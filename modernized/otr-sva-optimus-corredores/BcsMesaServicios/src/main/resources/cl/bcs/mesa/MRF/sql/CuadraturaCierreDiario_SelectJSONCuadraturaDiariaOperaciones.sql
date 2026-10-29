SELECT 
	"DetalleCuadraturaJSON"
FROM "BSCL.Mesa.BD::MES.CuadraturaDiariaOperacionesMRF"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa;
