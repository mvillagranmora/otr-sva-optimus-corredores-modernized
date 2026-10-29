CALL "BSCL.Plataforma.BD.Servicios::TAS.ModificarEstadoTasas" (
	p_IdEmpresa			=> :IdEmpresa,
	p_IdUsuario			=> :IdUsuario,
	p_IdModulo			=> :IdModulo,
	p_UsaReglasNegocio	=> :UsaReglasNegocio,
	p_Estado => 'OFI',
	p_TablaTasa => #ModificarEstadoTasas_p_TablaTasa,
	p_out_RefId	=> @p_out_RefId,
	p_out_AdvertenciasContador	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle	=> $p_out_AdvertenciasDetalle,	
	p_out_ErroresContador		=> @p_out_ErroresContador,
	p_out_ErroresDetalle		=> $p_out_ErroresDetalle
)