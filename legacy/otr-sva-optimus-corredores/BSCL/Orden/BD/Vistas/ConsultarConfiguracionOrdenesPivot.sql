SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenesPivot"; 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenesPivot"  
AS  
SELECT  CORD."IdEmpresa",
		MAX(CASE WHEN "CodigoParametro" = 'BovedaSimultanea' THEN "Valor" END) AS "BovedaSimultanea",
		MAX(CASE WHEN "CodigoParametro" = 'ListasAyBSimultanea' THEN "Valor" END) AS "ListasAyBSimultanea",
		MAX(CASE WHEN "CodigoParametro" = 'TemplateOrden' THEN "Valor" END) AS "TemplateOrden",
		MAX(CASE WHEN "CodigoParametro" = 'DiferenciaMontoAsignacion' THEN "Valor" END) AS "DiferenciaMontoAsignacion",
		MAX(CASE WHEN "CodigoParametro" = 'MonedaSimultanea' THEN "Valor" END) AS "MonedaSimultanea",
		MAX(CASE WHEN "CodigoParametro" = 'RuteoUsaFechaSistema' THEN "Valor" END) AS "RuteoUsaFechaSistema",
		MAX(CASE WHEN "CodigoParametro" = 'PorcentajeDivergenciaPrecio' THEN "Valor" END) AS "PorcentajeDivergenciaPrecio",
		MAX(CASE WHEN "CodigoParametro" = 'PorcentajeMaximoComision' THEN "Valor" END) AS "PorcentajeMaximoComision",
		MAX(CASE WHEN "CodigoParametro" = 'PorcentajeMinimoComision' THEN "Valor" END) AS "PorcentajeMinimoComision",
		MAX(CASE WHEN "CodigoParametro" = 'HabilitaIntegracionMME' THEN "Valor" END) AS "HabilitaIntegracionMME",
		MAX(CASE WHEN "CodigoParametro" = 'IdBovedaDefecto' THEN "Valor" END) AS "IdBovedaDefecto"
		 
FROM (
		SELECT "IdEmpresa","CodigoParametro","Valor"
        FROM "BSCL.Orden.BD::ORD.ConfiguracionOrdenes"
        ) AS CORD
 GROUP BY "IdEmpresa";

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarConfiguracionOrdenesPivot" TO  BCS_DESARROLLADOR WITH GRANT OPTION;





