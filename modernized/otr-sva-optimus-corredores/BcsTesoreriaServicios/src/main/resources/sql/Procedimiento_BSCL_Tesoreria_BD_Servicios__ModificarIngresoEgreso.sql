CALL "BSCL.Tesoreria.BD.Servicios::ModificarIngresoEgreso" (
    p_IdEmpresa => :p_IdEmpresa,  
    p_IdUsuario => :p_IdUsuario, 
    p_IdModulo => :p_IdModulo,
    p_UsaReglasNegocio => :p_UsaReglasNegocio,  
	p_SimularOperacion => :p_SimularOperacion,
	p_IngresoEgresoModificar => #LiquidarMovimientoAutomatico_P_INGRESOEGRESOMODIFICAR,
	p_FormaDePago => #LiquidarMovimientoAutomatico_P_FORMAPAGO,

	p_out_IngresoEgresoModificado => $p_out_IngresoEgresoModificado,
    p_out_ResultadoProceso => $p_out_ResultadoProceso,  
  	p_out_AdvertenciasContador => @p_out_AdvertenciasContador,
  	p_out_AdvertenciasDetalle => $p_out_AdvertenciasDetalle,
  	p_out_ErroresContador => @p_out_ErroresContador,
  	p_out_ErroresDetalle => $p_out_ErroresDetalle
)