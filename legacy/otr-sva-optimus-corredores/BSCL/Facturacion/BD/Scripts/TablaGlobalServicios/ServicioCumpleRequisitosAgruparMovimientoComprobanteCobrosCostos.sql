SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAgruparMovimientoComprobanteCobrosCostos_IN_MovimientoComprobante" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAgruparMovimientoComprobanteCobrosCostos_OUT_MovimientosRechazados" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAgruparMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle" cascade;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAgruparMovimientoComprobanteCobrosCostos_IN_MovimientoComprobante"
    ( "IdPortafolio" BIGINT,
      "IdCliente" BIGINT,
      "IdMovimientoComprobante" BIGINT, 
      "IdTipoComprobante" NVARCHAR(3)
    ) ON COMMIT DELETE ROWS;
    
   
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAgruparMovimientoComprobanteCobrosCostos_OUT_MovimientosRechazados"
    ( "IdMovimientoComprobante" BIGINT) ON COMMIT DELETE ROWS;
   

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosAgruparMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle"
    ( "IdMovimientoComprobante" BIGINT, 
      "Codigo" nvarchar(100),
      "Descripcion" nvarchar(520)   
    ) ON COMMIT DELETE ROWS;  
	
commit;