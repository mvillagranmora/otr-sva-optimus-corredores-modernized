SELECT
	ATR."IdEntidad" AS "IdCliente",
	TPO."IdTipoIndicador",
	IFNULL(ATR."ValorIndicador", 'N') AS "ValorIndicador"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO
INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON
(
	ATR."IdTipoIndicador" = TPO."IdTipoIndicador" AND ATR."IdEntidad" IN  (:p_IdCliente)
)
INNER JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" DOC ON TPO."CodigoTipoIndicador" = DOC."CodigoTipoIndicador" AND DOC."IdTipoDocumento" = :p_IdTipoDocumento
WHERE (TPO."IdTipoEntidad" = 'CLIENTE')
AND TPO."IdUso" = 'REQ'
AND TPO."IdEstado" = 'ACT'
AND TPO."IdEmpresa" = :p_IdEmpresa;