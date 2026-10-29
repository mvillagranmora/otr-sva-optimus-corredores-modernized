UPDATE "BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia"
SET "IdPaisResidencia" = :IdPaisResidencia,
	"TIN" = :TIN,
	"IdCausaNoTin" = :IdCausaNoTin,
	"Observacion" = :Observacion
WHERE "IdPersona" = :IdPersona
AND "PaisResidenciaPrincipal" = 'S';