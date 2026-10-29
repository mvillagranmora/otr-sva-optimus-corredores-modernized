SELECT 
PER."NombreCompleto" AS "NombreEjecutivo",
TEL."Numero" AS "TelefonoEjecutivo",
EMAIL."Direccion" AS "EmailEjecutivo"

FROM "BSCL.SocioNegocio.BD::EJE.Ejecutivo" EJE
JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" EMP ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON PER."IdPersona" = EMP."IdPersona"
JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" EMAIL ON EMAIL."IdDireccionElectronica" = PER."IdDireccionElectronicaPrincipal"
JOIN "BSCL.SocioNegocio.BD::PER.Telefono" TEL ON TEL."IdTelefono" = PER."IdTelefonoPrincipal"
JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdEmpleadoEjecutivo" = EJE."IdEmpleadoEjecutivo"
WHERE 
	POR."IdPortafolio" = :p_IdPortafolio;