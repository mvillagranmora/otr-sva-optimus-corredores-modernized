UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET 
	"IdDireccionEnvioCorrespondencia" = COALESCE(:IdDireccionEnvioCorrespondencia, 0),
	"IdTelefonoContacto" = COALESCE(:IdTelefonoContacto, 0)
WHERE "IdPortafolio" IN (:IdPortafolio);