CALL "BSCL.Mesa.BD.Servicios::SPT.CrearPosicionInicial" (
    p_IdEmpresa => :p_IdEmpresa,  
    p_IdUsuario => :p_IdUsuario, 
    p_IdMoneda => :p_IdMoneda,
    p_Fecha => :p_Fecha,  
	p_DatosPosicion => #ActualizarPosicionInicial_p_DatosPosicion,
	
  	p_out_AdvertenciasContador => @p_out_AdvertenciasContador,
  	p_out_AdvertenciasDetalle => $p_out_AdvertenciasDetalle,
  	p_out_ErroresContador => @p_out_ErroresContador,
  	p_out_ErroresDetalle => $p_out_ErroresDetalle
)