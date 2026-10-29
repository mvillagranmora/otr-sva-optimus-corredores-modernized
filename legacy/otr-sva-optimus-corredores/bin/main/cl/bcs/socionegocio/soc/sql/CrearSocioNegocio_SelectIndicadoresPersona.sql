SELECT 
IFNULL(IND."IdIndicador",0) AS "IdIndicador",
TIPIND."IdTipoIndicador",
IND."ValorIndicador"
FROM  "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIPIND
LEFT JOIN  "BSCL.Plataforma.BD::IND.Indicador" AS IND ON TIPIND."IdTipoIndicador" = IND."IdTipoIndicador" AND IND."IdEntidad" = :p_IdPersona
WHERE 
TIPIND."IdTipoEntidad" = 'PERSONA'
AND TIPIND."CodigoTipoIndicador" = :p_CodigoTipoIndicador;
