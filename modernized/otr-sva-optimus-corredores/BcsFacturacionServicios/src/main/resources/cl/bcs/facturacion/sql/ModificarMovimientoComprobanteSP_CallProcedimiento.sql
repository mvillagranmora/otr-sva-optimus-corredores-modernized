CALL "BSCL.Facturacion.BD.Servicios::ModificarMovimientoComprobante"(
	p_IdEmpresa => :p_IdEmpresa,
	p_IdUsuario => :p_IdUsuario,
	p_IdModulo => :p_IdModulo,
	p_simularOperacion => :p_simularOperacion,
	p_UsaReglasNegocio => :p_UsaReglasNegocio,
	p_EsIngresoAutomatico => :p_EsIngresoAutomatico,
	p_out_InformacionContador => @p_out_InformacionContador,
	p_out_AdvertenciasContador => @p_out_AdvertenciasContador,
	p_out_ErroresContador => @p_out_ErroresContador
);