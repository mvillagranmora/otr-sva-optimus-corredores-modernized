SET SCHEMA "BSCL";

DROP VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarTiemposAsignacion";

CREATE VIEW "BSCL.Estadisticas.BD.Vistas::ConsultarTiemposAsignacion"
AS 
	SELECT TO_DATE(UTCTOLOCAL (TO_TIMESTAMP(INICIO."FechaRegistro", 'YYYY-MM-DD HH24:MI:SS.FF7'), 'America/Santiago','platform')) AS "Fecha",
NANO100_BETWEEN(INICIO."FechaRegistro",FIN."FechaRegistro")/10000 AS "Tiempo Total",
FIN."Adicional1" as "Cantidad", CAST( (NANO100_BETWEEN(INICIO."FechaRegistro",FIN."FechaRegistro")/10000) / CAST(FIN."Adicional1" AS DECIMAL) AS DECIMAL(26)) AS "Promedio/Asignacion (ms)"
FROM "BSCL.Plataforma.BD::PLA.AuditoriaServicios" INICIO
LEFT JOIN "BSCL.Plataforma.BD::PLA.AuditoriaServicios" fin on inicio."IdTransaccion" = fin."IdTransaccion" AND fin."CodigoEvento" != 'INICIO'
where INICIO."CodigoEvento" = 'INICIO' and fin."IdTransaccion" is not null-- and INICIO."FechaRegistro" >= '2021-09-01 00:00:00' and FIN."FechaRegistro" <= '2021-09-07 00:00:00'
and INICIO."IdServicio" IN (1722,20024,20025) and FIN."Adicional1" != '0';


GRANT SELECT ON "BSCL.Estadisticas.BD.Vistas::ConsultarTiemposAsignacion" TO BCS_DESARROLLADOR WITH GRANT OPTION;
