SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarPosicionInicial";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarPosicionInicial"(
	IN p_IdEmpresa BIGINT,
	IN p_IdMoneda NVARCHAR(3),
	IN p_Fecha NVARCHAR(10),
	
	--Salida
   	OUT p_out_posicion	TABLE (	 "IdPosicionInicial"		BIGINT
   								,"IdEmpresa"				BIGINT
								,"Fecha" 					SECONDDATE
								,"IdMoneda"					NVARCHAR(3)	
								,"TipoCambioInicial"		DECIMAL(26,8)
								,"CierreIngresado"			DECIMAL(26,8)
								,"PosicionInicial"			DECIMAL(26,8)
								,"TipoInstrumento"			NVARCHAR(50)
								,"IdUsuario"				NVARCHAR(50)
								,"Estado"					NVARCHAR(3)
								,"ResultadoPosicionInicial"	DECIMAL(26,8))
)
LANGUAGE SQLSCRIPT 
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
--READS SQL DATA 
AS
BEGIN
	p_out_posicion =
		SELECT
			 PI."IdPosicionInicial"	
			,PI."IdEmpresa"		
			,PI."Fecha"		
			,PI."IdMoneda" 	
			,PI."TipoCambioInicial"	
			,PI."CierreIngresado"  	
			,PI."PosicionInicial"  	
			,PI."TipoInstrumento"  	
			,PI."IdUsuario"			
			,PI."Estado"		
			,PI."ResultadoPosicionInicial"
		FROM "BSCL.Mesa.BD::SPT.PosicionInicialMesa" AS PI
		WHERE PI."Estado" = 'ACT'
		  AND PI."IdMoneda" = :p_IdMoneda
		  AND PI."IdEmpresa" = :p_IdEmpresa
		  AND TO_VARCHAR(PI."Fecha",'YYYY-MM-DD') = :p_Fecha
		;
	
END;
