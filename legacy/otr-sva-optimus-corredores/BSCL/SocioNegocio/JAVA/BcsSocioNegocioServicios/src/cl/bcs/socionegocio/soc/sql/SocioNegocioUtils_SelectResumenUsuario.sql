SELECT U."IdUsuario"     AS "IdUsuario"
     , U."IdEntidad"     AS "IdEntidad"
     , U."TipoEntidad"   AS "TipoEntidad"
     , U."CodigoUsuario" AS "CodigoUsuario"
     , SOC."IdPersona"   AS "IdPersona"
     , CASE
           WHEN PER."Tipo" = 'J' THEN PER."RazonSocial"
           ELSE PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
    END                  AS "NombreCompleto"
FROM "BSCL.Plataforma.BD::ACC.Usuario" U
     JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
          ON (SOC."IdEmpleado" = U."IdEntidad" AND U."TipoEntidad" = 'EMP')
              OR (SOC."IdCliente" = U."IdEntidad" AND U."TipoEntidad" = 'CLI')
              OR (SOC."IdOperador" = U."IdEntidad" AND U."TipoEntidad" = 'OPE')
              OR (SOC."IdProveedor" = U."IdEntidad" AND U."TipoEntidad" = 'PRO')
              OR (SOC."IdPersona" = U."IdEntidad" AND U."TipoEntidad" = 'PER')
     LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (PER."IdPersona" = SOC."IdPersona")
WHERE U."IdEmpresa" = :IdEmpresa
  AND PER."IdPersona" IS NOT NULL
  AND (:CodigoUsuario = '' OR U."CodigoUsuario" = :CodigoUsuario)
  AND (:IdUsuario = -1 OR U."IdUsuario" = :IdUsuario);