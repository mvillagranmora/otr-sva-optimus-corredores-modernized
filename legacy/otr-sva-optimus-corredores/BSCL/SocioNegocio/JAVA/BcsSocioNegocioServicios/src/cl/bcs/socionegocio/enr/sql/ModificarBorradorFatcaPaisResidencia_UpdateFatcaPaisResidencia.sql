UPDATE "BSCL.SocioNegocio.BD::ENR.FatcaPaisResidencia"
SET "IdPaisResidencia" = :IdPaisResidencia,
	"TIN" = :TIN,
	"IdCausaNoTin" = :IdCausaNoTin,
	"Observacion" = :Observacion,
	"PaisResidenciaPrincipal" = :PaisResidenciaPrincipal
WHERE "IdEnrolamiento" = :IdEnrolamiento
AND "IdPaisResidencia" = :IdPaisResidencia;