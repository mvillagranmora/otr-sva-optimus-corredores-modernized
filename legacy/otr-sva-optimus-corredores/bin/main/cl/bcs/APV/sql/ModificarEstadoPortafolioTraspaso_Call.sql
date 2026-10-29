CALL "BSCL.Portafolio.BD.Servicios::ModificarEstadoPortafolio" (
	p_IdEmpresa => :IdEmpresa,
	p_IdUsuario => :IdUsuario,
	p_IdModulo => :IdModulo,
	p_UsaReglasNegocio => 'N',
	
	p_IdPortafolio => :IdPortafolio,
    p_IdEstado => :IdEstado,
	p_IdTipo => :IdTipo,
	p_Fecha => :Fecha,
	p_Comentario => '',

	
	--Salida
	p_out_RefId => @p_out_RefId,
	
	p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle 	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador 		=> @p_out_ErroresContador,
	p_out_ErroresDetalle 		=> $p_out_ErroresDetalle
);