UPDATE IND
SET IND."ValorIndicador" = 'S'
FROM "BSCL.Plataforma.BD::IND.Indicador"  AS IND
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIND ON TIND."IdTipoIndicador" = IND."IdTipoIndicador"
WHERE IND."IdEntidad" = :IdEntidad AND TIND."CodigoTipoIndicador" = :Codigo;
