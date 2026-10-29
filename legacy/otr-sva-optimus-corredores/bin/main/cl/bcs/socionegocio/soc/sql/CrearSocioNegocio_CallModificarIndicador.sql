CALL "BSCL.Plataforma.BD.Servicios::IND.ModificarIndicador" (
	p_IdEmpresa					=> :p_IdEmpresa,
	p_IdUsuario					=> :p_IdUsuario,
	p_IdModulo					=> :p_IdModulo,
	p_UsaReglasNegocio			=> :p_UsaReglasNegocio,
	p_IdTipoEntidad				=> :p_IdTipoEntidad,
	p_IdEntidad					=> :p_IdEntidad,
	p_RegistroAtributo			=> #CrearSocioNegocio_IndicadorPersonaCliente,
	p_ModeloCliente				=> :p_ModeloCliente,
	p_out_IdSecuencias			=> $p_out_IdSecuencias,
	p_out_AdvertenciasContador	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador		=> @p_out_ErroresContador,
	p_out_ErroresDetalle		=> $p_out_ErroresDetalle
);