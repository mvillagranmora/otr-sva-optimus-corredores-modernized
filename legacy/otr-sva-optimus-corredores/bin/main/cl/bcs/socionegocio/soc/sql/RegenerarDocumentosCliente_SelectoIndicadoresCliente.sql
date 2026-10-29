SELECT 
	 TAB."IdCliente"
	,COALESCE (PER."IdTipoCanalIngresoCorredor",'OPT') AS "IdCanal"  
FROM #TablaLog AS TAB 
INNER JOIN "BSCL.Plataforma.BD::FRM.Campo" AS CAMP ON  CAMP."CodigoCampoLogContratos" = TAB."CampoBackend"
INNER JOIN "BSCL.Plataforma.BD::FRM.CampoFormulario" AS FORM ON FORM."CodigoFormulario" = TAB."CodigoFormulario" AND FORM."CodigoCampo" = CAMP."CodigoCampo" 
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = TAB."IdPersona" 
WHERE FORM."RegeneraContratos" ='S';