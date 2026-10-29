SELECT TOP 1
	"IdEmpleado"
   ,"IdSucursal"
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"
WHERE "IdEmpleado" = :p_IdEmpleado;