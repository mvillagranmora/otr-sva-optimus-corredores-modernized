CALL "BSCL.Tesoreria.BD.Servicios::EdicionMultiple" (
    p_IdEmpresa => :p_IdEmpresa,  
    p_IdUsuario => :p_IdUsuario, 
    p_IdModulo => :p_IdModulo,
    p_UsaReglasNegocio => :p_UsaReglasNegocio,  
	p_FechaMovimiento => :p_FechaMovimiento,
	p_Entidad => :p_Entidad,
	p_LiquidaComprobante => :p_LiquidaComprobante,
	p_FormasDePago => #EdicionMultuple_P_FORMAPAGO,
	p_RegistrosAProcesar => #EdicionMultuple_P_REGISTROPROCESO,     

	p_out_InformacionContador => @p_out_InformacionContador,
    p_out_AdvertenciasContador => @p_out_AdvertenciasContador,  
  	p_out_ErroresContador => @p_out_ErroresContador,
  	p_out_FormaDePagoComprobante => $p_out_FormaDePagoComprobante
)