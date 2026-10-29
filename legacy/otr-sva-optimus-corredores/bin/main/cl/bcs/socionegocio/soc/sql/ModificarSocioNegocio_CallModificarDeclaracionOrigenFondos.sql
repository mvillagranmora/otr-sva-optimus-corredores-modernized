CALL "BSCL.SocioNegocio.BD.Servicios::CLI.ModificarDeclaracionOrigenFondos" (
	p_IdEmpresa					=> :p_IdEmpresa,
	p_IdUsuario					=> :p_IdUsuario,
	p_IdCliente					=> :p_IdCliente,
	p_IdModulo					=> :p_IdModulo,
	p_IdDeclaracionOrigenFondos	=> :p_IdDeclaracionOrigenFondos,
	p_RegistroCambio			=> #ModificarSocioNegocio_RegistroCambio,
	p_out_RefId					=> @p_out_RefId,
	p_out_AdvertenciasContador	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador		=> @p_out_ErroresContador,
	p_out_ErroresDetalle		=> $p_out_ErroresDetalle
);