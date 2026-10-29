SELECT	"IdPortafolio",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDSOBREGIRORV' THEN "ValorIndicador" END),'N') AS "SobregiroRV",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDSOBREGIRORF' THEN "ValorIndicador" END),'N') AS "SobregiroRF",
		COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDSOBREGIROIF' THEN "ValorIndicador" END),'N') AS "SobregiroIF"
FROM (
	SELECT "IdPortafolio","CodigoTipoIndicador","IdIndicador","ValorIndicador","CodigoValorIndicador"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
	WHERE "CodigoTipoIndicador" IN ('ORDSOBREGIRORV','ORDSOBREGIRORF', 'ORDSOBREGIROIF')
) 
WHERE "IdPortafolio" IN (:idsPortafolio)
GROUP BY "IdPortafolio";