SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCaja"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCaja" As
SELECT  MVTOCMBTE."IdMovimientoComprobante",
		"FechaLiquidacion",
		MVTOCMBTE."IdPortafolio",
		MVTOCMBTE."IdMonedaLiquidacion",
		TIPOOPERACION."Accion" As "AccionTipoOperacion",
		MVTOCMBTE."IdEstadoFacturacion",
		MVTOCMBTE."IdEmpresa",
		coalesce(TOTCOBROS."COMMVTOLIQ",0) As "COM",
		coalesce(TOTCOBROS."DERMVTOLIQ",0) As "DER",
		coalesce(TOTCOBROS."GASMVTOLIQ",0) As "GAS",
		coalesce(TOTCOBROS."IMPMVTOLIQ",0) As "IMP",
		coalesce(TOTCOBROS."RETMVTOLIQ",0) As "RET",
		(coalesce(TOTCOBROS."COMMVTOLIQ",0) + coalesce(TOTCOBROS."DERMVTOLIQ",0) + coalesce(TOTCOBROS."GASMVTOLIQ",0) + 
		 coalesce(TOTCOBROS."IMPMVTOLIQ",0) + coalesce(TOTCOBROS."RETMVTOLIQ",0)) As "MontoCobro",
		 coalesce("IdMonedaMovimientoComprobanteCobro",'') As "IdMonedaCobro",
		Case 
	     When TIPOOPERACION."Accion" = 'COMPRA' Then  
  	         (coalesce("MontoLiquidacion",0) + (coalesce(TOTCOBROS."COMMVTOLIQ",0) + 
	                                                      coalesce(TOTCOBROS."DERMVTOLIQ",0) + 
	                                                      coalesce(TOTCOBROS."GASMVTOLIQ",0) + 
	                                                      coalesce(TOTCOBROS."IMPMVTOLIQ",0) + 
 	                                                      coalesce(TOTCOBROS."RETMVTOLIQ",0)))
 	     When TIPOOPERACION."Accion" = 'VENTA' Then
 	         (coalesce("MontoLiquidacion",0) - (coalesce(TOTCOBROS."COMMVTOLIQ",0) + 
	                                                     coalesce(TOTCOBROS."DERMVTOLIQ",0) + 
	                                                     coalesce(TOTCOBROS."GASMVTOLIQ",0) + 
	                                                     coalesce(TOTCOBROS."IMPMVTOLIQ",0) + 
 	                                                     coalesce(TOTCOBROS."RETMVTOLIQ",0)))
 	     WHEN TIPOOPERACION."Accion" = 'RESCATE' THEN (COALESCE("MontoMovimientoComprobante",0) 
 	     + (COALESCE(TOTCOBROS."COMMVTOLIQ",0) 
 	     + COALESCE(TOTCOBROS."DERMVTOLIQ",0) 
 	     + COALESCE(TOTCOBROS."GASMVTOLIQ",0) 
 	     + COALESCE(TOTCOBROS."IMPMVTOLIQ",0) 
 	     + COALESCE(TOTCOBROS."RETMVTOLIQ",0)))
 	   Else 0                                                   
 	   End As "MontoFinal",
 	   "IdMonedaSpot",
 	   "Cantidad",
 	   "IdTipoInstrumento"
 	   ,CAST("FechaSistema" AS DATE) AS "FechaSistemaSinHora"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MVTOCMBTE
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION ON TIPOOPERACION."IdTipoOperacion" = MVTOCMBTE."IdTipoOperacion"
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteCobrosTotalizados" AS TOTCOBROS ON TOTCOBROS."IdMovimientoComprobante" = MVTOCMBTE."IdMovimientoComprobante";
--WHERE "ActualizaCuentaInversion" = 'S';
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" TO  BCS_DESARROLLADOR WITH GRANT OPTION;