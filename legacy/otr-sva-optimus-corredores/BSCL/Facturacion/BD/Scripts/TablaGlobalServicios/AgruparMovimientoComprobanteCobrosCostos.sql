SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_IN_MovimientoComprobante" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobante" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_Cobro" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_InformacionDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_ResultadoProceso" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_AdvertenciasDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle" cascade;



CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_IN_MovimientoComprobante"
    ( "IdPortafolio" BIGINT, 
      "IdCliente" BIGINT, 
      "IdMovimientoComprobante" BIGINT, 
      "IdTipoComprobante" NVARCHAR(3)
    ) ON COMMIT DELETE ROWS;
    
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_MovimientoComprobante"
    ( "IdMovimientoComprobanteCobro" BIGINT, 
      "IdMovimientoComprobante" BIGINT, 
      "IdCobro" NVARCHAR(40), 
      "NombreCobro" NVARCHAR(100), 
      "IdMonedaCobro" NVARCHAR(3), 
      "MontoCobro" DECIMAL(26,8),
      "IdMonedaMovimientoComprobanteCobro" NVARCHAR(3), 
      "MontoMovimientoComprobanteCobro" DECIMAL(26,8), 
      "IdMonedaLiquidacionCobro" NVARCHAR(3), 
      "MontoLiquidacionCobro" DECIMAL(26,8),
      "Modificable" NVARCHAR(1),
      "AumentarValor" NVARCHAR(1),
      "DisminuirValor" NVARCHAR(1)
    ) ON COMMIT DELETE ROWS;
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_Cobro"
    ( "IdCobro" NVARCHAR(40), 
      "NombreCobro" NVARCHAR(100), 
      "IdMonedaCobro" NVARCHAR(3), 
      "MontoCobro" DECIMAL(26,8),
      "IdMonedaMovimientoComprobanteCobro" NVARCHAR(3), 
      "MontoMovimientoComprobanteCobro" DECIMAL(26,8), 
      "IdMonedaLiquidacionCobro" NVARCHAR(3), 
      "MontoLiquidacionCobro" DECIMAL(26,8),
      "Modificable" NVARCHAR(1),
      "AumentarValor" NVARCHAR(1),
      "DisminuirValor" NVARCHAR(1)
    ) ON COMMIT DELETE ROWS; 
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_InformacionDetalle"
    ( "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;     
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_ResultadoProceso"
    ( "Campo" VARCHAR(40),
      "Id" BIGINT, 
      "Servicio" VARCHAR(250), 
      "Resultado" VARCHAR(40)
    ) ON COMMIT DELETE ROWS;    
    
    


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_AdvertenciasDetalle"
    ( "IdMovimientoComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "RequiereAutorizacion" NVARCHAR(40),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;
    
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AgruparMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle"
    ( "IdMovimientoComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;      
    
COMMIT;             
