CALL "BSCL.Plataforma.BD.Servicios::ACC.ValidaSesionUsuario"(
	p_Token => :p_Token,
	-- Salida Correcta
	p_out_IdEmpresa =>@p_out_IdEmpresa,
	p_out_IdUsuario =>@p_out_IdUsuario,	
	p_out_NombreEmpresa =>@p_out_NombreEmpresa,
	p_out_NombreUsuario =>@p_out_NombreUsuario,
    p_out_CodigoMenuUsuario =>@p_out_CodigoMenuUsuario,	
	p_out_IdSocioDeNegocio =>@p_out_IdSocioDeNegocio,	
	p_out_IdPersonaCorredora =>@p_out_IdPersonaCorredora,	
	p_out_IdMonedaSistema =>@p_out_IdMonedaSistema,	
	p_out_IdPaisSistema =>@p_out_IdPaisSistema,
	p_out_FechaSistema =>@p_out_FechaSistema,
	p_out_BolsaDefectoSistema =>@p_out_BolsaDefectoSistema,
	p_out_ValoresDefectoSistema	=>$p_out_ValoresDefectoSistema,
	p_out_IdEmpleado =>@p_out_IdEmpleado,	
	p_out_IdCliente =>@p_out_IdCliente,
	p_out_IdPersona =>@p_out_IdPersona,
	p_out_DebeCambiarClave =>@p_out_DebeCambiarClave,	
	-- Salidas para mensajes de advertencias y errores
	p_out_AdvertenciasContador =>@p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle =>$p_out_AdvertenciasDetalle,	
	p_out_ErroresContador =>@p_out_ErroresContador,
	p_out_ErroresDetalle =>$p_out_ErroresDetalle
);
