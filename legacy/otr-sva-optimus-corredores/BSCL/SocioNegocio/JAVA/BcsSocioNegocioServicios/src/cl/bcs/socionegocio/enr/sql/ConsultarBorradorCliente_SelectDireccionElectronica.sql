SELECT 
"IdDireccionElectronica"
,"Direccion"
,"CodigoProposito"
,"Comentario"
,"IdEnrolamiento"
,"Predeterminado"
,"IdDireccionElectronicaPersona"
,"IdDireccionElectronicaPersona" AS "IdDireccionElectronicaReal"
FROM "BSCL.SocioNegocio.BD::ENR.DireccionElectronica"
WHERE "IdEnrolamiento" IN (:IdEnrolamiento);
