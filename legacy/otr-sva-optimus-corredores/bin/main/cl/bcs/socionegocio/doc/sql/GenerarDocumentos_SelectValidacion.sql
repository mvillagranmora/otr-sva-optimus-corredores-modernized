SELECT
DOC."IdEmpresa"
,DOC."IdCliente"
,DOC."IdDocumento"
,DOC."IdUltimoAnexo"
,DOC."IdTipoDocumento"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" TPO
INNER JOIN "BSCL.Plataforma.BD::DOC.Documento" DOC ON TPO."IdTipoDocumento" = DOC."IdTipoDocumento"
INNER JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANE ON (DOC."IdUltimoAnexo" = ANE."IdAnexo")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = DOC."IdCliente"
WHERE CLI."IdEmpresa" = :IdEmpresa
AND DOC."IdCliente" = :IdCliente
AND DOC."IdTipoDocumento"= :IdTipoDocumento
AND ANE."IdEstado" IN ('PDF','ACT','VCD');