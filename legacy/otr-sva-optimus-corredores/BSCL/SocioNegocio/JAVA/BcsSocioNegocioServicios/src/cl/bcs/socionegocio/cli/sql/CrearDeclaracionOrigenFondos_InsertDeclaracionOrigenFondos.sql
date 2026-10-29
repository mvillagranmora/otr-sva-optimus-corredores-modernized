INSERT INTO "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos" (
 "IdDeclaracionOrigenFondos"
 ,"IdCliente"
 ,"IdTipoOperacionOrigenFondos"
 ,"FechaDeclaracion"
 ,"Comentario"
 ,"IdUsuarioCreador"
 ,"FechaIngreso"
 ,"FechaCreacion"
 ,"FechaModificacion"
 ,"IdEmpresa"
 ,"IdEstado"
 ,"IdEstadoAutorizacion"
 ,"OperacionInscripcion" )
VALUES (
:IdDeclaracionOrigenFondos
,:IdCliente
,:IdTipoOperacionOrigenFondos
,:FechaDeclaracion
,:Comentario
,:IdUsuarioCreador
,:FechaIngreso
,CURRENT_UTCTIMESTAMP
,CURRENT_UTCTIMESTAMP
,:IdEmpresa
,:IdEstado
,:IdEstadoAutorizacion
,:OperacionInscripcion
);