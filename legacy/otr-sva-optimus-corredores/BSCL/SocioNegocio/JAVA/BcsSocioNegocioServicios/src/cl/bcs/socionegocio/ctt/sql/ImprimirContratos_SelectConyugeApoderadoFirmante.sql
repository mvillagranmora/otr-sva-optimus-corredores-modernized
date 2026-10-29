SELECT
	PER."IdPersona"														AS "IdPersona",
	COALESCE(PER."IdPersonaConyuge", 0)									AS "IdPersonaConyuge",
	COALESCE(PER."DocumentoIdentidad", '')								AS "DocumentoIdentidad",
    COALESCE(NULLIF(TRIM(PER."NombreCompleto"), ''), '')				AS "NombreCompleto",
    COALESCE(NULLIF(TRIM(PER."Nombres"), ''), PER."RazonSocial", '')	AS "Nombres",
    COALESCE(PER."ApellidoPaterno", '') 								AS "ApellidoPaterno",
    COALESCE(PER."ApellidoMaterno", '') 								AS "ApellidoMaterno",
    COALESCE(PS."NombrePais", '') 										AS "NombrePaisNacionalidadPrincipal",
    COALESCE(RELC."NombreValorIndicador", '') 							AS "RelacionCorredora"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PS 
    ON PS."IdPais" = PER."IdPaisNacionalidadPrincipal"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPersona" RELC 
    ON RELC."IdPersona" = PER."IdPersona" AND RELC."CodigoTipoIndicador" = 'NCG380PER'
WHERE PER."IdPersona" IN (:Idpersonas);