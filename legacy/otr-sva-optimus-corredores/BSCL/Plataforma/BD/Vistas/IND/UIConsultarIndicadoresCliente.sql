SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresCliente"; 
GO		
CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresCliente" AS
SELECT
	CLI."IdCliente",
	CLI."IdPersona",
	CASE PER."Tipo" WHEN 'N' 
		THEN PER."Nombres" || ' ' || PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
		ELSE PER."RazonSocial"
		END AS "NombreCliente",
	CAST(CLI."IdTipoCliente" AS NVARCHAR(40)) AS "IdTipoCliente",
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
	IND."FechaCreacion",
	(CASE
		IND."FechaModificacion" WHEN '0001-01-01 00:00:00' THEN (NULL)
		ELSE IND."FechaModificacion"
	END) AS "FechaModificacion",
	INDBASE."IdEmpresa",
	DOC."DocumentoIdentidad"
FROM
	"BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
LEFT JOIN
 	"BSCL.SocioNegocio.BD::PER.Persona" PER
ON 
	CLI."IdPersona" = PER."IdPersona"
LEFT JOIN (
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
	FROM
		(
		SELECT
			TCLI."IdTipoCliente" ,
			TIND."IdTipoIndicador" ,
			TIND."IdTipoEntidad" ,
			TIND."IdEmpresa" ,
			TIND."ValorIndicadorDefecto" ,
			TIND."ClaseDato" ,
			TIND."NombreTipoIndicador" ,
			TIND."GrupoIndicador" ,
			TIND."CantidadDecimales" ,
			TIND."LongitudMinima" ,
			TIND."LongitudMaxima" ,
			TIND."IdUso" ,
			TIND."IdEstado" ,
			TIND."RequiereAutorizacion" ,
			TIND."CodigoTipoIndicador" ,
			TIND."IdTipoPersona" ,
			TIND."RequiereFechaVencimiento" ,
			TIND."IdIndicadorMigracion" ,
			TIND."IdCarga" ,
			TIND."Visible"
		FROM
			"BSCL.SocioNegocio.BD::CLI.TipoCliente" AS TCLI,
			"BSCL.Plataforma.BD::IND.TipoIndicador" AS TIND
		WHERE
			TIND."IdEstado" != 'INA'
			AND TCLI."IdEmpresa" = TIND."IdEmpresa"
			AND TIND."IdTipoEntidad" = 'CLIENTE' ) AS TIND
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON
		IND."IdTipoIndicador" = TIND."IdTipoIndicador"
		AND IND."ModeloCliente" = TIND."IdTipoCliente"
		AND TIND."IdEmpresa" = IND."IdEmpresa") AS INDBASE ON
	CLI."IdTipoCliente" = INDBASE."IdTipoCliente"
	AND CLI."IdEmpresa" = INDBASE."IdEmpresa"
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON
	IND."IdTipoIndicador" = INDBASE."IdTipoIndicador"
	AND IND."IdEntidad" = CASE
		WHEN INDBASE."IdTipoEntidad" = 'PERSONA' THEN CLI."IdPersona"
		WHEN INDBASE."IdTipoEntidad" = 'CLIENTE' THEN CLI."IdCliente"
	END
LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" AS DETIND ON
	INDBASE."ClaseDato" = 'COMBO'
	AND CAST(DETIND."IdDetalleIndicador" AS NVARCHAR) = COALESCE(IND."ValorIndicador",
	INDBASE."ValorIndicador")
	AND DETIND."IdTipoIndicador" = INDBASE."IdTipoIndicador"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON
    PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad";
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  