SELECT IND."IdIndicador", IND."IdEntidad" AS "IdInterno", IND."IdTipoIndicador", TIPO."CodigoTipoIndicador"
FROM "BSCL.Plataforma.BD::IND.Indicador" AS IND
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIPO ON TIPO."IdTipoIndicador" = IND."IdTipoIndicador"
WHERE IND."IdTipoEntidad" = 'PORTAFOLIO' AND IND."IdEntidad" IN (:IdsPortafolio);