SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.ActualizarEstadoPrograma";

CREATE PROCEDURE
-- 
--  Proposito: Actualiza tareas programadas 
--  Autor: Exequiel Espinoza  
--  Fecha Creacion: 28/12/2015
--   
"BSCL.Plataforma.BD.Servicios::BAT.ActualizarEstadoPrograma" (
	-- Parametros entrada del servicio publico
	IN p_IdEmpresa                      BIGINT,
	IN p_IdUsuario                      BIGINT,
	IN p_IdModulo                       NVARCHAR(3),
	
	IN p_IdPrograma 					BIGINT,
	IN p_Result							BIGINT,
	IN p_Description					NVARCHAR(200),
	
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("IdPrograma" BIGINT, "Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("IdPrograma" BIGINT,"Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
	 
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
	p_out_AdvertenciasDetalle	= select 0 as "IdPrograma", '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select 0 as "IdPrograma",'' as "Codigo",'' as "Descripcion" from dummy where 1=2;
 



BEGIN

		declare fechaSistemaUTC  NVARCHAR(40);
		declare fechaSistemaLOCAL NVARCHAR(40); 
		declare fechaHoraSistemaUTC NVARCHAR(40); 
		declare fechaHoraSistemaLOCAL NVARCHAR(40);
		declare fechaHoraUTC NVARCHAR(40);
		declare fechaHoraLocal NVARCHAR(40);
		declare v_IdBolsa NVARCHAR(3);
		declare v_IdOperador BIGINT;
		declare v_Canal NVARCHAR(3);
		
		

		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(1,fechaSistemaUTC,fechaSistemaLOCAL,fechaHoraSistemaUTC,fechaHoraSistemaLOCAL,fechaHoraUTC,fechaHoraLocal);
		
		
		IF :p_Result = 0 THEN
		update "BSCL.Plataforma.BD::BAT.Programa" set "idEstado" = 'ASI',"fechaFin" = to_seconddate('0001-01-01 00:00:00'), "Resultado" = 'En Ejecución' 
		where "IdPrograma" = p_IdPrograma;
		END IF;
		IF :p_Result = -1 THEN 
		update "BSCL.Plataforma.BD::BAT.Programa" set "idEstado" = 'ERR', "fechaFin" = fechaHoraUTC ,"Resultado" = p_Description
		where "IdPrograma" = p_IdPrograma;
		END IF;
		IF :p_Result = 1 THEN 
		update "BSCL.Plataforma.BD::BAT.Programa" set "idEstado" = 'TER', "fechaFin" = fechaHoraUTC , "Resultado" = p_Description
		where "IdPrograma" = p_IdPrograma;
		END IF;
		IF :p_Result = 2 THEN
		 update "BSCL.Plataforma.BD::BAT.Programa" set  "fechaEjecucion" = fechaHoraUTC, "Intento"  = 0, "idEstado" = 'PEN' 
		 where "IdPrograma" = p_IdPrograma;
		END IF;
		
END;
END;
