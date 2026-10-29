SELECT GD."IdGrupoDireccion"
     , GD."IdPersona"
     , CASE PER."Tipo"
           WHEN 'N' THEN PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
           ELSE PER."RazonSocial"
    END AS "NombreCompleto"
     , DI."DocumentoIdentidad"
     , GD."Grupo"
     , GD."Comentario"
     , GD."IdEstado"
     , GD."IdEstadoAutorizacion"
FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccion" AS GD
     LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = GD."IdPersona")
     LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DI ON PER."IdDocumentoIdentidad" = DI."IdDocumentoIdentidad"
WHERE GD."IdGrupoDireccion" IN (:IdGrupoDireccion);