SELECT 
	"IdRangoPatrimonio" AS "IdRangoPatrimonio",
	"DescripcionRango" AS "DescripcionRango" 
		FROM "BSCL.SocioNegocio.BD::PER.RangoPatrimonio"
			WHERE "IdRangoPatrimonio" IN (:IdRangoPatrimonio)