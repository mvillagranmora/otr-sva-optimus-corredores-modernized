SELECT 
"IdTelefono"
,"IdPais"
,"Numero"
,"Anexo"
,"CodigoProposito"
,"Comentario"
,"IdPersona"
,"IdTipoTelefono"
,"Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.Telefono"
WHERE "IdPersona" IN (:IdPersona) AND "Borrado" = 'N';