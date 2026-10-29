SELECT 
	case when PER."Tipo" = 'N' then concat(PER."Nombres", concat(' ', concat(PER."ApellidoPaterno", concat(' ', PER."ApellidoMaterno")))) 
		 when PER."Tipo" = 'J' then PER."RazonSocial" 
		 else PER."RazonSocial" END as "NombreSocioNegocio",
 	concat(PEREMP."Nombres", concat(' ', concat(PEREMP."ApellidoPaterno", concat(' ', PEREMP."ApellidoMaterno")))) as "NombreEjecutivo",
 	DOC."DocumentoIdentidad" as "DocumentosIdentidad",
 	TEL."Numero" as "Telefono",
    'CLP' as "MonedaCartola",
    POR."CodigoPortafolio" as"CodigoPortafolio",
    POR."IdPortafolio",
    POR."IdCliente"
    
FROM "BSCL.Portafolio.BD::POR.Portafolio" as POR
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI 
											   on (POR."IdCliente" = CLI."IdCliente") 	
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER 
											  on (CLI."IdPersona" = PER."IdPersona")	
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TEL
											   ON (TEL."IdTelefono" = PER."IdTelefonoPrincipal")						  
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC 
														 on (PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad")
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP 
											   on (POR."IdEmpleadoEjecutivo" = EMP."IdEmpleado")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PEREMP 
											  on (EMP."IdPersona" = PEREMP."IdPersona") 
WHERE (POR."IdPortafolio" IN (:IdPortafolio))
