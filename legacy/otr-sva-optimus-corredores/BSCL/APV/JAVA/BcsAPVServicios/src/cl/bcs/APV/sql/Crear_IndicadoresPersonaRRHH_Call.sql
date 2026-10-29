CALL "BSCL.Plataforma.BD.Servicios::IND.ModificarIndicador"(
	-- Parametros entrada comunes en todos los servicios publicos
	p_IdEmpresa 				=> :IdEmpresa,
	p_IdUsuario 				=> :IdUsuario,
	p_IdModulo 					=> :IdModulo,
	p_UsaReglasNegocio 			=> 'N',	  
	-- Parametros entrada del servicio publico
	p_IdTipoEntidad			  	=> 'PERSONA',	
	p_IdEntidad				  	=> :IdEntidad,
	p_RegistroAtributo		  	=> #RegistroAtributo,
	p_ModeloCliente			  	=> '',  ---<TipoCliente>
	-- Parametros salida del servicio publico
	p_out_IdSecuencias 			=> $p_out_IdSecuencias,
	-- Salidas para mensajes de advertencias y errores
	p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle 	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador 		=> @p_out_ErroresContador,
	p_out_ErroresDetalle 		=> $p_out_ErroresDetalle
);