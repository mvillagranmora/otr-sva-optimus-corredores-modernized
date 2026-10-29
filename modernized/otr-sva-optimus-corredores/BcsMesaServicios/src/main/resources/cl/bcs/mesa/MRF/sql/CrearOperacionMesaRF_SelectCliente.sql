-- Obtiene datos de los clientes

  SELECT 
	CLI."IdCliente", 
  	CLI."IdPersona", 
  	CLI."IdEmpresa", 
  	CLI."IdEstado", 
  	IDENT."DocumentoIdentidad",
	CASE WHEN PER."Tipo"='J' THEN PER."RazonSocial" ELSE (PER."Nombres"||' '||PER."ApellidoPaterno"||' '||PER."ApellidoMaterno") END AS "RazonSocial",
	POR."CodigoPortafolio"
  FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  
  INNER JOIN   "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CLI."IdPersona")
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS  IDENT ON (PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad")
  LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (POR."IdCliente"= CLI."IdCliente" AND POR."IdPortafolio" = :IdPortafolio)
  
  WHERE CLI."IdCliente" IN (:IdCliente);
  
