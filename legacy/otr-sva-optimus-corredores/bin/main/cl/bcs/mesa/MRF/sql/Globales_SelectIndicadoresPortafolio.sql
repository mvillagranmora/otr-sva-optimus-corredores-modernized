-- Consultar Valor de Indicadores PORTAFOLIO

SELECT	
	"IdPortafolio",		
	COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_OCCINV' THEN "ValorIndicador" END),'N') AS "OperaCuentaInversion",
	COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_SOBCTA' THEN "ValorIndicador" END),'N') AS "OperaLineaSobregiro",
	COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDOCUSTRV' THEN "ValorIndicador" END),'N') AS "OperaCustodiaRV",
	COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDOCUSTRF' THEN "ValorIndicador" END),'N') AS "OperaCustodiaRFIF"	
	
FROM (

	SELECT "IdPortafolio","CodigoTipoIndicador","IdIndicador","ValorIndicador"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
) 
WHERE "IdPortafolio" IN (:idsPortafolio)
GROUP BY "IdPortafolio";

