SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'MONTO_COMISION' AS "IdEvento", 'MONTO COMISION' AS "Nombre",  CASE "IdTipoProducto" WHEN 'INMI' THEN to_decimal(IFNULL("TotalComisCmbte",0),23,0)  ELSE  to_decimal(IFNULL("TotalComis",0),23,0)  END AS "MontoEvento", "IdMonedaComprobante" AS  "CodigoMoneda"
   FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad      
UNION
    SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'PERDIDA', 'PERDIDA',IFNULL("Spread",0),"IdMonedaLiquidacion"                
    FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
    WHERE "FechaSistema" = :FechaContable AND "Spread" < 0 AND "DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S' 
UNION
    SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante",'UTILIDAD', 'UTILIDAD',IFNULL("Spread",0),"IdMonedaLiquidacion"              
    FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
    WHERE "FechaSistema" = :FechaContable AND "Spread" > 0 AND "DocumentoIdentidad" != :DocumentoIdentidad AND "EsVentaCorta" != 'S'  
UNION   
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'VALOR_COMISION' AS "IdEvento", 'VALOR COMISION MX' AS "Nombre", IFNULL("TotalComisLiq",0) AS "MontoEvento", 
	(CASE WHEN COALESCE("IdMonedaSpot",'') != '' THEN "IdMonedaSpot" ELSE "IdMonedaLiquidacion" END) AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'MONTO_DERECHO' AS "IdEvento", 'MONTO DERECHO' AS "Nombre", CASE "IdTipoProducto" WHEN 'INMI' THEN to_decimal(IFNULL("TotalDereCmbte",0),23,0)  ELSE to_decimal(IFNULL("TotalDereCmbte",0),23,0)  END AS "MontoEvento", "IdMonedaComprobante" AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'VALOR_DERECHO' AS "IdEvento", 'VALOR DERECHO MX' AS "Nombre", IFNULL("TotalDereLiq",0) AS "MontoEvento", 
	(CASE WHEN COALESCE("IdMonedaSpot",'') != '' THEN "IdMonedaSpot" ELSE "IdMonedaLiquidacion" END) AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
UNION

	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'MONTO_GASTOS' AS "IdEvento", 'MONTO GASTOS' AS "Nombre", to_decimal(IFNULL("TotalGastoCmbte",0),23,0)  AS "MontoEvento", "IdMonedaComprobante" AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'VALOR_GASTOS' AS "IdEvento", 'VALOR GASTOS MX' AS "Nombre", IFNULL("TotalGastoLiq",0) AS "MontoEvento", 
	(CASE WHEN COALESCE("IdMonedaSpot",'') != '' THEN "IdMonedaSpot" ELSE "IdMonedaLiquidacion" END) AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'MONTO_IVA' AS "IdEvento", 'MONTO IVA' AS "Nombre", CASE "IdTipoProducto" WHEN 'INMI' THEN  to_decimal(IFNULL("TotalImptoCmbte",0),23,0) ELSE to_decimal(IFNULL("TotalImpto",0),23,0) END AS "MontoEvento", "IdMonedaComprobante" AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'VALOR_IVA' AS "IdEvento", 'VALOR IVA MX' AS "Nombre", IFNULL("TotalImptoLiq",0) AS "MontoEvento", 
	(CASE WHEN COALESCE("IdMonedaSpot",'') != '' THEN "IdMonedaSpot" ELSE "IdMonedaLiquidacion" END) AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'MONTO_TOTAL' AS "IdEvento", 'MONTO TOTAL' AS "Nombre", IFNULL("MontoComprobanteFinal",0) AS "MontoEvento", "IdMonedaComprobante" AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'VALOR_TOTAL' AS "IdEvento", 'VALOR TOTAL MX' AS "Nombre", IFNULL("MontoLiquidacionFinal",0) AS "MontoEvento", 
	(CASE WHEN COALESCE("IdMonedaSpot",'') != '' THEN "IdMonedaSpot" ELSE "IdMonedaLiquidacion" END) AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'MONTO_COBRO' AS "IdEvento", 'MONTO COBRO' AS "Nombre", to_decimal(IFNULL("MontoComprobanteCobro",0),23,0)  AS "MontoEvento", "IdMonedaComprobante" AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'VALOR_COBRO' AS "IdEvento", 'VALOR COBRO MX' AS "Nombre", IFNULL("MontoLiquidacionCobro",0) AS "MontoEvento", 
	(CASE WHEN COALESCE("IdMonedaSpot",'') != '' THEN "IdMonedaSpot" ELSE "IdMonedaLiquidacion" END) AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'MONTO_OPERADO' AS "IdEvento", 'MONTO OPERADO' AS "Nombre", IFNULL("MontoComprobante",0) AS "MontoEvento", "IdMonedaComprobante" AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'VALOR_OPERADO' AS "IdEvento", 'VALOR OPERADO MX' AS "Nombre", CASE "IdTipoProducto" WHEN 'INMI' THEN   IFNULL("MontoLiquidacion",0) ELSE  IFNULL("Monto",0) END AS "MontoEvento", 
	(CASE WHEN COALESCE("IdMonedaSpot",'') != '' THEN "IdMonedaSpot" ELSE "IdMonedaLiquidacion" END) AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad
    
    
--MILA
--"MontoLiquidacion"    
-- TP
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", A."IdComprobante" AS "IdMovimiento",'MONTO_CTDO_TP' AS "IdEvento", 'MONTO CTDO TP' AS "Nombre", IFNULL(C."Monto",0) AS "MontoEvento", "IdMonedaComprobante" AS  "CodigoMoneda"
	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"     A
	INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante"   B
	ON (A."IdComprobante" = B."IdComprobante")
	INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante"   C 
	ON (B."IdMovimientoRelacionado" = C."IdMovimientoComprobante")
	WHERE 
    A."FechaSistema"        = :FechaContable AND 
    A."IdEstadoComprobante" <> 'ANU' AND A."EsVentaCorta" != 'S'  AND 
    A."IdTipoInstrumento"   = 'SIM' AND
    A."DocumentoIdentidad" != :DocumentoIdentidad
UNION
	SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion", "IdComprobante" AS "IdMovimiento",'MONTO_DIVISA' AS "IdEvento", 'MONTO DIVISA' AS "Nombre", IFNULL("CantidadTotal",0) AS "MontoEvento", 
	(CASE WHEN COALESCE("IdMonedaSpot",'') != '' THEN "IdMonedaSpot" ELSE "IdMonedaLiquidacion" END) AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 
    WHERE "FechaSistema" = :FechaContable AND "IdEstadoComprobante" <> 'ANU' AND "EsVentaCorta" != 'S'  AND "DocumentoIdentidad" != :DocumentoIdentidad AND "IdTipoOperacion" IN ('COMSPT','VENSPT')
     