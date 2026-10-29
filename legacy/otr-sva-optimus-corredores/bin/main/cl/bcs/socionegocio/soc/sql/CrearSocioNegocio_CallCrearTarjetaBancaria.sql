CALL "BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaTarjetaBancaria" (
	p_IdEmpresa					=> :p_IdEmpresa,
	p_IdUsuario					=> :p_IdUsuario,
	p_IdModulo					=> :p_IdModulo,
	p_UsaReglasNegocio			=> :p_UsaReglasNegocio,
	p_IdBanco					=> :p_IdBanco,
	p_IdTipoTarjetaBancaria		=> :p_IdTipoTarjetaBancaria,
	p_NumeroTarjetaBancaria		=> :p_NumeroTarjetaBancaria,
	p_IdPersona					=> :p_IdPersona,
	p_FechaVencimiento			=> :p_FechaVencimiento,
	p_CodigoVerificacion		=> :p_CodigoVerificacion,
	p_IdEstado					=> :p_IdEstado,
	p_out_RefId					=> @p_out_RefId,
	p_out_AdvertenciasContador	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador		=> @p_out_ErroresContador,
	p_out_ErroresDetalle		=> $p_out_ErroresDetalle
);