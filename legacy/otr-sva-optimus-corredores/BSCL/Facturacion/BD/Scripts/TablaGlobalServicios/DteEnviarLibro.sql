SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.DteEnviarLibro_IN_Comprobante" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_InformacionDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ResultadoProceso" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_AdvertenciasDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ErroresDetalle" cascade;



CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.DteEnviarLibro_IN_Comprobante"
    ( "IdPortafolio"      BIGINT, 
      "IdCliente"         BIGINT, 
      "IdComprobante"     BIGINT, 
      "IdTipoComprobante" NVARCHAR(3)
    ) ON COMMIT DELETE ROWS;



CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_InformacionDetalle"
    ( "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ResultadoProceso"
    ( "Campo" VARCHAR(40),
      "Id" BIGINT, 
      "Servicio" VARCHAR(250), 
      "Resultado" VARCHAR(40)
    ) ON COMMIT DELETE ROWS;  
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_AdvertenciasDetalle"
    ( "IdComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "RequiereAutorizacion" NVARCHAR(40),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;      
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ErroresDetalle"
    ( "IdComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;     
    