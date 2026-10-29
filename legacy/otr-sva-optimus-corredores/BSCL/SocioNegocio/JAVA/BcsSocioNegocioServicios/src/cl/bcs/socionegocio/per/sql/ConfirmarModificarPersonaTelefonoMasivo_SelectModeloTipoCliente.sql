UPDATE "BSCL.SocioNegocio.BD::PER.Telefono"
SET "IdPais" = COALESCE(:IdPais, "IdPais")
,"IdTipoTelefono" = COALESCE(:IdTipoTelefono, "IdTipoTelefono")
,"Numero" = COALESCE(:Numero, "Numero")
,"Anexo" = COALESCE(:Anexo, "Anexo")
,"CodigoProposito" = COALESCE(:CodigoProposito, "CodigoProposito")
,"Comentario" = COALESCE(:Comentario, "Comentario")
,"Predeterminado" = COALESCE(:Predeterminado, "Predeterminado")
,"IdEstado" = COALESCE(:IdEstado, "IdEstado")
WHERE "IdTelefono" = :IdTelefono