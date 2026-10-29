CALL "BSCL.FondoMutuo.BD.Servicios::CargarValorCuota"(
	p_IdEmpresa							=> :p_IdEmpresa,
	p_IdUsuario							=> :p_IdUsuario,
	p_IdModulo							=> :p_IdModulo,
	p_UsaReglasNegocio					=> :p_UsaReglasNegocio,
	p_SimularOperacion 					=> :p_SimularOperacion,
	p_ValorCuota						=> #p_ValorCuota, --Se crea una tabla temporal el SP tiene que ir a buscarla a la BD
	
	p_out_Resultado						=> $p_out_Resultado,
	p_out_AdvertenciasContador			=> @p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle			=> $p_out_AdvertenciasDetalle,
	p_out_ErroresContador				=> @p_out_ErroresContador,
	p_out_ErroresDetalle				=> $p_out_ErroresDetalle
);
