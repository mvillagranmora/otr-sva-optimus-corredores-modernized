SELECT
	DE."IdDireccionElectronica" AS "IdDireccionElectronica",
	DE."IdPersona" AS "IdPersona",
	COALESCE(DE."Direccion", '') AS "Direccion"
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DE
WHERE DE."IdDireccionElectronica" IN (:IdDireccionElectronica);
