SELECT "FechaVigencia", "ValidaFechaVigencia"
FROM "BSCL.Plataforma.BD::DOC.Documento" DOC
INNER JOIN "BSCL.Plataforma.BD::DOC.Anexo" ANX on DOC."IdDocumento" = ANX."IdDocumento"
INNER JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" TDO on DOC."IdTipoDocumento" = TDO."IdTipoDocumento"
WHERE "IdCliente" = :p_IdCliente AND DOC."IdTipoDocumento" = :TipoDocumento AND "IdEstado" = 'ACT';