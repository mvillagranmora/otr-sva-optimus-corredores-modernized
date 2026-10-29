CALL "BSCL.SocioNegocio.BD.Servicios::PER.ModificarPersonaDocumentosIdentidad" (
	p_IdEmpresa					=> :p_IdEmpresa,
	p_IdUsuario					=> :p_IdUsuario,
	p_IdModulo					=> :p_IdModulo,
	p_UsaReglasNegocio			=> :p_UsaReglasNegocio,
	p_IdDocumentoIdentidad		=> :p_IdDocumentoIdentidad,
	p_RegistroCambio			=> #ModificarSocioNegocio_RegistroCambio,
	p_out_RefId					=> @p_out_RefId,
	p_out_AdvertenciasContador	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador		=> @p_out_ErroresContador,
	p_out_ErroresDetalle		=> $p_out_ErroresDetalle
);