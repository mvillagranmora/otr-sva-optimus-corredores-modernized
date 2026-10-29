CALL "BSCL.Portafolio.BD.Servicios::RevisaDatosPortafolio" (
	p_IdEmpresa => :IdEmpresa,
	p_IdUsuario => :IdUsuario,
	p_IdModulo => :IdModulo,
	p_UsaReglasNegocio => 'N',
	p_IdCliente => :IdCliente,
	p_IdPerfilComercialBase => :IdPerfilComercial,
	p_IdPersonaBrokerExtranjero => :IdPersonaBrokerExtranjero,
	p_Estado => 'ACT',
	
	--Salida
	p_out_RefId => @p_out_RefId,
	
	p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle 	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador 		=> @p_out_ErroresContador,
	p_out_ErroresDetalle 		=> $p_out_ErroresDetalle
);