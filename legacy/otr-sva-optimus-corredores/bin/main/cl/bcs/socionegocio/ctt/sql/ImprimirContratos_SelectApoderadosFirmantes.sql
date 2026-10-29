SELECT
	PER."IdPersona"																		AS "IdPersona",
	COALESCE(PER."IdPersonaConyuge", 0)													AS "IdPersonaConyuge",
    COALESCE(NULLIF(TRIM(PER."NombreCompleto"), ''), RP."NombreCompletoDestino", '')	AS "NombreCompleto",
    COALESCE(NULLIF(TRIM(PER."Nombres"), ''), PER."RazonSocial", '') 					AS "Nombres",
    COALESCE(PER."ApellidoPaterno", '') 												AS "ApellidoPaterno",
    COALESCE(PER."ApellidoMaterno", '') 												AS "ApellidoMaterno",
    COALESCE(PER."TipoDocumentoIdentidad", '') 											AS "TipoDocumento",
    COALESCE(PER."DocumentoIdentidad", RP."DocumentoIdentidadDestino", '')				AS "DocumentoIdentidad",
    COALESCE(PS."NombrePais", '') 														AS "NombrePaisNacionalidadPrincipal",
    COALESCE(EMAIL."Direccion", '') 													AS "DireccionElectronicaPrincipal",
    COALESCE(NULLIF(TRIM(COALESCE(DF."Calle", '') 
    || ' ' || COALESCE(CAST(DF."Numero" AS VARCHAR), '')), ''), '')						AS "DireccionParticular",
    COALESCE(DF."NombreCiudad" , '') 													AS "DireccionParticularCiudad",
    COALESCE(INITCAP(DF."NombreDivisionPais"), '') 										AS "DireccionParticularComuna",
    COALESCE(NULLIF(TRIM(COALESCE(TEL."Numero", '')
    || ' ' || COALESCE(TEL."Anexo", '')), ''), RP."Telefono", '')						AS "NumeroTelefonoPrincipal",
    COALESCE(RELC."NombreValorIndicador", '') 											AS "RelacionCorredora",
    COALESCE(CAR."NombreTipoCargoPersona", '') 											AS "Cargo",
    COALESCE(EC."Nombre" , '') 															AS "EstadoCivil"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" RP
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER 
    ON RP."IdPersonaDestino"  = PER."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PS 
    ON PS."IdPais" = PER."IdPaisNacionalidadPrincipal"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" EMAIL 
    ON EMAIL."IdDireccionElectronica" = PER."IdDireccionElectronicaPrincipal" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" TEL 
    ON TEL."IdTelefono" = PER."IdTelefonoPrincipal"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPersona" RELC 
    ON RELC."IdPersona" = PER."IdPersona" AND RELC."CodigoTipoIndicador" = 'NCG380PER'
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoCargoPersona" CAR 
    ON CAR."CodigoTipoCargoPersona" = PER."CodigoTipoCargoEmpleado" AND PER."IdSituacionLaboral" ='EMPL'
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DF 
	ON DF."IdDireccionFisica" = PER."IdDireccionFisicaPrincipal"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoEstadoCivil" EC 
	ON EC."IdTipoEstadoCivil" = PER."EstadoCivil"
WHERE RP."IdTipoRelacion" = 'APOFIR'
  AND RP."DocumentoIdentidad" = :DocumentoIdentidad
  AND RP."IdEstado" ='ACT'
ORDER BY
"FechaInicioRelacion" DESC,
"IdRelacionPersona" ASC
LIMIT 4;