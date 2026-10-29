CALL "BSCL.Plataforma.BD.Servicios::INS.ConsultarInstrumentoPrecioBolsa" (
p_Instrumentos 				=> #p_Instrumentos,
p_out_Instrumento 			=> $p_out_Instrumento,
p_out_AdvertenciasContador 	=> @p_out_AdvertenciasContador,
p_out_AdvertenciasDetalle 	=> $p_out_AdvertenciasDetalle,
p_out_ErroresContador 		=> @p_out_ErroresContador,
p_out_ErroresDetalle 		=> $p_out_ErroresDetalle);