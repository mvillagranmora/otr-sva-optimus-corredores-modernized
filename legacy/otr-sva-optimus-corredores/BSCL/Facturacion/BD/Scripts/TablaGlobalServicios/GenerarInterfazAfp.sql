SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_IN_Movimiento" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_Movimiento" cascade;

DROP TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_InformacionDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ResultadoProceso" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_AdvertenciasDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ErroresDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_Concatenado" cascade;
    


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_IN_Movimiento"
    ( "IdCliente"         BIGINT, 
      "CodigoOperadorAfp" BIGINT,
      "FechaMovimientoComprobante" NVARCHAR(10)
    ) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_Movimiento"
    ( "FechaTransaccion"       NVARCHAR(10), 
      "FolioTransaccion"       NVARCHAR(40), 
      "Corredora"              BIGINT,
      "CodigoOperadorAfp"      BIGINT,
      "AccionTipoOperacion"    NVARCHAR(1),
      "Cantidad"               DECIMAL(26,8),
      "Precio"                 DECIMAL(26,8),
      "HoraTransaccion"        NVARCHAR(8),
      "NemotecnicoInstrumento" NVARCHAR(40),
      "Total"                  DECIMAL(26,8),
      "Boveda"                 NVARCHAR(1),
      "OD"                     NVARCHAR(2),
      "Bolsa"                  INTEGER,
      "CondicionDeLiquidacion" NVARCHAR(2),
      "TipoOperador"           NVARCHAR(1),
      "Resultado"              NVARCHAR(500)
    ) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_InformacionDetalle"
    ( "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_Concatenado"
    ( 
      "Resultado"              NVARCHAR(4000)
      
    ) ON COMMIT DELETE ROWS;

    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ResultadoProceso"
    ( "Campo" VARCHAR(40),
      "Id" BIGINT, 
      "Servicio" VARCHAR(250), 
      "Resultado" VARCHAR(40)
    ) ON COMMIT DELETE ROWS;  
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_AdvertenciasDetalle"
    ( "IdCliente" BIGINT, 
      "Codigo" NVARCHAR(100),
      "RequiereAutorizacion" NVARCHAR(40),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;      
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ErroresDetalle"
    ( "IdCliente" BIGINT, 
      "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;
    


Commit;         
    
