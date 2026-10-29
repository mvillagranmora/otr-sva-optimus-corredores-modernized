CALL "BSCL.SocioNegocio.BD.Servicios::CLI.CrearDeclaracionOrigenFondos" (
	p_IdEmpresa						=> :p_IdEmpresa,
	p_IdUsuario						=> :p_IdUsuario,
	p_IdModulo						=> :p_IdModulo,
	p_IdCliente						=> :p_IdCliente,
	p_IdTipoOperacionOrigenFondos	=> :p_IdTipoOperacionOrigenFondos,
	p_FechaDeclaracion				=> :p_FechaDeclaracion,
	p_Comentario					=> :p_Comentario,
	p_IdEstado						=> :p_IdEstado,
	p_OperacionInscripcion			=> :p_OperacionInscripcion,
	p_Detalle						=> #p_Detalle,
	p_out_RefId						=> @p_out_RefId,
	p_out_AdvertenciasContador		=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle		=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador			=> @p_out_ErroresContador,
	p_out_ErroresDetalle			=> $p_out_ErroresDetalle
);