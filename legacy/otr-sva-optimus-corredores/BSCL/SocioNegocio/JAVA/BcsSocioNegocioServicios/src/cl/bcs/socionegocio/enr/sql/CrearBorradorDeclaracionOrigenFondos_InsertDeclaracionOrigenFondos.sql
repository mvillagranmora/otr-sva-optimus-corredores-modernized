INSERT INTO "BSCL.SocioNegocio.BD::ENR.DeclaracionOrigenFondos" (
 "IdDeclaracionOrigenFondos"
 ,"IdEnrolamiento"
 ,"IdTipoOperacionOrigenFondos"
 ,"FechaDeclaracion"
 ,"Comentario"
 ,"IdUsuarioCreador"
 ,"FechaIngreso"
 ,"FechaCreacion"
 ,"FechaModificacion"
 ,"IdEmpresa"
 ,"OperacionInscripcion" )
VALUES (
:IdDeclaracionOrigenFondos
,:IdEnrolamiento
,:IdTipoOperacionOrigenFondos
,:FechaDeclaracion
,:Comentario
,:IdUsuarioCreador
,:FechaIngreso
,CURRENT_UTCTIMESTAMP
,CURRENT_UTCTIMESTAMP
,:IdEmpresa
,:OperacionInscripcion
);