SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_IN_MovimientoComprobanteCobro" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_IN_Cobro" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobanteCobro" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_InformacionDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ResultadoProceso" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_AdvertenciasDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle" cascade;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_IN_MovimientoComprobanteCobro"
    ( "IdMovimientoComprobanteCobro" BIGINT, 
      "IdMovimientoComprobante" BIGINT, 
      "IdCobro" NVARCHAR(40), 
      "MontoCobro" DECIMAL(26,8)
    ) ON COMMIT DELETE ROWS;
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_IN_Cobro"
    ( "IdCobro" NVARCHAR(40), 
      "MontoCobro" DECIMAL(26,8), 
      "MontoCobroNuevo" DECIMAL(26,8), 
      "PorcentajeCobroNuevo" DECIMAL(26,8)
    ) ON COMMIT DELETE ROWS;  
    
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobanteCobro"
    ( "IdMovimientoComprobanteCobro" BIGINT, 
      "IdMovimientoComprobante" BIGINT, 
      "IdCobro" NVARCHAR(40),        
      "MontoMovimientoComprobanteCobro" DECIMAL(26,8)
    ) ON COMMIT DELETE ROWS;      
    
    


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_InformacionDetalle"
    ( "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;     
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ResultadoProceso"
    ( "Campo" VARCHAR(40),
      "Id" BIGINT, 
      "Servicio" VARCHAR(250), 
      "Resultado" VARCHAR(40)
    ) ON COMMIT DELETE ROWS;    
    
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_AdvertenciasDetalle"
    ( "IdMovimientoComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "RequiereAutorizacion" NVARCHAR(40),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;
    
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle"
    ( "Id" BIGINT, 
      "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;    

    
COMMIT;