SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_IN_Comprobante" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_OUT_ResultadoProceso" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_OUT_MovimientosRechazados" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_OUT_ErroresDetalle" cascade;




CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_IN_Comprobante"
    ( "IdPortafolio" BIGINT, 
      "IdCliente" BIGINT, 
      "IdComprobante" BIGINT, 
      "IdTipoComprobante" NVARCHAR(3)
    ) ON COMMIT DELETE ROWS;
    
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_OUT_ResultadoProceso"
    ( "Campo" VARCHAR(40),
      "Id" BIGINT, 
      "Servicio" VARCHAR(250), 
      "Resultado" VARCHAR(40)) ON COMMIT DELETE ROWS; 
            
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_OUT_MovimientosRechazados"
    ( "IdComprobante" BIGINT) ON COMMIT DELETE ROWS; 
    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.ServicioCumpleRequisitosCrearComprobanteNotaDeCredito_OUT_ErroresDetalle"
    ( "IdComprobante" BIGINT, 
      "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(520)
    ) ON COMMIT DELETE ROWS;        
