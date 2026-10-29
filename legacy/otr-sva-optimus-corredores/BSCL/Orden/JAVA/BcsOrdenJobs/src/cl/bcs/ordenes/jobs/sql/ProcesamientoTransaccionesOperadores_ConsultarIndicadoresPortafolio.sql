SELECT	"IdPortafolio",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDMAILNTF' THEN "ValorIndicador" END),null) AS "IdDireccionNotificacion",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDOCUSTRV' THEN "ValorIndicador" END),'N') AS "OperaCustodiaRV",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDOCUSTRF' THEN "ValorIndicador" END),'N') AS "OperaCustodiaRFIF",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDVNNOMRV' THEN "ValorIndicador" END),'N') AS "PermiteVenderNominalRV",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDVNNOMRF' THEN "ValorIndicador" END),'N') AS "PermiteVenderNominalRFIF",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_OCCINV' THEN "ValorIndicador" END),'N') AS "OperaCuentaInversion",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_SOBCTA' THEN "ValorIndicador" END),'N') AS "OperaLineaSobregiro",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDLIBRO' THEN "ValorIndicador" END),null) AS "IdLibroOrdenes"
FROM (

	SELECT "IdPortafolio","CodigoTipoIndicador","IdIndicador","ValorIndicador"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
) 
WHERE "IdPortafolio" IN (:idsPortafolio)
GROUP BY "IdPortafolio";
