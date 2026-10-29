SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.ActualizarAgenda";

CREATE PROCEDURE
-- 
--  Proposito: Actualiza estado de Agenda 
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::BAT.ActualizarAgenda" (

	IN p_IdEmpresa          BIGINT,
	IN p_IdUsuario          BIGINT,
	IN p_IdModulo           NVARCHAR(3),
	
	IN p_IdAgenda 			BIGINT,
	IN p_Accion 			NVARCHAR(1), --H (HABILITAR), D (DESHABILITAR)
	
	
-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ( "Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
	
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
		p_out_ErroresDetalle		= select  '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		
	BEGIN	
		update "BSCL.Plataforma.BD::BAT.Agenda" set "habilitado" = 
		CASE
			WHEN  p_Accion ='H' THEN 'S'
			WHEN  p_Accion ='D' THEN 'N'
		END
		where "IdAgenda" = p_IdAgenda;
		
		
		
		delete from "BSCL.Plataforma.BD::BAT.Programa" where "IdAgenda" =  p_IdAgenda and "idEstado" = 'PEN';
	END;

END;
