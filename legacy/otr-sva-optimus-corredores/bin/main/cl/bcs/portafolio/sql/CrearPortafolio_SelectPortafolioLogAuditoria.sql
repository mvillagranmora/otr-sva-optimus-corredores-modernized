SELECT  
	COALESCE("CLI"."NombreCompleto",'')							AS "NombreCompleto",
	COALESCE("CLI"."DocumentoIdentidad",'')						AS "DocumentoIdentidad",
	COALESCE("CLI"."TipoDocumentoIdentidad",'')					AS "TipoDocumentoIdentidad",
	COALESCE("POR"."CodigoPortafolio",'')						AS "CodigoPortafolio",
	COALESCE("POR"."DescripcionPortafolio",'')					AS "DescripcionPortafolio",
	COALESCE("PROPOS"."Descripcion", '')						AS "PropositoPortafolio",
	COALESCE("POR"."IdPerfilComercialBaseOrigen",'')			AS "PerfilComercialBase",
	COALESCE("POR"."EstadoPortafolioAutorizacion",'')			AS "EstadoPortafolioAutorizacion",	 
    COALESCE("POR"."IdEmpleadoEjecutivoComercial" ,0)			AS "IdEjecutivoComercial"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 					AS "POR"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			AS "CLI"
	ON("POR"."IdCliente" = "CLI"."IdCliente")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio"		AS "PROPOS"
	ON("POR"."Proposito" = "PROPOS"."IdPropositoPortafolio")
WHERE "POR"."IdPortafolio" = :IdPortafolio;