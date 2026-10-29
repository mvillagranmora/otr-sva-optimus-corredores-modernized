SET SCHEMA "BSCL";  
 
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarConfiguracionMesa";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarConfiguracionMesa"
    ( 
	-- Tabla Entrada
	IN p_IdEmpresa BIGINT,	
	IN p_IdTipoProducto NVARCHAR(4),
	IN p_CodigoParametro NVARCHAR(40),
	
	--Salida   	--------------------------------------------------
   	OUT p_out_datosMesa table ("IdEmpresa"			BIGINT
							, "IdTipoProducto" 		NVARCHAR(4)	
							, "CodigoParametro"		NVARCHAR(40)	
							, "Descripcion"			NVARCHAR(520)
							, "Valor"				NVARCHAR(520))

    ) 

	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA 
AS   
  BEGIN			
		p_out_datosMesa = SELECT CM."IdEmpresa"
								, CM."IdTipoProducto" 		
								, CM."CodigoParametro"		
								, CM."Descripcion"		
								, CM."Valor"			 			
						FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa" CM
						WHERE CM."IdEmpresa" = :p_IdEmpresa
						  AND CM."IdTipoProducto" = :p_IdTipoProducto
						  AND (CM."CodigoParametro" = :p_CodigoParametro OR :p_CodigoParametro = '');
END;