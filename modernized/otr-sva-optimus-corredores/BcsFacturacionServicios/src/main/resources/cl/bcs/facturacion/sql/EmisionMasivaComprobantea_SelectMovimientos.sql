Select MvtoCmbte."IdPortafolio", MvtoCmbte."IdCliente", MvtoCmbte."IdMovimientoComprobante",  
   MvtoCmbte."IdTipoComprobante", TO_DATE(MvtoCmbte."FechaMovimientoComprobante") As "FechaComprobante",MvtoCmbte."NombreLargoCliente",MvtoCmbte."DocumentoIdentidad",MvtoCmbte."NemotecnicoInstrumento"
From "BSCL.Facturacion.BD::FAC.MovimientoComprobante" As MvtoCmbte                                  
Where COALESCE(MvtoCmbte."IdClasificacionTipoInstrumento",'') = (Case 
                When (:IdClasificacionTipoInstrumento = '') Then COALESCE(MvtoCmbte."IdClasificacionTipoInstrumento",'')
                                                  Else :IdClasificacionTipoInstrumento 
                                                  End)
  And COALESCE(MvtoCmbte."IdTipoInstrumento",'') = (Case 
     When (:IdTipoInstrumento = '') Then COALESCE(MvtoCmbte."IdTipoInstrumento",'')
                                       Else :IdTipoInstrumento 
                                       End)
  And COALESCE(MvtoCmbte."IdTipoComprobante",'') = (Case 
     When (:IdTipoComprobante = '') Then COALESCE(MvtoCmbte."IdTipoComprobante",'')
                                       Else :IdTipoComprobante 
                                       End)
  And COALESCE(MvtoCmbte."IdCondicionLiquidacion",'') = (Case 
          When (:IdCondicionLiquidacion = '') Then COALESCE(MvtoCmbte."IdCondicionLiquidacion",'')
                                            Else :IdCondicionLiquidacion 
                                            End)
  And COALESCE(MvtoCmbte."IdBoveda",'') = (Case
When (:IdBoveda = '') Then  COALESCE(MvtoCmbte."IdBoveda",'')
                              Else :IdBoveda
                              End)
  And Cast(MvtoCmbte."FechaMovimientoComprobante" As DATE) = :FechaMovimientoComprobante
  And MvtoCmbte."IdEstadoFacturacion" = 'ING'
Order By MvtoCmbte."IdCliente", MvtoCmbte."IdMovimientoComprobante"
