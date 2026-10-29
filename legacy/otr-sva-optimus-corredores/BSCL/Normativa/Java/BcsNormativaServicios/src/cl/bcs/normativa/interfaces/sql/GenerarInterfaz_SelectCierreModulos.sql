SELECT MODULO."IdEmpresa",
	   MODULO."CodigoModulo",
	   PLAMODULO."Nombre",
	   MODULO."IdEstado" 
FROM "BSCL.Plataforma.BD::PAU.Modulo" AS MODULO
INNER JOIN "BSCL.Plataforma.BD::PLA.Modulo" AS PLAMODULO ON PLAMODULO."Codigo" = MODULO."CodigoModulo"
WHERE "CodigoModulo" IN (:IdModulo)
AND "IdEmpresa" = :IdEmpresa
UNION ALL
SELECT 	"IdEmpresa",
		"IdCuadraturaDiariaOperaciones" AS "CodigoModulo", 
		'CAVALI' AS "Nombre",
		"Resultado" AS "IdEstado"
FROM "BSCL.Orden.BD::ORD.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" = 'CUACAVALI'
AND "IdEmpresa" = :IdEmpresa 
AND "FechaEjecucion" <= :FechaEjecucion;