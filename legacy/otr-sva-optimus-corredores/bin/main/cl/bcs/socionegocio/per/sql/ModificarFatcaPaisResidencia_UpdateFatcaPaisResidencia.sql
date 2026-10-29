UPDATE "BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia"
SET "IdPaisResidencia" = :IdPaisResidencia,
	"TIN" = :TIN,
	"IdCausaNoTin" = :IdCausaNoTin,
	"Observacion" = :Observacion,
	"PaisResidenciaPrincipal" = :PaisResidenciaPrincipal
WHERE "IdPersona" = :IdPersona
AND "IdPaisResidencia" = :IdPaisResidencia;