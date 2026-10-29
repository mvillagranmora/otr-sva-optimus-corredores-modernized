SELECT 
	"CodigoTipoCargoEmpleado" AS "CodigoTipoCargoEmpleado", 
	"NombreTipoCargoEmpleado" AS "NombreTipoCargoEmpleado" 
		FROM "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado"
			WHERE "CodigoTipoCargoEmpleado" IN (:CodigoTipoCargoEmpleado)