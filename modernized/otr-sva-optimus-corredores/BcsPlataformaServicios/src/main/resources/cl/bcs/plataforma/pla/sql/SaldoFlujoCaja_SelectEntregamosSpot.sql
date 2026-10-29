WITH ENTREGAMOS_SPOT as (
SELECT (
CASE 
	WHEN (:TipoCalculoFlujoCajaSpot = 'LIQUIDACION') THEN (CASE WHEN TO_DATE(FP."FechaLiquidacionComprobantePagoOperacion") >= :FechaHasta THEN TO_DATE(FP."FechaLiquidacionComprobantePagoOperacion") ELSE TO_DATE(:FechaDesde) END)
	WHEN (:TipoCalculoFlujoCajaSpot = 'INGRESO' AND SAL."IdMoneda" <> FP."IdMoneda") THEN (CASE WHEN TO_DATE(FP."FechaLiquidacionComprobantePagoOperacion") >= :FechaHasta THEN TO_DATE(FP."FechaLiquidacionComprobantePagoOperacion") ELSE TO_DATE(:FechaDesde) END)
	ELSE (CASE WHEN TO_DATE(FP."FechaLiquidacionComprobantePagoOperacion") >= :FechaHasta THEN TO_DATE(FP."FechaLiquidacionComprobantePagoOperacion") ELSE TO_DATE(:FechaDesde) END)
END) AS "FechaLiquidacion"
,OP."IdPortafolio"
,FP."IdMoneda" AS "IdMoneda"
,FP."Monto" AS "Monto"
,'ENTREGAMOS_OPERACION_SPOT' AS "ItemCalculo"
FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" SAL 
INNER JOIN "BSCL.Mesa.BD::SPT.OperacionSpot" AS OP
		ON (SAL."IdPortafolio" = OP."IdPortafolio"
			AND OP."Estado" IN ('VIG','PEN')
			AND OP."IdTipoOperacion" IN ('COMSPT','VENSPT')
			AND OP."IdEmpresa" = :IdEmpresa
			AND TO_DATE(OP."FechaOperacion") <= :FechaHasta)
INNER JOIN "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" AS FP
	ON (FP."IdOperacion" =  OP."IdOperacionSpot" 
	AND FP."IdEmpresa"= OP."IdEmpresa" 
	AND FP."IdMoneda" = SAL."IdMoneda" 
	AND FP."IdTipoProducto" = 'MEME' 
	AND FP."RecibePaga" = 'P' 
	AND (CASE 
			WHEN (:TipoCalculoFlujoCajaSpot = 'LIQUIDACION' AND SAL."IdMoneda" = FP."IdMoneda") THEN FP."FechaLiquidacionComprobantePagoOperacion"
			WHEN (:TipoCalculoFlujoCajaSpot = 'INGRESO' AND SAL."IdMoneda" <> FP."IdMoneda") THEN FP."FechaLiquidacionComprobantePagoOperacion"
		  	ELSE FP."FechaLiquidacionComprobantePagoOperacion" 			  	
		END) <= :FechaHasta)
INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" AS CFP ON  FP."IdFormaDePago" = CFP."IdFormaDePago"
        															   AND FP."IdEmpresa" = CFP."IdEmpresa"
        															   AND FP."IdMoneda" = CFP."IdMoneda"
        															   AND CFP."IdMedioDePago" = 'CIN'
        															   
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MV ON MV."IdTransaccion" = OP."IdOperacionSpot" AND MV."IdTipoInstrumento" = 'SPT' AND "IdEstadoFacturacion" != 'ANU'
WHERE MV."IdTransaccion" IS NULL
)

SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id","FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM(TO_DECIMAL(0,26,8)) as "MontoCompra",SUM("Monto") as "MontoVenta","ItemCalculo"
,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"
FROM ENTREGAMOS_SPOT 
GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";