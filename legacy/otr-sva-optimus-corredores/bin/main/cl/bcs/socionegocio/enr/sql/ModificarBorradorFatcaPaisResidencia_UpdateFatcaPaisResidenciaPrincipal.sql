UPDATE "BSCL.SocioNegocio.BD::ENR.FatcaPaisResidencia"
SET "IdPaisResidencia" = :IdPaisResidencia,
	"TIN" = :TIN,
	"IdCausaNoTin" = :IdCausaNoTin,
	"Observacion" = :Observacion
WHERE "IdEnrolamiento" = :IdEnrolamiento
AND "PaisResidenciaPrincipal" = 'S';