SELECT 
"IdPortafolio"
,"CodigoPortafolio"
,"IdCliente"
,"FechaBloqueo"
,"FechaBloqueoTemporal" 
FROM "BSCL.Portafolio.BD::POR.Portafolio"
WHERE "IdEstado" = 'BLO'
AND "IdTipoBloqueo" = 'BTEMPO'
AND "FechaBloqueoTemporal" IS NOT NULL
AND "FechaBloqueoTemporal" > TO_SECONDDATE('1900-01-01 00:00:00')
AND "FechaBloqueoTemporal" <> '0001-01-01 00:00:00'
AND "FechaBloqueoTemporal" <= :FechaSistema;