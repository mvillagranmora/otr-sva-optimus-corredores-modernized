UPDATE "BSCL.SocioNegocio.BD::PER.DireccionFisica"
SET "IdDivisionPais" = (CASE WHEN :p_ValorNuevo <> '' THEN :p_ValorNuevo ELSE 0 END),
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDireccionFisica" = :p_IdDireccionFisica
