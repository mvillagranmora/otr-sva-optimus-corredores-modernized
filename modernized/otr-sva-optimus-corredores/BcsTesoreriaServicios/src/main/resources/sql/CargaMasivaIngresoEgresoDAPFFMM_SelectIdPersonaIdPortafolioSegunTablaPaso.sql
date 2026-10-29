SELECT distinct
	 DOC."IdPersona"
	,POR."IdPortafolio"
	,PASO."IdTipoDocumentoIdentidad"
	,PASO."DocumentoIdentidad"
	,PASO."CodigoPortafolio"
	,PASO."IdTrabajo"
FROM "BSCL.Tesoreria.BD::GBL.CargaMasivaIngresoEgresoDAPFFMM_TablaPaso" AS PASO
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."DocumentoIdentidad" = PASO."DocumentoIdentidad" AND DOC."IdTipoDocumentoIdentidad" = PASO."IdTipoDocumentoIdentidad"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = DOC."IdPersona" 
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON POR."CodigoPortafolio" = PASO."CodigoPortafolio" AND POR."IdCliente" = CLI."IdCliente"
WHERE PASO."IdTrabajo" = :IdTrabajo;