SET SCHEMA "BSCL";
/*drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
		"IdAsignacion" BIGINT
);

insert into #TT
select 3726043 from dummy;

insert into #TT
select 3726044 from dummy;
*/
	
CALL "BSCL.CanalAgente.BD.Servicios::ConsultarHistorialMovimientosCustodia" (
	p_IdEmpresa  	=> 1,
	p_IdUsuario  	=> 4,
	p_IdModulo   	=> 'PLA',
	p_IdPortafolio	=> 27500,
	p_FechaDesde	=> '2016-12-03',
	p_FechaHasta	=> '2016-12-10',
	
	
	p_out_Resultado	 => ?,
	
	p_out_AdvertenciasContador=> ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
	 
) 
