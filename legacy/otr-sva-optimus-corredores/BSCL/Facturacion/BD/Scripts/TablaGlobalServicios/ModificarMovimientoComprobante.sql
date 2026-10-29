SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobante" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobanteFormaDePago" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobanteCobro" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_InformacionDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ResultadoProceso" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_AdvertenciasDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ErroresDetalle" cascade;




CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobante"
   ( "IdMovimientoComprobante" BIGINT,
     "IdPortafolio"     BIGINT,
     "IdFormaCustodia"  NVARCHAR(3),
     "IdBoveda"         NVARCHAR(3),
     "IdTipoMovimiento" NVARCHAR(6),
     "EsComprobIndividual" NVARCHAR(1)
   ) ON COMMIT DELETE ROWS;
   

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobanteFormaDePago"
   ( "IdMovimientoComprobanteFormaDePago" BIGINT,
     "IdMovimientoComprobante"  BIGINT, 
     "IdTipoMovimiento"         NVARCHAR(6),
     "IdMedioDePago"            NVARCHAR(3),
     "NombreMedioDePago"        NVARCHAR(100),
     "IdModalidadDePago"        NVARCHAR(6),
     "NombreModalidadDePago"    NVARCHAR(100),
     "MontoLiquidacionFormaDePago"  DECIMAL(26,8),
     "IdMoneda"                 NVARCHAR(3),
     "IdBanco"                  BIGINT,
     "CodigoBanco"              NVARCHAR(40),
     "NombreBanco"              NVARCHAR(100),
     "IdEstado"                 NVARCHAR(3),
     "IdCuentaBancaria"         BIGINT,
     "NombreCuentaBancaria"     NVARCHAR(100),
     "NumeroCuentaBancaria"     NVARCHAR(100),
     "IdTarjetaBancaria"        BIGINT,
     "FechaVctoTarjetaBancaria" NVARCHAR(10),
     "IdTipoCuentaBancaria"     NVARCHAR(4),
     "NombreTipoCuentaBancaria" NVARCHAR(100),
     "NumeroSerie"              NVARCHAR(40),
     "NumeroDocumento"          BIGINT,
     "FechaDocumentoPago"       NVARCHAR(10),
     "IdTipoTarjetaBancaria"    NVARCHAR(40),
     "NombreTipoTarjetaBancaria" NVARCHAR(100),
     "NumeroTarjetaBancaria"    NVARCHAR(100)
   ) ON COMMIT DELETE ROWS;
   

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_IN_MovimientoComprobanteCobro"   
   ( "IdMovimientoComprobanteCobro" BIGINT,
     "IdMovimientoComprobante"      BIGINT, 
     "IdCobro"                      NVARCHAR(6),
     "IdEstado"                     NVARCHAR(3),
     "Porcentaje"                   DECIMAL(26,8),
     "IdMonedaCobro"                NVARCHAR(3),
     "MontoCobro"                   DECIMAL(26,8),
     "IdMonedaMovimientoComprobanteCobro" NVARCHAR(3), 
     "MontoMovimientoComprobanteCobro"    DECIMAL(26,8),
     "IdMonedaLiquidacionCobro"     NVARCHAR(3),
     "MontoLiquidacionCobro"        DECIMAL(26,8)
   ) ON COMMIT DELETE ROWS;
   
   
   
   
   

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_InformacionDetalle"
    ( "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ResultadoProceso"
    ( "Campo" VARCHAR(40),
      "Id" BIGINT, 
      "Servicio" VARCHAR(250), 
      "Resultado" VARCHAR(40)
    ) ON COMMIT DELETE ROWS;  
        
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_AdvertenciasDetalle"
    ( "IdMovimientoComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "RequiereAutorizacion" NVARCHAR(40),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;      
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ModificarMovimientoComprobante_OUT_ErroresDetalle"
    ( "IdMovimientoComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;
    


Commit;    
