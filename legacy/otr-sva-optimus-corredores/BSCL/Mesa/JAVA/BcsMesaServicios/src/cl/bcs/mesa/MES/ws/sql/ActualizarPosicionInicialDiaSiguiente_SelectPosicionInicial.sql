CALL "BSCL.Mesa.BD.Servicios::SPT.ConsultarPosicionPorMoneda" (
    p_IdEmpresa => :p_IdEmpresa,  
    p_IdUsuario => :p_IdUsuario, 
    p_IdMoneda => :p_IdMoneda,
    p_FechaOperacion => :p_FechaOperacion,  
	p_TipoMesa => :p_TipoMesa,
	
	p_out_posicionPorMonedas => $p_out_posicionPorMonedas,
  	p_out_AdvertenciasContador => @p_out_AdvertenciasContador,
  	p_out_AdvertenciasDetalle => $p_out_AdvertenciasDetalle,
  	p_out_ErroresContador => @p_out_ErroresContador,
  	p_out_ErroresDetalle => $p_out_ErroresDetalle
)