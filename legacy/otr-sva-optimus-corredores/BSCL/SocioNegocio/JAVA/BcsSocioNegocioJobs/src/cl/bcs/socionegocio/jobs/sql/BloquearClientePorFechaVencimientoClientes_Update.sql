update "BSCL.SocioNegocio.BD::CLI.Cliente" 
set "IdEstado"='BLO',
"IdTipoBloqueo" = :IdTipoBloqueo, 
"ComentarioBloqueo" = :ComentarioBloqueo, 
"FechaBloqueo"=CURRENT_UTCTIMESTAMP,
"FechaModificacion"=CURRENT_UTCTIMESTAMP  
where "IdCliente" IN (:IdCliente)