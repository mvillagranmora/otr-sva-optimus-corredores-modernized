SET SCHEMA BSCL;

DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClienteNCG380Detallado";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClienteNCG380Detallado" AS
SELECT	"IdCliente",
	MAX(CASE WHEN "CodigoTipoIndicador" = 'TCLICMF' 	THEN "NombreValorIndicador" END) AS "TipoCliente",
	MAX(CASE WHEN "CodigoTipoIndicador" = 'CC' 			THEN "ValorIndicador" 		END) AS "ContratoCustodia",
	MAX(CASE WHEN "CodigoTipoIndicador" = 'CONFINT' 	THEN "ValorIndicador" 		END) AS "ConflictoInteres",
	MAX(CASE WHEN "CodigoTipoIndicador" = 'INFPRV' 		THEN "ValorIndicador" 		END) AS "InfoPrivilegiadaArt171",
	MAX(CASE WHEN "CodigoTipoIndicador" = 'CI1237' 		THEN "NombreValorIndicador" END) AS "CargoSVS171",
	MAX(CASE WHEN "CodigoTipoIndicador" = 'RELCORR' 	THEN "ValorIndicador"	 	END) AS "RelCorredoraArt100",
	MAX(CASE WHEN "CodigoTipoIndicador" = 'NCG380' 		THEN "NombreValorIndicador" END) AS "RelCorredora_NCG380",
	MAX(CASE WHEN "CodigoTipoIndicador" = '1064PROP' 	THEN "NombreValorIndicador" END) AS "RelCorredora_CIRC1064",
	MAX(CASE WHEN "CodigoTipoIndicador" = 'NCG69PROP' 	THEN "NombreValorIndicador" END) AS "Cargo_NCG_68_69",
	MAX(CASE WHEN "CodigoTipoIndicador" = 'PEP' 		THEN "ValorIndicador" 		END) AS "PersonaExpuestaPoliticamente",
	MAX(CASE WHEN "CodigoTipoIndicador" = 'CCG' 		THEN "ValorIndicador" 		END) AS "ContratoCondicionesGenerales"
FROM (

	SELECT "IdCliente","CodigoTipoIndicador","IdIndicador","ValorIndicador","NombreValorIndicador"
	FROM (
		SELECT
				CLI."IdCliente",
				CAST(INDBASE."IdTipoEntidad" AS NVARCHAR(40)) AS "IdTipoEntidad",
				INDBASE."IdTipoIndicador",
				CAST(INDBASE."CodigoTipoIndicador" AS NVARCHAR(40)) AS "CodigoTipoIndicador",
				CAST(INDBASE."ClaseDato" AS NVARCHAR(40)) AS "ClaseDato",
				CAST(IFNULL(IND."ValorIndicador", INDBASE."ValorIndicador") AS NVARCHAR(255)) AS "ValorIndicador",
				CAST(IFNULL(DETIND."DetalleIndicador", '') AS NVARCHAR(255)) AS "NombreValorIndicador",
				IFNULL(IND."IdIndicador", 0) AS "IdIndicador",
				(CASE
					IND."FechaModificacion" WHEN '0001-01-01 00:00:00' THEN (NULL)
					ELSE IND."FechaModificacion"
				END) AS "FechaModificacion",
				INDBASE."IdEmpresa"
			FROM
				"BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
			LEFT JOIN (
				SELECT
					TIND."IdTipoCliente",
					TIND."IdTipoIndicador",
					TIND."IdTipoEntidad",
					TIND."IdEmpresa",
					TIND."ClaseDato",
					TIND."CodigoTipoIndicador",
					IFNULL(IND."IdIndicador", 0) AS "IdIndicador",
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
						TIND."CodigoTipoIndicador"
					FROM
						"BSCL.SocioNegocio.BD::CLI.TipoCliente" AS TCLI,
						"BSCL.Plataforma.BD::IND.TipoIndicador" AS TIND
					WHERE
						TIND."IdEstado" != 'INA'
						AND TCLI."IdEmpresa" = TIND."IdEmpresa"
						AND TIND."IdTipoEntidad" IN ('CLIENTE','PERSONA') ) AS TIND
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
				INDBASE."ClaseDato" IN ('COMBO', 'CHECKBOX') 
				AND CAST(DETIND."IdDetalleIndicador" AS NVARCHAR) = COALESCE(IND."ValorIndicador",
				INDBASE."ValorIndicador")
				AND DETIND."IdTipoIndicador" = INDBASE."IdTipoIndicador"
	)
	WHERE "IdTipoEntidad" = 'CLIENTE' OR  "IdTipoEntidad" = 'PERSONA'
) 
GROUP BY "IdCliente"

COMMIT;