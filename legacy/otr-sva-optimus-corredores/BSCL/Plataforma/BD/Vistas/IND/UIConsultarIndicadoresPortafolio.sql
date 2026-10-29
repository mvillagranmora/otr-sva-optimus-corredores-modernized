SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio"; 
GO		
CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" AS
WITH tipoIndicadores AS (
	SELECT 
		TCLI."IdTipoCliente",
		TIND.*
	FROM "BSCL.SocioNegocio.BD::CLI.TipoCliente" as TCLI, "BSCL.Plataforma.BD::IND.TipoIndicador" as TIND
	WHERE TCLI."IdEmpresa" = TIND."IdEmpresa" AND TIND."IdEstado" != 'INA'
), indicadoresBase AS (
	SELECT 
		TIND."IdTipoCliente",
		TIND."IdTipoIndicador",
		TIND."IdTipoEntidad",
		TIND."IdEmpresa",
		TIND."ClaseDato",
		TIND."CantidadDecimales",
		TIND."LongitudMinima",
		TIND."LongitudMaxima",
		TIND."NombreTipoIndicador",
		TIND."GrupoIndicador",
		TIND."IdUso",
		TIND."IdEstado",
		TIND."CodigoTipoIndicador",
		TIND."IdTipoPersona",
		IFNULL(IND."IdIndicador", 0) AS "IdIndicador",
		IFNULL(IND."ModeloCliente", 'DEFAULT') AS "ModeloCliente",
		IFNULL(IND."ValorIndicador", TIND."ValorIndicadorDefecto") AS "ValorIndicador"
	FROM tipoIndicadores AS TIND
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND
		ON IND."IdTipoIndicador" = TIND."IdTipoIndicador" 
		AND IND."ModeloCliente" = TIND."IdTipoCliente" 
		AND TIND."IdEmpresa" = IND."IdEmpresa" 
		AND IND."IdEntidad" = 0
)
SELECT 
	CLI."IdCliente",
	CLI."IdPersona",
	CAST(CLI."IdTipoCliente" AS NVARCHAR(40)) AS "IdTipoCliente",
	POR."IdPortafolio",
	CAST(INDBASE."IdTipoEntidad" AS NVARCHAR(40)) AS "IdTipoEntidad",
	CAST(INDBASE."NombreTipoIndicador" AS NVARCHAR(255)) AS "NombreTipoIndicador",
	CAST(INDBASE."GrupoIndicador" AS NVARCHAR(40)) AS "GrupoIndicador",
	CAST(INDBASE."IdUso" AS NVARCHAR(3)) AS "IdUso",
	INDBASE."IdTipoIndicador", 
	CAST(INDBASE."CodigoTipoIndicador" AS NVARCHAR(40)) AS "CodigoTipoIndicador",
	CAST(INDBASE."ClaseDato" AS NVARCHAR(40)) AS "ClaseDato", 
	INDBASE."CantidadDecimales",
	INDBASE."LongitudMinima",
	INDBASE."LongitudMaxima",
	CAST(INDBASE."IdTipoPersona" AS NVARCHAR(10)) AS "IdTipoPersona",
	CAST(INDBASE."ModeloCliente" AS NVARCHAR(40)) AS "ModeloCliente",
	CAST(IFNULL(IND."ValorIndicador", INDBASE."ValorIndicador") AS NVARCHAR(255)) AS "ValorIndicador",
	CAST(IFNULL(DETIND."Codigo", '') AS NVARCHAR(40)) AS "CodigoValorIndicador",
	CAST(IFNULL(DETIND."DetalleIndicador", '') AS NVARCHAR(255)) AS "NombreValorIndicador",
	IFNULL(IND."IdIndicador", 0) AS "IdIndicador",
	INDBASE."IdEmpresa"
FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
	ON POR."IdCliente" = CLI."IdCliente"
LEFT JOIN indicadoresBase AS INDBASE 
	ON CLI."IdTipoCliente" = INDBASE."IdTipoCliente" 
	AND CLI."IdEmpresa" = INDBASE."IdEmpresa" 
	AND INDBASE."IdTipoEntidad" = 'PORTAFOLIO' 	
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND 
	ON IND."IdTipoIndicador" = INDBASE."IdTipoIndicador" 
	AND IND."IdEntidad" = POR."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" AS DETIND
	ON INDBASE."ClaseDato" = 'COMBO'
	AND CAST(DETIND."IdDetalleIndicador" AS NVARCHAR) = COALESCE(IND."ValorIndicador", INDBASE."ValorIndicador")	
	AND DETIND."IdTipoIndicador" = INDBASE."IdTipoIndicador";
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  