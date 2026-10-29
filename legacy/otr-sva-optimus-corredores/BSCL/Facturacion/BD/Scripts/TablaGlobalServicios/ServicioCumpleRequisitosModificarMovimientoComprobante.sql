SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_IN_MovimientoComprobante" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_MovimientosRechazados" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_ErroresDetalle" cascade;



CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_IN_MovimientoComprobante"
    ( "IdMovimientoComprobante" BIGINT ) ON COMMIT DELETE ROWS;    
    
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_MovimientosRechazados"
    ( "IdMovimientoComprobante" BIGINT ) ON COMMIT DELETE ROWS;  
    
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosModificarMovimientoComprobante_OUT_ErroresDetalle"
    (  "IdMovimientoComprobante" BIGINT, 
       "Codigo" NVARCHAR(100),
       "Descripcion" NVARCHAR(520)
    ) ON COMMIT DELETE ROWS;    

    
COMMIT;