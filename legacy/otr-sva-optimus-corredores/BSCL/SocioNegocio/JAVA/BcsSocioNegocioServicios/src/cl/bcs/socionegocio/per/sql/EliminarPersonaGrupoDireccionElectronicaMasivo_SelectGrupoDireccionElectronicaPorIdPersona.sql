SELECT GD."IdGrupoDireccion" AS "IdEntidadPersonaEliminar"
     , GD."IdPersona"
     , PER."Tipo" AS "TipoPersona"
	 , COALESCE(CLI."TipoEnrolamiento", '') AS "TipoEnrolamiento"
     , CASE PER."Tipo"
           WHEN 'N' THEN PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
           ELSE PER."RazonSocial"
    END                      AS "NombreCompleto"
     , IDENT."DocumentoIdentidad"
     , GD."Borrado"
FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccion" AS GD
     LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = GD."IdPersona")
     LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdPersona" = PER."IdPersona")
     LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE GD."IdPersona" IN (:IdEntidad)
	AND GD."Borrado" = 'N';