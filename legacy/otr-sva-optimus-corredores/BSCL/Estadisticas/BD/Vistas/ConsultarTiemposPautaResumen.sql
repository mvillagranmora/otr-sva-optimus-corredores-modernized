SET SCHEMA "BSCL";

DROP VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarTiemposPautaResumen";

CREATE VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarTiemposPautaResumen"
AS 
	SELECT year("FechaSistema") AS agno, month("FechaSistema") AS MES,MIN("HoraEntrega") AS MINIMO,MAX("HoraEntrega") AS MAXIMO,
	to_time(add_seconds(to_Date('2021-01-01 00:00:00'),avg(SegundosDesde))) as PROMEDIo
	FROM "BSCL.Estadisticas.BD.Vistas::ConsultarTiemposPauta"
	group by year("FechaSistema"), month("FechaSistema");


GRANT SELECT ON "BSCL.Estadisticas.BD.Vistas::ConsultarTiemposPautaResumen" TO BCS_DESARROLLADOR WITH GRANT OPTION;