CALL "BSCL.Portafolio.BD.Servicios::CrearPortafolio" (
	p_IdEmpresa => :IdEmpresa,
	p_IdUsuario => :IdUsuario,
	p_IdModulo => :IdModulo,
	p_UsaReglasNegocio => 'N',
	
	p_IdCliente => :IdCliente,
	p_IdPerfilComercialBase => :IdPerfilComercial,
	p_PropositoPortafolio => :PropositoPortafolio,
	p_CodigoPortafolio => :CodigoPortafolio,
	p_CodigoRegimen => :CodigoRegimen,
	p_NombrePlan => :NombrePlan,
	p_CodigoAgrupador => :CodigoAgrupador,
	p_Estado => 'ACT',
	p_IdCuentaOrigen => :p_IdCuentaOrigen,
	
	--Salida
	p_out_RefId => @p_out_RefId,
	
	p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle 	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador 		=> @p_out_ErroresContador,
	p_out_ErroresDetalle 		=> $p_out_ErroresDetalle
);

