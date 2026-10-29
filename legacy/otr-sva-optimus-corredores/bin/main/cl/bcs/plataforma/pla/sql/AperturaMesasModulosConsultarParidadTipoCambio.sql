CALL "BSCL.Mesa.BD.Servicios::SPT.ConsultarParidadYTipoCambio"(
	--IN
	p_IdEmpresa=>:p_IdEmpresa,
	p_IdUsuario=>:p_IdUsuario,
	p_Fecha=>:p_Fecha,
	--OUT
	p_out_ParidadesYTipoCambio=>$p_out_ParidadesYTipoCambio,
	p_out_AdvertenciasContador=>@p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle=>$p_out_AdvertenciasDetalle,
	p_out_ErroresContador=>@p_out_ErroresContador,
	p_out_ErroresDetalle=>$p_out_ErroresDetalle
)