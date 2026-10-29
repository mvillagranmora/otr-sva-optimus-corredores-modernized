SELECT
	ind."ValorIndicador" AS "OperaConSaldoNegativo",
	ind."IdEntidad" AS "IdPortafolio"
FROM "BSCL.Plataforma.BD::IND.Indicador" ind
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" tind ON tind."IdTipoIndicador" = ind."IdTipoIndicador" AND tind."CodigoTipoIndicador" = 'GEN_OCSNEG'
WHERE ind."IdEntidad" IN (:IdPortafolio)