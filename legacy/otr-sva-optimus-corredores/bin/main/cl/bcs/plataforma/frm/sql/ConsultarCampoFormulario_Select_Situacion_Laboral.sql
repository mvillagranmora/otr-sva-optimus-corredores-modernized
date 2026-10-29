SELECT
	TO_NVARCHAR("CodigoSituacionLaboral") AS "CodigoSituacionLaboral"
	,"SituacionLaboral"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.SituacionLaboral"
WHERE TO_NVARCHAR("CodigoSituacionLaboral") in (:IdsCodigoSituacionLaboral);