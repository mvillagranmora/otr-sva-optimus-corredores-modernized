SELECT "FechaSistema",UTCTOLOCAL (TO_TIMESTAMP("FechaInicio", 'YYYY-MM-DD HH24:MI:SS'), 'America/Santiago','platform') AS "FechaEntrega",floor(SECONDS_BETWEEN("FechaInicio","FechaFin")/60) AS "MinutosPauta"
FROM BSCL."BSCL.Plataforma.BD::PAU.Ejecucion"
WHERE "FechaSistema" >= '2021-08-01'
ORDER BY "FechaSistema" ASC


SELECT * FROM SYS.TIMEZONES WHERE TIMEZONE_NAME LIKE '%Santiago%'

SELECT *,UTCTOLOCAL (TO_TIMESTAMP("FechaInicio", 'YYYY-MM-DD HH24:MI:SS'), 'CHILE') "utctolocal" FROM BSCL."BSCL.Plataforma.BD::PAU.Ejecucion";