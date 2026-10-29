SELECT 
	"IdEmpleadoEjecutivo",
	"IdPersonaEjecutivo",	
	"NombreCompleto" 				
		FROM "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"
		WHERE "IdPersonaEjecutivo" IN (:IdPersonaEjecutivo)