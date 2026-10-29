CALL "BSCL.Portafolio.BD.Servicios::CrearAutorizadoOrden" (
    p_IdEmpresa => :p_IdEmpresa,  
    p_IdUsuario => :p_IdUsuario, 
    p_IdModulo => :p_IdModulo,
    p_UsaReglasNegocio => :p_UsaReglasNegocio,  
	p_IdPortafolio => :p_IdPortafolio,
	p_IdPersona => :p_IdPersona,
	p_IdEstado => :p_IdEstado,
	p_IdTipoAutorizado => :p_IdTipoAutorizado,
	p_FirmaOptimusCliente => :p_FirmaOptimusCliente,
	p_NumeroRegistroPublico => :p_NumeroRegistroPublico,
	p_Asiento => :p_Asiento,
	p_FichaPartida => :p_FichaPartida,
	p_TipoFirma => :p_TipoFirma,
	
	p_out_RefId => @p_out_RefId,
    p_out_AdvertenciasContador => @p_out_AdvertenciasContador,  
  	p_out_AdvertenciasDetalle => $p_out_AdvertenciasDetalle,
  	p_out_ErroresContador => @p_out_ErroresContador,
  	p_out_ErroresDetalle => $p_out_ErroresDetalle
)