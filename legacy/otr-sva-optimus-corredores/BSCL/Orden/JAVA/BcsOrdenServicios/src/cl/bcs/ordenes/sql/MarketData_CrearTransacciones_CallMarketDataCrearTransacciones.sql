CALL "BSCL.Orden.BD.Servicios::MarketData_CrearTransacciones"(
	p_IdEmpresa					=> :p_IdEmpresa,
	p_IdUsuario					=> :p_IdUsuario,
	p_SegundosZonaHoraria		=> :p_SegundosZonaHoraria,
	p_out_InformacionContador	=> @p_out_InformacionContador,
   	p_out_AdvertenciasContador	=> @p_out_AdvertenciasContador,
	p_out_ErroresContador		=> @p_out_ErroresContador
	);
