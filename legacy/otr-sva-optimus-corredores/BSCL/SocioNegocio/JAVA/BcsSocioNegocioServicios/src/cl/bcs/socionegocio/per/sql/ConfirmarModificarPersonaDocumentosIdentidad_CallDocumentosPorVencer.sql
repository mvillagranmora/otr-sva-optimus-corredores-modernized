CALL "BSCL.Plataforma.BD.Servicios::DOC.DocumentosPorVencer" (
	p_IdEmpresa					=> :p_IdEmpresa,
	p_IdUsuario					=> :p_IdUsuario,
	p_IdModulo					=> :p_IdModulo,
	p_out_AdvertenciasContador	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador		=> @p_out_ErroresContador,
	p_out_ErroresDetalle		=> $p_out_ErroresDetalle
);
