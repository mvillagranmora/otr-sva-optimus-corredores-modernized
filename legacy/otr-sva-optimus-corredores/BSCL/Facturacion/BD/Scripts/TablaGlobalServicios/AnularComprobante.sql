SET SCHEMA "BSCL"; 
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.AnularComprobante_IN_Comprobante" cascade;
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.AnularComprobante_OUT_Comprobante" cascade;
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.AnularComprobante_OUT_ResultadoProceso" cascade;
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.AnularComprobante_OUT_InformacionDetalle" cascade;
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.AnularComprobante_OUT_AdvertenciasDetalle" cascade;
GO

DROP TABLE "BSCL.Facturacion.BD::GBL.AnularComprobante_OUT_ErroresDetalle" cascade;
GO

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AnularComprobante_IN_Comprobante"
    ( "IdPortafolio" 		BIGINT, 
      "IdCliente" 			BIGINT, 
      "IdComprobante" 		BIGINT,
      "IdTipoComprobante"	NVARCHAR(3)
    ) ON COMMIT DELETE ROWS;
GO
    
    
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AnularComprobante_OUT_Comprobante"
   ( "IdComprobante" 		BIGINT
   ) ON COMMIT DELETE ROWS;  
GO   
   
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AnularComprobante_OUT_ResultadoProceso"
    ( "Campo"		VARCHAR(40),
      "Id"			BIGINT, 
      "Servicio"	VARCHAR(250), 
      "Resultado"	VARCHAR(40)
    ) ON COMMIT DELETE ROWS;  
GO
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AnularComprobante_OUT_InformacionDetalle"
    ( "Codigo" NVARCHAR(100),
      "Descripcion" NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;
GO    
    
    
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::GBL.AnularComprobante_OUT_AdvertenciasDetalle"
    ( "IdComprobante"	BIGINT, 
      "Codigo"					NVARCHAR(100),
      "RequiereAutorizacion"	NVARCHAR(40),
      "Descripcion"				NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;
GO    

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.AnularComprobante_OUT_ErroresDetalle"
    ( "IdComprobante"	BIGINT, 
      "Codigo"					NVARCHAR(100),
      "Descripcion"				NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;
GO    
    
    
        
       
commit;
GO