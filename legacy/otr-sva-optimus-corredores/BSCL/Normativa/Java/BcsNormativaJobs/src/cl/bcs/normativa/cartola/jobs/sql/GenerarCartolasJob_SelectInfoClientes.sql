SELECT
	DOC."DocumentoIdentidad" AS "DocumentoIdentidad",
	POR."CodigoPortafolio" AS "CodigoPortafolio",
	TRIM(PER."Nombres") || ' ' || TRIM(PER."ApellidoPaterno") || ' ' || TRIM(PER."ApellidoMaterno") AS "NombreCompleto",
	EMP."CodigoEmpleado" AS "CodigoEjecutivo",
	TRIM(DIRE."Direccion") AS "Correo",
	TRIM(DIR."Calle") || ' ' || TRIM(DIR."Numero") || ' ' || TRIM(DIR."Departamento") AS "Direccion",
	TRIM(COM."NombreDivisionPais") AS "Comuna",
	TRIM(CIU."NombreCiudad") AS "Ciudad",
	TRIM(REG."NombreClasificacionDivisionPais") AS "Region",
	TRIM(PAIS."NombrePais") AS "NombrePais"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS PER
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON PER."IdPersona" = CLI."IdPersona"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON CLI."IdCliente" = POR."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON PER."IdPersona" = DOC."IdPersona"
INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIR ON PER."IdPersona" = DIR."IdPersona"
INNER JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS CIU ON DIR."IdCiudad" = CIU."IdCiudad"
INNER JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS COM ON DIR."IdDivisionPais" = COM."IdDivisionPais"
INNER JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAIS ON PAIS."IdPais" = CIU."IdPais" 
INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DIRE ON DIRE."IdPersona" = PER."IdPersona" 
INNER JOIN "BSCL.Plataforma.BD::REG.ClasificacionDivisionPais" AS REG ON COM."IdClasificacion1" = REG."IdClasificacionDivisionPais"
INNER JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" AS EJE ON  EJE."IdEmpleadoEjecutivo" = POR."IdEmpleadoEjecutivo"
INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
WHERE  
	DIR."Predeterminado" = 'S'
	AND DOC."Predeterminado" = 'S'
	AND DIRE."Predeterminado" = 'S'
	AND POR."IdPortafolio" IN (:IdPortafolio);