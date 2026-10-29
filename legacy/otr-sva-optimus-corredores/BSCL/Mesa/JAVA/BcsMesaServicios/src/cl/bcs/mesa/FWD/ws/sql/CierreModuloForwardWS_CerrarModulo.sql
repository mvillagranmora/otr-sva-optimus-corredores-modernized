CALL "BSCL.Plataforma.BD.Servicios::PAU.CerrarModulo" (
p_IdEmpresa => :IdEmpresa,
p_IdUsuario => :IdUsuario,
p_IdModulo => 'MFW',
p_Modulo => #CierreModulo_p_Modulo,
p_out_Resultado	=> $p_out_Resultado,
p_out_AdvertenciasContador => @p_out_AdvertenciasContador,
p_out_AdvertenciasDetalle => $p_out_AdvertenciasDetalle,	
p_out_ErroresContador => @p_out_ErroresContador,
p_out_ErroresDetalle => $p_out_ErroresDetalle
);