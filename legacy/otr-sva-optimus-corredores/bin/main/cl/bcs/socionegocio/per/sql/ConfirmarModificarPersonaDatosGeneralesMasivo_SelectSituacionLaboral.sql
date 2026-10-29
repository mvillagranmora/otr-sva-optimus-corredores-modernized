SELECT 
	"IdSituacionLaboral" AS "IdSituacionLaboral",
	"CodigoSituacionLaboral" AS "CodigoSituacionLaboral", 
	"SituacionLaboral" AS "SituacionLaboral" 
		FROM "BSCL.SocioNegocio.BD::PER.SituacionLaboral"
			WHERE "CodigoSituacionLaboral" IN (:CodigoSituacionLaboral)