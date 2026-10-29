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
CALL "BSCL.CanalAgente.BD.Servicio::ConsultarHistorialMovimientosCuentaInversion" (
	p_IdEmpresa  	=> 1,
	p_IdUsuario  	=> 36,
	p_IdModulo   	=> 'CTA',
	p_IdPortafolio	=> 20232,
	p_FechaDesde	=> '2016-11-01',
	p_FechaHasta	=> '2016-11-30',
	p_IdMoneda			=> 'CLP',
	
	p_SaldoEfectivoInicial		=> ?,
	p_SaldoEfectivoFinal		=> ?,
	p_out_Resultado	 => ?,
	
	p_out_AdvertenciasContador=> ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
	 
) 


CALL "BSCL.CuentaInversion.BD.Servicios::ConsultarCartolaDeMovimiento" (
	p_IdEmpresa  				=> 1,
	p_IdUsuario  				=> 36,
	p_IdModulo  				=> 'CTA',
	p_UsaReglasNegocio  		=> 'S',
	p_IdCuentaInversion  		=> 21151,
	p_IdCliente  				=> null,
	p_IdPortafolio  			=> 20232,
	p_IdMoneda   				=> 'CLP',
	p_IdEstado					=> null,
	p_FechaDesde				=> '2016-11-01',
	p_FechaHasta				=> '2016-11-30',
	p_SaldoPorFechaEfectiva		=> 'S',
	
	p_out_CartolaDeMovimiento	=> ?,
	p_out_InformacionContador	=> ?,
	p_out_InformacionDetalle	=> ?,
	p_out_AdvertenciasContador	 => ?,
	
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
	 
);
