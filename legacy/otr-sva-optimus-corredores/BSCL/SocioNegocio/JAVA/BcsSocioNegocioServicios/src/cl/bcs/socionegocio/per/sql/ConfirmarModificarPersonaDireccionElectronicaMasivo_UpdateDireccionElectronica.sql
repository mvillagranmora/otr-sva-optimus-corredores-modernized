UPDATE "BSCL.SocioNegocio.BD::PER.DireccionElectronica"
SET "Direccion" = COALESCE(:Direccion, "Direccion")
,"CodigoProposito" = COALESCE(:CodigoProposito, "CodigoProposito")
,"Comentario" = COALESCE(:Comentario, "Comentario")
,"IdEstado" = COALESCE(:IdEstado, "IdEstado")
,"Predeterminado" 	= COALESCE(:Predeterminado, "Predeterminado")
,"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDireccionElectronica" = :IdDireccionElectronica