SELECT
	"IdEmpleado" AS "IdEmpleado",
	"NombreCompleto" AS "NombreCompleto" 
	FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"
		WHERE "IdEmpleado" IN (:IdEjecutivo)
		