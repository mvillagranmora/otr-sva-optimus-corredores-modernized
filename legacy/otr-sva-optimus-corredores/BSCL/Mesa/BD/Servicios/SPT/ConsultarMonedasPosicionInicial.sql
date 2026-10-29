SET SCHEMA "BSCL";  
 
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarMonedasPosicionInicial";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarMonedasPosicionInicial"
    ( 
	-- Tabla Entrada
	IN p_IdEmpresa BIGINT,			   
	--Salida   	1--------------------------------------------------
   	OUT p_out_monedasPosicionInicial table ("IdPosicionInicial"	BIGINT
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
		p_out_monedasPosicionInicial = SELECT *
									   FROM "BSCL.Mesa.BD::SPT.PosicionInicialMesa";
							
END;