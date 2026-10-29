SELECT 
	"IdPortafolio",
	"CodigoPortafolio",
    "IdCliente",
    "FechaDesbloqueo",
    "FechaDesbloqueoTemporal"
FROM "BSCL.Portafolio.BD::POR.Portafolio"
WHERE "IdEstado" = 'ACT'
AND "IdTipoDesbloqueo" = 'DESBTEMPORAL'
AND "FechaDesbloqueoTemporal" IS NOT NULL
AND "FechaDesbloqueoTemporal" > TO_SECONDDATE('1900-01-01 00:00:00')
AND "FechaDesbloqueoTemporal" <> '0001-01-01 00:00:00'
AND "FechaDesbloqueoTemporal" <= :FechaSistema;