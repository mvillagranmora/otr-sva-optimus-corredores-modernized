SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdAsignacion" BIGINT
);

insert into #TT
select -1 from dummy;

insert into #TT
select 3726044 from dummy;

CALL "BSCL.Orden.BD.Servicios::AnularAsignacionMasiva"
(	p_IdEmpresa  	=> 11111 ,
	p_IdUsuario  	=> 3 ,
	p_IdModulo  	=> 'ORD' ,
	p_asignaciones 	=> #tt ,
	p_out_OrdenesAnuladas 	=> ?,
	p_out_SalidaDetalle 	=> ? ,
	p_out_InformacionContador => ?,
	p_out_InformacionDetalle => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);

rollback
