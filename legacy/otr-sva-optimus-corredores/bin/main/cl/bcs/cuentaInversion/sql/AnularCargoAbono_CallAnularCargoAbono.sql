CALL "BSCL.CuentaInversion.BD.Servicios::AnularCargoAbono" (
	p_IdEmpresa                 => :p_IdEmpresa,
	p_IdUsuario                 => :p_IdUsuario,
	p_IdModulo                  => :p_IdModulo,
	p_simularOperacion			=> :p_simularOperacion,
	p_UsaReglasNegocio	 		=> :p_UsaReglasNegocio,
	p_EjecutaAutorizacion		=> :p_EjecutaAutorizacion,
	p_OmiteRegistroSinAutorizar	=> :p_OmiteRegistroSinAutorizar,
	p_FechaMovimiento           => :p_FechaMovimiento,
	p_EsAnulacionManual			=> :p_EsAnulacionManual,
	p_out_InformacionContador  	=> @p_out_InformacionContador,
	p_out_AdvertenciasContador  => @p_out_AdvertenciasContador,
	p_out_ErroresContador       => @p_out_ErroresContador
);
