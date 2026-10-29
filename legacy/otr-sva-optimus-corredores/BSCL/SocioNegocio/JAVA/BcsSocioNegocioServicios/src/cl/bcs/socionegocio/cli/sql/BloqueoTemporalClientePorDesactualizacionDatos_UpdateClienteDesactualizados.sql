UPDATE "BSCL.SocioNegocio.BD::CLI.Cliente"
SET 
"IdEstado" = 'BLO'
,"IdTipoBloqueo" = 'BINFNCG380'
,"FechaBloqueo" = CURRENT_UTCTIMESTAMP
,"FechaModificacion" = CURRENT_UTCTIMESTAMP
,"IdTipoDesbloqueo" = ''
,"FechaDesbloqueo" = '0001-01-01'
WHERE "IdCliente" = :IdCliente