CALL "BSCL.Plataforma.BD.Servicios::PAU.AbrirModulo"(
	--IN
	p_IdEmpresa=>:p_IdEmpresa,
	p_IdUsuario=>:p_IdUsuario,
	p_IdModulo=>:p_IdModulo,
	p_Modulo=>#AbrirModulo_p_Modulo,
	p_MesaAbierta=>:p_MesaAbierta,
	--OUT
	p_out_Resultado=>$p_out_Resultado,
	p_out_AdvertenciasContador=>@p_out_AdvertenciasContador,
	p_out_AdvertenciasDetalle=>$p_out_AdvertenciasDetalle,
	p_out_ErroresContador=>@p_out_ErroresContador,
	p_out_ErroresDetalle=>$p_out_ErroresDetalle
)