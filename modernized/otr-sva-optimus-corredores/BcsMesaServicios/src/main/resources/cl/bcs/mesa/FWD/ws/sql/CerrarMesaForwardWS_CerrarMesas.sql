CALL "BSCL.Mesa.BD.Servicios::CierreAperturaMesa" (
	p_IdEmpresa			=> :IdEmpresa,
	p_IdUsuario			=> :IdUsuario,
	p_IdModulo			=> 'MRF',
	p_simularOperacion	=> 'N',
	p_UsaReglasNegocio	=> 'S',
	p_DatosMesas => #CierreAperturaMesa_p_DatosMesas,
	p_out_ResultadoProceso	=> $p_out_ResultadoProceso,
	p_out_AdvertenciasContador	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle	=> $p_out_AdvertenciasDetalle,	
	p_out_ErroresContador		=> @p_out_ErroresContador,
	p_out_ErroresDetalle		=> $p_out_ErroresDetalle
)