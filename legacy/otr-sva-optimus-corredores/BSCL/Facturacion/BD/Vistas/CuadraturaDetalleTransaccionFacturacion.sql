SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.Facturacion.BD.Vistas::CuadraturaDetalleTransaccionFacturacion";
GO

CREATE VIEW "BSCL.Facturacion.BD.Vistas::CuadraturaDetalleTransaccionFacturacion" AS
Select Trans."IdTransaccion", 
       Trans."Folio" 						As "FolioTransaccion", 
       MvtoCmbte."IdComprobante"	 		As "IdComprobante",
       MvtoCmbte."IdComprobanteReferencia"  As "IdComprobanteReferencia",
       TipCmbte."Nombre" 					As "TipoComprobante",
       Portafolio."CodigoPortafolio"		As "IdPortafolio",
       MvtoCmbte."DocumentoIdentidad" 		As "DocumentoIdentidad",
       MvtoCmbte."NombreLargoCliente" 		As "NombreCliente",              
       Trans."IdCondicionDeLiquidacion" 	As "IdCondicionLiquidacion", 
       Trans."IdMoneda" 					As "IdMoneda",
       Trans."Cantidad" 					As "Cantidad", 
       Trans."Precio" 						As "Precio", 
       Trans."Monto" 						As "Monto",
       Coalesce(Asig."CantidadAsignada",0)	As "CantidadAsignada", 
       Coalesce(MvtoCmbte."Cantidad",0)		As "CantidadEnviada",                     
       Coalesce(Cmbte."Cantidad",0)			As "CantidadFacturada",
       Coalesce(Cmbte."Monto",0)			As "MontoFacturado",       
       MvtoCmbte."IdEstadoFacturacion",
       EstPla."Nombre"                      As "NombreEstadoFacturacion"
From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MvtoCmbte  
  Inner Join "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" Portafolio
  	On MvtoCmbte."IdPortafolio" = Portafolio."IdPortafolio"
  Right Join "BSCL.Orden.BD::ORD.Asignacion" Asig
    On Asig."IdAsignacion" = MvtoCmbte."IdAsignacion"
  Right Join "BSCL.Orden.BD::ORD.Transaccion" Trans
    On Trans."IdTransaccion" = Asig."IdTransaccion"
  Left Join "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" CmbteDetalle 
    On MvtoCmbte."IdComprobante" = CmbteDetalle."IdComprobante"
    AND Asig."IdTransaccion" = CmbteDetalle."IdTransaccion"
  Left Join "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" Cmbte 
	On Cmbte."IdMovimientoComprobante" = MvtoCmbte."IdMovimientoComprobante" 
	And Cmbte."IdTransaccion" = Trans."IdTransaccion" 
	And Cmbte."IdComprobanteDetalle" = CmbteDetalle."IdComprobanteDetalle"
  Left Join "BSCL.Plataforma.BD::INS.Bolsa" Bolsa
    On Bolsa."IdBolsa" = Trans."IdBolsa"
  Left Join "BSCL.Plataforma.BD::INS.TipoInstrumento" TipIns
    On TipIns."IdTipoInstrumento" = Trans."IdTipoInstrumento" 
  Left Join "BSCL.Facturacion.BD::FAC.TipoComprobante" TipCmbte
    On TipCmbte."IdTipoComprobante" = MvtoCmbte."IdTipoComprobante"
  Inner Join "BSCL.Facturacion.BD::FAC.EstadoFacturacion" As EstFact 
    On EstFact."IdEstado" = MvtoCmbte."IdEstadoFacturacion"
   And EstFact."NombreEntidad" = 'FAC.MovimientoComprobante'
  Inner Join "BSCL.Plataforma.BD::PLA.Estado" AS EstPla
    On EstPla."IdEstado" = EstFact."IdEstado"
Where --Coalesce(MvtoCmbte."IdEstadoFacturacion",'') <> 'ANU'
   Coalesce(Asig."IdEstadoAsignacion",'') <> 'REF'
  And Coalesce(Trans."IdEstado", '') <> 'ANU'
Order by Trans."IdTransaccion", "IdComprobante" Asc;
GO

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::CuadraturaDetalleTransaccionFacturacion" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO 
