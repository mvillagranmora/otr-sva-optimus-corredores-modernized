SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeruTotalizados"; 
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeruTotalizados" As 

SELECT MOV."IdMovimientoComprobante",
	   "IdMonedaMovimientoComprobanteCobro",
       "IdMonedaLiquidacionCobro",
       SUM(Case When "IdCobroClasificacion" = 'COMISION_BVL' Then "MontoCobro" Else 0 End) As "ComisionBVLMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_SAB' Then "MontoCobro" Else 0 End) As "ComisionSABMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_SMV' Then "MontoCobro" Else 0 End) As "ComisionSMVMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_CAVALI' Then "MontoCobro" Else 0 End) As "ComisionCavaliMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'FONDO_GARANTIA' Then "MontoCobro" Else 0 End) As "FondoGarantiaMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'FONDO_LIQUIDACION' Then "MontoCobro" Else 0 End) As "FondoLiquidacionMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_GENERICA' Then "MontoCobro" Else 0 End) As "OtrasComisionesMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'IMPUESTO_IGV' Then "MontoCobro" Else 0 End) As "ImpuestoIGVMonedaCobro",
	   
       SUM(Case When "IdCobroClasificacion" = 'COMISION_BVL' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "ComisionBVLMonedaMovimiento",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_SAB' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "ComisionSABMonedaMovimiento",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_SMV' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "ComisionSMVMonedaMovimiento",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_CAVALI' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "ComisionCavaliMonedaMovimiento",
	   SUM(Case When "IdCobroClasificacion" = 'FONDO_GARANTIA' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "FondoGarantiaMonedaMovimiento",
	   SUM(Case When "IdCobroClasificacion" = 'FONDO_LIQUIDACION' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "FondoLiquidacionMonedaMovimiento",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_GENERICA' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "OtrasComisionesMonedaMovimiento",
	   SUM(Case When "IdCobroClasificacion" = 'IMPUESTO_IGV' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "ImpuestoIGVMonedaMovimiento",
	   
       SUM(Case When "IdCobroClasificacion" = 'COMISION_BVL' Then "MontoLiquidacionCobro" Else 0 End) As "ComisionBVLMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_SAB' Then "MontoLiquidacionCobro" Else 0 End) As "ComisionSABMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_SMV' Then "MontoLiquidacionCobro" Else 0 End) As "ComisionSMVMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_CAVALI' Then "MontoLiquidacionCobro" Else 0 End) As "ComisionCavaliMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'FONDO_GARANTIA' Then "MontoLiquidacionCobro" Else 0 End) As "FondoGarantiaMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'FONDO_LIQUIDACION' Then "MontoLiquidacionCobro" Else 0 End) As "FondoLiquidacionMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_GENERICA' Then "MontoLiquidacionCobro" Else 0 End) As "OtrasComisionesMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'IMPUESTO_IGV' Then "MontoLiquidacionCobro" Else 0 End) As "ImpuestoIGVMonedaLiquidacion",
	   
	   SUM(COALESCE("MontoMovimientoComprobanteCobro",0)) As "MontoMovimientoComprobanteCobrosTotal",
	   SUM(COALESCE("MontoLiquidacionCobro",0)) As "MontoLiquidacionCobrosTotal"
	   
	   
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeru" MOVCOB ON MOV."IdMovimientoComprobante" = MOVCOB."IdMovimientoComprobante"
--WHERE MOV."IdMovimientoComprobante" = 3979
GROUP BY MOV."IdMovimientoComprobante", "IdMonedaMovimientoComprobanteCobro","IdMonedaLiquidacionCobro";
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosPeruTotalizados" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO
