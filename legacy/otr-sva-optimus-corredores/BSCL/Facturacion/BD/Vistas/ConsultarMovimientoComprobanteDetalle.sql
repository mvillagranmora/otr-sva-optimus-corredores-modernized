SET SCHEMA "BSCL"; 
GO 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteDetalle"; 
GO 

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteDetalle" As
SELECT MvtoCmbteDet."IdMovimientoComprobanteDetalle", 
       CmbteDet."IdComprobanteDetalle" As "IdComprobanteDetalle", 
       MvtoCmbte."IdMovimientoComprobante" As "IdMovimientoComprobante", 
       MvtoCmbteDet."IdTransaccion" ,
       MvtoCmbteDet."IdInstrumento" , 
       MvtoCmbteDet."NemotecnicoInstrumento" ,
       MvtoCmbteDet."IdTipoInstrumento",
       Case 
	   		When MvtoCmbteDet."IdTipoInstrumento" in ('PRT', 'EMP', 'SIM','RRV','RRF') then Max(MvtoCmbteDet."IdInstrumentoSubyacente") 
	   		Else Max(MvtoCmbteDet."IdInstrumento")
	   End As "IdInstrumentoUnificado",		      
	   Case 
	   		When MvtoCmbteDet."IdTipoInstrumento" in ('PRT', 'EMP', 'SIM','RRV','RRF') then Max(MvtoCmbteDet."NemotecnicoSubyacente") 
	   		Else Max(MvtoCmbteDet."NemotecnicoInstrumento")
	   End As "NemotecnicoInstrumentoUnificado",
	   Case 
	   		When MvtoCmbteDet."IdTipoInstrumento" in ('PRT', 'EMP', 'SIM','RRV','RRF') then Max(MvtoCmbteDet."IdTipoInstrumentoSubyacente") 
	   		Else Max(MvtoCmbteDet."IdTipoInstrumento")
	   End As "IdTipoInstrumentoUnificado",
	   Case 
	   		When MvtoCmbteDet."IdTipoInstrumento" in ('PRT', 'EMP', 'SIM','RRV','RRF') then Max(MvtoCmbteDet."NombreTipoInstrumentoSubyacente") 
	   		Else Max(MvtoCmbteDet."NombreTipoInstrumento")
	   End As "NombreTipoInstrumentoUnificado",
       MvtoCmbteDet."IdConceptoMovimiento" , 
       MvtoCmbteDet."Cantidad" , 
       MvtoCmbteDet."Precio" , 
       MvtoCmbteDet."PrecioPromedio" ,
       MvtoCmbteDet."Monto",
       MvtoCmbteDet."MontoMovimientoComprobanteDetalle" ,
       MvtoCmbteDet."MontoLiquidacion",
       MvtoCmbteDet."Tasa" , 
       MvtoCmbteDet."TasaEmision" , 
       MvtoCmbteDet."Spread" , 
       MvtoCmbteDet."ValorRescate" , 
       MvtoCmbteDet."IdMoneda" , 
       MvtoCmbteDet."IdMonedaMovimientoComprobanteDetalle", 
       MvtoCmbteDet."IdMonedaLiquidacion", 
       MvtoCmbteDet."ValorParidad" , 
       MvtoCmbteDet."ValorCaptacionMonedaOrigen" ,
       MvtoCmbteDet."SecuenciaAgrupacion" , 
       MvtoCmbteDet."IdEstadoMovimientoComprobanteDetalle" , 
       EstPla."Nombre" AS "NombreEstado" , 
       MvtoCmbteDet."FechaIngresoRegistro", 
       CmbteDet."NroLinea",
       CmbteDet."FolioComprobanteFacturacionMigracion",
       MvtoCmbte."IdTipoComprobante",
       Case 
          When Sum(Case When ("IdCobro") In ('COFI', 'COSI','COVA') Then 1 Else 0 End) > 0 Then 'S'
       Else 'N'           
       End As "AfectoComision",
       Case 
          When Sum(Case When ("IdCobro") In ('DESI', 'DEFI') Then 1 Else 0 End) > 0 Then 'S'
       Else 'N'           
       End As "AfectoDerecho",
       Case 
          When Sum(Case When ("IdCobro") = 'GFOD' Then 1 Else 0 End) > 0 Then 'S'
       Else 'N'           
       End As "AfectoGasto",
       Case 
          When Sum(Case When ("IdCobro") = 'IVA' Then 1 Else 0 End) > 0 Then 'S'
       Else 'N'           
       End As "AfectoIva" ,
       MvtoCmbteDet."IndicadorOrdenDirecta"
From "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" As MvtoCmbteDet
   Left Outer Join "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" As CmbteDet 
     On CmbteDet."IdComprobanteDetalle" = MvtoCmbteDet."IdComprobanteDetalle"  
   Left Outer Join "BSCL.Facturacion.BD::FAC.EstadoFacturacion" As EstFact 
     On EstFact."IdEstado" = MvtoCmbteDet."IdEstadoMovimientoComprobanteDetalle"  
   Left Outer Join "BSCL.Plataforma.BD::PLA.Estado" As EstPla 
     On EstPla."IdEstado" = EstFact."IdEstado"  
   Left Outer Join "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte 
     On MvtoCmbte."IdMovimientoComprobante" = MvtoCmbteDet."IdMovimientoComprobante"
   Left Outer Join "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" As MvtoCmbteCobro 
     On MvtoCmbteCobro."IdMovimientoComprobante" = MvtoCmbte."IdMovimientoComprobante"
Where Upper(EstFact."NombreEntidad") = Upper('FAC.Comprobante')
Group By MvtoCmbteDet."IdMovimientoComprobanteDetalle", CmbteDet."IdComprobanteDetalle", MvtoCmbte."IdMovimientoComprobante",
         MvtoCmbteDet."IdTransaccion", MvtoCmbteDet."IdInstrumento", MvtoCmbteDet."NemotecnicoInstrumento", MvtoCmbteDet."IdTipoInstrumento", 
         MvtoCmbteDet."IdConceptoMovimiento", MvtoCmbteDet."Cantidad", MvtoCmbteDet."Precio", MvtoCmbteDet."PrecioPromedio", 
         MvtoCmbteDet."Monto", MvtoCmbteDet."MontoMovimientoComprobanteDetalle", MvtoCmbteDet."MontoLiquidacion", MvtoCmbteDet."Tasa", MvtoCmbteDet."TasaEmision", 
         MvtoCmbteDet."Spread", MvtoCmbteDet."ValorRescate", MvtoCmbteDet."IdMoneda", MvtoCmbteDet."IdMonedaMovimientoComprobanteDetalle", 
         MvtoCmbteDet."IdMonedaLiquidacion", MvtoCmbteDet."ValorParidad", MvtoCmbteDet."ValorCaptacionMonedaOrigen",
         MvtoCmbteDet."SecuenciaAgrupacion", MvtoCmbteDet."IdEstadoMovimientoComprobanteDetalle", EstPla."Nombre", 
         MvtoCmbteDet."FechaIngresoRegistro", CmbteDet."NroLinea", CmbteDet."FolioComprobanteFacturacionMigracion",
         MvtoCmbte."IdTipoComprobante", MvtoCmbteDet."IndicadorOrdenDirecta";
GO         

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteDetalle" TO BCS_DESARROLLADOR WITH GRANT OPTION;
