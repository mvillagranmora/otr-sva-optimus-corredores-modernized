UPDATE POR
SET
	POR."FechaBloqueo" = COALESCE(:FechaBloqueo, POR."FechaBloqueo"),
	POR."ComentarioBloqueo" = COALESCE(:ComentarioBloqueo, POR."ComentarioBloqueo"),
	POR."IdTipoBloqueo" = COALESCE(:IdTipoBloqueo, POR."IdTipoBloqueo"),
	POR."IdPerfilProductos" = COALESCE(:IdPerfilProductos, POR."IdPerfilProductos"),
	POR."IdPerfilCobros" = COALESCE(:IdPerfilCobros, POR."IdPerfilCobros"),
	POR."DescripcionPortafolio" = COALESCE(:DescripcionPortafolio, POR."DescripcionPortafolio"),
	POR."IdPerfilLimites" = COALESCE(:IdPerfilLimites, POR."IdPerfilLimites"),
	POR."NumeroFicha" = COALESCE(:NumeroFicha, POR."NumeroFicha"),
	POR."IdNombreFantasia" = COALESCE(:IdNombreFantasia, POR."IdNombreFantasia"),
	POR."Alias" = COALESCE(:Alias, POR."Alias"),
	POR."IdPersonaBrokerExtranjero" = COALESCE(:IdPersonaBrokerExtranjero, POR."IdPersonaBrokerExtranjero")
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR	
WHERE POR."IdPortafolio" = :IdPortafolio;