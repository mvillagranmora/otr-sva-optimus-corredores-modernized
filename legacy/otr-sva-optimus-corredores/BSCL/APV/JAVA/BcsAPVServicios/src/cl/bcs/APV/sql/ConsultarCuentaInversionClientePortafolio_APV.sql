CALL "BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversionCliente"
(
	p_IdEmpresa => :p_IdEmpresa,
	p_IdUsuario => :p_IdUsuario,
	p_IdModulo => :p_IdModulo,
	p_UsaReglasNegocio => :p_UsaReglasNegocio,
	p_IncluyeOtrosSaldos => :p_IncluyeOtrosSaldos,
	p_TipoAgrupador => :p_TipoAgrupador,
	p_CuentaInversionAConsultar => #p_CuentaInversionAConsultar,
	p_FechaDesde => :p_FechaDesde,
	p_FechaHasta => :p_FechaHasta,
	--Salida
	p_out_SaldoPorFechaMovimiento => $p_out_SaldoPorFechaMovimiento,
	p_out_InformacionContador	=> @p_out_InformacionContador,
	p_out_InformacionDetalle	=> $p_out_InformacionDetalle,
	p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle 	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador 		=> @p_out_ErroresContador,
	p_out_ErroresDetalle 		=> $p_out_ErroresDetalle
);