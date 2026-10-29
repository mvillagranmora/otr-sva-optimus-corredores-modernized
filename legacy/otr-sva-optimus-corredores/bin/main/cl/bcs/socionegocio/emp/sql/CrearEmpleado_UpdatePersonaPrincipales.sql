UPDATE "BSCL.SocioNegocio.BD::PER.Persona"
SET
	"IdDocumentoIdentidad" = :IdDocumentoIdentidad,
	"IdDocumentoIdentidadAdicional" = :IdDocumentoIdentidadAdicional,
	"IdDireccionElectronicaPrincipal" = :IdDireccionElectronicaPrincipal,
	"IdDireccionFisicaPrincipal" = :IdDireccionFisicaPrincipal,
	"IdTelefonoPrincipal" = :IdTelefonoPrincipal
WHERE "IdPersona" = :IdPersona
