CALL "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarInstrumentoUnico" ( 
	 p_IdEmpresa					=> 1,
	 p_FiltroInstrumento			=> 'COP',
	 p_FiltroClasificacion			=> 'RV',
	 p_MuestraPrecio             	=> 'S',
	 p_out_Instrumento => ?,
	 
	 p_out_AdvertenciasContador	=> ?,
	p_out_AdvertenciasDetalle	=> ?,
	p_out_ErroresContador		=> ?,
	p_out_ErroresDetalle		=> ?)



SELECT *
	FROM "BSCL"."BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS inst
	
	WHERE inst."Nemotecnico" like '%'||'COP'||'%'
		