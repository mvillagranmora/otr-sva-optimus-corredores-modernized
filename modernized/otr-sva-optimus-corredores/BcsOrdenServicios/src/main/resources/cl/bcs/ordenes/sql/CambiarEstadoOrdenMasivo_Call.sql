CALL "BSCL.Orden.BD.Servicios::CambiarEstadoOrdenMasivo" (
		p_Ordenes				=> #entrada_cantar_ordenes,	
		p_NuevoEstado			=> :p_NuevoEstado,
		p_out_SalidaDetalle		=> $p_out_SalidaDetalle,
		p_out_SalidaResumen		=> $p_out_SalidaResumen
);