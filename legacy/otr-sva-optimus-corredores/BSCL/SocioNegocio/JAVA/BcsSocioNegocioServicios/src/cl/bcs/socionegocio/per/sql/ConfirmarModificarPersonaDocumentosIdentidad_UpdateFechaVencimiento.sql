UPDATE "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
SET "FechaVencimiento" = :p_ValorNuevo,
	"FechaModificacion" = CURRENT_UTCTIMESTAMP,
	"IdEstado" = (CASE WHEN :p_ValorNuevo >= CURRENT_UTCTIMESTAMP THEN 'ACT' ELSE 'VCD' END)
WHERE "IdDocumentoIdentidad" = :p_IdDocumentoIdentidad
