SELECT GD."IdGrupoDireccion"   AS "IdGrupoDireccion"
     , PER."IdPersona"         AS "IdPersona"
     , CLI."IdCliente"         AS "IdCliente"
     , CASE PER."Tipo"
           WHEN 'N' THEN PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
           ELSE PER."RazonSocial"
    END                        AS "NombreCompleto"
     , DI."DocumentoIdentidad" AS "DocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccion" AS GD
     LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = GD."IdPersona"
     LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
     LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS di ON DI."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad"
WHERE GD."IdGrupoDireccion" IN (:IdGrupoDireccionElectronica);
