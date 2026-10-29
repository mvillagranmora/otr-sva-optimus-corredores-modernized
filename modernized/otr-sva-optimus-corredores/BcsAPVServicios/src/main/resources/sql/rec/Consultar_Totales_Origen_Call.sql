CALL "BSCL.APV.BD.Servicios::APV.ConsultarTotalesOrigenPorDeposito" (
	
	--Entrada
	p_IdEmpresa => :IdEmpresa,
	p_IdUsuario => :IdUsuario,
	p_IdModulo => :IdModulo,
	p_IdRecaudacion => :IdRecaudacion,
	
	--Salida
	p_out_TotalesOrigen => $p_out_TotalesOrigen,
	
	p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle 	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador 		=> @p_out_ErroresContador,
	p_out_ErroresDetalle 		=> $p_out_ErroresDetalle
);

