SET SCHEMA "BSCL"; 
GO  
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteConDetalle"; 
GO  

CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteConDetalle" As
SELECT 
       Cmbte."IdComprobante", 
       Cmbte."IdPortafolio",
       Cmbte."ModPrefFact", 
       Cmbte."IdCliente",
       Cmbte."DocumentoIdentidad", 
       Cmbte."FolioComprobante", 
       Cmbte."IdTipoComprobante", 
       Cmbte."IdConcepto",
       Cmbte."Monto" As "MontoComprobante",
       "TipCmbte"."Codigo" AS "CodigoTipoComprobante", 
       "TipCmbte"."Nombre" AS "NombreTipoComprobante", 
       Cmbte."FechaComprobante",
       Cmbte."FechaEmision", 
       Cmbte."IdComprobanteReferencia", 
       Cmbte."IdTipoComprobanteReferencia", 
       Cmbte."FolioComprobanteReferencia", 
       Cmbte."FechaComprobanteReferencia",         
       Cmbte."FechaIngresoRegistro" AS "FechaIngresoRegistroComprobante", 
       Cmbte."IdEmpresa", 
       "Empresa"."Codigo" AS "CodigoEmpresa", 
       "Empresa"."Nombre" AS "NombreEmpresa", 
       Cmbte."IdEstadoComprobante", 
       CmbteDet."IdComprobanteDetalle", 
       CmbteDet."NroLinea", 
       Cmbte."Glosa", 
       Cmbte."EsExento", 
       CmbteDet."FechaLiquidacion",
       Cmbte."IdClasificacionTipoInstrumento",
       CmbteDet."NemotecnicoInstrumento",
       CmbteDet."IdTipoOperacion", 
       TipOper."Accion" As "AccionTipoOperacion", 
       TipOper."Nombre" As "NombreTipoOperacion",
       CmbteDet."IdTipoProducto", 
       CmbteDet."Cantidad" As "ComprobanteDetalleCantidad",
       CmbteDet."Precio" As "ComprobanteDetallePrecio",
       
       CmbteDet."Monto" As "MontoComprobanteDetalle",
       CmbteDet."MontoComprobanteDetalle" As "MontoComprobanteComprobanteDetalle",
       CmbteDet."MontoLiquidacion" As "MontoLiquidacionComprobanteDetalle",
       
       CmbteDet."IdBolsa", 
       CmbteDet."IdEmpleadoEjecutivoPortafolio", 
       CmbteDet."IdSucursalEmpleadoEjecutivoPortafolio", 
       CmbteDet."FechaIngresoRegistro" AS "FechaIngresoRegistroComprobanteDetalle", 
       CmbteDet."IdEstadoComprobanteDetalle",
       CmbteDet."IdInstrumento",
       CmbteDet."IndicadorOrdenDirecta",
       Cmbte."IdCondicionLiquidacion",
       CondLiqu."Codigo" As "CodigoCondicionDeLiquidacion",
	   CondLiqu."Nombre" As "NombreCondicionDeLiquidacion",
	   CmbteDet."NemotecnicoSubyacente",
	   CmbteDet."ValorRescate"
FROM  "BSCL.Facturacion.BD::FAC.Comprobante" As Cmbte 
  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" As CmbteDet 
    ON CmbteDet."IdComprobante" = Cmbte."IdComprobante"
  LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS "Empresa"
    ON "Empresa"."IdEmpresa" = Cmbte."IdEmpresa"
  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.EstadoFacturacion" AS "EstFac" 
    ON "EstFac"."IdEstado" = Cmbte."IdEstadoComprobante"  
  LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS "TipCmbte"
    ON "TipCmbte"."IdTipoComprobante" = Cmbte."IdTipoComprobante"
  Left Join "BSCL.Plataforma.BD::INS.TipoOperacion" As TipOper
    On TipOper."IdTipoOperacion" = CmbteDet."IdTipoOperacion"
  INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CondLiqu
    ON CondLiqu."IdCondicionDeLiquidacion" = Cmbte."IdCondicionLiquidacion"  
WHERE  Upper("EstFac"."NombreEntidad") = Upper('FAC.Comprobante');
GO 

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteConDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO 