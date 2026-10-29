SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema";

CREATE PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (
-- 
--  Proposito: Obtener la fecha de sistema con hora (UTC)
--  Autor: NNN - BCS
--  Fecha Creacion: 18/08/2015
--  07-04-2016 CSI  Corrige Zona Horaria LOCAL TO UTC
--  
	IN  p_IdEmpresa				BIGINT,
	OUT p_out_FechaHoraSistema  NVARCHAR(40) --SECONDDATE
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN 

	DECLARE v_FechaSistema 			NVARCHAR(10);
	DECLARE v_FechaSistemaUTC 		SECONDDATE;
	DECLARE v_FechaHoraSistemaUTC 	SECONDDATE;
	DECLARE v_FechaHoraUTC 			SECONDDATE;
	DECLARE v_Cantidad 				INTEGER;
	DECLARE v_SegundosZona  		INTEGER;	
	DECLARE FECHA_DE_SISTEMA_NO_CONFIGURADA CONDITION FOR SQL_ERROR_CODE 10003;
	
	v_Cantidad := 0;
	
	SELECT COUNT("CodigoParametro") INTO v_Cantidad
	  FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	 WHERE "IdEmpresa"=p_IdEmpresa AND  "CodigoParametro" = 'PLA.FechaSistema';
	 
	 IF (v_Cantidad < 1) THEN
	 	SIGNAL FECHA_DE_SISTEMA_NO_CONFIGURADA;
	 ELSE
	 	 SELECT "Valor" INTO v_FechaSistema
		 FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		 WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro" = 'PLA.FechaSistema';
	 END IF;
	 
	 
	SELECT TO_INTEGER("Valor") INTO v_SegundosZona
	  FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE "IdEmpresa" =  p_IdEmpresa and  "CodigoParametro" = 'PLA.SegundosZonaHoraria';

	v_FechaHoraUTC 			:= CURRENT_UTCTIMESTAMP;			
	v_FechaSistemaUTC 		:= ADD_SECONDS(TO_DATE(v_FechaSistema,'YYYY-MM-DD'),-1*v_SegundosZona);	
	v_FechaHoraSistemaUTC	:= ADD_SECONDS(TO_DATE(v_FechaSistema,'YYYY-MM-DD'), (HOUR(v_FechaHoraUTC)*3600) + (MINUTE(v_FechaHoraUTC)*60) + SECOND(v_FechaHoraUTC));	 
	
	--FECHA SISTEMA	( SI F.SISTEMA EXCEDE a BD SE REPITE EL DIA )
	p_out_FechaHoraSistema  := v_FechaSistema || ' ' || TO_VARCHAR (v_FechaHoraSistemaUTC,'HH24:MI:SS');
END;	
