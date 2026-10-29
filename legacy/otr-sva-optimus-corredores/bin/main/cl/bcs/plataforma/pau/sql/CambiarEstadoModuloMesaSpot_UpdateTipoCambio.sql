CALL "BSCL.Mesa.BD.Servicios::SPT.ModificarParidadYTipoCambio"(
	--IN
	p_IdEmpresa=>:p_IdEmpresa,
	p_IdUsuario=>:p_IdUsuario,
	p_IdModulo=>:p_IdModulo,
	p_Fecha=>:p_Fecha,
	p_OrigenJob=>:p_OrigenJob,
	p_DatosParidadYTipoCambio=>#ModificarParidadYTipoCambio_p_DatosParidadYTipoCambio,
	--OUT
	p_out_AdvertenciasContador=>@p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle=>$p_out_AdvertenciasDetalle,
	p_out_ErroresContador=>@p_out_ErroresContador,
	p_out_ErroresDetalle=>$p_out_ErroresDetalle
)