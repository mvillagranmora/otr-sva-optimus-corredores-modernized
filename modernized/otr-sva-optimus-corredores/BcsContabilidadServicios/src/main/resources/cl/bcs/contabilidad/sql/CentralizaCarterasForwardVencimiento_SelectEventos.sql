SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_PRINCIPAL' AS "IdEvento",'Monto Moneda Principal' AS "Nombre", ABS("EGRMontoMonedaPrincipal") AS "MontoEvento", "IdMonedaPrincipal" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' 
AND CASE WHEN cc."IdEstado"='ANT' OR ("IdEstado" IN ('VCD','LIQ') AND "EGRIdTipoOperacion" = 'ANTFWD') 
  	THEN "EGRFechaOperacion" 
	ELSE "FechaVencimientoOperacion" 
END	= :FechaContable
AND ("IdEstado" IN ('ANT','LIQ') OR ("EGRIdTipoOperacion" = 'ANTFWD' AND "IdEstado" = 'VCD'))
UNION
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_SECUNDARIA' AS "IdEvento",'Monto Moneda Secundaria' AS "Nombre", ABS("EGRMontoMonedaSecundaria") AS "MontoEvento", "IdMonedaSecundaria" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' 
AND CASE WHEN cc."IdEstado"='ANT' OR ("IdEstado" IN ('VCD','LIQ') AND "EGRIdTipoOperacion" = 'ANTFWD') 
  	THEN "EGRFechaOperacion" 
	ELSE "FechaVencimientoOperacion" 
END	= :FechaContable
AND ("IdEstado" IN ('ANT','LIQ') OR ("EGRIdTipoOperacion" = 'ANTFWD' AND "IdEstado" = 'VCD'))
UNION
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_RECIBIMOS' AS "IdEvento",'Monto Recibimos' AS "Nombre", ABS("EGRMontoLiquidacion") AS "MontoEvento", "EGRIdMonedaLiquidacion" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' 
AND CASE WHEN cc."IdEstado"='ANT' OR ("IdEstado" IN ('VCD','LIQ') AND "EGRIdTipoOperacion" = 'ANTFWD') 
  	THEN "EGRFechaOperacion" 
	ELSE "FechaVencimientoOperacion" 
END	= :FechaContable
AND ("IdEstado" IN ('ANT','LIQ') OR ("EGRIdTipoOperacion" = 'ANTFWD' AND "IdEstado" = 'VCD')) AND CC."EGRMontoLiquidacion" >0
UNION
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'UTILIDAD_VENCIMIENTO' AS "IdEvento",'Utilidad al Vencimiento' AS "Nombre", ABS("EGRMontoLiquidacion") AS "MontoEvento", "EGRIdMonedaLiquidacion" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' 
AND CASE WHEN cc."IdEstado"='ANT' OR ("IdEstado" IN ('VCD','LIQ') AND "EGRIdTipoOperacion" = 'ANTFWD') 
  	THEN "EGRFechaOperacion" 
	ELSE "FechaVencimientoOperacion" 
END	= :FechaContable
AND ("IdEstado" IN ('ANT','LIQ') OR ("EGRIdTipoOperacion" = 'ANTFWD' AND "IdEstado" = 'VCD')) AND CC."EGRMontoLiquidacion" >0
UNION
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_PAGAMOS' AS "IdEvento",'Monto Pagamos' AS "Nombre", ABS("EGRMontoLiquidacion") AS "MontoEvento", "EGRIdMonedaLiquidacion" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' 
AND CASE WHEN cc."IdEstado"='ANT' OR ("IdEstado" IN ('VCD','LIQ') AND "EGRIdTipoOperacion" = 'ANTFWD') 
  	THEN "EGRFechaOperacion" 
	ELSE "FechaVencimientoOperacion" 
END	= :FechaContable
AND ("IdEstado" IN ('ANT','LIQ') OR ("EGRIdTipoOperacion" = 'ANTFWD' AND "IdEstado" = 'VCD')) AND CC."EGRMontoLiquidacion" < 0
UNION
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'PERDIDA_VENCIMIENTO' AS "IdEvento",'Perdida al Vencimiento' AS "Nombre", ABS("EGRMontoLiquidacion") AS "MontoEvento", "EGRIdMonedaLiquidacion" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' 
AND CASE WHEN cc."IdEstado"='ANT' OR ("IdEstado" IN ('VCD','LIQ') AND "EGRIdTipoOperacion" = 'ANTFWD') 
  	THEN "EGRFechaOperacion" 
	ELSE "FechaVencimientoOperacion" 
END	= :FechaContable
AND ("IdEstado" IN ('ANT','LIQ') OR ("EGRIdTipoOperacion" = 'ANTFWD' AND "IdEstado" = 'VCD')) AND CC."EGRMontoLiquidacion" < 0
UNION
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_DESCUENTO_ANTICIPO' AS "IdEvento",'Monto descuento Anticipo' AS "Nombre", ("EGRMontoLiquidacion"-"EGRMontoARecibirAnticipo") AS "MontoEvento", "EGRIdMonedaLiquidacion" AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' 
AND CASE WHEN cc."IdEstado"='ANT' OR ("IdEstado" IN ('VCD','LIQ') AND "EGRIdTipoOperacion" = 'ANTFWD') 
  	THEN "EGRFechaOperacion" 
	ELSE "FechaVencimientoOperacion" 
END	= :FechaContable
AND ("IdEstado" IN ('ANT','LIQ') OR ("EGRIdTipoOperacion" = 'ANTFWD' AND "IdEstado" = 'VCD'))
UNION
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_MONEDA_CLP' AS "IdEvento",'Monto en CLP' AS "Nombre"
	, CASE WHEN "IdMonedaSecundaria" = 'CLP' THEN ABS(CC."INGMontoMonedaSecundaria") ELSE ABS("INGMontoATasaCompraActual") END AS "MontoEvento"
	, 'CLP' AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' 
AND CASE WHEN cc."IdEstado"='ANT' OR ("IdEstado" IN ('VCD','LIQ') AND "EGRIdTipoOperacion" = 'ANTFWD') 
  	THEN "EGRFechaOperacion" 
	ELSE "FechaVencimientoOperacion" 
END	= :FechaContable
AND ("IdEstado" IN ('ANT','LIQ') OR ("EGRIdTipoOperacion" = 'ANTFWD' AND "IdEstado" = 'VCD'))
UNION
SELECT TO_BIGINT(:IdTipoCentralizacion) AS "IdTipoCentralizacion","IdCartera" AS "IdMovimiento",'MONTO_MONEDA_PRINCIPAL_CLP' AS "IdEvento",'Monto Moneda Principal en CLP' AS "Nombre"
	, CASE WHEN "IdMonedaSecundaria" = 'CLP' THEN ABS(CC."INGMontoMonedaSecundaria") ELSE ABS(CC."MontoMonedaPrincipal" * PTCDO."TipoCambioMercado") END AS "MontoEvento"
	, 'CLP' AS "CodigoMoneda"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCDO
 ON (PTCDO."Origen" = 'OBSERVADO' AND
 PTCDO."Fecha" = CASE  WHEN "EGRIdTipoOperacion" = 'ANTFWD' THEN CC."EGRFechaOperacion"  ELSE CC."FechaVencimientoOperacion"
 END  AND
 PTCDO."IdMoneda"= 'DO')
WHERE "IdEmpresa" = :IdEmpresa AND "IdTipoProducto" = 'MEFW' 
AND CASE WHEN cc."IdEstado"='ANT' OR (CC."IdEstado" IN ('VCD','LIQ') AND "EGRIdTipoOperacion" = 'ANTFWD') 
  	THEN "EGRFechaOperacion" 
	ELSE "FechaVencimientoOperacion" 
END	= :FechaContable
AND (CC."IdEstado" IN ('ANT','LIQ') OR ("EGRIdTipoOperacion" = 'ANTFWD' AND CC."IdEstado" = 'VCD'))