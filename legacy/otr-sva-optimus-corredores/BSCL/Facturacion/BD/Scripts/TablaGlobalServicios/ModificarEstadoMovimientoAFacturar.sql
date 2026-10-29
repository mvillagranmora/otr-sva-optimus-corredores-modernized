SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_IN_MovimientoComprobante" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_InformacionDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_ResultadoProceso" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_AdvertenciasDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_ErroresDetalle" cascade;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_IN_MovimientoComprobante"
    ( "IdMovimientoComprobante" BIGINT ) ON COMMIT DELETE ROWS;
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_InformacionDetalle"
    ( "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;     
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_ResultadoProceso"
    ( "Campo" VARCHAR(40),
      "Id" BIGINT, 
      "Servicio" VARCHAR(250), 
      "Resultado" VARCHAR(40)
    ) ON COMMIT DELETE ROWS;  
    
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_AdvertenciasDetalle"
    ( "IdMovimientoComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "RequiereAutorizacion" NVARCHAR(40),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS; 
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarEstadoMovimientoAFacturar_OUT_ErroresDetalle"
    ( "IdMovimientoComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS; 
    
    
COMMIT;    
