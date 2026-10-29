insert into "BSCL.Plataforma.BD::COM_2.DetallePerfilCobro"(
"IdDetallePerfilCobros","IdEmpresa","IdPerfilCobros","IdTipoProducto","IdTipoOperacion","IdBolsa","IdTipoCanalIngreso","CodigoDefinicionCobro","CodigoDefinicionCobroBase","FechaCreacion","FechaIngresoRegistro","IdEstadoAutorizacion","IdUsuarioCreador")
values (
:IdDetallePerfilCobros,:IdEmpresa,:IdPerfilCobros,:IdTipoProducto,:IdTipoOperacion,:IdBolsa,:IdTipoCanalIngreso,:CodigoDefinicionCobro,:CodigoDefinicionCobroBase,:FechaCreacion,CURRENT_TIMESTAMP,:IdEstadoAutorizacion,:IdUsuarioCreador)
