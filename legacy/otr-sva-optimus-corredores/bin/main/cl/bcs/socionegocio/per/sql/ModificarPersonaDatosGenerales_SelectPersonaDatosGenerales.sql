-- Obtiene datos de la persona a modificar
SELECT		
		PER."IdPersona",
		PER."FechaIngreso",
		PER."IdEstado",
		PER."Tipo",
		PER."Nombres",
		PER."ApellidoPaterno",
		PER."ApellidoMaterno",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"			
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",
		IDENT."DocumentoIdentidad",
		PER."RazonSocial",
		PER."IdGiroPrincipal",
		PER."IdPaisNacimiento",
		PER."IdPaisResidencia",
		PER."FechaNacimiento",
		PER."FechaDefuncion",
		PER."FechaInicioActividad",
		PER."Sexo",
		PER."EsExtranjero",
		PER."EstadoCivil",
		PER."RegimenConyugal",
		PER."CodigoTipoCargoEmpleado",
		PER."Cargo",
		PER."DetalleCargo",
		PER."Empleador",
		PER."IdProfesion",
		PER."IdUsuarioCreador",
		PER."IdPersonaConyuge",
		PER."IdPersonaRepresentante",
		PER."IdPaisNacionalidadPrincipal",
		PER."IdNombreFantasiaPrincipal",
		PER."GlobalLegalEntity",
		PER."IdPersonaEmpleador",
		PER."IdRangoPatrimonio",
		PER."IdTransparenciaFiscal",
		PER."ResidenciaTributariaExtranjera",
		PER."IdGiroCIIU",
		PER."CodigoRangoRenta",
		PER."CodigoRangoNivelVenta"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS PER
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
WHERE  PER."IdPersona" IN (:IdPersona);