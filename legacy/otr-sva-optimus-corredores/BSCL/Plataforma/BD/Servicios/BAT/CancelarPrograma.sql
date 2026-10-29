SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.CancelarPrograma";

CREATE PROCEDURE
-- 
--  Proposito: Cancela programa sólo si se encuentra pendiente
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::BAT.CancelarPrograma" (
	-- Parametros entrada del servicio publico
	IN p_IdEmpresa                      BIGINT,
	IN p_IdUsuario                      BIGINT,
 	IN p_IdModulo                       NVARCHAR(3),
 	
	IN p_IdPrograma						BIGINT,
	
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table (  "Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
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
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
 

	BEGIN	

		delete from "BSCL.Plataforma.BD::BAT.ParametrosPrograma" where "IdPrograma" = p_IdPrograma;
		Delete from "BSCL.Plataforma.BD::BAT.Programa" where "IdPrograma" = p_IdPrograma and "idEstado" = 'PEN';
		
	END; 
		
		

END;
