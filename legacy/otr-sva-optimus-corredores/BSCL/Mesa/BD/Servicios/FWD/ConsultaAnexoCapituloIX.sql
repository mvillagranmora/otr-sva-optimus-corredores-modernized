SET SCHEMA "BSCL";  
 
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.ConsultaAnexoCapituloIX";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.ConsultaAnexoCapituloIX"
    ( 
	-- Tabla Entrada
	IN IdMoneda NVARCHAR(3),
	IN IdEmpresa BIGINT,			   
	--Salida   	1--------------------------------------------------
   	OUT p_out_posicion table ("IdPosicionInicial"	BIGINT
							, "IdEmpresa"			BIGINT
							, "Fecha" 				SECONDDATE
							, "IdMoneda"			NVARCHAR(3)	
							, "TipoCambioInicial"	DECIMAL(26,8)
							, "CierreIngresado"		DECIMAL(26,8)
							, "PosicionInicial"		DECIMAL(26,8)
							, "TipoInstrumento"		NVARCHAR(50)
							, "IdUsuario"			NVARCHAR(50)
							, "Estado"				NVARCHAR(3)
							, "ResultadoPosicionInicial"  	DECIMAL(26,8))
    ) 

	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA 
AS   
  BEGIN			
		p_out_posicion = SELECT TOP 1
						  PI."IdPosicionInicial"	
						, PI."IdEmpresa"		
						, PI."Fecha"		
						, PI."IdMoneda" 	
						, PI."TipoCambioInicial"	
						, PI."CierreIngresado"  	
						, PI."PosicionInicial"  	
						, PI."TipoInstrumento"  	
						, PI."IdUsuario"			
						, PI."Estado"		
						, PI."ResultadoPosicionInicial"
						FROM "BSCL.Mesa.BD::SPT.PosicionInicialMesa" AS PI
							WHERE PI."Estado" = 'ACT'
							AND PI."IdMoneda" = :IdMoneda
							AND PI."IdEmpresa" = :IdEmpresa
				ORDER BY PI."Fecha" DESC;
END;