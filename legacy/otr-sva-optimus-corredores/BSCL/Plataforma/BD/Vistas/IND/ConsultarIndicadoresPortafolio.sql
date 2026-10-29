SET SCHEMA "BSCL";
GO
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"  AS

WITH tipoIndicadores AS(
	SELECT 
		TCLI."IdTipoCliente",
		TIND.*
	FROM "BSCL.SocioNegocio.BD::CLI.TipoCliente" as TCLI, "BSCL.Plataforma.BD::IND.TipoIndicador" as TIND
	WHERE TCLI."IdEmpresa" = TIND."IdEmpresa" AND TIND."IdEstado"!='INA'
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
		IFNULL(IND."IdIndicador",0) as "IdIndicador",
		IFNULL(IND."ModeloCliente",'DEFAULT') as "ModeloCliente",
		IFNULL(IND."ValorIndicador",TIND."ValorIndicadorDefecto") as "ValorIndicador"
	FROM tipoIndicadores as TIND
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND
		ON IND."IdTipoIndicador" = TIND."IdTipoIndicador" AND IND."ModeloCliente" = TIND."IdTipoCliente" AND TIND."IdEmpresa" = IND."IdEmpresa" AND IND."IdEntidad" = 0
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
		INDBASE."IdTipoIndicador", -- nuevo
		CAST(INDBASE."CodigoTipoIndicador" AS NVARCHAR(40)) AS "CodigoTipoIndicador",
		CAST(INDBASE."ClaseDato" AS NVARCHAR(40)) AS "ClaseDato", -- nuevo
		INDBASE."CantidadDecimales",
		INDBASE."LongitudMinima",
		INDBASE."LongitudMaxima",
		CAST(INDBASE."IdTipoPersona" AS NVARCHAR(10)) AS "IdTipoPersona",
		CAST(INDBASE."ModeloCliente" AS NVARCHAR(40)) AS "ModeloCliente",
		CAST(IFNULL(IND."ValorIndicador",INDBASE."ValorIndicador") AS NVARCHAR(255)) as "ValorIndicador",
		CAST(IFNULL(DETIND."Codigo",'') AS NVARCHAR(40)) as "CodigoValorIndicador", -- nuevo
		CAST(IFNULL(DETIND."DetalleIndicador",'') AS NVARCHAR(255)) as "NombreValorIndicador", -- nuevo
		IFNULL(IND."IdIndicador",0) as "IdIndicador",
		INDBASE."IdEmpresa"
	FROM "BSCL.Portafolio.BD::POR.Portafolio" as POR
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI
		ON (POR."IdCliente" = CLI."IdCliente")
	LEFT JOIN indicadoresBase AS INDBASE 
		ON CLI."IdTipoCliente" = INDBASE."IdTipoCliente" AND CLI."IdEmpresa" = INDBASE."IdEmpresa" AND INDBASE."IdTipoEntidad" = 'PORTAFOLIO' 	
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND 
		ON IND."IdTipoIndicador" = INDBASE."IdTipoIndicador" AND IND."IdEntidad" = POR."IdPortafolio"
	LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" as DETIND
		ON  INDBASE."ClaseDato" = 'COMBO'
		AND	CAST(DETIND."IdDetalleIndicador" as NVARCHAR) = COALESCE(IND."ValorIndicador",INDBASE."ValorIndicador")	
		AND DETIND."IdTipoIndicador" = INDBASE."IdTipoIndicador";
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

