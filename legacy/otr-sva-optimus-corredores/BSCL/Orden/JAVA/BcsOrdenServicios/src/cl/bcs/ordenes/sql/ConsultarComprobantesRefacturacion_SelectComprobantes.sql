SELECT Cmbte."IdComprobante",Cmbte."IdEstadoComprobante",Cmbte."IdTipoComprobante",
CmbteNotaCredito."IdComprobante" AS "IdComprobanteNotaCredito",
Cmbte."IdCliente",
Cmbte."IdPortafolio",
Cmbte."IdCondicionLiquidacion" AS "IdCondicionDeLiquidacion",
Cmbte."FechaLiquidacion",
Cmbte."DocumentoIdentidad",
Cmbte."NombreLargoCliente" AS "NombreCompleto",
port."CodigoPortafolio",
MvtoCmbte."FechaTransaccion",
MvtoCmbte."FolioTransaccion",
MvtoCmbte."IdTransaccion",
MvtoCmbte."NemotecnicoInstrumento" AS "Instrumento",
MvtoCmbte."NemotecnicoSubyacente" AS "InstrumentoSubyacente",
MvtoCmbte."IdTipoInstrumento",
MvtoCmbte."IdClasificacionTipoInstrumento",
MvtoCmbte."IdMoneda",
MvtoCmbte."Cantidad",
EstadoPlaCmbte."Nombre" As "NombreEstadoComprobante"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Cmbte
INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS CmbteDet ON Cmbte."IdComprobante" = CmbteDet."IdComprobante"
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" MvtoCmbteDet ON CmbteDet."IdComprobanteDetalle" = MvtoCmbteDet."IdComprobanteDetalle"
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MvtoCmbte ON MvtoCmbte."IdMovimientoComprobante" = MvtoCmbteDet."IdMovimientoComprobante"
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" As EstadoPlaCmbte On EstadoPlaCmbte."IdEstado" = Cmbte."IdEstadoComprobante"   
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS CmbteNotaCredito ON Cmbte."IdComprobante" = CmbteNotaCredito."IdComprobanteReferencia" AND CmbteNotaCredito."IdEstadoComprobante" != 'ANU'  
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" port ON Cmbte."IdPortafolio" = port."IdPortafolio"
WHERE 
CmbteNotaCredito."IdComprobante" IS NULL
AND Cmbte."IdEstadoComprobante" != 'ANU'
AND Cmbte."FechaComprobante" = :fechaComprobante
AND Cmbte."IdTipoComprobante" IN (:idsTipoComprobante);