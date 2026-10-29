CALL "BSCL.Mesa.BD.Servicios::CierreAperturaMesa"(
	--IN
	p_IdEmpresa=>:p_IdEmpresa,
	p_IdUsuario=>:p_IdUsuario,
	p_IdModulo=>:p_IdModulo,
	p_simularOperacion=>:p_simularOperacion,
	p_UsaReglasNegocio=>:p_UsaReglasNegocio,
	p_DatosMesas=>#CierreAperturaMesa_p_DatosMesas,
	--OUT
	p_out_ResultadoProceso=>$p_out_ResultadoProceso,
	p_out_AdvertenciasContador=>@p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle=>$p_out_AdvertenciasDetalle,
	p_out_ErroresContador=>@p_out_ErroresContador,
	p_out_ErroresDetalle=>$p_out_ErroresDetalle
)