SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdAsignacion" BIGINT
);

insert into #TT
select 3725866 from dummy;

CALL "BSCL.Orden.BD.Servicios::AnularMovFacturarAsignacionMasiva"
(	p_IdEmpresa  	=> 1 ,
	p_IdUsuario  	=> 3 ,
	p_IdModulo  	=> 'ORD' ,
	p_asignaciones => #tt ,
	p_out_SalidaResumen => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);
