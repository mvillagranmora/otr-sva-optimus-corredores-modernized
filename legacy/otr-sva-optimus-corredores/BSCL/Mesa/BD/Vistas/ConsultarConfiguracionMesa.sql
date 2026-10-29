SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa"; 
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa"  
AS
	SELECT "IdEmpresa"		
		 , "IdTipoProducto" 	
		 , "CodigoParametro" 
		 , "Descripcion" 	
		 , "Valor" 			 			
	  FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa"
	
	WITH READ ONLY;
    
GRANT SELECT 	ON "BSCL.Mesa.BD.Vistas::ConsultarConfiguracionMesa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
