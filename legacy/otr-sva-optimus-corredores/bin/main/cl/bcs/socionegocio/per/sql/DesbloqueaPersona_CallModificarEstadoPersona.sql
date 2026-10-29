CALL "BSCL.SocioNegocio.BD.Servicios::PER.ModificarEstadoPersona" (
	p_IdEmpresa => :p_IdEmpresa,
	p_IdUsuario => :p_IdUsuario,
	p_IdModulo => :p_IdModulo,
	p_UsaReglasNegocio => :p_UsaReglasNegocio,
	p_IdPersona => :p_IdPersona,
	p_IdEstado => :p_IdEstado,
	p_IdTipo => :p_IdTipo,
	p_Fecha => :p_Fecha,
	p_Comentario => :p_Comentario,
	p_OUT_REFID => @P_OUT_REFID,
	p_OUT_ADVERTENCIASCONTADOR => @P_OUT_ADVERTENCIASCONTADOR,
	p_OUT_ADVERTENCIASDETALLE => $P_OUT_ADVERTENCIASDETALLE,
	p_OUT_ERRORESCONTADOR => @P_OUT_ERRORESCONTADOR,
	p_OUT_ERRORESDETALLE => $P_OUT_ERRORESDETALLE
)