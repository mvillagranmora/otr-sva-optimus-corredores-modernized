CALL "BSCL.Portafolio.BD.Servicios::ModificarEstadoPortafolio" (
	p_IdEmpresa	=> :p_IdEmpresa,
	p_IdUsuario => :p_IdUsuario,
	p_IdModulo => :p_IdModulo,
	p_UsaReglasNegocio => :p_UsaReglasNegocio,	
	p_IdPortafolio => :p_IdPortafolio,
	p_IdEstado => :p_IdEstado,
	p_IdTipo => :p_IdTipo,
	p_Fecha => :p_Fecha,
	p_FechaHasta => :p_FechaHasta,
	p_Comentario => :p_Comentario,
	p_out_RefId => @p_out_RefId,
	p_out_AdvertenciasContador => @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle => $p_out_AdvertenciasDetalle,	
	p_out_ErroresContador => @p_out_ErroresContador,
	p_out_ErroresDetalle => $p_out_ErroresDetalle
)