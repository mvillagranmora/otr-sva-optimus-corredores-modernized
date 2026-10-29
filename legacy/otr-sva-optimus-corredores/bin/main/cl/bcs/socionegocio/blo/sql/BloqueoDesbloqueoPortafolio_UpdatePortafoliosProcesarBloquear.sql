UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdEstado" = :IdEstado
,"IdTipoBloqueo" = :IdTipoBloqueo
,"ComentarioBloqueo" = :ComentarioBloqueo
,"FechaBloqueo" = CURRENT_UTCTIMESTAMP
,"FechaModificacion" = CURRENT_UTCTIMESTAMP
,"IdTipoDesbloqueo" = ''
,"FechaDesbloqueoTemporal" = '0001-01-01 00:00:00'
,"FechaBloqueoTemporal" = '0001-01-01 00:00:00'
,"FechaDesbloqueo" = '0001-01-01 00:00:00'
,"ComentarioDesbloqueo" = ''
WHERE "IdPortafolio" = :IdPortafolio
AND "IdEstado" = 'ACT'
AND "IdTipoDesbloqueo" = 'DESBTEMPORAL';