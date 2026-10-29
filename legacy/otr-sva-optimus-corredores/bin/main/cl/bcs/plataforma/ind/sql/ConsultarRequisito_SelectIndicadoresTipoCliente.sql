SELECT
	IFNULL(ATR."IdIndicador",0) AS "IdIndicador",
	TPO."IdTipoIndicador",
	TPO."NombreTipoIndicador",
	TPO."ClaseDato",
	TPO."CodigoTipoIndicador",
	CASE 
		WHEN DOC."IdTipoDocumento" IS NULL THEN 'N' 
		WHEN (DOC."IdTipoDocumento" IS NOT NULL) AND DOC."IdClase" = 1 THEN 'N' 
		ELSE 'S' 
	END as "RelacionDocumento",
	IFNULL(ATR."ValorIndicador",TPO."ValorIndicadorDefecto") as "ValorIndicador",
	IFNULL(ATR."IdEstadoAutorizacion",'ACT') as "IdEstadoAutorizacion",
	TPO."GrupoIndicador",
	TPO."IdUso",
	TPO."CantidadDecimales",
	TPO."LongitudMinima",
	TPO."LongitudMaxima",
	TPO."IdTipoEntidad",
	IFNULL(ATR."IdEntidad",0) AS "IdEntidad",
	IFNULL(ATR."ModeloCliente",'') AS "ModeloCliente",
	TPO."IdTipoPersona",
	DOC."IdTipoDocumento",
	IFNULL(DOC."IdClase",0) AS "IdClase"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON
(
	(ATR."IdTipoIndicador" = TPO."IdTipoIndicador")
	AND (:p_ModeloCliente = '' OR (ATR."ModeloCliente" = :p_ModeloCliente AND ATR."IdEntidad" = 0))--Si viene modelo cliente el Id de Entidad siempre es 0
	AND (:p_IdIndicador = 0 OR ATR."IdIndicador" = :p_IdIndicador)
	AND ATR."IdEntidad" = :p_IdEntidad
)
INNER JOIN #ConsultarRequisito_Indicadores IND ON  --- Indicadores sin valor
(
	IND."CodigoTipoIndicador" = TPO."CodigoTipoIndicador"
	AND IND."GrupoIndicador" = TPO."GrupoIndicador"
	AND IND."ModeloCliente" = (CASE WHEN IND."ModeloCliente" = '' THEN IND."ModeloCliente" ELSE :p_IdTipoCliente END)	
)
LEFT JOIN "BSCL.Plataforma.BD::DOC.TipoDocumento" DOC ON TPO."CodigoTipoIndicador" = DOC."CodigoTipoIndicador"
WHERE
	TPO."IdTipoEntidad" = (CASE WHEN :p_IdTipoEntidad = '' THEN ATR."IdTipoEntidad" ELSE :p_IdTipoEntidad END) --'CLIENTE'
AND TPO."IdUso" = :p_IdUso 
AND TPO."IdEstado" = 'ACT'
AND (:p_CodigoTipoIndicador = '' OR  TPO."CodigoTipoIndicador" = :p_CodigoTipoIndicador)
AND (:p_GrupoIndicador = '' OR  TPO."GrupoIndicador" = :p_GrupoIndicador)
AND TPO."IdEmpresa" = :p_IdEmpresa;