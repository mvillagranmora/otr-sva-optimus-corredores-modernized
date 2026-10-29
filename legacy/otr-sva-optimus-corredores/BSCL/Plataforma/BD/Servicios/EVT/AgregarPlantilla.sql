SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.AgregarPlantilla";

CREATE PROCEDURE
-- 
--  Proposito: 
--  Autor: VARIACODE - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::EVT.AgregarPlantilla"
 (
 						            IN p_IdEmpresa          BIGINT,
									IN p_IdUsuario          BIGINT,
	                                IN p_IdModulo           NVARCHAR(3),
	                                
	                                IN  p_Asunto			NVARCHAR(540),
	                                IN  p_Nombre			NVARCHAR(520),
	                                IN  p_Template	 		NVARCHAR(65000),
   									OUT p_salida 			NVARCHAR(520),
 									OUT p_out_AdvertenciasContador int,
									OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
									OUT p_out_ErroresContador int,
									OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	-- Salidas para mensajes de advertencias y errores									
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 


	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;


		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		p_out_AdvertenciasDetalle	= select  '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
		p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN
		DECLARE CONTADOR BIGINT;
		SELECT COUNT(1) INTO CONTADOR FROM "BSCL.Plataforma.BD::EVT.Plantilla" WHERE UPPER("Nombre") = UPPER(p_Nombre);
	
 		
 	
		IF :CONTADOR = 0 THEN
	  		INSERT INTO "BSCL.Plataforma.BD::EVT.Plantilla"
	 		("IdPlantilla",
	 		"IdEmpresa",
	 		"IdUsuario",
	 		"FechaCreacion",
	 		"Template",
	 		"Asunto",
	 		"Nombre")
	 		
	 		 VALUES 
	 		 ("BSCL.Plataforma.BD.Secuencias::EVT.Plantilla".nextval,
	 		  p_IdEmpresa,
	 		  p_IdUsuario, 
	 		  CURRENT_UTCTIMESTAMP,
	 		  to_char(p_Template),
	 		  p_Asunto,
	 		  p_Nombre);
	 		  
	 		 p_salida := 'Inserción Correcta';
	 	ELSE
	 		 p_out_ErroresContador := -1;
	 		 p_salida := 'Inserción Incorrecta - Nombre Duplicado';
	 		
	 	END IF;
	END;

END;
