CALL "BSCL.Orden.BD.Servicios::VerificarSecuenciasFoliosOrdenes" (
	p_IdEmpresa => :IdEmpresa,
	p_IdUsuario => :IdUsuario,
	p_IdModulo => :IdModulo,
	--Salida
	p_out_Mensaje => @p_out_Mensaje,
	p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle 	=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador 		=> @p_out_ErroresContador,
	p_out_ErroresDetalle 		=> $p_out_ErroresDetalle
);