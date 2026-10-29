SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosPeruTotalizados"; 
GO
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosPeruTotalizados" As 

SELECT CMBTE."IdComprobante",
	   "IdMonedaLocalCobro",
       "IdMonedaLiquidacionCobro",
       SUM(Case When "IdCobroClasificacion" = 'COMISION_BVL' Then CMBTECOB."MontoCobro" Else 0 End) As "ComisionBVLMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_SAB' Then CMBTECOB."MontoCobro" Else 0 End) As "ComisionSABMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_SMV' Then CMBTECOB."MontoCobro" Else 0 End) As "ComisionSMVMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_CAVALI' Then CMBTECOB."MontoCobro" Else 0 End) As "ComisionCavaliMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'FONDO_GARANTIA' Then CMBTECOB."MontoCobro" Else 0 End) As "FondoGarantiaMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'FONDO_LIQUIDACION' Then CMBTECOB."MontoCobro" Else 0 End) As "FondoLiquidacionMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_GENERICA' Then CMBTECOB."MontoCobro" Else 0 End) As "OtrasComisionesMonedaCobro",
	   SUM(Case When "IdCobroClasificacion" = 'IMPUESTO_IGV' Then CMBTECOB."MontoCobro" Else 0 End) As "ImpuestoIGVMonedaCobro",
	   
       SUM(Case When "IdCobroClasificacion" = 'COMISION_BVL' Then CMBTECOB."MontoLocalCobro" Else 0 End) 		As "ComisionBVLMonedaLocal",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_SAB' Then CMBTECOB."MontoLocalCobro" Else 0 End) 		As "ComisionSABMonedaLocal",
	   SUM(Case When "IdCobroClasificacion" = 'COMISI_SMV' Then CMBTECOB."MontoLocalCobro" Else 0 End) 			As "ComisionSMVMonedaLocal",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_CAVALI' Then CMBTECOB."MontoLocalCobro" Else 0 End) 	As "ComisionCavaliMonedaLocal",
	   SUM(Case When "IdCobroClasificacion" = 'FONDO_GARANTIA' Then CMBTECOB."MontoLocalCobro" Else 0 End) 		As "FondoGarantiaMonedaLocal",
	   SUM(Case When "IdCobroClasificacion" = 'FONDO_LIQUIDACION' Then CMBTECOB."MontoLocalCobro" Else 0 End) 	As "FondoLiquidacionMonedaLocal",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_GENERICA' Then CMBTECOB."MontoLocalCobro" Else 0 End) 	As "OtrasComisionesMonedaLocal",
	   SUM(Case When "IdCobroClasificacion" = 'IMPUESTO_IGV' Then CMBTECOB."MontoLocalCobro" Else 0 End) 		As "ImpuestoIGVMonedaLocal",
	   
       SUM(Case When "IdCobroClasificacion" = 'COMISION_BVL' Then CMBTECOB."MontoLiquidacionCobro" Else 0 End) As "ComisionBVLMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_SAB' Then CMBTECOB."MontoLiquidacionCobro" Else 0 End) As "ComisionSABMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_SMV' Then CMBTECOB."MontoLiquidacionCobro" Else 0 End) As "ComisionSMVMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_CAVALI' Then CMBTECOB."MontoLiquidacionCobro" Else 0 End) As "ComisionCavaliMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'FONDO_GARANTIA' Then CMBTECOB."MontoLiquidacionCobro" Else 0 End) As "FondoGarantiaMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'FONDO_LIQUIDACION' Then CMBTECOB."MontoLiquidacionCobro" Else 0 End) As "FondoLiquidacionMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'COMISION_GENERICA' Then CMBTECOB."MontoLiquidacionCobro" Else 0 End) As "OtrasComisionesMonedaLiquidacion",
	   SUM(Case When "IdCobroClasificacion" = 'IMPUESTO_IGV' Then CMBTECOB."MontoLiquidacionCobro" Else 0 End) As "ImpuestoIGVMonedaLiquidacion",
	   
	   SUM(Case When "IdClaseCobroBase" = 'COMISION' Then CMBTECOB."MontoLiquidacionCobro"  Else 0 End) As "TotalComisionLiquidacion",
	   SUM(Case When "IdClaseCobroBase" = 'COMISION' Then CMBTECOB."MontoLocalCobro"  Else 0 End) As "TotalComisionMonedaLocal",
	   SUM(COALESCE(CMBTECOB."MontoLocalCobro",0)) As "MontoLocalCobrosTotal",
	   SUM(COALESCE(CMBTECOB."MontoLiquidacionCobro",0)) As "MontoLiquidacionCobrosTotal"
	   
	   
FROM "BSCL.Facturacion.BD::FAC.Comprobante" CMBTE
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosPeru" CMBTECOB ON CMBTE."IdComprobante" = CMBTECOB."IdComprobante"
--WHERE CMBTE."IdComprobante" = 2516
GROUP BY CMBTE."IdComprobante", "IdMonedaLocalCobro","IdMonedaLiquidacionCobro";
GO
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteCobrosPeruTotalizados" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO
