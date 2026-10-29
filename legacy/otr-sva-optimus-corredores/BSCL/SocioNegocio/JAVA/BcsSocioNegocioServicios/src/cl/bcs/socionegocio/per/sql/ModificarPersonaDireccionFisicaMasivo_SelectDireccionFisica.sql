-- Obtiene datos de las direcciones fisicas a modificar

SELECT		
		DIR."IdDireccionFisica",
		DIR."IdPersona",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",		
		IDENT."DocumentoIdentidad",
		DIR."Calle",
		DIR."Numero",
		DIR."Departamento",
		DIR."Sector",
		DIR."CodigoPostal",
		DIR."IdCiudad",
		DIR."IdDivisionPais",
		DIR."CodigoProposito",
		DIR."Comentario",
		DIR."IdEstado",
		DIR."CodigoCompuesto"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica"  AS DIR
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = DIR."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
WHERE  DIR."IdDireccionFisica" IN (:IdDireccionFisica);