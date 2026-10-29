SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaAnteriorMasivo";

CREATE PROCEDURE
-- 
--  Proposito: Obtener el anterior dia de corrido o habil, segun fecha enviada.
--  Autor: Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 17/06/2016
--   
"BSCL.Plataforma.BD.Utilitarios::ObtenerDiaAnteriorMasivo" (
	IN p_IdPais		BIGINT,
	IN p_DiaHabil	NVARCHAR(1),
	IN p_FechasIN	TABLE (
						"FechaInicial" 	NVARCHAR(10),
						"NumeroDias"	BIGINT
						),
	OUT p_FechasOUTf	TABLE(
						"FechaInicial"		NVARCHAR(10),
						"NumeroDias"	BIGINT,
						"FechaCalculada"	NVARCHAR(10)
						)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN
	DECLARE v_min_date DATE;
	DECLARE v_max_date DATE;
	DECLARE v_calendario_inhabiles TABLE ("Fecha" DATE);
	
	v_calendario_inhabiles = SELECT '0001-01-01' AS "Fecha" FROM DUMMY WHERE 1=2;
	
	v_FechasOUT =
		SELECT 
			"FechaInicial","NumeroDias", ADD_DAYS (TO_DATE ("FechaInicial", 'YYYY-MM-DD'), "NumeroDias" * (-1)) as "FechaRestada",
			ROW_NUMBER() over (ORDER BY "FechaInicial","NumeroDias" asc) AS "IdRegistro"
		FROM :p_FechasIN;
	
	SELECT ADD_DAYS (TO_DATE ( MIN("FechaRestada") ,'YYYY-MM-DD') , -30), ADD_DAYS (TO_DATE ( MAX("FechaInicial") ,'YYYY-MM-DD'), 1) into v_min_date, v_max_date
	FROM :v_FechasOUT;
	
	--select 'v_FechasOUT',:v_min_date,:v_max_date,* from :v_FechasOUT;--BORRAR
	
	v_calendario =
		SELECT GENERATED_PERIOD_START as "Fecha" 
		FROM SERIES_GENERATE_DATE('INTERVAL 1 DAY',:v_min_date,:v_max_date) ;
	
	--select 'v_calendario',* from :v_calendario;--BORRAR
	
	IF (:p_DiaHabil = 'S') THEN
		v_calendario_inhabiles =
			SELECT cal.*
			FROM :v_calendario cal, "BSCL.Plataforma.BD::REG.DiaFeriado" fer
			WHERE ((WEEKDAY (cal."Fecha") = fer."Dia" and fer."Tipo" = 'INHABIL') OR ( fer."Tipo" ='FERIADO' and cal."Fecha" = fer."Fecha" )) and fer."IdPais" = :p_IdPais and year(cal."Fecha") = fer."AgnoCarga";
	END IF;
	
	--select 'v_calendario_inhabiles',* from :v_calendario_inhabiles;--BORRAR

	v_calendario_habiles =
		SELECT * FROM :v_calendario
		EXCEPT 
		SELECT * FROM :v_calendario_inhabiles;
	
	--select 'v_calendario_habiles',* from :v_calendario_habiles;--BORRAR
	
	v_DiasSolicitadosConDiasHabiles =
		SELECT
	  		s."FechaInicial",h."Fecha",s."IdRegistro", (ROW_NUMBER() over (PARTITION BY s."IdRegistro" ORDER BY h."Fecha" DESC)-1) AS "NumeroDias"
		FROM :v_FechasOUT AS s,:v_calendario_habiles h
		WHERE h."Fecha" <= s."FechaInicial";
	
	--SELECT 'v_DiasSolicitadosConDiasHabiles',* FROM :v_DiasSolicitadosConDiasHabiles; --BORRAR

	p_FechasOUTf =
		SELECT 
 			s."FechaInicial",s."NumeroDias", d."Fecha" AS "FechaCalculada"
		FROM :v_FechasOUT AS s
		INNER JOIN :v_DiasSolicitadosConDiasHabiles AS d ON (d."IdRegistro" = s."IdRegistro" 
		                                                 AND d."NumeroDias" = s."NumeroDias");
END;
