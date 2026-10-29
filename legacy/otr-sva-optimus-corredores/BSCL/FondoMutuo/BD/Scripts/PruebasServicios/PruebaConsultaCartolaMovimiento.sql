set schema bscl;

call "BSCL.CuentaInversion.BD.Servicios::ConsultarCartolaDeMovimiento" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'CTA',
	p_UsaReglasNegocio => 'S',
	p_IdCuentaInversion => 1460504,-- USD: 1460504, CLP: 1460503
	p_IdCliente => null,
	p_IdPortafolio => 512348,
	p_IdMoneda => 'USD',
	p_IdEstado => null,
	p_FechaDesde => '2016-09-14',
	p_FechaHasta => '2016-09-14',
	p_SaldoPorFechaEfectiva => 'S',
	p_out_CartolaDeMovimiento => ?,
	p_out_InformacionContador => ?,
	p_out_InformacionDetalle => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);