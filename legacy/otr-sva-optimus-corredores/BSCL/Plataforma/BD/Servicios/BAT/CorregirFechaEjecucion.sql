SET SCHEMA BSCL;
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::BAT.CorregirFechaEjecucion";

CREATE PROCEDURE
-- 
--  Proposito: Corrige fecha de ejecucion en caso de cambio de hora
--  Autor: - BCS 
--  Fecha Creacion: 20/05/2016
--   
"BSCL.Plataforma.BD.Servicios::BAT.CorregirFechaEjecucion" (
	-- Parametros entrada del servicio publico
	IN p_IdEmpresa                      BIGINT,
	IN p_IdUsuario                      BIGINT,
 	IN p_IdModulo                       NVARCHAR(3),
 	
 	
	OUT SALIDA							NVARCHAR(520),
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
	 		DECLARE v_gtmActual BIGINT;
	 		DECLARE v_gtmFuturo BIGINT;
	 		DECLARE v_Segundos  BIGINT;
	 		
 	 		SELECT TO_INTEGER("Valor")/3600 INTO v_gtmFuturo  FROM "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.SegundosZonaHoraria' AND "IdEmpresa" = p_IdEmpresa;
	 		
	 		select TOP 1 "ZonaHoraria" INTO v_gtmActual from "BSCL.Plataforma.BD::BAT.Programa";

	 		IF:v_gtmFuturo > 0 THEN
				v_Segundos :=  ((v_gtmActual*-1)+v_gtmFuturo)*3600;
	 			IF:v_gtmActual > v_gtmFuturo THEN
	 				v_Segundos :=  0;
	 			END IF;
	 		ELSE 
	 			
	 			--Si el GMT futuro es mayor que el actual, se resta a la hora utc
				-- EJ. GMT actual = -3, GMT futuro = -4; Futuro es menor, se debe sumar uno

	 			--Si el GMT futuro es menor que el actual, se suma a la hora utc
				-- EJ. GMT actual = -3, GMT futuro = -4; Futuro es menor, se debe sumar uno
	 			v_Segundos :=  ((v_gtmFuturo*-1)+v_gtmActual)*3600;
	 			IF:v_gtmActual = v_gtmFuturo THEN
	 				v_Segundos := 0;
	 			END IF;
	 		
	 		END IF;
	 		
 			--select * from M_HOST_INFORMATION where upper(KEY) like '%TIMEZONE%' where
	 		--select * from "BSCL.Plataforma.BD::BAT.Programa" where "IdPrograma" = 94360; 
	 		--UPDATE "BSCL.Plataforma.BD::BAT.Programa" SET "fechaEjecucion" = '2016-05-23 12:00:00', "fechaCreacion" = '2016-05-20 12:01:00' where "IdPrograma" = 94360; 
			UPDATE "BSCL.Plataforma.BD::BAT.Programa" SET "ZonaHoraria" = v_gtmFuturo, "fechaEjecucion" = add_seconds("fechaEjecucion",v_Segundos);
			SALIDA := 'OK';
		END;
END;