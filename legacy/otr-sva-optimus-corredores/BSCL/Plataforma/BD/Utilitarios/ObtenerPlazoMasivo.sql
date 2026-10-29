SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerPlazoMasivo";

CREATE PROCEDURE
-- 
--  Proposito: Obtener el plazo (cantidad de dias) entre dos fechas enviadas.
--  Autor: - ACC 
--  Fecha Creacion: 16-05-2016
--   
"BSCL.Plataforma.BD.Utilitarios::ObtenerPlazoMasivo" ( 	
	IN p_tbl table ("IdInterno" BIGINT 
					,"FechaDesde" NVARCHAR(10)
					,"FechaHasta" NVARCHAR(10)
					,"IdPais" BIGINT
					,"SoloDiasHabiles" NVARCHAR(1)),
	
	
	--ENUMERAR VARIABLES DE SALIDA	
	OUT p_out_Plazos table ("IdInterno" BIGINT
					,"FechaDesde" NVARCHAR(10)
					,"FechaHasta" NVARCHAR(10)
					,"IdPais" BIGINT
					,"Plazo" INTEGER
					,"SoloDiasHabiles" NVARCHAR(1))
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN

	DECLARE v_min_date DATE;
	DECLARE v_max_date DATE;
	
	DECLARE v_tblPaso TABLE ("IdInterno" BIGINT
					,"FechaDesde" NVARCHAR(10)
					,"FechaHasta" NVARCHAR(10)
					,"IdPais" BIGINT
					,"Plazo" INTEGER
					,"SoloDiasHabiles" NVARCHAR(1)
					) DEFAULT 
					SELECT 0 as "IdInterno", '' AS "FechaDesde", '' AS "FechaHasta", 0 AS "IdPais", cast(0 as INTEGER) AS "Plazo", '' AS "SoloDiasHabiles" FROM DUMMY WHERE 1=2;
						
	

 	v_tblPaso = 
 		SELECT "IdInterno", "FechaDesde","FechaHasta", "IdPais", 
 			cast(DAYS_BETWEEN (TO_DATE ("FechaDesde", 'YYYY-MM-DD'), TO_DATE("FechaHasta", 'YYYY-MM-DD')) as INTEGER) AS "Plazo","SoloDiasHabiles" 			
 			from :p_tbl;


 		select min(TO_DATE ("FechaDesde", 'YYYY-MM-DD')) into v_min_date from :p_tbl;
 		select max(TO_DATE ("FechaHasta", 'YYYY-MM-DD')) into v_max_date from :p_tbl;
 	
		v_calendario = 	SELECT GENERATED_PERIOD_START as "Fecha" 
				FROM SERIES_GENERATE_DATE('INTERVAL 1 DAY', :v_min_date, :v_max_date) ;
				
		v_calendario_inhabiles = SELECT tbl."FechaDesde", tbl."FechaHasta", cal."Fecha", tbl."IdPais"
				FROM :v_calendario cal, "BSCL.Plataforma.BD::REG.DiaFeriado" fer, :p_tbl tbl
				WHERE 
					( (WEEKDAY (cal."Fecha") = fer."Dia" and fer."Tipo" = 'INHABIL') or ( fer."Tipo" ='FERIADO' and cal."Fecha" = fer."Fecha" )) and 
					cal."Fecha" between tbl."FechaDesde" and tbl."FechaHasta" and
					fer."IdPais" = tbl."IdPais" 
					and year(cal."Fecha") = fer."AgnoCarga";
		
		p_out_Plazos = 
			SELECT a."IdInterno", a."FechaDesde",a."FechaHasta", a."IdPais", cast(a."Plazo" as INTEGER) as "Plazo", a."SoloDiasHabiles" FROM :v_tblPaso a WHERE "SoloDiasHabiles" = 'N'
		
			UNION ALL
		
			SELECT a."IdInterno", a."FechaDesde",a."FechaHasta", a."IdPais", cast(a."Plazo" - 
				(SELECT COUNT(1) FROM :v_calendario_inhabiles b WHERE b."FechaDesde" = a."FechaDesde" AND b."FechaHasta" = a."FechaHasta") as INTEGER) AS "Plazo",
				a."SoloDiasHabiles"
			from :v_tblPaso a WHERE "SoloDiasHabiles" = 'S';
END;
