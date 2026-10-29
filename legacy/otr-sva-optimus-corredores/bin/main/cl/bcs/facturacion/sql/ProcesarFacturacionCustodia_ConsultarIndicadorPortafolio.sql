-- Busca indicador de permite venta descubierta en preferencia de negocio
SELECT  "IdPortafolio",      
        COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDVENDES' THEN "ValorIndicador" END),null) AS "PermiteVentaDescubierta",
        COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDSOBREGIRORV' THEN "ValorIndicador" END),null) AS "SobregiroRV",
        COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDSOBREGIRORF' THEN "ValorIndicador" END),null) AS "SobregiroRF",
        COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'ORDSOBREGIROIF' THEN "ValorIndicador" END),null) AS "SobregiroIF"
FROM (
        SELECT "IdPortafolio","CodigoTipoIndicador","IdIndicador","ValorIndicador"
        FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
     ) 
WHERE "IdPortafolio" IN (:idsPortafolio)
GROUP BY "IdPortafolio";
