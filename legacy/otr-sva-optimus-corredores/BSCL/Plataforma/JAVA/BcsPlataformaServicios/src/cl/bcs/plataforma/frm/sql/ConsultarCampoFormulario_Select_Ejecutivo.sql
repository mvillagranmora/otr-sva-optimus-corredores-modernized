SELECT
	TO_NVARCHAR("IdEmpleadoEjecutivo") AS "IdEmpleadoEjecutivo"
	,"NombreCompleto"
FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"
WHERE TO_NVARCHAR("IdEmpleadoEjecutivo") in (:IdsEmpleadoEjecutivo);