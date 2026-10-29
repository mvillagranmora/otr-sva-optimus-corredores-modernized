SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosDteEnviarLibro_IN_Comprobante" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosDteEnviarLibro_OUT_ComprobantesRechazados" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosDteEnviarLibro_OUT_ErroresDetalle" cascade;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosDteEnviarLibro_IN_Comprobante"
    ( "IdPortafolio" BIGINT, 
      "IdCliente" BIGINT,
      "IdComprobante" BIGINT,
      "IdTipoComprobante" NVARCHAR(3)
    ) ON COMMIT DELETE ROWS;
    
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosDteEnviarLibro_OUT_ComprobantesRechazados"
    ( "IdComprobante" BIGINT) ON COMMIT DELETE ROWS;
   

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosDteEnviarLibro_OUT_ErroresDetalle"
    ( "IdComprobante" BIGINT, 
      "Codigo" nvarchar(100),
      "Descripcion" nvarchar(520)   
    ) ON COMMIT DELETE ROWS;     
    
    
Commit;    