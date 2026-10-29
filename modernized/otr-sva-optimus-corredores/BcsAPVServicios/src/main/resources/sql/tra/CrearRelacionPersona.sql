CALL "BSCL.SocioNegocio.BD.Servicios::REL.CrearRelacionPersona" (
	-- Parametros entrada comunes en todos los servicios publicos
	p_IdEmpresa						=> :IdEmpresa,			-- ID  Empresa destino
	p_IdUsuario						=> :IdUsuario,			-- ID  del usuario que solicita el servicio
	p_IdModulo						=> :IdModulo,  	-- ID  del Modulo que lo invoca
	p_UsaReglasNegocio				=> 'N',	-- S/N se usa en caso de servicios anidados.
			  
	-- Parametros entrada del servicio publico
	p_IdTipoRelacion				=> :IdTipoRelacion,
	p_IdPersonaOrigen 				=> :IdPersonaOrigen,
	p_IdPersonaDestino 				=> :IdPersonaDestino,
	p_IdEstado 						=> :IdEstado,
	p_FechaInicioRelacion 			=> :FechaInicioRelacion,
	p_FechaFinRelacion 				=> :FechaFinRelacion,
	p_PorcentajeParticipacion 		=> :PorcentajeParticipacion,
	p_RetiraDocumentos 				=> 'N',
	p_FinalidadRelacion				=> '',
	p_CondicionesRelacion			=> '',
	p_IdGradoVinculacionParental	=> 0,
		
	-- Parametros salida del servicio publico
	p_out_RefId 					=> @p_out_RefId,
	-- Salidas para mensajes de advertencias y errores
	p_out_AdvertenciasContador 		=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle 		=> $p_out_AdvertenciasDetalle,	
	p_out_ErroresContador 			=> @p_out_ErroresContador,
	p_out_ErroresDetalle 			=> $p_out_ErroresDetalle
); 