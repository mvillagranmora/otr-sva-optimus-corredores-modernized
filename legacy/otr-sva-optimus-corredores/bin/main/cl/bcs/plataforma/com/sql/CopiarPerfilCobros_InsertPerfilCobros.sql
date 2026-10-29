insert into "BSCL.Plataforma.BD::COM_2.PerfilCobros"
(
"IdPerfilCobros",
"DescripcionPerfilCobros",
"EsPerfilPersonalizado",
"TipoEntidadReferencia",
"IdReferencia",
"FechaCreacion",
"FechaModificacion",
"IdEstado",
"IdEmpresa",
"FechaIngreso",
"IdEstadoAutorizacion",
"IdUsuarioCreador"
) VALUES (
  :IdPerfilCobrosNuevo,
  :DescripcionPerfilCobros,
  :EsPerfilPersonalizado,
  :TipoEntidadReferencia,
  :IdReferencia,
  CURRENT_UTCTIMESTAMP, 
  CURRENT_UTCTIMESTAMP,				  
  :IdEstado,
  :IdEmpresa,
  :FechaIngreso,
  :IdEstadoAutorizacion,
  :IdUsuario);