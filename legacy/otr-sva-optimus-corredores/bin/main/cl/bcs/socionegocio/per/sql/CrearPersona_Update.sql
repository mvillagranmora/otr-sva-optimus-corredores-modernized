UPDATE "BSCL.SocioNegocio.BD::PER.Persona"
SET 
	"IdDocumentoIdentidad" = :p_IdDocumentoIdentidad,
	"IdDireccionElectronicaPrincipal" = :p_IdDireccionElectronicaPrincipal,
	"IdDireccionFisicaPrincipal" = :p_IdDireccionFisicaPrincipal,
	"IdTelefonoPrincipal" = :p_IdTelefonoPrincipal
WHERE "IdPersona" = :p_IdPersona;