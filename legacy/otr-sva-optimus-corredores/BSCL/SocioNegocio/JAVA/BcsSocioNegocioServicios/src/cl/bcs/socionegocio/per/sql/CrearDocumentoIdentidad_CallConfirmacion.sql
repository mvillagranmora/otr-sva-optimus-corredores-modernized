CALL "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarCrearPersonaDocumentosIdentidad" (
	-- Parametros entrada comunes en todos los servicios publicos
	p_IdEmpresa => :p_IdEmpresa,
	p_IdUsuario	=> :p_IdUsuario,
	p_IdModulo => :p_IdModulo,
	p_UsaReglasNegocio => :p_UsaReglasNegocio,
	
	-- Parametros entrada del servicio publico
	p_IdDocumentoIdentidad => :p_IdDocumentoIdentidad,	
	p_IdEstadoAutorizacion => :p_IdEstadoAutorizacion,
			
	-- Salidas para mensajes de advertencias y errores
	p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle 	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador 		=> @p_out_ErroresContador,
	p_out_ErroresDetalle 		=> $p_out_ErroresDetalle
) 