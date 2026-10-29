SELECT
	TPO."GrupoIndicador"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON 
(
	ATR."IdIndicador" = TPO."IdTipoIndicador"
	AND (ATR."IdEntidad" = :p_IdEntidad OR :p_IdEntidad = 0)
	AND (:p_ModeloCliente = '' OR (ATR."ModeloCliente" = :p_ModeloCliente AND ATR."IdEntidad" = 0))--Si viene modelo cliente el Id de Entidad siempre es 0
	AND (:p_IdIndicador = 0 OR ATR."IdIndicador" = :p_IdIndicador)
)
WHERE
	(:p_IdTipoEntidad = '' OR TPO."IdTipoEntidad" = :p_IdTipoEntidad)
AND TPO."IdUso" = :p_IdUso
AND TPO."IdEstado" = 'ACT'
AND TPO."IdEmpresa" = :p_IdEmpresa
AND (:p_CodigoTipoIndicador = '' OR TPO."CodigoTipoIndicador" = :p_CodigoTipoIndicador)
AND (:p_GrupoIndicador = '' OR TPO."GrupoIndicador" = :p_GrupoIndicador)
GROUP BY
	TPO."GrupoIndicador";