SELECT 
"IdDireccionElectronica"
,"Direccion"
,"CodigoProposito"
,"Comentario"
,"IdPersona"
,"Predeterminado"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica"
WHERE "IdPersona" IN (:IdPersona) AND "Borrado" = 'N';