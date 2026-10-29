SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::PLA.CambiarZonaHoraria";

CREATE PROCEDURE
-- 
--  Proposito: Cambia de zona horaria el sistema
--  Autor: - BCS 
--  Fecha Creacion: 22/10/2015
--   
"BSCL.Plataforma.BD.Servicios::PLA.CambiarZonaHoraria" (
	-- Parametros entrada del servicio publico
	IN p_IdEmpresa                      BIGINT,
	IN p_IdUsuario                      BIGINT,
 	IN p_IdModulo                       NVARCHAR(3),
 	
	IN p_CambioDeHoraSegundos			BIGINT,
	
	OUT SALIDA							NVARCHAR(520),
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
			DECLARE v_salida NVARCHAR(520);

			UPDATE "BSCL.Plataforma.BD::PLA.Configuracion" 
			SET "Valor" 			= CAST(p_CambioDeHoraSegundos AS NVARCHAR),
				"FechaModificacion" = CURRENT_UTCTIMESTAMP
			WHERE "CodigoParametro" = 'PLA.SegundosZonaHoraria' and "IdEmpresa" = p_IdEmpresa;
			CALL "BSCL.Plataforma.BD.Servicios::BAT.CorregirFechaEjecucion"(p_IdEmpresa,p_IdUsuario,p_IdModulo,v_salida,p_out_AdvertenciasContador,p_out_AdvertenciasDetalle,p_out_ErroresContador,p_out_ErroresDetalle);
 	 		SALIDA := 'GMT Actualizado sin problemas.';
 		END;
END;
