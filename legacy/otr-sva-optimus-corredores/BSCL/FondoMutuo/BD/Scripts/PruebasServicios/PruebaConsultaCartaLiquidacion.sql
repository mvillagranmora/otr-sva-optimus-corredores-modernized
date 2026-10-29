set schema bscl;


call "BSCL.FondoMutuo.BD.Servicios::ConsultarCartaLiquidacion" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_IdEmisor	=> '',
	p_FechaMovimiento =>	'2016-08-30',
	p_FechaPago	=> '2016-08-30',
	p_SerieFondo	=> '',
	p_Estado	=> 'CRT',
	p_TipoSolicitud => 'APO'
);


