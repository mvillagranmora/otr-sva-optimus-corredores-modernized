SET SCHEMA "BSCL";
DROP VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarPorComprobanteOrigenLiquidado";

CREATE VIEW "BSCL.Tesoreria.BD.Vistas::ConsultarPorComprobanteOrigenLiquidado" AS
SELECT	 ComprobanteDetalle."IdTipoComprobanteOrigen"
		,ComprobanteDetalle."IdComprobanteOrigen"
		,CASE WHEN Comprobante."IdEstado" = 'LIQ' THEN 'LIQ'
			  WHEN Comprobante."IdEstado" = 'LIP' AND ComprobanteFP."IdEstado" = 'LIQ' AND CONF."LIQUIDACIONPARCIALENVIAACTA" = 'S' THEN 'LIQ'
			  ELSE 'PEN'
		  END AS "IdEstado"		
		,Comprobante."IdTipoMovimiento"
		,Comprobante."IdConcepto"
		,Comprobante."IdMoneda"
		,Comprobante."Monto" AS "MontoComprobante"		
		,CASE WHEN ComprobanteFP."IdEstado" = 'LIQ' AND CONF."LIQUIDACIONPARCIALENVIAACTA" = 'S' THEN ComprobanteFP."Monto"
			  ELSE 0
		  END AS "MontoLiquidado"		  
		,Comprobante."IdComprobante"
		,ComprobanteDetalle."IdComprobanteDetalle"
		,ComprobanteFP."IdComprobanteFormaDePago"
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS ComprobanteDetalle
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante ON Comprobante."IdComprobante" = ComprobanteDetalle."IdComprobante"
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFP ON ComprobanteFP."IdComprobante" = ComprobanteDetalle."IdComprobante"
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarConfiguracionTesoreriaPivot" AS CONF ON CONF."IdEmpresa" = Comprobante."IdEmpresa"
WHERE Comprobante."IdEstado" NOT IN ('ANU','MOD')
  AND ComprobanteDetalle."IdEstado" <> 'ANU'
  AND ComprobanteFP."IdEstado" <> 'ANU'
WITH READ ONLY; 

GRANT SELECT ON "BSCL.Tesoreria.BD.Vistas::ConsultarPorComprobanteOrigenLiquidado" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
