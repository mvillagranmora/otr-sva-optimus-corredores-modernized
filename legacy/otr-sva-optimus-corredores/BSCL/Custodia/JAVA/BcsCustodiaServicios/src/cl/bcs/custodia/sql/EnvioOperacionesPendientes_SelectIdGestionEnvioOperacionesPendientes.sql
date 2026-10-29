SELECT COALESCE(MAX("IdGestion"), 1000) +1 AS "idGestion"
FROM "BSCL.Custodia.BD::CUS.EnvioOperacionesPendientes" bcbceop 
WHERE "FechaEjecucion"=:FechaEjecucion 