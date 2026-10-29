UPDATE "BSCL.SocioNegocio.BD::ENR.Telefono"
SET "IdPais" = :IdPais
,"Numero" = :Numero
,"Anexo" = :Anexo
,"CodigoProposito" = :CodigoProposito
,"Comentario" = :Comentario
,"IdTipoTelefono" = :IdTipoTelefono
,"Predeterminado" = :Predeterminado
WHERE "IdTelefono" = :IdTelefono;
