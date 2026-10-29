SELECT 	CPDI."DocumentoIdentidad" AS "RutCavali", 
		ConCli."IdCliente",
		CPDI."IdSocioDeNegocio"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad"	AS CPDI
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"	AS ConCli
		ON ConCli."IdSocioDeNegocio" = CPDI."IdSocioDeNegocio"
WHERE TRIM(CPDI."IdTipoDocumentoIdentidad") IN (:TipoDocumentoIdentidad)
  AND TRIM(CPDI."DocumentoIdentidad") IN (:DocumentoIdentidad);