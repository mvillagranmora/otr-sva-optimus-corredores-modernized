UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdEstado" = :IdEstado
,"IdTipoDesbloqueo" = :IdTipoDesbloqueo
,"ComentarioDesbloqueo" = :ComentarioDesbloqueo
,"FechaDesbloqueo" = CURRENT_UTCTIMESTAMP
,"FechaModificacion" = CURRENT_UTCTIMESTAMP
,"IdTipoBloqueo" = ''
,"FechaDesbloqueoTemporal" = '0001-01-01 00:00:00'
,"FechaBloqueoTemporal" = '0001-01-01 00:00:00'
,"FechaBloqueo" = '0001-01-01 00:00:00'
,"ComentarioBloqueo" = ''
WHERE "IdPortafolio" = :IdPortafolio
AND "IdEstado" = 'BLO'
AND "IdTipoBloqueo" = 'BTEMPO';