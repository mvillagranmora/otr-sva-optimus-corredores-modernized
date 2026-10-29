SELECT 
     CLI."IdPersona"
    ,USU."TipoEntidad"
    ,USU."CodigoUsuario"
    ,CLI."IdEmpresa"
    ,USU."IdUsuario"
    ,CASE WHEN USU."TipoEntidad" = 'CLI' THEN POR."IdPortafolio" ELSE AUT."IdPortafolio" END AS "IdPortafolio"
    ,CASE WHEN USU."TipoEntidad" = 'CLI' THEN POR."IdCliente" ELSE AUT."IdCliente" END AS "IdCliente"
FROM "BSCL.Plataforma.BD::ACC.Usuario" USU 
    INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" CLI 
      ON CLI."IdEmpresa" = :IdEmpresa AND (USU."TipoEntidad" = 'CLI' AND USU."IdEntidad" = CLI."IdCliente") OR (USU."TipoEntidad" = 'PER' AND USU."IdEntidad" = CLI."IdPersona") 
    LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON (POR."IdCliente" = CLI."IdCliente")
    LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarAutorizadoOrden" AUT ON CLI."IdPersona" = AUT."IdPersona" AND AUT."IdTipoRelacion" = 'AUTZDO' AND USU."TipoEntidad" = 'PER'
WHERE USU."IdEmpresa" = :IdEmpresa AND USU."IdUsuario" IN (:IdUsuario);