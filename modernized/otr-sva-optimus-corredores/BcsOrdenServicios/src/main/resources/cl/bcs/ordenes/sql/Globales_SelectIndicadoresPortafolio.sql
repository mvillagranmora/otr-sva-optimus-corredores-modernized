SELECT	"IdPortafolio",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDMAILNTF' THEN "ValorIndicador" END),null) AS "IdDireccionNotificacion",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDOCUSTRV' THEN "ValorIndicador" END),'N') AS "OperaCustodiaRV",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDSOBREGIRORV' THEN "ValorIndicador" END),'N') AS "SobregiroRV",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDOCUSTRF' THEN "ValorIndicador" END),'N') AS "OperaCustodiaRFIF",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDSOBREGIRORF' THEN "ValorIndicador" END),'N') AS "SobregiroRF",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDSOBREGIROIF' THEN "ValorIndicador" END),'N') AS "SobregiroIF",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDVNNOMRV' THEN "ValorIndicador" END),'N') AS "PermiteVenderNominalRV",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDVNNOMRF' THEN "ValorIndicador" END),'N') AS "PermiteVenderNominalRFIF",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_OCCINV' THEN "ValorIndicador" END),'N') AS "OperaCuentaInversion",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_SOBCTA' THEN "ValorIndicador" END),'N') AS "OperaLineaSobregiro",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDLIBRO' THEN "ValorIndicador" END),null) AS "IdLibroOrdenes",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDLIBROAGENTE' THEN "ValorIndicador" END),null) AS "IdLibroOrdenesAgente",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDLIBRORETAIL' THEN "ValorIndicador" END),null) AS "IdLibroOrdenesRetail",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDMODALIDAD' THEN "ValorIndicador" END),null) AS "MecanismoCaptacion",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_CODIFRS' THEN "ValorIndicador" END),null) AS "CodigoIfrs",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDVENDES' THEN "ValorIndicador" END),null) AS "PermiteVentaDescubierta",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_RIECRE' THEN "ValorIndicador" END),'N') AS "OperaRiesgoCredito",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'FAC_MODFAC' THEN "CodigoValorIndicador" END),'MODPREFNORM') AS "ModificaTipoLiquidacion",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDCLIRUR' THEN "ValorIndicador" END),'N') AS "PermiteRutear"
FROM (
	SELECT "IdPortafolio","CodigoTipoIndicador","IdIndicador","ValorIndicador","CodigoValorIndicador"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
	
	WHERE "CodigoTipoIndicador" IN ('ORDMAILNTF','ORDOCUSTRV','ORDOCUSTRF',	'ORDVNNOMRV','ORDVNNOMRF','GEN_OCCINV',	'GEN_SOBCTA',
		'ORDLIBRO',	'ORDLIBROAGENTE','ORDLIBRORETAIL','ORDMODALIDAD','GEN_CODIFRS',	'ORDVENDES', 'GEN_RIECRE','FAC_MODFAC','ORDSOBREGIRORV','ORDSOBREGIRORF', 'ORDSOBREGIROIF','ORDCLIRUR')
) 
WHERE "IdPortafolio" IN (:idsPortafolio)
GROUP BY "IdPortafolio";
