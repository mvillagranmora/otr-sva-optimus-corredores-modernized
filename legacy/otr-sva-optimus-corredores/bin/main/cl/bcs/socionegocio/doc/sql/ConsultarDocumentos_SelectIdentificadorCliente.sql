SELECT CLI."IdCliente"
	 , CLI."IdPersona"
FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" PDOC
	 LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON (CLI."IdPersona" = PDOC."IdPersona" AND CLI."IdPersona" IS NOT NULL)
WHERE PDOC."Borrado" = 'N'
  AND PDOC."DocumentoIdentidad" = :DocumentoIdentidad;