SELECT COUNT(1) AS "Count"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON ATR."IdTipoIndicador" = TPO."IdTipoIndicador"
WHERE ATR."ModeloCliente" = :CLI_IdTipoCliente AND TPO."IdTipoEntidad" = 'PORTAFOLIO'  AND ATR."ValorIndicador" = 'S'
AND (TPO."CodigoTipoIndicador" = 'ORDOCUSTRV' OR TPO."CodigoTipoIndicador" = 'ORDOCUSTRF')
AND TPO."IdEmpresa" = :IdEmpresa