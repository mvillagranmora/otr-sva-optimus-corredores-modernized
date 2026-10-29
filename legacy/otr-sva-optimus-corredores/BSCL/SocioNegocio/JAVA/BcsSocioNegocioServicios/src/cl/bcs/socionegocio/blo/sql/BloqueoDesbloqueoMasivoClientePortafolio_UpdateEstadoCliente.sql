UPDATE "BSCL.SocioNegocio.BD::CLI.Cliente" 
SET 
	"IdEstado" = :IdEstado, 
	"IdTipoBloqueo" = CASE WHEN :IdEstado = 'BLO' THEN :IdTipoBloqueo ELSE "IdTipoBloqueo" END,
	"IdTipoDesbloqueo" = CASE WHEN :IdEstado = 'ACT' THEN :IdTipoDesbloqueo ELSE "IdTipoDesbloqueo" END,
	"ComentarioBloqueo" = CASE WHEN :IdEstado = 'BLO' THEN :ComentarioBloqueo ELSE "ComentarioBloqueo" END,
	"ComentarioDesbloqueo" = CASE WHEN :IdEstado = 'ACT' THEN :ComentarioDesbloqueo ELSE "ComentarioDesbloqueo" END,
	"FechaBloqueo" = CASE WHEN :IdEstado = 'BLO' THEN :FechaBloqueo ELSE "FechaBloqueo" END , 
	"FechaDesbloqueo" = CASE WHEN :IdEstado = 'ACT' THEN :FechaDesbloqueo ELSE "FechaDesbloqueo" END,
	"FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdCliente" = :IdCliente

