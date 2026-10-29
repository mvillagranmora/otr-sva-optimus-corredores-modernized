SELECT 
	PAI."IdPersona"
	,PAI."IdPaisResidencia"
	,PAI."TIN"
	,PAI."IdCausaNoTin"
	,PAI."Observacion" AS "OtraCausa"
	,PAI."PaisResidenciaPrincipal"
	,CLI."TipoEnrolamiento"
	,CLI."IdCliente"
	,PER."Tipo"
	,CASE WHEN PAI."TIN" <> '' THEN 'S' ELSE 'N' END AS "TieneTin"
FROM "BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia" AS PAI
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PAI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE PAI."IdPersona"  = :IdPersona
AND PAI."IdPaisResidencia" IN (:IdPaisesResidencias);