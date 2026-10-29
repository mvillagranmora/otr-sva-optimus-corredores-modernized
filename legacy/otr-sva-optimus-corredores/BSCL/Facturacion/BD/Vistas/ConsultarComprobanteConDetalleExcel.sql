SET SCHEMA "BSCL"; 
GO  
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteConDetalleExcel"; 
GO  

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteConDetalleExcel" As
SELECT
cc."IdCliente",
cc."IdPortafolio",
cc."NombreEstado",
"NombreEstadoDTE",
"NombreEstadoSII",
cc."IdComprobante",
cc."FolioComprobante",
"IdComprobanteReferencia",
"CodigoTipoComprobante",
"DocumentoIdentidad",
"NombreLargoCliente",
"CodigoPortafolio",
"NombreEmpleadoEjecutivoPortafolio",
"NombreSucursalEmpleadoEjecutivoPortafolio",
cc."FechaComprobante",
cc."FechaLiquidacion",
cc."IdMoneda",
cc."Monto",
"MontoCobro",
"MontoFinal",
"MontoPagado",
cc."NombreTipoProducto",
cc."NombreTipoOperacion",
"EsVentaCorta",
"TotalComis",
"TotalDere",
"TotalGasto",
"TotalImpto",
"TotalRetencion",
--detalle
"NroLinea",
CCD."NemotecnicoInstrumento",
"Cantidad",
CCD."Precio",
ccd."Monto" AS "MontoDetalle",
ccd."NombreTipoInstrumento",
ccd."IdTipoProducto",
ccd."IdTipoOperacion",
ccd."IdBolsa"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" CC
INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CCD ON CC."IdComprobante" = CCD."IdComprobante";
GO 

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteConDetalleExcel" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO 