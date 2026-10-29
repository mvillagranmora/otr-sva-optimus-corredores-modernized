SELECT
	CD."IdCargaMasivaDetalle",
	CD."IdCargaMasiva",
	CD."DocumentoIdentidad",
	CD."IdPortafolio",
	CD."CodigoPortafolio",
	CD."Entidad",
	CD."CodigoIndicador",
	CD."ValorIndicador",
	CD."IdEstado"
FROM "BSCL.SocioNegocio.BD::CAR.CargaMasivaDetalle" CD
LEFT JOIN "BSCL.SocioNegocio.BD::CAR.CargaMasiva" CM ON CM."IdCargaMasiva" = CD."IdCargaMasiva"
WHERE CD."IdCargaMasiva" IN (:IdCargaMasiva);