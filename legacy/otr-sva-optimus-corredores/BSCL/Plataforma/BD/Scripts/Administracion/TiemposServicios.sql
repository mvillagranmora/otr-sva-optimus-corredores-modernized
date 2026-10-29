
SELECT "IdServicio","RutaServicio",count(1) as k FROM bscl."BSCL.Plataforma.BD::PLA.AuditoriaServicios" 
group by "IdServicio","RutaServicio" order by k


SELECT "IdUsuario",count(1) as k FROM bscl."BSCL.Plataforma.BD::PLA.AuditoriaServicios" 
group by "IdUsuario" order by k

SELECT * FROM bscl."BSCL.Plataforma.BD::PLA.AuditoriaServicios" 
where "FechaRegistro" >= '2021-10-13 10:30:00'
order by "FechaRegistro" asc

SELECT * FROM bscl."BSCL.Plataforma.BD::PLA.AuditoriaServicios" 
where "RutaServicio" like '%n.CrearComprobante%'

PLUGIN_ORD.jar@cl.bcs.ordenes.EjecutarAsignacionPropCrono 1722
PLUGIN_FAC.jar@cl.bcs.facturacion.CrearComprobante 13

SELECT TO_DATE(UTCTOLOCAL (TO_TIMESTAMP(INICIO."FechaRegistro", 'YYYY-MM-DD HH24:MI:SS.FF7'), 'America/Santiago','platform')) AS "Fecha",
NANO100_BETWEEN(INICIO."FechaRegistro",FIN."FechaRegistro")/10000 AS "Tiempo Total",
FIN."Adicional1" as "Cantidad", CAST( (NANO100_BETWEEN(INICIO."FechaRegistro",FIN."FechaRegistro")/10000) / FIN."Adicional1" AS DECIMAL(26)) AS "Promedio/Asignacion (ms)"
FROM bscl."BSCL.Plataforma.BD::PLA.AuditoriaServicios" INICIO
LEFT JOIN bscl."BSCL.Plataforma.BD::PLA.AuditoriaServicios" fin on inicio."IdTransaccion" = fin."IdTransaccion" AND fin."CodigoEvento" != 'INICIO'
where INICIO."CodigoEvento" = 'INICIO' and fin."IdTransaccion" is not null and INICIO."FechaRegistro" >= '2023-10-31 00:00:00' and FIN."FechaRegistro" <= '2023-10-31 17:50:00'
and INICIO."IdServicio" IN (13) and FIN."Adicional1" != '0'
order by INICIO."FechaRegistro" desc


SELECT NANO100_BETWEEN(INICIO."FechaRegistro",FIN."FechaRegistro")/10000 AS "Tiempo Total",*  FROM bscl."BSCL.Plataforma.BD::PLA.AuditoriaServicios" INICIO
LEFT JOIN bscl."BSCL.Plataforma.BD::PLA.AuditoriaServicios" fin on inicio."IdTransaccion" = fin."IdTransaccion" AND fin."CodigoEvento" != 'INICIO'
where INICIO."FechaRegistro" >= '2024-11-11 14:25:00' and INICIO."FechaRegistro" <= '2024-11-11 15:35:00' and INICIO."CodigoEvento" NOT IN('FIN','FIN_ERROR','ERROR')
--and fin."IdTransaccion" is null
and NANO100_BETWEEN(INICIO."FechaRegistro",FIN."FechaRegistro")/10000 > 100
order by INICIO."FechaRegistro" asc

--servicios interrumpidos
SELECT NANO100_BETWEEN(INICIO."FechaRegistro",FIN."FechaRegistro")/10000 AS "Tiempo Total",*  FROM bscl."BSCL.Plataforma.BD::PLA.AuditoriaServicios" INICIO
LEFT JOIN bscl."BSCL.Plataforma.BD::PLA.AuditoriaServicios" fin on inicio."IdTransaccion" = fin."IdTransaccion" AND fin."CodigoEvento" != 'INICIO'
where INICIO."FechaRegistro" >= '2024-11-11 11:00:00' and INICIO."FechaRegistro" <= '2024-11-11 15:45:00' and INICIO."CodigoEvento" NOT  IN('FIN','FIN_ERROR','ERROR')
and fin."IdTransaccion" is null
--and NANO100_BETWEEN(INICIO."FechaRegistro",FIN."FechaRegistro")/10000 > 100
order by INICIO."FechaRegistro" asc

select * from "BSCL.Plataforma.BD::PLA.AuditoriaServicios" bpbpas where "FechaRegistro"  >='2023-10-31 17:46:24.205'

select * from "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" IN (22,601123 )

SELECT "FechaInicio","FechaSistema",UTCTOLOCAL (TO_TIMESTAMP("FechaInicio", 'YYYY-MM-DD HH24:MI:SS'), 'America/Santiago','platform') AS "FechaEntrega",floor(SECONDS_BETWEEN("FechaInicio","FechaFin")/60) AS "MinutosPauta"
FROM BSCL."BSCL.Plataforma.BD::PAU.Ejecucion"
WHERE "FechaSistema" >= '2021-08-01'
ORDER BY "FechaSistema" ASC



