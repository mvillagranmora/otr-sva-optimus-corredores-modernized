SELECT *
FROM "BSCL.Plataforma.BD::DOC.Documento" AS DOC
LEFT JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" AS TDOC ON TDOC."IdTipoDocumento" = DOC."IdTipoDocumento"
WHERE DOC."IdCliente" = :IdCliente AND DOC."IdTipoDocumento" IN(:IdTipoDocumentos) AND TDOC."IdTipoPersona" IN ('A',:IdTipoPersona);