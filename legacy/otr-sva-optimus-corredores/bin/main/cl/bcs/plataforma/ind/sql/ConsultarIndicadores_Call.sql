CALL "BSCL.Plataforma.BD.Servicios::IND.ConsultarPreferencia"(
	p_IdEmpresa => :p_IdEmpresa,
	p_IdEntidad => COALESCE(:p_IdEntidad,0),
	p_IdTipoEntidad => COALESCE(:p_IdTipoEntidad,''),
	p_CodigoTipoIndicador => COALESCE(:p_CodigoTipoIndicador,''),
	p_GrupoIndicador => COALESCE(:p_GrupoIndicador,''),
	p_ModeloCliente => COALESCE(:p_ModeloCliente,''),
	p_IdIndicador => COALESCE(:p_IdIndicador,0), 
	
	p_out_IndicadoresContador => @p_out_IndicadoresContador,
	p_out_Indicadores => $p_out_Indicadores,
	p_out_GrupoIndicadoresContador => @p_out_GrupoIndicadoresContador,
	p_out_GrupoIndicadores => $p_out_GrupoIndicadores
);
