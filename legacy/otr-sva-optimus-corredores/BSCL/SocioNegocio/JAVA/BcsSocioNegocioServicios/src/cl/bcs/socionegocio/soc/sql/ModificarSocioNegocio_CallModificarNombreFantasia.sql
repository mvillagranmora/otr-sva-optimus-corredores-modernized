CALL "BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaNombreFantasia" (
	p_IdEmpresa					=> :p_IdEmpresa,
	p_IdUsuario					=> :p_IdUsuario,
	p_IdModulo					=> :p_IdModulo,
	p_UsaReglasNegocio			=> :p_UsaReglasNegocio,
	p_IdPersona					=> :p_IdPersona,
	p_IdNombreFantasia			=> :p_IdNombreFantasia,
	p_NombreFantasia			=> :p_NombreFantasia,
	p_IdEstado					=> :p_IdEstado,
	p_out_AdvertenciasContador	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador		=> @p_out_ErroresContador,
	p_out_ErroresDetalle		=> $p_out_ErroresDetalle
);