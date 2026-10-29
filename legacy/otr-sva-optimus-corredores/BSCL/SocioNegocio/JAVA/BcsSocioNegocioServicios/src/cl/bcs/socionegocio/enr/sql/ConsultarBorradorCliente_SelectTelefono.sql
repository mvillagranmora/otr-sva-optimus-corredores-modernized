SELECT 
"IdTelefono"
,"IdPais"
,"Numero"
,"Anexo"
,"CodigoProposito"
,"Comentario"
,"IdEnrolamiento"
,"IdTipoTelefono"
,"Predeterminado"
,"IdTelefonoPersona"
FROM "BSCL.SocioNegocio.BD::ENR.Telefono"
WHERE "IdEnrolamiento" IN (:IdEnrolamiento);
