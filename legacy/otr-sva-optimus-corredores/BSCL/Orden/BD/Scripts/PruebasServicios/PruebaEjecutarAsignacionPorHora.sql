SET SCHEMA "BSCL";



CALL "BSCL.Orden.BD.Servicios::EjecutarAsignacionPorHora"
(	p_IdEmpresa  => 1 ,
	p_IdUsuario  => 3 ,
	p_IdModulo  => 'ORD' ,
	p_simularOperacion => 'S',
	p_HoraHasta => '23:00:00' ,
	p_IdTipoAsignacion => 'ASIGNACION_CRONOLOGICA' ,
	p_IdLibroOrdenes => null,
	p_IdClasificacionTipoInstrumento => null,
	p_IdTipoInstrumento => null,
	p_IdAccionOrden => null,
	p_IdBolsa => null,
	P_OUT_RESULTADO => ?,
	p_out_cobros => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
	
);


rollback