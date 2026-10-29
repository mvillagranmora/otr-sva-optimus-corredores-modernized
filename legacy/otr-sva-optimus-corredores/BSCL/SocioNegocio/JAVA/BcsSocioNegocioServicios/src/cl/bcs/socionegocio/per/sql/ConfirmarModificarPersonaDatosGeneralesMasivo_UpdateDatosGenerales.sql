-- Actualiza los campos de Persona
UPDATE  PER 
SET     PER."Nombres" = COALESCE(:Nombres,PER."Nombres"),
		PER."ApellidoPaterno" = COALESCE(:ApellidoPaterno,PER."ApellidoPaterno"),
		PER."ApellidoMaterno" = COALESCE(:ApellidoMaterno,PER."ApellidoMaterno"),
		PER."RazonSocial" = COALESCE(:RazonSocial,PER."RazonSocial"),
		PER."IdGiroPrincipal" = COALESCE(:IdGiroPrincipal,PER."IdGiroPrincipal"),
		PER."IdPaisNacimiento" = COALESCE(:IdPaisNacimiento,PER."IdPaisNacimiento"),
		PER."IdPaisResidencia" = COALESCE(:IdPaisResidencia,PER."IdPaisResidencia"),
		PER."FechaNacimiento" = COALESCE(:FechaNacimiento,PER."FechaNacimiento"),
		PER."FechaDefuncion" = COALESCE(:FechaDefuncion,PER."FechaDefuncion"),
		PER."FechaInicioActividad" = COALESCE(:FechaInicioActividad,PER."FechaInicioActividad"),
		PER."Sexo" = COALESCE(:Sexo,PER."Sexo"),
		PER."EsExtranjero" = COALESCE(:EsExtranjero,PER."EsExtranjero"),
		PER."EstadoCivil" = COALESCE(:EstadoCivil,PER."EstadoCivil"),
		PER."RegimenConyugal" = CASE 
		    WHEN COALESCE(:EstadoCivil, PER."EstadoCivil") IN ('CA', 'CC')
		    THEN COALESCE(NULLIF(:RegimenConyugal, ''), PER."RegimenConyugal")
		    ELSE NULL 
		END,
		PER."CodigoTipoCargoEmpleado" = COALESCE(:CodigoTipoCargoEmpleado,PER."CodigoTipoCargoEmpleado"),
		PER."Cargo" = COALESCE(:Cargo,PER."Cargo"),
		PER."DetalleCargo" = COALESCE(:DetalleCargo,PER."DetalleCargo"),
		PER."IdProfesion" = COALESCE(:IdProfesion,PER."IdProfesion"),
		PER."IdPersonaConyuge" = COALESCE(:IdPersonaConyuge,PER."IdPersonaConyuge"),
		PER."IdPaisNacionalidadPrincipal" = COALESCE(:IdPaisNacionalidadPrincipal,PER."IdPaisNacionalidadPrincipal"),
		PER."IdPersonaRepresentante" = COALESCE(:IdPersonaRepresentante,PER."IdPersonaRepresentante"),
		PER."IdRangoPatrimonio" = COALESCE(:IdRangoPatrimonio,PER."IdRangoPatrimonio"),
		PER."IdPersonaEmpleador" = COALESCE(:IdPersonaEmpleador,PER."IdPersonaEmpleador"),
		PER."IdNombreFantasiaPrincipal" = COALESCE(:IdNombreFantasiaPrincipal,PER."IdNombreFantasiaPrincipal"),
		PER."GlobalLegalEntity" = COALESCE(:GlobalLegalEntity,PER."GlobalLegalEntity"),
		PER."IdTransparenciaFiscal" = COALESCE(:IdTransparenciaFiscal,PER."IdTransparenciaFiscal"),
		PER."ResidenciaTributariaExtranjera" = COALESCE(:ResidenciaTributariaExtranjera,PER."ResidenciaTributariaExtranjera"),
		PER."FechaModificacion" = CURRENT_UTCTIMESTAMP,
		PER."IdSituacionLaboral" = COALESCE(:IdSituacionLaboral,PER."IdSituacionLaboral"),
		PER."CodigoPropositoTransaccion" = COALESCE(:CodigoPropositoTransaccion,PER."CodigoPropositoTransaccion"),
		PER."CodigoRangoRenta" = COALESCE(:CodigoRangoRenta,PER."CodigoRangoRenta"),
		PER."CodigoRangoNivelVenta" = COALESCE(:CodigoRangoNivelVenta,PER."CodigoRangoNivelVenta")
FROM	"BSCL.SocioNegocio.BD::PER.Persona" PER
WHERE "IdPersona" 		= :IdPersona;