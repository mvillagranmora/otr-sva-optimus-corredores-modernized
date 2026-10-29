SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_IN_Movimiento" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_OUT_ClienteRechazados" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_OUT_ErroresDetalle" cascade;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_IN_Movimiento"
    ( "IdCliente"         BIGINT, 
      "CodigoOperadorAfp" BIGINT,
      "FechaMovimientoComprobante" NVARCHAR(10)
    ) ON COMMIT DELETE ROWS;
    
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_OUT_ClienteRechazados"
    ( "IdCliente" BIGINT) ON COMMIT DELETE ROWS;
   

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosGenerarInterfazAfp_OUT_ErroresDetalle"
    ( "IdCliente" BIGINT, 
      "Codigo" nvarchar(100),
      "Descripcion" nvarchar(520)   
    ) ON COMMIT DELETE ROWS;     

    
Commit;    