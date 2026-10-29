SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteEmisionMasiva_IN_MovimientoComprobante" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteEmisionMasiva_OUT_Comprobante" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteEmisionMasiva_OUT_AdvertenciasDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteEmisionMasiva_OUT_ErroresDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteEmisionMasiva_OUT_InformacionDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteEmisionMasiva_OUT_ResultadoProceso" cascade;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteEmisionMasiva_IN_MovimientoComprobante"
    ( "IdCliente"     BIGINT,
      "IdPortafolio"  BIGINT
    ) ON COMMIT DELETE ROWS;
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteEmisionMasiva_OUT_Comprobante"
    ( "IdComprobante" BIGINT ) ON COMMIT DELETE ROWS; 
    
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteEmisionMasiva_OUT_InformacionDetalle"
    ( "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;
    
        
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteEmisionMasiva_OUT_ResultadoProceso"
    ( "Campo" NVARCHAR(40),
      "Id" BIGINT, 
      "Servicio" NVARCHAR(250), 
      "Resultado" NVARCHAR(40)
    ) ON COMMIT DELETE ROWS;    
    
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteEmisionMasiva_OUT_AdvertenciasDetalle"
    ( "IdMovimientoComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "RequiereAutorizacion" NVARCHAR(40),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;         
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CrearComprobanteEmisionMasiva_OUT_ErroresDetalle"
    ( "IdMovimientoComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;
    
    

