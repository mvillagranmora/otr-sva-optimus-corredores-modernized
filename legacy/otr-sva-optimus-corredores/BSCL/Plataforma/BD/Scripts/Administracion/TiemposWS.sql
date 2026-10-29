SELECT top 1000 SECONDS_BETWEEN("FechaInicio","FechaFin"),*
FROM "BSCL"."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE SECONDS_BETWEEN("FechaInicio","FechaFin") > 100
ORDER BY "FechaInicio" DESC


SELECT top 100 SECONDS_BETWEEN("FechaInicio","FechaFin"),*
FROM "BSCL"."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
WHERE SECONDS_BETWEEN("FechaInicio","FechaFin") > 10
ORDER BY "Key" desc
