WITH MONTO_COMISION AS(
SELECT   
	TO_BIGINT(:IdTipoCentralizacion)		AS "IdTipoCentralizacion",
	Cmbte."IdComprobante"					AS "IdComprobante",
--	MovCmbte."IdMovimientoComprobante" 		AS "IdMovimiento",
	IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
	'MONTO_COMISION' 						AS "IdEvento", 
	'MONTO COMISION' 						AS "Nombre",
CASE MovCmbte."IdTipoProducto" WHEN 'INMI' THEN IFNULL("TotalComisCmbte",0) ELSE  IFNULL("TotalComis",0) END AS "MontoEvento",
Cmbte."IdMonedaComprobante" 				AS  "CodigoMoneda"
   FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 				Cmbte
   LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 			MovCmbte
	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")
WHERE Cmbte."FechaSistema" = :FechaContable AND Cmbte."IdEstadoComprobante" <> 'ANU' AND Cmbte."DocumentoIdentidad" != :DocumentoIdentidad
-- AND Cmbte."IdComprobante" IN ( 2436, 2423)

UNION

SELECT 
	TO_BIGINT(:IdTipoCentralizacion)		AS "IdTipoCentralizacion",
	Cmbte."IdComprobante"					AS "IdComprobante",
--	MovCmbte."IdMovimientoComprobante" 		AS "IdMovimiento",
	IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
	'VALOR_COMISION' 						AS "IdEvento", 
	'VALOR COMISION MX' 					AS "Nombre", 
	IFNULL(Cmbte."TotalComisLiq",0)	AS "MontoEvento", 
	CASE WHEN COALESCE(Cmbte."IdMonedaSpot",'') != '' THEN Cmbte."IdMonedaSpot" ELSE Cmbte."IdMonedaLiquidacion" END AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 	Cmbte
   	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 	MovCmbte
 	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")
	
    WHERE Cmbte."FechaSistema" = :FechaContable AND Cmbte."IdEstadoComprobante" <> 'ANU' AND Cmbte."DocumentoIdentidad" != :DocumentoIdentidad
-- AND Cmbte."IdComprobante" IN ( 2436, 2423) 
--
UNION
SELECT 
	TO_BIGINT(:IdTipoCentralizacion)		AS "IdTipoCentralizacion",
	Cmbte."IdComprobante"					AS "IdComprobante",
--	MovCmbte."IdMovimientoComprobante" 		AS "IdMovimiento",
	IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
	'VALOR_DERECHO' 						AS "IdEvento",
	'VALOR DERECHO MX' 						AS "Nombre",
	IFNULL(Cmbte."TotalDereLiq",0) 			AS "MontoEvento", 
	CASE WHEN COALESCE(Cmbte."IdMonedaSpot",'') != '' THEN Cmbte."IdMonedaSpot" ELSE Cmbte."IdMonedaLiquidacion" END AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 		Cmbte
   	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 		MovCmbte
	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")    
    WHERE Cmbte."FechaSistema" = :FechaContable AND Cmbte."IdEstadoComprobante" <> 'ANU' AND Cmbte."DocumentoIdentidad" != :DocumentoIdentidad   
-- AND Cmbte."IdComprobante" IN ( 2436, 2423) 
UNION
SELECT 
	TO_BIGINT(:IdTipoCentralizacion)		AS "IdTipoCentralizacion",
	Cmbte."IdComprobante"					AS "IdComprobante",
--	MovCmbte."IdMovimientoComprobante" 		AS "IdMovimiento",
	IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
	'MONTO_DERECHO' 							AS "IdEvento", 
	'MONTO DERECHO' 							AS "Nombre", 
	CASE MovCmbte."IdTipoProducto" WHEN 'INMI' THEN  IFNULL("TotalDereCmbte",0) ELSE IFNULL("TotalDereCmbte",0) END AS "MontoEvento", 
	Cmbte."IdMonedaComprobante" 				AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 			Cmbte
   	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 			MovCmbte
	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")    
    WHERE Cmbte."FechaSistema" = :FechaContable AND Cmbte."IdEstadoComprobante" <> 'ANU' AND Cmbte."DocumentoIdentidad" != :DocumentoIdentidad
    -- AND Cmbte."IdComprobante" IN ( 2436, 2423) 

    UNION
SELECT 
	TO_BIGINT(:IdTipoCentralizacion)		AS "IdTipoCentralizacion",
	Cmbte."IdComprobante"					AS "IdComprobante",
--	MovCmbte."IdMovimientoComprobante" 		AS "IdMovimiento",
	IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
	'MONTO_GASTOS' 							AS "IdEvento",
	'MONTO GASTOS' 							AS "Nombre",
	IFNULL(Cmbte."TotalGastoCmbte",0) 			AS "MontoEvento",
	Cmbte."IdMonedaComprobante" 				AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 		Cmbte
   	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 		MovCmbte
	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")    
    WHERE Cmbte."FechaSistema" = :FechaContable AND Cmbte."IdEstadoComprobante" <> 'ANU' AND Cmbte."DocumentoIdentidad" != :DocumentoIdentidad    
    -- AND Cmbte."IdComprobante" IN ( 2436, 2423) 

    UNION
SELECT 
	TO_BIGINT(:IdTipoCentralizacion)		AS "IdTipoCentralizacion",
	Cmbte."IdComprobante"					AS "IdComprobante",
--	MovCmbte."IdMovimientoComprobante" 		AS "IdMovimiento",
	IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
	'VALOR_GASTOS' 							AS "IdEvento",
	'VALOR GASTOS MX' 						AS "Nombre",
	IFNULL(Cmbte."TotalGastoLiq",0) 		AS "MontoEvento", 
	CASE WHEN COALESCE(Cmbte."IdMonedaSpot",'') != '' THEN Cmbte."IdMonedaSpot" ELSE Cmbte."IdMonedaLiquidacion" END AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 		Cmbte
   	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 		MovCmbte
	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")    
    WHERE Cmbte."FechaSistema" = :FechaContable AND Cmbte."IdEstadoComprobante" <> 'ANU' AND Cmbte."DocumentoIdentidad" != :DocumentoIdentidad    
	-- AND Cmbte."IdComprobante" IN ( 2436, 2423) 
UNION
SELECT 
	TO_BIGINT(:IdTipoCentralizacion)			AS "IdTipoCentralizacion",
	Cmbte."IdComprobante"						AS "IdComprobante",
--	MovCmbte."IdMovimientoComprobante" 			AS "IdMovimiento",
	IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
	'MONTO_IVA' 								AS "IdEvento",
	'MONTO IVA' 								AS "Nombre",
	CASE Cmbte."IdTipoProducto" WHEN 'INMI' THEN  IFNULL(Cmbte."TotalImptoCmbte",0) ELSE IFNULL(Cmbte."TotalImpto",0) END AS "MontoEvento",
	Cmbte."IdMonedaComprobante" AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 		Cmbte
   	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 		MovCmbte
	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")    
    WHERE Cmbte."FechaSistema" = :FechaContable AND Cmbte."IdEstadoComprobante" <> 'ANU' AND Cmbte."DocumentoIdentidad" != :DocumentoIdentidad
	-- AND Cmbte."IdComprobante" IN ( 2436, 2423) 

    UNION
SELECT 
	TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
	Cmbte."IdComprobante"								AS "IdComprobante",
--	MovCmbte."IdMovimientoComprobante" 					AS "IdMovimiento",
	IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
	'VALOR_IVA' 										AS "IdEvento",
	'VALOR IVA MX' 										AS "Nombre",
	IFNULL(Cmbte."TotalImptoLiq",0) 					AS "MontoEvento", 
	CASE WHEN COALESCE(Cmbte."IdMonedaSpot",'') != '' THEN Cmbte."IdMonedaSpot" ELSE Cmbte."IdMonedaLiquidacion" END AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 		Cmbte
   	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 		MovCmbte
	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")    
    WHERE Cmbte."FechaSistema" = :FechaContable AND Cmbte."IdEstadoComprobante" <> 'ANU' AND Cmbte."DocumentoIdentidad" != :DocumentoIdentidad
	-- AND Cmbte."IdComprobante" IN ( 2436, 2423) 
    UNION
SELECT 
	TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
	Cmbte."IdComprobante"								AS "IdComprobante",
	IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
--	MovCmbte."IdMovimientoComprobante" 					AS "IdMovimiento",
	'MONTO_TOTAL' 										AS "IdEvento",
	'MONTO TOTAL' 										AS "Nombre",
	IFNULL(Cmbte."MontoComprobanteFinal",0) 			AS "MontoEvento",
	Cmbte."IdMonedaComprobante" 						AS "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 		Cmbte
   	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 		MovCmbte
   	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante") 
    WHERE Cmbte."FechaSistema" = :FechaContable AND Cmbte."IdEstadoComprobante" <> 'ANU' AND Cmbte."DocumentoIdentidad" != :DocumentoIdentidad
    

	UNION
SELECT 
	TO_BIGINT(:IdTipoCentralizacion)				 						AS "IdTipoCentralizacion", 
	Cmbte."IdComprobante"								AS "IdComprobante",
	IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")		AS "IdMovimiento",
	'VALOR_TOTAL' 															AS "IdEvento", 
	'VALOR TOTAL MX' 														AS "Nombre", 
	IFNULL(Cmbte."MontoLiquidacionFinal",0) 	AS "MontoEvento", 
	(CASE WHEN COALESCE(Cmbte."IdMonedaSpot",'') != '' THEN Cmbte."IdMonedaSpot" ELSE Cmbte."IdMonedaLiquidacion" END) AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 		Cmbte
   	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 		MovCmbte
   	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante") 
    WHERE Cmbte."FechaSistema" = :FechaContable AND Cmbte."IdEstadoComprobante" <> 'ANU' AND Cmbte."DocumentoIdentidad" != :DocumentoIdentidad    

)    

SELECT  "IdTipoCentralizacion", max("IdMovimiento") AS "IdMovimiento","IdEvento", "Nombre", "MontoEvento", "CodigoMoneda"  
FROM MONTO_COMISION 
GROUP BY "IdTipoCentralizacion", "IdComprobante","IdEvento", "Nombre", "MontoEvento", "CodigoMoneda"

UNION
-----------------------------------------------------------------------------------------------------------------------------

SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 									AS "IdTipoCentralizacion", 
	IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
	'MONTO_OPERADO' 													AS "IdEvento", 
	'MONTO OPERADO' 													AS "Nombre", 
	IFNULL(MovCmbte."Monto",Cmbte."MontoComprobante") 					AS "MontoEvento", 
	IFNULL(MovCmbte."IdMoneda",Cmbte."IdMonedaComprobante") 			AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 			Cmbte
    LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 			MovCmbte
	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")
    WHERE Cmbte."FechaSistema" = :FechaContable AND Cmbte."IdEstadoComprobante" <> 'ANU' AND Cmbte."DocumentoIdentidad" != :DocumentoIdentidad
   

UNION 
    
SELECT 
	TO_BIGINT(:IdTipoCentralizacion) 									AS "IdTipoCentralizacion",
	IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
	'VALOR_OPERADO' 													AS "IdEvento", 
	'VALOR OPERADO MX' 													AS "Nombre", 
	CASE IFNULL(MovCmbte."IdTipoProducto",Cmbte."IdTipoProducto") 
		WHEN 'INMI' THEN   IFNULL(MovCmbte."MontoLiquidacion",Cmbte."MontoLiquidacion") 
		ELSE  IFNULL(MovCmbte."Monto", Cmbte."Monto") 
	END 																AS "MontoEvento", 
	CASE 
		WHEN COALESCE(MovCmbte."IdMonedaSpot",Cmbte."IdMonedaSpot") != '' 
			THEN IFNULL(MovCmbte."IdMonedaSpot",Cmbte."IdMonedaSpot") 
			ELSE IFNULL(MovCmbte."IdMonedaLiquidacion" ,Cmbte."IdMonedaLiquidacion")
		END 															AS  "CodigoMoneda"
    FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 			Cmbte
    LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 			MovCmbte
	ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")
    WHERE Cmbte."FechaSistema" = :FechaContable AND Cmbte."IdEstadoComprobante" <> 'ANU' AND Cmbte."DocumentoIdentidad" != :DocumentoIdentidad    
	      
 
