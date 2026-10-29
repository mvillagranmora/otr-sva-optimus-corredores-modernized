WITH
	IndicadorClientePortafolio AS(
		SELECT
			 INDCLI."ValorIndicador"
			,INDCLI."IdCliente"
			,CASE WHEN INDCLI."IdPortafolioDefecto" = 0 THEN COALESCE(POR."IdPortafolio",0) ELSE INDCLI."IdPortafolioDefecto" END AS "IdPortafolio"
			,COALESCE(INDCLI."NombreCompleto",'') AS "NombreCompleto"
			,ROW_NUMBER() OVER (PARTITION BY INDCLI."ValorIndicador" ORDER BY POR."IdPortafolio" DESC) AS "POS2"
		FROM (
			SELECT
				 COALESCE(IND."ValorIndicador",'') AS "ValorIndicador"
				,COALESCE(IND."IdEntidad",0) AS "IdCliente"
				,COALESCE(CLI."IdPortafolioDefecto",0) AS "IdPortafolioDefecto"
				,CASE WHEN PER."Tipo" = 'N' THEN PER."Nombres"||' '||PER."ApellidoPaterno"||' '||PER."ApellidoMaterno" ELSE PER."RazonSocial" END AS "NombreCompleto"
				,ROW_NUMBER() OVER (PARTITION BY IND."ValorIndicador" ORDER BY CLI."IdCliente" DESC) AS "POS1"
			FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TIP
			LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND
				ON (TIP."IdTipoIndicador" = IND."IdTipoIndicador" AND
					IND."ValorIndicador" IN (:ValorIndicador))
			LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
				ON (IND."IdEntidad" = CLI."IdCliente" AND
					CLI."Borrado" = 'N')
			--LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
			LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER
				ON (CLI."IdPersona" = PER."IdPersona")
			WHERE TIP."IdTipoEntidad" = 'CLIENTE'
			  AND TIP."GrupoIndicador" = 'GENERALES'
			  AND TIP."CodigoTipoIndicador" = 'CODIGO_DATATEC'
			  AND TIP."IdEmpresa" = :IdEmpresa
			  AND TIP."IdEstado" != 'INA'
		) INDCLI
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
			ON (INDCLI."IdCliente" = POR."IdCliente" AND
				POR."IdEstado" != 'INA')
		WHERE INDCLI."POS1" = 1
	)
SELECT
	 IND."ValorIndicador"
	,IND."IdCliente"
	,IND."IdPortafolio"
	,IND."NombreCompleto"
FROM IndicadorClientePortafolio IND
WHERE IND."POS2" = 1;
