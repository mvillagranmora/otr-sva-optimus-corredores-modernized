-- Obtiene datos de la persona a modificar
SELECT		
		PERSONA."IdPersona",
		PERSONA."FechaIngreso",
		PERSONA."IdEstado",
		PERSONA."Tipo",
		PERSONA."Nombres",
		PERSONA."ApellidoPaterno",
		PERSONA."ApellidoMaterno",
		CASE PERSONA."Tipo" 
			WHEN 'N' THEN PERSONA."Nombres" ||' '|| PERSONA."ApellidoPaterno" || ' ' || PERSONA."ApellidoMaterno"			
			ELSE PERSONA."RazonSocial"
		END  as "NombreCompleto",
		DOCUMENTO_IDENTIDAD."DocumentoIdentidad",
		PERSONA."RazonSocial",
		PERSONA."IdGiroPrincipal",
		PERSONA."IdPaisNacimiento",
		PERSONA."IdPaisResidencia",
		PERSONA."FechaNacimiento",
		PERSONA."FechaDefuncion",
		PERSONA."FechaInicioActividad",
		PERSONA."Sexo",
		PERSONA."EsExtranjero",
		PERSONA."EstadoCivil",
		PERSONA."RegimenConyugal",
		PERSONA."CodigoTipoCargoEmpleado",
		PERSONA."Cargo",
		PERSONA."DetalleCargo",
		PERSONA."Empleador",
		PERSONA."IdProfesion",
		PERSONA."IdUsuarioCreador",
		PERSONA."IdPersonaConyuge",
		PERSONA."IdPersonaRepresentante",
		PERSONA."IdPaisNacionalidadPrincipal",
		PERSONA."IdNombreFantasiaPrincipal",
		PERSONA."GlobalLegalEntity",
		PERSONA."IdPersonaEmpleador",
		PERSONA."IdRangoPatrimonio",
		PERSONA."IdTransparenciaFiscal",
		PERSONA."ResidenciaTributariaExtranjera",
		PERSONA."IdGiroCIIU",
		PERSONA."IdTipoCanalIngresoCorredor",
		PERSONA."IdSituacionLaboral",
		PERSONA."CodigoPropositoTransaccion",
		PERSONA."CodigoRangoRenta",
		PERSONA."CodigoRangoNivelVenta"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD ON PERSONA."IdDocumentoIdentidad" = DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
WHERE  PERSONA."IdPersona" IN (:IdPersona);