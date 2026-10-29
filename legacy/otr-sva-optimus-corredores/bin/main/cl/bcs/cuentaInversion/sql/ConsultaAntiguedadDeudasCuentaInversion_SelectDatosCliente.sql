SELECT
	PER."IdPersona"
	,CASE 
		WHEN PER."Tipo" = 'N' THEN PER."NombreCompleto"
		ELSE PER."RazonSocial" 
	END AS  "NombreCompleto" 
	,PER."DocumentoIdentidad" 
	,CLI."IdCliente"
	,PORT."IdPortafolio"
	,PORT."CodigoPortafolio"
	,CASE 
		WHEN COALESCE(INPER."ValorIndicador", 'N') = 'S' AND COALESCE(IDCLI."ValorIndicador", 'N') = 'S' THEN 'SI'
		ELSE 'NO'
	END AS "Relacionado"
FROM "BSCL.Portafolio.BD::POR.Portafolio" PORT
INNER JOIN (SELECT 
				DISTINCT "IdPortafolio"
			FROM "BSCL.CuentaInversion.BD::GBL.ConsultarAntiguedadDeudasCuentaInversion_Cuenta_Paso"
			WHERE "IdTrabajo" = :IdTrabajo) AS TP
	ON (PORT."IdPortafolio" = TP."IdPortafolio")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
	ON (PORT."IdCliente" =  CLI."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
	ON (PER."IdPersona" = CLI."IdPersona")
LEFT JOIN (SELECT 
				INDPER."IdEntidad" 
				,INDPER."IdIndicador"
				,INDPER."ValorIndicador"
			FROM "BSCL.Plataforma.BD::IND.Indicador" INDPER
			INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIPINDPER
				ON (INDPER."IdTipoIndicador" = TIPINDPER."IdTipoIndicador" AND INDPER."IdTipoEntidad" = 'PERSONA'
				AND TIPINDPER."IdTipoEntidad" = 'PERSONA' AND  TIPINDPER."CodigoTipoIndicador" = 'RELCORRPER')) INPER
	ON (PER."IdPersona" = INPER."IdEntidad")
LEFT JOIN (SELECT 
				INDCLI."IdEntidad" 
				,INDCLI."IdIndicador"
				,INDCLI."ValorIndicador"
			FROM "BSCL.Plataforma.BD::IND.Indicador" INDCLI
			INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIPINDCLI
				ON (INDCLI."IdTipoIndicador" = TIPINDCLI."IdTipoIndicador" AND INDCLI."IdTipoEntidad" = 'CLIENTE'
				AND TIPINDCLI."IdTipoEntidad" = 'CLIENTE' AND  TIPINDCLI."CodigoTipoIndicador" = 'RELCORR')) IDCLI
	ON (CLI."IdCliente" = IDCLI."IdEntidad")