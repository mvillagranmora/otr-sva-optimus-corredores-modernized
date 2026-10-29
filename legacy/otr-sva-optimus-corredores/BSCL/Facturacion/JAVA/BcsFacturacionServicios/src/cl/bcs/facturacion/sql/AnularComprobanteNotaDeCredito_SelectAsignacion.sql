SELECT MvtoCmbte."IdMovimientoComprobante" As "NumeroMvtoCmbte",
MvtoCmbte."IdComprobante", MvtoCmbte."IdAsignacion" as "IdAsignacion",ASI."IdEstadoAsignacion" as "IdEstadoAsignacion"

From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte
  Left Join "BSCL.Plataforma.BD::INS.TipoOperacion" TipoOperacion
    On TipoOperacion."IdTipoOperacion" = MvtoCmbte."IdTipoOperacion"
  Left Join "BSCL.Facturacion.BD.Vistas::ConsultarConceptosFacturacion" ConcepFact
    On ConcepFact."IdTipoOperacion" = TipoOperacion."IdTipoOperacionAnulacion"
   And TipoOperacion."IdTipoOperacionAnulacion" <> ''
  Left Join "BSCL.Facturacion.BD::FAC.MovimientoComprobanteReferencia" MCR
  	On MvtoCmbte."IdMovimientoComprobante" = MCR."IdMovimientoComprobante"
  Left Join "BSCL.Facturacion.BD::FAC.ReferenciaDocumento" MC
  	On MCR."TipoReferencia" = MC."TipoReferencia"
  	Left Join "BSCL.Orden.BD::ORD.Asignacion"  ASI
  	On ASI."IdAsignacion" = MvtoCmbte."IdAsignacion"
WHERE MvtoCmbte."IdComprobante" IN (:idsComprobante);