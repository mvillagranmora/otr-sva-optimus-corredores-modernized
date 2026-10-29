SELECT 
	 TAB."IdCliente"
	,(CASE WHEN PER."IdTipoCanalIngresoCorredor" = NULL OR PER."IdTipoCanalIngresoCorredor" ='' THEN 'OPT' ELSE PER."IdTipoCanalIngresoCorredor" END ) AS "IdCanal"
FROM "BSCL.Plataforma.BD::FRM.CampoFormulario" AS FORM
INNER JOIN "BSCL.Plataforma.BD::FRM.Campo" AS CAMP ON CAMP."CodigoCampo"= FORM."CodigoCampo"
INNER JOIN #TablaLog AS TAB ON TAB."CampoBackend"=CAMP."CodigoCampoBackend"  
							AND TAB."CodigoFormulario" = FORM."CodigoFormulario"
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = TAB."IdPersona"
WHERE FORM."RegeneraContratos" = 'S' AND (
	
	COALESCE(PER."IdTipoCanalIngresoCorredor",'OPT') = FORM."IdTipoCanal"
	OR 
	PER."IdTipoCanalIngresoCorredor" = '' AND FORM."IdTipoCanal" = 'OPT')
