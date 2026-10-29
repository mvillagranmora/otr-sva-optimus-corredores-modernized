call "BSCL.Normativa.BD.Servicios.NOR::LibroOperaciones" (
	 p_IdEmpresa		=> :p_IdEmpresa,
	 p_FechaDesde		=> :p_FechaOperacionOrdenI,
     p_FechaHasta		=> :p_FechaOperacionOrdenF,
	 p_IdCliente		=> NULL,
	 p_IdMoneda		    => NULL,
	 p_IdBolsa		    => NULL,
	 p_IdClasificacion 	=> NULL,
	 p_IdTipoInstrumento => NULL,
	 p_IdInstrumento	=> NULL,
	 p_IdTipOperacion   => NULL,
	 p_out_LibroOperaciones	=> $p_out_LibroOperaciones

);