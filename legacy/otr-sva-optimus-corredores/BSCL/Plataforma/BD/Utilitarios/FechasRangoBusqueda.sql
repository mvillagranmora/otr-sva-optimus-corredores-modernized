SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::FechasRangoBusqueda";

CREATE PROCEDURE "BSCL.Plataforma.BD.Utilitarios::FechasRangoBusqueda" (
	IN p_FechaIngresoDesde   NVARCHAR(10),
	IN p_FechaIngresoHasta   NVARCHAR(10),	
	
	--ENUMERAR VARIABLES DE SALIDA	
	OUT p_out_FechaDesde    DATETIME,
	OUT p_out_FechaHasta    DATETIME
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN

    DECLARE v_FechaDesde DATE;
	DECLARE v_FechaHasta DATE;
	
	
	SELECT TO_DATE (p_FechaIngresoDesde) INTO v_FechaDesde FROM DUMMY;
	SELECT TO_DATE (p_FechaIngresoHasta) INTO v_FechaHasta FROM DUMMY;

	SELECT ADD_DAYS (v_FechaHasta, 1) INTO v_FechaHasta FROM DUMMY;

    --RESTAMOS UN SEGUNDO A LA FECHA DESDE, PARA QUE QUEDE 23:59:59
	SELECT ADD_SECONDS (TO_TIMESTAMP (v_FechaDesde),-1) INTO p_out_FechaDesde FROM DUMMY;

    p_out_FechaHasta := v_FechaHasta;     
END;
