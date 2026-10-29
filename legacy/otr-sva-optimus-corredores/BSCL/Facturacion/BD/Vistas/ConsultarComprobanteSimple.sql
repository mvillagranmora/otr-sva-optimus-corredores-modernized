SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteSimple";
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteSimple" As 

SELECT "IdComprobante", 
       "IdEmpresa", 
       "IdPortafolio", 
       "ModPrefFact", 
       "IdCliente", 
       "DocumentoIdentidad", 
       "TipoDocumentoIdentidad", 
       "NombreLargoCliente", 
       "FolioComprobante", 
       "IdTipoComprobante", 
       "IdConcepto", 
       "IdTipoOperacion", 
       "IdTipoProducto", 
       "IdClasificacionTipoInstrumento", 
       "IdTipoInstrumento",       
       "MontoFinal", 
       "MontoPagado", 
       "IdEmpleadoEjecutivoPortafolio", 
       "IdSucursalEmpleadoEjecutivoPortafolio",
       "IdCondicionLiquidacion", 
       "FechaLiquidacion", 
       "FechaEmision", 
       "FechaEnvio", 
       "IdEstadoDTE", 
       "IdEstadoSII", 
       "FechaRespuestaEnvio", 
       "IdEstadoComprobante", 
       "IdComprobanteReferencia", 
       "IdTipoComprobanteReferencia", 
       "FolioComprobanteReferencia", 
       "FechaComprobanteReferencia", 
       "FechaComprobante", 
       "Glosa", 
       "EsExento", 
       "FechaIngresoRegistro", 
       "FechaSistema", 
       "IdCarga", 
       "FolioComprobanteFacturacionMigracion", 
       "IdMoneda", 
       "Monto", 
       "IdMonedaComprobante", 
       "MontoComprobante", 
       "IdMonedaLiquidacion", 
       "MontoLiquidacion",
       Round("MontoCobro", 0) As "MontoCobro",
       Round("MontoComprobanteCobro", 0) As "MontoCobroComprobante", 
       Round("MontoLiquidacionCobro", 0) As "MontoCobroLiquidacion"
FROM "BSCL.Facturacion.BD::FAC.Comprobante";


GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteSimple" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 