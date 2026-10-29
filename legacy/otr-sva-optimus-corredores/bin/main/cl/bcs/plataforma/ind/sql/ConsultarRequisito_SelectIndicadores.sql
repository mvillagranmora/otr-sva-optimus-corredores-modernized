WITH Indicadores AS (
	SELECT
		TPO."IdTipoIndicador",
		TPO."NombreTipoIndicador",
		TPO."ClaseDato",
		TPO."CodigoTipoIndicador",
		TPO."ValorIndicadorDefecto",
		TPO."GrupoIndicador",
		TPO."IdUso",
		TPO."CantidadDecimales",
		TPO."LongitudMinima",
		TPO."LongitudMaxima",
		TPO."IdTipoEntidad",
	  	TPO."IdTipoPersona",
	  	TPO."IdEmpresa"
	FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TPO
	WHERE
			(:p_IdTipoEntidad = '' OR TPO."IdTipoEntidad" = :p_IdTipoEntidad)
		AND TPO."IdUso" = :p_IdUso
		AND TPO."IdEstado" = 'ACT'
		AND (:p_CodigoTipoIndicador = '' OR TPO."CodigoTipoIndicador" = :p_CodigoTipoIndicador)
		AND (:p_GrupoIndicador = '' OR TPO."GrupoIndicador" = :p_GrupoIndicador)
		AND TPO."IdEmpresa" = :p_IdEmpresa
),
TipoDocumentoIndicador AS 
(
	SELECT tdoc."IdTipoDocumento"
		, tdoc."IdClase"
		, doci."IdTipoIndicador"
	FROM "BSCL.Plataforma.BD::DOC.TipoDocumento" tdoc
	INNER JOIN "BSCL.Plataforma.BD::DOC.TipoDocumentoIndicador" doci ON doci."IdTipoDocumento" = tdoc."IdTipoDocumento"
),
IndicadoreTipoDocumento AS (
	SELECT  tind."IdTipoIndicador"
		, doci."IdTipoDocumento"
		, doci."IdClase"
		, ROW_NUMBER() OVER (PARTITION BY tind."CodigoTipoIndicador" ORDER BY doci."IdClase" DESC) AS "RowNumber"
	FROM Indicadores tind
	LEFT JOIN TipoDocumentoIndicador doci ON tind."IdTipoIndicador" = doci."IdTipoIndicador"
)
SELECT IFNULL(ATR."IdIndicador",0) AS "IdIndicador"
	, TPO."IdTipoIndicador"
	, TPO."NombreTipoIndicador"
	, TPO."ClaseDato"
	, TPO."CodigoTipoIndicador"
	, CASE 
		WHEN ITDOC."IdTipoDocumento" IS NULL THEN 'N' 
		ELSE 'S' 
	  END as "RelacionDocumento"
	, IFNULL(ATR."ValorIndicador",TPO."ValorIndicadorDefecto") as "ValorIndicador"
	, IFNULL(ATR."IdEstadoAutorizacion",'ACT') as "IdEstadoAutorizacion"
	, TPO."GrupoIndicador"
	, TPO."IdUso"
	, TPO."CantidadDecimales"
	, TPO."LongitudMinima"
	, TPO."LongitudMaxima"
	, TPO."IdTipoEntidad"
	, IFNULL(ATR."IdEntidad",:p_IdEntidad) AS "IdEntidad"
	, IFNULL(ATR."ModeloCliente",'') AS "ModeloCliente"
  	, TPO."IdTipoPersona"
  	, ITDOC."IdTipoDocumento"
  	, IFNULL(ITDOC."IdClase", 0) AS "IdClase"
FROM IndicadoreTipoDocumento ITDOC
INNER JOIN Indicadores TPO ON TPO."IdTipoIndicador" = ITDOC."IdTipoIndicador"
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" ATR ON
(
	ATR."IdTipoIndicador" = TPO."IdTipoIndicador" AND (ATR."IdEntidad" = :p_IdEntidad OR :p_IdEntidad = 0)
	AND (:p_ModeloCliente = '' OR (ATR."ModeloCliente" = :p_ModeloCliente AND ATR."IdEntidad" = 0))--Si viene modelo cliente el Id de Entidad siempre es 0
	AND (:p_IdIndicador = 0 OR ATR."IdIndicador" = :p_IdIndicador)
)
WHERE ITDOC."RowNumber" = 1;