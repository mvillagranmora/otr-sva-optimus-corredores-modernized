WITH TABLATEMPORAL AS (
SELECT DISTINCT "IdPortafolio"
	FROM "BSCL.CuentaInversion.BD::GBL.ConsultaReporteAcreedores_Cuenta_Paso"
	WHERE "IdTrabajo" = :IdTrabajo
)
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
INNER JOIN TABLATEMPORAL ON (PORT."IdPortafolio" = TABLATEMPORAL."IdPortafolio")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON (PORT."IdCliente" =  CLI."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER	ON (PER."IdPersona" = CLI."IdPersona")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS INPER ON (INPER."IdTipoEntidad" = 'PERSONA' AND INPER."CodigoTipoIndicador" = 'RELCORRPER' AND PER."IdPersona" = INPER."IdPersona")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS IDCLI ON (IDCLI."IdTipoEntidad" = 'CLIENTE' AND IDCLI."CodigoTipoIndicador" = 'RELCORR' AND CLI."IdCliente" = IDCLI."IdCliente");