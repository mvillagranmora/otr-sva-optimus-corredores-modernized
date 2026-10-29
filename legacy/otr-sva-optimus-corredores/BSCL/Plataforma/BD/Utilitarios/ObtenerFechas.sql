SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas";

CREATE PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas" (
-- 
--  Proposito: Obtener las diferentes fechas desde la base datos
--  Autor:  CST - BCS
--  Fecha Creacion: 16/09/2015
--  
	IN  p_IdEmpresa				BIGINT,
	--
	OUT p_out_FechaSistemaUTC   	NVARCHAR(40), 
	OUT p_out_FechaSistemaLOCAL   	NVARCHAR(40), 
	OUT p_out_FechaHoraSistemaUTC   NVARCHAR(40), 
	OUT p_out_FechaHoraSistemaLOCAL NVARCHAR(40), 
	OUT p_out_FechaHoraUTC   		NVARCHAR(40), 
	OUT p_out_FechaHoraLOCAL   		NVARCHAR(40)	 
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN 

	DECLARE v_FechaSistema 			NVARCHAR(10);
	DECLARE v_FechaSistemaUTC 		SECONDDATE;
	DECLARE v_FechaHoraSistemaUTC 	SECONDDATE;
	DECLARE v_FechaHoraSistemaLOC 	SECONDDATE;
	DECLARE v_FechaHoraUTC 			SECONDDATE;
	DECLARE v_fechatemp	 			SECONDDATE;
	DECLARE v_Cantidad 				INTEGER;
	DECLARE v_SegundosZona  		INTEGER;	
	DECLARE FECHA_DE_SISTEMA_NO_CONFIGURADA CONDITION FOR SQL_ERROR_CODE 10003;
	
	v_Cantidad := 0;
	
	SELECT COUNT("CodigoParametro") INTO v_Cantidad
	  FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE "IdEmpresa" =  p_IdEmpresa and  "CodigoParametro" = 'PLA.FechaSistema' ;
	 
	IF (v_Cantidad < 1) THEN
	 	SIGNAL FECHA_DE_SISTEMA_NO_CONFIGURADA;
	END IF;
	 
 	 SELECT "Valor" INTO v_FechaSistema
	  FROM "BSCL.Plataforma.BD::PLA.Configuracion"
     WHERE "IdEmpresa" =  p_IdEmpresa and  "CodigoParametro" = 'PLA.FechaSistema' ;
     
	SELECT TO_INTEGER("Valor") INTO v_SegundosZona
	  FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE "IdEmpresa" =  p_IdEmpresa and  "CodigoParametro" = 'PLA.SegundosZonaHoraria';
		         
    v_FechaHoraUTC 				:= CURRENT_UTCTIMESTAMP;
	--SISTEMA FECHA
	v_FechaSistemaUTC 			:= ADD_SECONDS(TO_DATE(v_FechaSistema,'YYYY-MM-DD'),-1*v_SegundosZona);	
	p_out_FechaSistemaUTC   	:= TO_VARCHAR (v_FechaSistemaUTC,'YYYY-MM-DD HH24:MI:SS');	
	p_out_FechaSistemaLOCAL 	:= TO_VARCHAR (ADD_SECONDS(v_FechaSistemaUTC,v_SegundosZona),'YYYY-MM-DD  HH24:MI:SS');	 
	--SISTEMA FECHA + HORA  ( Si Sistema excede dia de BD, se repite el dia)
	v_FechaHoraSistemaUTC		:= ADD_SECONDS(TO_DATE(v_FechaSistema,'YYYY-MM-DD'), (HOUR(v_FechaHoraUTC)*3600) + (MINUTE(v_FechaHoraUTC)*60) + SECOND(v_FechaHoraUTC));    
	p_out_FechaHoraSistemaUTC   := v_FechaSistema || ' ' || TO_VARCHAR (v_FechaHoraSistemaUTC,'HH24:MI:SS'); 
	v_FechaHoraSistemaLOC		:= ADD_SECONDS(v_FechaHoraSistemaUTC,v_SegundosZona);	
	p_out_FechaHoraSistemaLOCAL := v_FechaSistema || ' ' || TO_VARCHAR (v_FechaHoraSistemaLOC,'HH24:MI:SS');
	--BASE DATOS FECHA + HORA	 
	p_out_FechaHoraUTC         := TO_VARCHAR (v_FechaHoraUTC, 'YYYY-MM-DD HH24:MI:SS');
	p_out_FechaHoraLOCAL        := TO_VARCHAR (ADD_SECONDS(v_FechaHoraUTC,v_SegundosZona), 'YYYY-MM-DD HH24:MI:SS');	        
END;
