CALL "BSCL.Plataforma.BD.Servicios::TAS.GenerarArchivoTasas" 
(
	p_IdEmpresa  				=>:p_IdEmpresa,
	p_IdUsuario  				=>:p_IdUsuario,
	p_IdModulo   				=>:p_IdModulo,
	p_UsaReglasNegocio 			=>:p_UsaReglasNegocio,
	
	-- Parametros entrada
	p_Fecha 					=>:p_Fecha,
	p_CodigoFuente				=>:p_Fuente,
	p_Mercado					=>:p_Mercado,
	
	-- Parametros salida
	p_out_ResultadoProceso		=> $p_out_ResultadoProceso,
	p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle 	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador 		=> @p_out_ErroresContador,
	p_out_ErroresDetalle 		=> $p_out_ErroresDetalle
);