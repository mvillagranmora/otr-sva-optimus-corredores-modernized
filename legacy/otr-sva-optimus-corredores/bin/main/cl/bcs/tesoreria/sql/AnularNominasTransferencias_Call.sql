CALL "BSCL.Tesoreria.BD.Servicios.NOM::AnularNominasTransferencias" (
	p_IdEmpresa 				=> :p_IdEmpresa,
	p_IdUsuario 				=> :p_IdUsuario,
	p_IdModulo 					=> :p_IdModulo,  
	p_UsaReglasNegocio 			=> :p_UsaReglasNegocio,
	
	p_FechaMovimiento			=> :p_FechaMovimiento,
	p_RegistrosAAnular 			=> #AnularNominasTransferencias_p_RegistrosAProcesar,
	
	p_out_ResultadoProceso		=> $p_out_ResultadoProceso,
	p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle  	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador      	=> @p_out_ErroresContador,
	p_out_ErroresDetalle       	=> $p_out_ErroresDetalle
);