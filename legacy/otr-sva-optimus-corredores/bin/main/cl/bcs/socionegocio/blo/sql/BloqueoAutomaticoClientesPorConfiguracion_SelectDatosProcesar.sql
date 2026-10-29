SELECT
    cli."IdCliente",
    cli."IdEstado" AS "IdEstadoCliente",
    conf."IdTipoDocumento",	
    doc."IdDocumento",
    doc."IdUltimoAnexo",
    anx."IdEstado" AS "IdEstadoAnexo",
    ANX."FechaVigencia"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" cli
JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarBloqueoAutomaticoClientes" conf
    ON cli."IdEmpresa" = conf."IdEmpresa"
LEFT JOIN "BSCL.Plataforma.BD::DOC.Documento" doc
    ON doc."IdTipoDocumento" = conf."IdTipoDocumento"
    AND doc."IdCliente" = cli."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD::DOC.Anexo" anx
    ON anx."IdDocumento" = doc."IdDocumento"
    AND anx."IdAnexo" = doc."IdUltimoAnexo"
WHERE cli."IdEmpresa" = :IdEmpresa
    AND conf."IdEstadoTipoDocumento" = 'ACT'
ORDER BY cli."IdCliente", conf."IdTipoDocumento";
