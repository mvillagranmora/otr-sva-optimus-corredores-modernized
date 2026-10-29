-- Obtiene datos de los clientes

  SELECT 
	CLI."IdCliente", 
  	CLI."IdPersona", 
  	CLI."IdEmpresa", 
  	CLI."IdEstado", 
  	IDENT."DocumentoIdentidad"
	  
  FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  
  INNER JOIN   "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CLI."IdPersona")
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS  IDENT ON (PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad")
  
  WHERE "IdCliente" IN (:IdCliente);