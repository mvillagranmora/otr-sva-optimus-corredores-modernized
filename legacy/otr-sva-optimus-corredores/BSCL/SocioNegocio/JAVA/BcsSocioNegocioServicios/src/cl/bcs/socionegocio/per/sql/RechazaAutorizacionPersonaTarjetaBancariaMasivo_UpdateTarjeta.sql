UPDATE "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"
SET 
	"IdEstadoAutorizacion" = :p_IdEstadoAutorizacion,
	"IdEstado" = CASE WHEN :p_IdEstado IS NOT NULL THEN :p_IdEstado ELSE "IdEstado" END
WHERE "IdTarjetaBancaria" = :p_IdTarjetaBancaria
