SET SCHEMA "BSCL";

DROP VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarTiemposPauta";

CREATE VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarTiemposPauta"
AS 
	SELECT *,seconds_between(TO_SECONDdATE(TO_NVARCHAR("FechaEntrega",'YYYY-MM-DD') || ' 00:00:00'),"FechaEntrega") AS SegundosDesde,
	TO_TIME("FechaEntrega") as "HoraEntrega"
	FROM (
		SELECT "FechaSistema",UTCTOLOCAL (TO_TIMESTAMP("FechaInicio", 'YYYY-MM-DD HH24:MI:SS'), 'America/Santiago','platform') AS "FechaEntrega",
		floor(SECONDS_BETWEEN("FechaInicio","FechaFin")/60) AS "MinutosPauta"
		FROM "BSCL.Plataforma.BD::PAU.Ejecucion"
		WHERE "FechaSistema" >= '2021-08-01'
	) where "MinutosPauta" > 0;

GRANT SELECT ON "BSCL.Estadisticas.BD.Vistas::ConsultarTiemposPauta" TO BCS_DESARROLLADOR WITH GRANT OPTION;
