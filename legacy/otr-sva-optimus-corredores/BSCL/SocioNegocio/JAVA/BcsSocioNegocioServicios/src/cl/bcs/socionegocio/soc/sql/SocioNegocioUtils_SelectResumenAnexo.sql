SELECT VCLI."IdCliente"                                              AS "IdCliente"
	 , VCLI."IdPersona"                                              AS "IdPersona"
	 , VCLI."TipoEnrolamiento"                                       AS "TipoEnrolamiento"
	 , VCLI."NombreCompleto"                                         AS "NombreCompletoCliente"
	 , VCLI."DocumentoIdentidad"                                     AS "DocumentoIdentidadCliente"
	 , VCLI."IdPersonaEjecutivo"                                     AS "IdPersonaAgente"
	 , VCLI."NombreEmpleadoEjecutivo"                                AS "NombreCompletoAgente"
	 , ANE."IdAnexo"                                                 AS "IdAnexo"
	 , ANE."IdDocumento"                                             AS "IdDocumento"
	 , TD."NombreDocumento"                                          AS "NombreDocumento"
	 , ANE."IdEstado"                                                AS "IdEstadoAnexo"
	 , TD."IdTipoDocumento"                                          AS "IdTipoDocumento"
	 , COALESCE(ANE."NumeroFirmasMinimas", TD."NumeroFirmasMinimas") AS "NumeroFirmasMinimas"
	 , COALESCE(ANE."NumeroFirmasMaximas", TD."NumeroFirmasMaximas") AS "NumeroFirmasMaximas"
FROM "BSCL.Plataforma.BD::DOC.Anexo" ANE
	 LEFT JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON DOC."IdDocumento" = ANE."IdDocumento"
	 LEFT JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" TD ON TD."IdTipoDocumento" = DOC."IdTipoDocumento"
	 LEFT JOIN (SELECT CLI."IdCliente"          AS "IdCliente"
					 , CLI."IdPersona"          AS "IdPersona"
					 , CLI."TipoEnrolamiento"   AS "TipoEnrolamiento"
					 , DI."DocumentoIdentidad"  AS "DocumentoIdentidad"
					 , CASE
						   WHEN PER."Tipo" = 'J' THEN PER."RazonSocial"
						   ELSE PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
		END                                     AS "NombreCompleto"
					 , CLI."IdPersonaEjecutivo" AS "IdPersonaEjecutivo"
					 , CASE
						   WHEN PEJE."Tipo" = 'J' THEN PEJE."RazonSocial"
						   ELSE PEJE."Nombres" || ' ' || PEJE."ApellidoPaterno" || ' ' || PEJE."ApellidoMaterno"
		END                                     AS "NombreEmpleadoEjecutivo"
				FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
					 LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (PER."IdPersona" = CLI."IdPersona")
					 LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON (DI."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
					 LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PEJE ON (PEJE."IdPersona" = CLI."IdPersonaEjecutivo")) VCLI ON VCLI."IdCliente" = DOC."IdCliente"
WHERE ANE."IdDocumento" IS NOT NULL
  AND DOC."IdCliente" IS NOT NULL
  AND ANE."IdAnexo" IN (:IdsAnexos);
