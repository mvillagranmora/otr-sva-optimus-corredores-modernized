SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguienteMasivo";

CREATE PROCEDURE
-- 
--  Proposito: Obtener el siguiente dia de corrido o habil, segun fecha enviada.
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--   
"BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguienteMasivo" (
	IN p_IdPais		BIGINT,
	IN p_DiaHabil	NVARCHAR(1), --SOLO DIAS HABILES
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
			"FechaInicial","NumeroDias", ADD_DAYS (TO_DATE ("FechaInicial", 'YYYY-MM-DD'), "NumeroDias") as "FechaSumada",
			ROW_NUMBER() over (ORDER BY "FechaInicial") AS "IdRegistro"
		FROM :p_FechasIN;
	
	SELECT ADD_DAYS (TO_DATE ( MIN("FechaInicial") ,'YYYY-MM-DD') , -1), ADD_DAYS (TO_DATE ( MAX("FechaSumada") ,'YYYY-MM-DD') , 30) into v_min_date, v_max_date
	FROM :v_FechasOUT;
	
	--select 'v_FechasOUT',:v_min_date,:v_max_date,* from :v_FechasOUT;--BORRAR
	
	v_calendario =
		SELECT GENERATED_PERIOD_START as "Fecha" 
		FROM SERIES_GENERATE_DATE('INTERVAL 1 DAY', :v_min_date, :v_max_date);
		
	--select 'v_calendario',* from :v_calendario;--BORRAR
	
	IF (:p_DiaHabil = 'S') THEN
		v_calendario_inhabiles =
			SELECT cal.*
			FROM :v_calendario cal, "BSCL.Plataforma.BD::REG.DiaFeriado" fer 
			WHERE  fer."IdPais" = :p_IdPais AND fer."IdEstado" ='ACT' AND ((WEEKDAY (cal."Fecha") = fer."Dia" and fer."Tipo" = 'INHABIL'  AND fer."AgnoCarga"= YEAR(v_min_date)) OR ( fer."Tipo" ='FERIADO' and cal."Fecha" = fer."Fecha"));
	END IF;
	
	v_calendario_habiles =
		SELECT * FROM :v_calendario
		EXCEPT 
		SELECT * FROM :v_calendario_inhabiles;
	
	--SELECT 'v_calendario_habiles', * FROM :v_calendario_habiles; --BORRAR
		
	v_DiasSolicitadosConDiasHabiles =
		SELECT
	  		h."Fecha",s."IdRegistro", (ROW_NUMBER() over (PARTITION BY s."IdRegistro" ORDER BY h."Fecha")-1) AS "NumeroDias"
		FROM :v_FechasOUT AS s,:v_calendario_habiles h
		WHERE h."Fecha" >= s."FechaInicial";
	
	--SELECT 'v_DiasSolicitadosConDiasHabiles',* FROM :v_DiasSolicitadosConDiasHabiles; --BORRAR

	p_FechasOUTf =
		SELECT 
 			s."FechaInicial",s."NumeroDias", d."Fecha" AS "FechaCalculada"
		FROM :v_FechasOUT AS s
		LEFT JOIN :v_DiasSolicitadosConDiasHabiles AS d ON (d."IdRegistro" = s."IdRegistro" 
		                                                 AND d."NumeroDias" = s."NumeroDias");
END;
