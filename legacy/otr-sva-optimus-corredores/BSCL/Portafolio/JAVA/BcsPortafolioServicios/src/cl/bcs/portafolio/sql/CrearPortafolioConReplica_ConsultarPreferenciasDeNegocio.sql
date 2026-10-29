SELECT 
    ATR."IdIndicador",
    ATR."IdTipoIndicador",
    ATR."ValorIndicador"
FROM "BSCL.Plataforma.BD::IND.Indicador" ATR
INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TPO 
    ON ATR."IdTipoIndicador" = TPO."IdTipoIndicador"
WHERE ATR."IdEntidad" = :IdEntidad
AND ATR."IdEntidad" != 0
AND TPO."IdTipoEntidad" = 'PORTAFOLIO'
AND TPO."IdUso" = 'PRE'
AND TPO."IdEstado" = 'ACT'
AND TPO."IdEmpresa" = :IdEmpresa
AND ATR."ValorIndicador" IS NOT NULL
AND ATR."ValorIndicador" <> ''