SET SCHEMA "BSCL"; 
GO
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosTotalizados"; 
GO

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosTotalizados" As
SELECT "IdMovimientoComprobante",
       --"IdMonedaCobro", 
       "IdMonedaMovimientoComprobanteCobro",
       "IdMonedaLiquidacionCobro",
       Max(Case When "CodigoCobroClasificacion" = 'COMISION' Then "MontoCobro" Else 0 End) As "COMMVTO",
	   Max(Case When "CodigoCobroClasificacion" = 'DERECHO' Then "MontoCobro" Else 0 End) As "DERMVTO",
	   Max(Case When "CodigoCobroClasificacion" = 'GASTO' Then "MontoCobro" Else 0 End) As "GASMVTO",
	   Max(Case When "CodigoCobroClasificacion" = 'IMPUESTO' Then "MontoCobro" Else 0 End) As "IMPMVTO",
	   Max(Case When "CodigoCobroClasificacion" = 'RETENCION' Then "MontoCobro" Else 0 End) As "RETMVTO",
	   
	   Max(Case When "CodigoCobroClasificacion" = 'COMISION' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "COMMVTOCMBTE",
	   Max(Case When "CodigoCobroClasificacion" = 'DERECHO' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "DERMVTOCMBTE",
	   Max(Case When "CodigoCobroClasificacion" = 'GASTO' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "GASMVTOCMBTE",
	   Max(Case When "CodigoCobroClasificacion" = 'IMPUESTO' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "IMPMVTOCMBTE",
	   Max(Case When "CodigoCobroClasificacion" = 'RETENCION' Then "MontoMovimientoComprobanteCobro" Else 0 End) As "RETMVTOCMBTE",
	   
	   Max(Case When "CodigoCobroClasificacion" = 'COMISION' Then "MontoLiquidacionCobro" Else 0 End) As "COMMVTOLIQ",
	   Max(Case When "CodigoCobroClasificacion" = 'DERECHO' Then "MontoLiquidacionCobro" Else 0 End) As "DERMVTOLIQ",
	   Max(Case When "CodigoCobroClasificacion" = 'GASTO' Then "MontoLiquidacionCobro" Else 0 End) As "GASMVTOLIQ",
	   Max(Case When "CodigoCobroClasificacion" = 'IMPUESTO' Then "MontoLiquidacionCobro" Else 0 End) As "IMPMVTOLIQ",
	   Max(Case When "CodigoCobroClasificacion" = 'RETENCION' Then "MontoLiquidacionCobro" Else 0 End) As "RETMVTOLIQ"
FROM (
	    Select "IdMovimientoComprobante",
			   "IdCobroClasificacion",
			   "CodigoCobroClasificacion", 
			   --"IdMonedaCobro",
			   "IdMonedaMovimientoComprobanteCobro",
			   "IdMonedaLiquidacionCobro",
			   Sum("MontoCobro") As "MontoCobro",
			   Sum("MontoMovimientoComprobanteCobro") As "MontoMovimientoComprobanteCobro",
			   Sum("MontoLiquidacionCobro") As "MontoLiquidacionCobro"
		From "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobros" 
		Where "IdEstado" = 'VIG'
		Group By "IdMovimientoComprobante", "IdCobroClasificacion", "CodigoCobroClasificacion",
		         --"IdMonedaCobro", 
		         "IdMonedaLiquidacionCobro", "IdMonedaMovimientoComprobanteCobro"
        Order By "IdCobroClasificacion")
GROUP BY "IdMovimientoComprobante", "IdMonedaMovimientoComprobanteCobro", "IdMonedaLiquidacionCobro";
GO

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosTotalizados" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
