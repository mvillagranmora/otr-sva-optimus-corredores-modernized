SELECT IND."IdIndicador"          AS "IdIndicador"
	 , IND."IdTipoIndicador"      AS "IdTipoIndicador"
	 , IND."IdEntidad"            AS "IdEntidad"
	 , IND."IdTipoEntidad"        AS "IdTipoEntidad"
	 , IND."ValorIndicador"       AS "ValorIndicador"
	 , TIND."CodigoTipoIndicador" AS "CodigoTipoIndicador"
FROM "BSCL.Plataforma.BD::IND.Indicador" IND
	 LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIND ON (
		TIND."IdTipoIndicador" = IND."IdTipoIndicador"
		AND TIND."IdTipoIndicador" IS NOT NULL
	)
WHERE IND."IdTipoEntidad" = :IdTipoEntidad
  AND IND."IdEmpresa" = :IdEmpresa
  AND IND."IdEntidad" IN (:IdsEntidades)
  AND TIND."CodigoTipoIndicador" = :CodigoTipoIndicador
  AND TIND."IdEstado" = :IdEstadoTipoIndicador;