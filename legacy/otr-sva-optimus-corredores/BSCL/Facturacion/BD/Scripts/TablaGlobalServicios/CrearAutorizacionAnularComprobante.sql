SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_IN_Comprobante" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_AdvertenciasDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_ErroresDetalle" cascade;
DROP TABLE "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_InformacionDetalle" cascade;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_IN_Comprobante"
    ( "IdPortafolio" 		BIGINT, 
      "IdCliente" 			BIGINT, 
      "IdComprobante" 		BIGINT,
      "IdTipoComprobante"	NVARCHAR(3)
    ) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_AdvertenciasDetalle"
    ( "IdComprobante"	BIGINT, 
      "Codigo"					NVARCHAR(100),
      "RequiereAutorizacion"	NVARCHAR(40),
      "Descripcion"				NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS; 

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_ErroresDetalle"
    ( "IdComprobante"	BIGINT, 
      "Codigo"					NVARCHAR(100),
      "Descripcion"				NVARCHAR(5000)
    ) ON COMMIT DELETE ROWS;         
    
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Facturacion.BD::GBL.CrearAutorizacionAnularComprobante_OUT_InformacionDetalle"
    (	"Codigo" 				nvarchar(100),
    	"Descripcion" 			nvarchar(5000)
    ) ON COMMIT DELETE ROWS;      
        
       
commit;
